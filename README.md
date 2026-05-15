<div align="center">

<img src="https://img.shields.io/badge/DevFlow-Enterprise%20Platform-4fffb0?style=for-the-badge&logoColor=white" />

# DevFlow

### A production-grade distributed microservice platform for developer collaboration —<br/>repository management, real-time chat, automated CI/CD, and event-driven notifications.

<br/>

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=flat-square&logo=apachekafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white)
![MinIO](https://img.shields.io/badge/MinIO-C72E49?style=flat-square&logo=minio&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)

<br/>

</div>

---

## Table of Contents

- [Overview](#-overview)
- [Architecture](#-architecture)
- [Services](#-services)
- [Infrastructure](#-infrastructure)
- [Service Port Map](#-service-port-map)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Kafka Event Streams](#-kafka-event-streams)
- [Security Model](#-security-model)

---

## 🧭 Overview

DevFlow is a fully distributed microservice ecosystem built with **Spring Boot 3.5**, **Spring Cloud**, and **Apache Kafka**. It simulates a real-world developer platform — think GitHub meets a CI/CD runner — with every service independently deployable, communicating through a combination of synchronous REST (via Eureka load balancing) and asynchronous event streaming (via Kafka).

All services are battle-tested end-to-end including live WebSocket sessions, real email delivery, and Kafka event round-trips.

---

## 🏗 Architecture

```
                        ┌─────────────────────────────────┐
                        │         API Gateway :8080        │
                        │   (JWT Auth · Routing · CORS)    │
                        └────────────┬────────────────────┘
                                     │
          ┌──────────────────────────┼──────────────────────────────┐
          │                          │                              │
          ▼                          ▼                              ▼
  ┌───────────────┐        ┌─────────────────┐           ┌──────────────────┐
  │  Auth Service  │        │ Repo Service    │           │ Code Review Svc  │
  │    :8081       │        │   :8082         │           │     :8085        │
  │  PostgreSQL    │        │ PostgreSQL+MinIO│           │   PostgreSQL      │
  └───────────────┘        └────────┬────────┘           └────────┬─────────┘
                                    │ Kafka                        │ Kafka
                                    ▼                              ▼
                          ┌──────────────────────────────────────────────┐
                          │              Apache Kafka :9092               │
                          │   project-lifecycle  ·  code-review-events   │
                          └──────────────────────┬───────────────────────┘
                                                 │
                                    ┌────────────┴────────────┐
                                    ▼                         ▼
                          ┌──────────────────┐    ┌────────────────────┐
                          │ Notification Svc  │    │   CI/CD Service    │
                          │     :8083         │    │      :8084         │
                          │  Gmail SMTP       │    │   PostgreSQL       │
                          │  Thymeleaf Email  │    │                    │
                          └──────────────────┘    └────────────────────┘

                                    ┌──────────────────┐
                                    │   Chat Service   │
                                    │     :8005        │
                                    │  MongoDB · STOMP │
                                    │  WebSocket+SockJS│
                                    └──────────────────┘

          ┌──────────────────┐                    ┌──────────────────┐
          │  Eureka Server   │                    │  Config Server   │
          │     :8761        │                    │     :8888        │
          │  Service Registry│                    │  Centralised Cfg │
          └──────────────────┘                    └──────────────────┘
```

---

## 🔧 Services

### 🚪 API Gateway — `:8080`
The single entry point for all external traffic. Validates JWTs on every request and injects `X-User-Id` into downstream headers. Routes to all services via Eureka load balancing (`lb://`). The `/api/auth` and `/api/chat` paths are whitelisted — all others require a valid Bearer token.

---

### 🔐 Auth Service — `:8081`
Handles user registration and login. Passwords are hashed with **BCrypt**. Issues stateless **JWTs** (24-hour expiry) signed with a shared HMAC-SHA secret. The token carries the `userId` claim used by all downstream services to identify the caller without a database lookup.

**Endpoints**
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/signup` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT |

---

### 🗄️ Repository Service — `:8082`
The Vault. Stores project metadata in **PostgreSQL** and physical code assets in **MinIO** (S3-compatible object storage). On every new repository creation it fires a `project-lifecycle` event to Kafka, which triggers the welcome email from the Notification Service.

**Endpoints** *(via Gateway — requires JWT)*
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/repositories/create` | Create a new repository |
| `GET` | `/api/repositories/getrepos/{ownerEmail}` | List user's repositories |
| `GET` | `/api/repositories/repoexists/{ownerEmail}/{name}` | Check existence |
| `POST` | `/api/repositories/upload/{ownerEmail}/{projectName}` | Upload a file |
| `GET` | `/api/repositories/files/{ownerEmail}/{projectName}` | List files |
| `GET` | `/api/repositories/download/{ownerEmail}/{projectName}?fileName=` | Download a file |

---

### 💬 Chat Service — `:8005`
Real-time developer chat over **STOMP WebSocket** with **SockJS** fallback. JWT is validated at the handshake level by `ChatHandshakeInterceptor` — the token is passed as a query parameter. Messages are persisted to **MongoDB** and broadcast to all topic subscribers.

**WebSocket**
| Detail | Value |
|--------|-------|
| Endpoint | `ws://localhost:8005/ws?token=<jwt>` |
| Send destination | `/app/send` |
| Subscribe topic | `/topic/messages` |
| Payload | `{ pullrequestId, content }` |

---

### 🔍 Code Review Service — `:8085`
Manages the lifecycle of pull request code reviews. Before saving a review it validates the `repositoryId` via a synchronous **OpenFeign** call to the Repository Service. On success it fires an event to the `code-review-events` Kafka topic.

**Endpoints** *(via Gateway — requires JWT)*
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/reviews` | Submit a new code review |

---

### ⚙️ CI/CD Service — `:8084`
Manages pipeline definitions and run history. Stores pipeline state in **PostgreSQL** and publishes pipeline events to Kafka for downstream consumption.

---

### 📭 Notification Service — `:8083`
The Sentinel. A purely event-driven background worker with **no REST endpoints**. Listens to two Kafka topics and dispatches HTML emails using **JavaMailSender** and **Thymeleaf** templates via Gmail SMTP.

| Topic | Handler | Action |
|-------|---------|--------|
| `project-lifecycle` | `consumeProjectEventCreated` | Sends *"Repository Provisioned"* email |
| `code-review-events` | `listenCodeReviewEvents` | Logs alert (email not yet wired) |

---

### 🗺️ Eureka Server — `:8761`
Central service registry. Every microservice registers on boot. Inter-service calls use `lb://ServiceName` URIs — Eureka resolves them to live instances automatically. Dashboard available at [http://localhost:8761](http://localhost:8761).

---

### ⚙️ Config Server — `:8888`
Centralised configuration using Spring Cloud Config (native file mode). All per-service `application.yaml` overrides live in `config-server/src/main/resources/configs/`. Services pull their config on startup via `spring.config.import`.

---

## 🐳 Infrastructure

All infrastructure dependencies are managed via Docker Compose.

```bash
docker compose up -d
```

| Container | Purpose | Port(s) |
|-----------|---------|---------|
| `devflow-postgres` | Relational data (auth, repos, reviews, cicd) | `5432` |
| `devflow-mongodb` | Chat message persistence | `27017` |
| `devflow-minio` | Object storage for repository files | `9000`, `9001` |
| `kafka` | Event streaming backbone | `9092` |
| `zookeeper` | Kafka coordination | `2181` |
| `redis` | Caching layer | `6379` |

---

## 🗺 Service Port Map

| Service | Port | Protocol |
|---------|------|----------|
| API Gateway | `8080` | HTTP/REST |
| Auth Service | `8081` | HTTP/REST |
| Repository Service | `8082` | HTTP/REST |
| CI/CD Service | `8084` | HTTP/REST |
| Code Review Service | `8085` | HTTP/REST |
| Chat Service | `8005` | WebSocket/STOMP |
| Notification Service | `8083` | Kafka (no HTTP) |
| Eureka Server | `8761` | HTTP |
| Config Server | `8888` | HTTP |

---

## 🚀 Getting Started

### Prerequisites
- Java 21
- Maven
- Docker & Docker Compose
- Node.js *(only needed for WebSocket testing)*

### 1. Start Infrastructure
```bash
docker compose up -d
```

### 2. Create Databases
```bash
# Auth + Repository
docker exec -it devflow-postgres psql -U devflow -d postgres -c "CREATE DATABASE devflow_db;"

# Code Review
docker exec -it devflow-postgres psql -U devflow -d postgres -c "CREATE DATABASE code_review_db;"

# CI/CD
docker exec -it devflow-postgres psql -U devflow -d postgres -c "CREATE DATABASE cicd_db;"
```

### 3. Configure Secrets
In your IDE run configurations (never commit these), set:
```
USEREMAIL=your_gmail@gmail.com
USERPASS=your_16_digit_app_password
```

### 4. Start Services — in this order
```
1. config-server    (port 8888)
2. eureka-server    (port 8761)
3. auth-service     (port 8081)
4. api-gateway      (port 8080)
5. repository-service, chat-service, code-review-service, ci-cd-service, notification-service
```

Each service can be started with:
```bash
./mvnw spring-boot:run
```

---

## 📡 API Reference

### Authentication
All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```
Obtain a token via `POST /api/auth/login`.

### Example — Create Repository
```bash
curl -X POST http://localhost:8080/api/repositories/create \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"my-repo","ownerEmail":"dev@devflow.com","description":"My project","isPrivate":false}'
```

### Example — Submit Code Review
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "repositoryId": 1,
    "pullrequestId": "550e8400-e29b-41d4-a716-446655440000",
    "authorId": "123e4567-e89b-12d3-a456-426614174000",
    "comments": "Looks good — please add unit tests."
  }'
```

---

## 📨 Kafka Event Streams

```
Repository Service ──► project-lifecycle ──────────► Notification Service
                        { ownerEmail, name }           → sends welcome email

Code Review Service ──► code-review-events ──────────► Notification Service
                        "New review created for PR: x"  → logs alert

Repository Service ──► code-uploaded-topic
                        { ownerEmail, name, fileName }
```

---

## 🔒 Security Model

```
Client → API Gateway (JWT validation) → X-User-Id header injected → Downstream Service
                │
                └── /api/auth/**   ← no token required
                └── /api/chat/**   ← JWT validated at WebSocket handshake (query param)
                └── all others     ← Bearer token mandatory
```

- Tokens signed with **HMAC-SHA256**, 24-hour expiry
- `userId` extracted from token by gateway — services never trust client-supplied identity
- Chat service performs its own JWT validation independently at the WebSocket handshake level
- Mail credentials injected via environment variables — never stored in source

---

<div align="center">

Built with ☕ Java · 🍃 Spring · 🪶 Kafka · 🐋 Docker

**Happy Coding — The DevFlow Team**

</div>