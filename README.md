# URL Shortening Service

> 🇧🇷 **Leia em português:** [README.pt-BR.md](README.pt-BR.md)

A URL shortening REST API built with **Spring Boot 4** and **Java 25**.
Short codes are generated deterministically from the record ID using
[Sqids](https://sqids.org/), backed by **PostgreSQL** for persistence,
**Flyway** for schema versioning and **Redis** as a read cache.

---

## Table of contents

- [Features](#features)
- [Stack](#stack)
- [Architecture](#architecture)
- [How the short code works](#how-the-short-code-works)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Running with Docker Compose](#running-with-docker-compose)
  - [Running locally](#running-locally)
- [Configuration](#configuration)
- [API](#api)
  - [Create a short URL](#create-a-short-url)
  - [Look up the original URL](#look-up-the-original-url)
  - [Update a URL](#update-a-url)
  - [Delete a URL](#delete-a-url)
  - [URL statistics](#url-statistics)
  - [Service status](#service-status)
  - [Error format](#error-format)
- [Interactive documentation](#interactive-documentation)
- [Cache](#cache)
- [Database](#database)
- [Tests](#tests)
- [Project structure](#project-structure)
- [License](#license)

---

## Features

- Short URL creation with format validation (`http`/`https`)
- **Idempotent** creation: submitting the same URL twice returns the same record
- Lookup, update and delete by short code
- Per-URL access counting, exposed through a statistics endpoint
- Service status endpoint for liveness checks
- Distributed cache in Redis, automatically invalidated on update/delete
- Resilient caching: if Redis is down the application keeps serving requests (falls back to the database)
- Centralized error handling with a consistent error payload
- OpenAPI / Swagger UI documentation
- Versioned migrations with Flyway (Hibernate only validates the schema)
- Multi-stage Docker image using Spring Boot layers and a non-root user

---

## Stack

| Layer          | Technology                                  |
|----------------|---------------------------------------------|
| Language       | Java 25                                     |
| Framework      | Spring Boot 4.1.0 (Web MVC, Data JPA, Validation, Cache) |
| Database       | PostgreSQL 16                               |
| Migrations     | Flyway                                      |
| Cache          | Redis 7 (Spring Data Redis)                 |
| ID generation  | Sqids 0.1.0                                 |
| Documentation  | springdoc-openapi 3.1.0                     |
| Build          | Maven (wrapper included)                    |
| Container      | Docker / Docker Compose                     |

---

## Architecture

```
HTTP  ──►  UrlResource        (@RestController — input validation, DTOs, HTTP status)
             │
             ▼
           UrlService         (@Service — business rules, transactions, @Cacheable/@CacheEvict)
             │        │
             │        └──►  Redis        (cache "urls", key = shortCode)
             ▼
           UrlRepository      (Spring Data JPA)
             │
             ▼
           PostgreSQL         (short_urls table, schema managed by Flyway)
```

Domain exceptions (`DomainException`, `UrlNotFoundException`) bubble up to
`GlobalHandlerException` (`@RestControllerAdvice`), which translates them into
the `ApiError` format. Unexpected failures get an `errorId` (UUID) that is
logged server-side and returned to the client, without leaking internals.

---

## How the short code works

1. The original URL is persisted and receives a sequential `id` from PostgreSQL.
2. That `id` is encoded with Sqids, producing an obfuscated short code
   (e.g. `1` → `5CAHFl9`) — no predictable sequence is exposed.
3. The code is stored in the `short_code` column, which has a unique index.

Sqids parameters are configurable:

- `SQIDS_MIN_LENGTH` (default `7`) — minimum code length
- `SQIDS_ALPHABET` (default: alphanumeric `[0-9A-Za-z]`)

> Changing the alphabet or the minimum length changes the codes generated for
> **new** records. Codes already issued remain valid, since they are persisted
> in the table.

---

## Getting started

### Prerequisites

- **Docker + Docker Compose** — the recommended path, nothing else needs to be installed
- To run outside containers: **JDK 25**, **PostgreSQL 16** and **Redis 7**

### Running with Docker Compose

```bash
git clone git@github.com:EberteSampaio/URL-Shortening-Service.git
cd URL-Shortening-Service

cp .env.example .env      # adjust DB_PASSWORD and the other values
docker compose up --build
```

This starts three services:

| Service      | Container     | Port (host)                     |
|--------------|---------------|---------------------------------|
| Application  | `us-app`      | `${SERVER_HOST_PORT:-8080}`     |
| PostgreSQL   | `us-postgres` | `${DB_HOST_PORT:-5432}`         |
| Redis        | `us-redis`    | `6379`                          |

The application only starts once the Postgres and Redis healthchecks pass.
Flyway migrations run automatically on startup.

Check it:

```bash
curl -s http://localhost:8080/api-docs | head
```

To tear everything down (keeping the data volumes):

```bash
docker compose down
```

To also remove the persisted data:

```bash
docker compose down -v
```

### Running locally

With PostgreSQL and Redis already available on your machine:

```bash
cp .env.example .env      # point DB_HOST/REDIS_HOST at localhost
./mvnw spring-boot:run
```

The application reads the `.env` in the project root via
`spring.config.import=optional:file:.env[.properties]`. The import is optional:
in production you can simply export real environment variables.

To build the executable JAR:

```bash
./mvnw clean package
java -jar target/url-shortening-0.0.1-SNAPSHOT.jar
```

---

## Configuration

Every key has a default in `application.properties`, except the ones marked as
required. Copy `.env.example` to `.env` and adjust it.

There are two example files, with the same keys and the same values — only the
comment language differs:

| File                  | Comments   |
|-----------------------|------------|
| `.env.example`        | English    |
| `.env.example.pt-BR`  | Portuguese |

Use whichever you prefer as your starting point:

```bash
cp .env.example .env
```

> `.env` is listed in `.gitignore` and must **never** be committed.

### Application

| Variable      | Default          | Description         |
|---------------|------------------|---------------------|
| `APP_NAME`    | `url-shortening` | Application name    |
| `SERVER_PORT` | `8080`           | HTTP server port    |

### Database (required)

| Variable      | Default          | Description                |
|---------------|------------------|----------------------------|
| `DB_HOST`     | `localhost`      | PostgreSQL host            |
| `DB_PORT`     | `5432`           | PostgreSQL port            |
| `DB_NAME`     | `url_shortening` | Database name              |
| `DB_USERNAME` | —                | Username (**required**)    |
| `DB_PASSWORD` | —                | Password (**required**)    |

### Cache

| Variable     | Default     | Description                        |
|--------------|-------------|------------------------------------|
| `REDIS_HOST` | `localhost` | Redis host                         |
| `REDIS_PORT` | `6379`      | Redis port                         |
| `CACHE_TTL`  | `3600000`   | Default cache TTL, in ms (1 hour)  |

### Connection pool (HikariCP)

| Variable                     | Default   | Description                        |
|------------------------------|-----------|------------------------------------|
| `DB_POOL_MAX_SIZE`           | `10`      | Maximum number of connections      |
| `DB_POOL_MIN_IDLE`           | `5`       | Minimum idle connections           |
| `DB_POOL_CONNECTION_TIMEOUT` | `30000`   | Connection acquisition timeout (ms)|
| `DB_POOL_IDLE_TIMEOUT`       | `600000`  | Time before closing an idle connection (ms) |
| `DB_POOL_MAX_LIFETIME`       | `1800000` | Maximum lifetime of a connection (ms) |

### JPA / Flyway / Sqids

| Variable                    | Default    | Description                                     |
|-----------------------------|------------|-------------------------------------------------|
| `JPA_DDL_AUTO`              | `validate` | Keep it as `validate`: the schema belongs to Flyway |
| `JPA_SHOW_SQL`              | `true`     | Logs the generated SQL (turned off in compose)  |
| `JPA_FORMAT_SQL`            | `true`     | Formats the SQL in the logs                     |
| `FLYWAY_ENABLED`            | `true`     | Enables migrations on startup                   |
| `FLYWAY_BASELINE_ON_MIGRATE`| `true`     | Baselines a pre-existing database               |
| `SQIDS_MIN_LENGTH`          | `7`        | Minimum short code length                       |
| `SQIDS_ALPHABET`            | `0-9A-Za-z`| Alphabet used for encoding                      |

---

## API

Base URL: `http://localhost:8080/api/shorten`
All requests and responses use `application/json`.

### Create a short URL

```http
POST /api/shorten
Content-Type: application/json

{
  "link": "https://www.example.com/some/very/long/url"
}
```

**`201 Created`** — with a `Location: /api/shorten/{id}` header

```json
{
  "id": 1,
  "url": "https://www.example.com/some/very/long/url",
  "shortcode": "5CAHFl9",
  "createdAt": "2026-08-25T10:30:00",
  "updatedAt": "2026-08-25T10:30:00"
}
```

The operation is idempotent per URL: submitting the same `link` again returns
the existing record, with the same `shortcode`. A unique constraint on the
`url` column enforces this at the database level.

The `link` field is validated as an absolute URL and must match `^https?://.+`.

```bash
curl -i -X POST http://localhost:8080/api/shorten \
  -H 'Content-Type: application/json' \
  -d '{"link":"https://www.example.com"}'
```

### Look up the original URL

```http
GET /api/shorten/{shortCode}
```

**`200 OK`** — same payload as create.
**`404 Not Found`** — unknown short code.

```bash
curl http://localhost:8080/api/shorten/5CAHFl9
```

> This is a JSON API: the endpoint returns the original URL in the response
> body, it does **not** issue an HTTP 301/302 redirect.

Every successful call to this endpoint counts as one access and increments the
counter reported by [URL statistics](#url-statistics). Unknown short codes fail
with `404` before anything is counted.

### Update a URL

```http
PUT /api/shorten/{shortCode}
Content-Type: application/json

{
  "link": "https://www.example.com/new-url"
}
```

**`200 OK`** — the `shortcode` is preserved and the matching cache entry is
evicted.
**`404 Not Found`** — unknown short code.

### Delete a URL

```http
DELETE /api/shorten/{shortCode}
```

**`204 No Content`** — deleted, with cache invalidation.
**`404 Not Found`** — unknown short code.

### URL statistics

```http
GET /api/shorten/{shortCode}/stats
```

**`200 OK`**

```json
{
  "id": 1,
  "url": "https://www.example.com/some/very/long/url",
  "shortcode": "5CAHFl9",
  "createdAt": "2026-08-25T10:30:00",
  "updatedAt": "2026-08-25T10:30:00",
  "accessCount": 42
}
```

**`404 Not Found`** — unknown short code.

```bash
curl http://localhost:8080/api/shorten/5CAHFl9/stats
```

`accessCount` is how many times the [lookup endpoint](#look-up-the-original-url)
resolved this short code. Reading the statistics does **not** count as an
access.

This endpoint deliberately bypasses the Redis cache and reads straight from the
database: the cached `Link` carries the counter value frozen at cache-write
time, so serving statistics from it would report a number up to 30 minutes
stale.

### Service status

```http
GET /api/status
```

**`200 OK`**

```json
{
  "status": "UP",
  "timestamp": "2026-08-25T10:30:00"
}
```

```bash
curl http://localhost:8080/api/status
```

A lightweight liveness check. It only proves the HTTP layer is answering — it
does not probe PostgreSQL or Redis.

### Error format

Every error follows the same contract (`ApiError`):

```json
{
  "timestamp": "2026-08-25T10:31:22.481",
  "status": 400,
  "error": "Erro de validação",
  "message": "Um ou mais campos estão inválidos. Faça o preenchimento correto e tente novamente.",
  "path": "/api/shorten",
  "fields": [
    { "field": "link", "message": "O link deve ser uma URL válida" }
  ]
}
```

> Error messages are currently returned in Portuguese — that is what the
> application produces today.

The `fields` array only shows up on field validation errors. On unexpected
failures (`500`), `message` carries a correlation code (`errorId`) that is also
written to the server logs.

| Status | When it happens                                        |
|--------|--------------------------------------------------------|
| `400`  | Invalid payload or business rule violation             |
| `404`  | Short code not found                                   |
| `500`  | Unexpected failure — response carries an `errorId`     |

---

## Interactive documentation

With the application running:

- **Swagger UI**: <http://localhost:8080/documentation.html>
- **OpenAPI JSON**: <http://localhost:8080/api-docs>

---

## Cache

Caching is declarative, through Spring Cache annotations on `UrlService`:

- `getLinkByShortCode` is `@Cacheable(value = "urls", key = "#shortCode")`
- `update` and `delete` are `@CacheEvict` on the same key

Configured in `CacheConfig`:

- `urls` cache: **30 minute** TTL, values serialized as JSON
- Other caches: default **1 hour** TTL
- `null` values are not cached
- A custom `CacheErrorHandler` catches GET/PUT/EVICT/CLEAR failures, logs a
  `WARN` and lets the request fall through to the database — a Redis outage
  degrades performance, not availability

---

## Database

The schema is versioned by Flyway under `src/main/resources/db/migrations`:

| Migration | Description                                        |
|-----------|----------------------------------------------------|
| `V1`      | Creates the `short_urls` table                     |
| `V2`      | Adds a unique constraint on the `url` column       |
| `V3`      | Adds the `access_count` column                     |

Resulting schema:

```sql
CREATE TABLE short_urls
(
    id           BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    url          TEXT   NOT NULL UNIQUE,
    short_code   VARCHAR(255) UNIQUE,
    access_count BIGINT NOT NULL DEFAULT 0,
    created_at   TIMESTAMP WITHOUT TIME ZONE,
    updated_at   TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_short_urls PRIMARY KEY (id)
);
```

`access_count` is bumped with an atomic `UPDATE ... SET access_count =
access_count + 1`, not a read-modify-write cycle, so concurrent accesses to the
same short code cannot lose increments. As a side effect the statement does not
trigger Hibernate's `@UpdateTimestamp`, which keeps `updated_at` meaning "the
URL was edited" rather than "the URL was visited".

Timestamps are generated by the database (`@CreationTimestamp`/
`@UpdateTimestamp` with `SourceType.DB`). Hibernate runs with
`ddl-auto=validate`, so it never alters the schema — every structural change
must go in as a new migration.

---

## Tests

```bash
./mvnw test
```

`UrlResourceIntegrationTest` is a `@WebMvcTest` slice that exercises the web
layer with `MockMvc` and a `UrlService` mocked via `@MockitoBean`, covering the
four CRUD endpoints, the HTTP statuses and the `Location` header.

The `/stats` and `/api/status` endpoints have no tests yet, and neither does the
access counter itself — that one needs a persistence-level test, since the
increment happens in a JPQL `@Modifying` query rather than in Java code.

---

## Project structure

```
src/main/java/br/com/deveberte/urlshortening/
├── UrlShorteningApplication.java
├── api/
│   ├── dto/
│   │   ├── PingResponse.java            # service status payload
│   │   ├── UrlRequest.java              # input payload + validations
│   │   ├── UrlResponse.java             # output payload
│   │   └── UrlStatisticResponse.java    # output payload + accessCount
│   └── resource/
│       ├── APIStatusResource.java       # GET /api/status
│       └── UrlResource.java             # REST controller + OpenAPI annotations
├── config/
│   ├── CacheConfig.java                 # Redis, TTLs and failure tolerance
│   ├── SqidsConfig.java                 # short code generator bean
│   ├── SqidsRecord.java                 # @ConfigurationProperties app.sqids
│   └── exceptionhandler/
│       ├── ApiError.java                # error contract
│       ├── FieldWithError.java
│       └── GlobalHandlerException.java  # @RestControllerAdvice
├── domain/entity/
│   └── Link.java                        # JPA entity (short_urls)
├── exception/
│   ├── DomainException.java
│   └── UrlNotFoundException.java
├── repository/
│   └── UrlRepository.java
└── service/
    └── UrlService.java                  # business rules + caching

src/main/resources/
├── application.properties
└── db/migrations/                       # Flyway migrations
```

---

## License

A study project, with no license defined at the moment.
