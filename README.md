# 🏠 Matie — University Housemate & Housing Platform MVP

> 🇹🇷 Türkçe için: [README.tr.md](README.tr.md)

Matie is a dedicated housing and roommate-finding backend platform designed exclusively for university students. Starting initially as a closed ecosystem for IYTE (İzmir Institute of Technology), the ultimate vision of this MVP is to scale and open its doors to other universities across the country.

> **💡 MVP & Engineering Philosophy:** Matie was built with a strict MVP (Minimum Viable Product) mindset. The primary goal was to deliver a fully functional, secure, and performant backend rapidly to the market. Instead of over-engineering the system with microservices from day one, we focused on practical, scalable monolith architecture, smart database indexing, and efficient reverse-proxy utilization to get the job done flawlessly.

---

## 🚀 Why This Project?

University students typically look for housing or roommates in chaotic Facebook/Telegram groups, which leads to spam, unverified users, and scattered communication. Matie aims to:

- Create a closed, secure ecosystem where users are verified via University IDs.
- Standardize listings into clear categories (`ROOM_AVAILABLE` or `ROOMMATE_WANTED`).
- Replace chaotic direct messaging with a structured application system.
- Provide a robust, self-updating infrastructure requiring minimal manual maintenance.

This project is an end-to-end monolith design covering everything from database migrations and security (JWT) to Nginx static file optimization and automated CI/CD deployments.

---

## 🧠 Quick Explanation (Non-Technical)

When a student (Applicant) finds an available room listed by another student (Owner), the system:

1. Prevents the Applicant from just opening a random chat and saying "Hi".
2. Forces the Applicant to submit a formal **Application** with an introductory message.
3. Leaves the application in a `PENDING` state, invisible in the messaging tab.
4. If the Owner reviews the application and clicks "Accept", the system generates a **Conversation** room on the fly and drops the initial message as the first chat bubble.

**Result:** No spam, no empty chat rooms in the database, and a highly organized inbox for users renting out their rooms.

---

## 🏗️ Architecture Flow

```
[GitHub Actions] ──► Builds Image ──► [GHCR (Container Registry)]
                                                │
                                                ▼
                                         [Watchtower] (Auto-pulls & Restarts)
                                                │
┌───────────────────────────────────────────────▼──────────────────────────┐
│                                HETZNER VM                                  │
│                                                                            │
│                 [Nginx (Reverse Proxy & Static Server)]                    │
│                   │                │                │                      │
│            CORS/OPTIONS       /uploads/          /api/                     │
│           (Handled early)  (Direct to Disk)  (To Backend)                  │
│                   │                │                │                      │
│                   ▼                ▼                ▼                      │
│               [HTTP 204]    [Docker Volume]  [Spring Boot 3 (Java 21)]     │
│                                                     │                      │
│                                                     ▼                      │
│                                           [PostgreSQL (Flyway)]            │
└────────────────────────────────────────────────────────────────────────── ┘
```

- **Backend:** Java 21, Spring Boot 3
- **Database:** PostgreSQL + Flyway (Migration-based schema management)
- **Auth:** Stateless JWT Authentication + Spring Security
- **DevOps/Deployment:** Docker, Docker Compose, Nginx, Watchtower (for zero-touch CI/CD)

---

## ⚡ Hard Parts & Practical Engineering Solutions

### 1) Chat System Bloat (Spam & Empty Rooms)

**Problem:** Allowing users to directly message listing owners leads to thousands of abandoned, empty chat rooms in the database and overwhelms the listing owner with spam.

**Solution:** We implemented a **State-Machine Application Flow**. The `Conversation` and `Message` entities are NOT created when a user reaches out. Instead, an `Application` entity is created. Only when the listing owner explicitly triggers a `PATCH /status` to `ACCEPTED`, the backend dynamically provisions the `Conversation` and injects the initial message.

### 2) The N+1 Query Problem in JPA

**Problem:** Fetching a user's conversations or listings could trigger dozens of secondary SQL queries to fetch related entities (Applicants, Owners, Photos), crippling the database performance.

**Solution:** We heavily utilized `@EntityGraph` and `LEFT JOIN FETCH` in our Spring Data JPA repositories. This ensures that deeply nested relations (like a conversation's application's listing's owner) are pulled with a single, highly optimized SQL JOIN.

### 3) Static File Serving Overhead

**Problem:** Serving user-uploaded images via the Java (JVM) backend ties up application threads, increases memory usage, and slows down API performance. Using AWS S3 was an over-engineering for an MVP.

**Solution:** We bound the photo storage directory to a Docker Volume and configured Nginx to serve the `/uploads/` path directly from the disk. The Java application only saves the file, but Nginx handles thousands of image requests concurrently at the OS level without the backend even knowing.

### 4) CORS & Preflight (OPTIONS) Hell

**Problem:** The frontend (Next.js) running on a different port constantly sends OPTIONS preflight requests, which hit the Spring Security filter chain, causing unnecessary processing and frequent CORS errors.

**Solution:** We handled CORS at the Reverse Proxy layer. Nginx intercepts all OPTIONS requests, attaches the correct headers, and returns a `204 No Content` immediately. The Spring Boot backend only receives actual GET/POST traffic.

---

## 🧪 Live Demo & API Testing (MVP Phase)

Since Matie is currently in its active MVP phase and has not yet reached a fully polished consumer frontend level, we have made the live backend accessible via Swagger for review.

Reviewers and engineers can use the provided test credentials to authenticate, explore endpoints, create mock listings, and simulate the application/chat state-machine flow on the live database.

**Swagger API Docs:** http://46.224.29.82/swagger-ui/index.html

**Test User Login Credentials:**
Use these credentials in the `POST /api/v1/auth/login` endpoint to retrieve a JWT Token for testing.

```json
{
  "email": "ali@iyte.edu.tr",
  "password": "password123"
}
```
