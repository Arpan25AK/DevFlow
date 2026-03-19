# 🚪 API Gateway (The Front Door)

**Port:** `8080`

## 📖 Overview
The API Gateway serves as the single entry point into the DevFlow ecosystem. Frontend applications (React/Angular) or external clients only ever communicate with this service.

It abstracts the internal microservice architecture, handles dynamic request routing using Eureka (`lb://`), and acts as the first line of defense for security and rate limiting.

## 🛠️ Tech Stack
* **Java 21**
* **Spring Boot 3.5.x**
* **Spring Cloud Gateway**
* **Spring Cloud Netflix Eureka Client**

## 🚦 Current Routing Rules
* `/api/auth/**` ➡️ Routes to `auth-service`