# SudoBeats Backend

Spring Boot backend for **SudoBeats**, a musical Sudoku platform where players solve Sudoku boards, build musical tracks from their moves, compete on leaderboards, and share replay links.

Frontend repository: https://github.com/snbaay/sudosynth-beats.git  
Backend repository: https://github.com/snbaay/sudobeats-backend.git  
Production frontend: https://sudosynth-beats-main.vercel.app

## Tech Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT authentication
- Maven
- Render deployment

## What This Backend Provides

- User registration and login
- JWT-based authentication
- Password hashing with Spring Security
- PostgreSQL persistence
- Sudoku game/session storage
- Streak-ready user fields
- Leaderboard APIs
- Shared track/replay APIs
- AI coach/hint API contract
- CORS integration for the deployed Vercel frontend

## Related Frontend

The React/Vite/Tailwind/Tone.js frontend lives here:

```text
https://github.com/snbaay/sudosynth-beats.git
```

Production frontend:

```text
https://sudosynth-beats-main.vercel.app
```

The frontend expects this backend to expose `/api/...` endpoints for auth, leaderboards, tracks, and hints.

## Local Setup

Start PostgreSQL:

```bash
docker run --name sudobeats-postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=sudobeats \
  -p 5432:5432 \
  postgres:16
```

Run the backend:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Environment Variables

For local development, configure PostgreSQL and JWT settings in `src/main/resources/application.properties` or environment variables.

Typical production variables:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<database>
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>
SUDOBEATS_JWT_SECRET=<long-random-secret-at-least-32-bytes>
SUDOBEATS_CORS_ALLOWED_ORIGINS=https://sudosynth-beats-main.vercel.app
```

Do not commit real database passwords or JWT secrets.

## API Overview

Auth:

```http
POST /api/auth/register
POST /api/auth/login
```

Leaderboard:

```http
GET  /api/leaderboard?difficulty=MEDIUM
POST /api/leaderboard/complete
```

Tracks:

```http
POST /api/tracks
GET  /api/tracks/{slug}
```

Coach:

```http
POST /api/games/{gameId}/hint
```

Protected endpoints use:

```http
Authorization: Bearer <jwt>
```

## Frontend Configuration

In the frontend repo, set:

```env
VITE_API_URL=http://localhost:8080
```

For the deployed backend:

```env
VITE_API_URL=https://sudobeats-backend-ly0b.onrender.com
```

The backend must allow the frontend origin:

```text
https://sudosynth-beats-main.vercel.app
```

## Render Deployment

Recommended Render settings:

```text
Build Command: ./mvnw clean package -DskipTests
Start Command: java -jar target/*.jar
```

If the project uses a Dockerfile, Render can also deploy from the Dockerfile directly.

## Repositories

Frontend:

```text
https://github.com/snbaay/sudosynth-beats.git
```

Backend:

```text
https://github.com/snbaay/sudobeats-backend.git
```
