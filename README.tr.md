# 🏠 Matie — Üniversite Ev ve Oda Arkadaşı Platformu

> 🇬🇧 For English: [README.md](README.md)

Matie, yalnızca üniversite öğrencileri için tasarlanmış, kapalı devre bir ev ve oda arkadaşı bulma platformudur. Başlangıçta İYTE (İzmir Yüksek Teknoloji Enstitüsü) için özel bir ağ olarak yola çıkan bu MVP'nin temel vizyonu, ilerleyen aşamalarda Türkiye'deki diğer üniversitelere de açılmaktır.

> **💡 MVP ve Mühendislik Felsefesi:** Matie, katı bir MVP (Minimum Viable Product) zihniyetiyle inşa edilmiştir. Temel amaç; çalışan, güvenli ve performanslı bir ürünü en hızlı şekilde pazara sunmaktır. İlk günden sistemi mikroservislere bölmek (over-engineering) yerine; pratik, ölçeklenebilir bir monolit mimari, akıllı veritabanı indekslemesi ve Reverse-Proxy (Nginx) gücü kullanılarak işin kusursuzca çözülmesine odaklanılmıştır.

---

## 🚀 Neden Bu Proje?

Üniversite öğrencileri genellikle Facebook/Telegram gibi karmaşık gruplarda ev ararlar. Bu durum spam mesajlara, sahte hesaplara ve güvensiz bir ortama yol açar. Matie'nin hedefleri:

- Sadece Üniversite ID'si ile girilebilen güvenli, kapalı bir ekosistem yaratmak.
- İlanları standartlaştırmak (Oda Müsait veya Ev Arkadaşı Aranıyor).
- Kaotik "DM atma" kültürünü, yapılandırılmış bir başvuru sistemine dönüştürmek.
- Minimum manuel bakımla kendi kendini güncelleyen bir altyapı sunmak.

Bu proje; veritabanı migrasyonlarından JWT güvenliğine, Nginx statik dosya optimizasyonundan otomatik CI/CD süreçlerine kadar uçtan uca tasarlanmış bir monolit sistem dizaynıdır.

---

## 🧠 Nasıl Çalışır? (Kullanıcı Gözünden)

Bir öğrenci (Başvuran), başka bir öğrencinin (Ev Sahibi) ilanını beğendiğinde sistem:

1. Başvuranın doğrudan rastgele bir sohbet odası açıp "Selam" yazmasını engeller.
2. Başvuranı, kendini tanıtan bir mesajla resmi bir **Başvuru** (Application) yapmaya zorlar.
3. Başvuruyu `PENDING` (Beklemede) statüsünde tutar ve mesajlar sekmesinde göstermez.
4. Ev Sahibi başvuruyu inceleyip "Kabul Et" butonuna basarsa, sistem arka planda bir **Sohbet Odası** (Conversation) oluşturur ve atılan o ilk mesajı sohbetin ilk balonu olarak içeri aktarır.

**Sonuç:** Sıfır spam, veritabanında çöplüğe dönmüş boş sohbet odaları yok ve ev sahipleri için son derece düzenli bir gelen kutusu.

---

## 🏗️ Mimari Akış

```
[GitHub Actions] ──► Image Build ──► [GHCR (Container Registry)]
                                                │
                                                ▼
                                         [Watchtower] (Otomatik çeker & yeniden başlatır)
                                                │
┌───────────────────────────────────────────────▼──────────────────────────┐
│                                HETZNER VM                                  │
│                                                                            │
│                 [Nginx (Reverse Proxy & Statik Sunucu)]                    │
│                   │                │                │                      │
│            CORS/OPTIONS       /uploads/          /api/                     │
│           (Erken ele alınır) (Doğrudan diskten) (Backend'e)                │
│                   │                │                │                      │
│                   ▼                ▼                ▼                      │
│               [HTTP 204]    [Docker Volume]  [Spring Boot 3 (Java 21)]     │
│                                                     │                      │
│                                                     ▼                      │
│                                           [PostgreSQL (Flyway)]            │
└────────────────────────────────────────────────────────────────────────── ┘
```

- **Backend:** Java 21, Spring Boot 3
- **Veritabanı:** PostgreSQL + Flyway (Migration tabanlı şema yönetimi)
- **Auth:** Stateless JWT Authentication + Spring Security
- **DevOps/Deployment:** Docker, Docker Compose, Nginx, Watchtower (zero-touch CI/CD için)

---

## ⚡ Karşılaşılan Zorluklar ve Pratik Çözümler

### 1) Sohbet Odası Çöplüğü (Spam ve Boş Odalar)

**Problem:** Kullanıcıların doğrudan mesaj atmasına izin vermek, veritabanında binlerce terk edilmiş sohbet odası oluşmasına ve ilan sahiplerinin spam'e boğulmasına neden olur.

**Çözüm:** **Durum Makineli Başvuru Akışı** (State-Machine Flow) uygulandı. Birisi iletişime geçtiğinde `Conversation` değil, `Application` oluşur. Sadece ilan sahibi isteği `ACCEPTED` durumuna getirdiğinde backend dinamik olarak sohbet odasını ayağa kaldırır ve ilk mesajı içine kopyalar.

### 2) JPA'de N+1 Sorgu Problemi

**Problem:** Bir kullanıcının sohbetlerini veya ilanlarını çekerken, ilişkili tabloları (İlan sahibi, Başvuran, Fotoğraflar) getirmek için veritabanına onlarca ekstra SQL sorgusu atılması performansı çökertir.

**Çözüm:** Spring Data JPA repository'lerinde yoğun olarak `@EntityGraph` ve `LEFT JOIN FETCH` kullanıldı. Bu sayede derin ilişkiler (Örn: Sohbet → Başvuru → İlan → İlan Sahibi) veritabanından tek ve optimize edilmiş bir SQL JOIN ile çekildi.

### 3) Statik Dosya (Fotoğraf) Yükü

**Problem:** Kullanıcıların yüklediği fotoğrafları Java (JVM) üzerinden sunmak, thread'leri meşgul eder ve API'yi yavaşlatır. MVP aşamasında AWS S3 kullanmak ise gereksiz bir maliyet ve zaman kaybıydı (over-engineering).

**Çözüm:** Fotoğraf klasörü Docker Volume ile diske bağlandı ve Nginx, `/uploads/` isteklerini doğrudan işletim sistemi seviyesinde diskten okuyarak sunacak şekilde yapılandırıldı. Backend sadece dosyayı kaydeder; Nginx ise saniyede binlerce fotoğrafı backend'in ruhu bile duymadan kullanıcılara iletir.

### 4) CORS ve Preflight (OPTIONS) Krizi

**Problem:** Farklı portta çalışan Frontend (Next.js), sürekli OPTIONS ön-kontrol istekleri atar. Bu istekler Spring Security filtrelerine takılıp yük oluşturur ve sık sık CORS hatası verirdi.

**Çözüm:** CORS yönetimi tamamen Reverse Proxy (Nginx) katmanına devredildi. Nginx, gelen tüm OPTIONS isteklerini yakalar, doğru header'ları ekler ve anında `204 No Content` döner. Java backend'ine sadece gerçek veri (GET/POST) trafiği ulaşır.

---

## 🧪 Canlı Demo & API Testleri (MVP Aşaması)

Proje henüz son kullanıcı ürünü (product) seviyesine tam olarak gelmediği için, inceleme yapacak kişi ve mühendislerin sistemi doğrudan deneyimleyebilmesi adına backend API katmanı Swagger üzerinden dışarıya açılmıştır.

Aşağıdaki test kullanıcısı ile giriş yaparak bir yetkilendirme Token'ı alabilir, sahte ilanlar oluşturabilir ve başvuru/mesajlaşma döngüsünü canlı veritabanı üzerinde bizzat test edebilirsiniz.

**Swagger API Dokümantasyonu:** http://46.224.29.82/swagger-ui/index.html

**Test Kullanıcısı Giriş Bilgileri:**
Token almak için bu bilgileri `POST /api/v1/auth/login` endpoint'inde kullanabilirsiniz.

```json
{
  "email": "ali@iyte.edu.tr",
  "password": "password123"
}
```
