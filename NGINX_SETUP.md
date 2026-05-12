# Nginx Reverse Proxy Setup for Matie

Bu dokümantasyon, Matie projesi için Nginx reverse proxy yapılandırmasını açıklar.

## Proje Mimarisi

```
Dış Dünya (80 portu)
        ↓
    Nginx (80)
    ├─ /api/* → housemate-backend:8080
    └─ /* → Frontend (dist klasörü)
```

## Dosya Yapısı

```
housemate/
├── nginx/
│   └── default.conf          # Nginx konfigürasyonu
├── frontend/
│   └── dist/                 # React/Vite build dosyaları (statik)
├── compose.yaml              # Docker Compose yapılandırması
└── Dockerfile               # Backend Docker image
```

## Nginx Yapılandırması (nginx/default.conf)

### Özellikler

1. **Reverse Proxy untuk Backend**
   - `/api/*` istekleri `http://housemate-backend:8080` adresine yönlendirilir
   - Client IP bilgisi backend'e iletilir (`X-Real-IP`, `X-Forwarded-For` gibi headers)

2. **Frontend Sunumu**
   - `/` istekleri frontend's statik dosyalarına (`/usr/share/nginx/html`) yönlendirilir
   - React Router desteği için `try_files` kullanılır

3. **API Dokumentasyon**
   - `/v3/*` (OpenAPI) ve `/swagger-ui/*` istekleri backend'e proxy yapılır

4. **Statik Dosya Caching**
   - JS, CSS, resim, font dosyaları 30 gün cache'lenir
   - `index.html` cache'lenmez (always fresh)

5. **Gzip Compression**
   - Tüm metin tabanlı yanıtlar sıkıştırılır (CSS, JS, JSON)

6. **Security Headers**
   - `Strict-Transport-Security`
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: SAMEORIGIN`
   - `X-XSS-Protection`

7. **CORS Desteği**
   - CORS headers proxy seviyesinde eklenir (opsiyonel)
   - Backend'de CORS yapılandırması varsa Nginx'i kaldırabilirsiniz

## Docker Compose Yapılandırması

### Nginx Servisi

```yaml
nginx:
  image: nginx:1.27-alpine
  container_name: housemate-nginx
  ports:
    - "80:80"
  volumes:
    - ./nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    - ./frontend/dist:/usr/share/nginx/html:ro
    - nginx_cache:/var/cache/nginx
  depends_on:
    - housemate-be
  networks:
    - housemate-net
  restart: unless-stopped
  healthcheck:
    test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
    interval: 30s
    timeout: 10s
    retries: 3
    start_period: 10s
```

### Değişiklikler

1. **Backend Port Kaldırıldı**: Backend artık sadece internal ağda çalışır
2. **Nginx Port 80**: Dışarıya açılan tek port
3. **Depends On**: Nginx, backend'den sonra başlar

## Kullanım

### 1. Frontend Build (Gerekli Adım)

Frontend'i build ettikten sonra `frontend/dist` klasörü oluşturulmalıdır:

```bash
cd frontend
npm run build
# dist/ klasörü oluşturulur
```

### 2. Docker Compose ile Başlatma

```bash
docker-compose up -d
```

### 3. Kontrol

**Nginx erişim:**
```bash
curl http://localhost/  # Frontend
curl http://localhost/api/v1/...  # Backend API
curl http://localhost/health  # Health check
```

**Logs:**
```bash
docker logs housemate-nginx
docker logs housemate-backend
```

## Proxy Headers Açıklaması

Backend'e iletilen headers:

```nginx
X-Real-IP: $remote_addr              # Client'ın asıl IP adresi
X-Forwarded-For: $proxy_add_x_forwarded_for  # Proxy zinciri
X-Forwarded-Proto: $scheme           # HTTP veya HTTPS
X-Forwarded-Host: $server_name       # Orijinal host header
X-Forwarded-Port: $server_port       # Orijinal port
```

**Spring Boot'ta kullanmak için:**

```java
@RestController
@RequestMapping("/api/v1")
public class MyController {
    
    @PostMapping("/example")
    public ResponseEntity<?> example(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null) {
            clientIP = request.getRemoteAddr();
        }
        // ...
    }
}
```

## Önemli Notlar

### CORS Yapılandırması

1. **Backend'de CORS varsa**: Nginx'deki CORS headers'larını kaldırabilirsiniz
2. **Backend'de CORS yoksa**: Nginx üzerindeki CORS headers'ları kullanın (mevcut config'de aktif)

Backend'de CORS yapılandırması:

```java
@Configuration
public class CorsConfig {
    
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173", "http://localhost")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

### Frontend Build Optimizasyonu

`frontend/dist` klasörü sadece gerekli dosyaları içermeli:

```bash
frontend/dist/
├── index.html
├── assets/
│   ├── index-[hash].js
│   └── index-[hash].css
└── favicon.ico
```

### Performance İyileştirmeleri

1. **Gzip**: Aktif (yazı tabanlı dosyalar)
2. **Cache**: Statik dosyalar 30 gün cache'lenir
3. **Buffering**: Proxy buffer ayarları optimize edilmiş
4. **Timeouts**: 60 saniye proxy timeout

## Sorun Giderme

### 404 Frontend Error
- `frontend/dist` klasörü var mı? → `npm run build` çalıştırın
- Nginx container başladı mı? → `docker ps` kontrol edin

### Backend Erişim Hatası
- Backend container'ı çalışıyor mu? → `docker logs housemate-backend`
- Network'te aynı ağda mı? → `docker network ls`

### CORS Error
- Nginx CORS headers'larını devre dışı bırakın (test için)
- Backend'de CorsConfig ekleyin

### Nginx Conf Error
```bash
docker exec housemate-nginx nginx -t  # Syntax kontrolü
```

## SSL/HTTPS Setup (Gelecek)

Nginx'de SSL eklemek için (Let's Encrypt ile):

```yaml
nginx:
  ports:
    - "80:80"
    - "443:443"
  volumes:
    - ./nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
    - ./certbot/conf:/etc/letsencrypt:ro
    - ./certbot/www:/var/www/certbot:ro
```

Nginx config'de:
```nginx
server {
    listen 443 ssl http2;
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;
}
```
