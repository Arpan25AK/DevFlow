# 🔐 Auth Service (The Bouncer)

**Internal Port:** `8081`

## 📖 Overview
The Auth Service handles user registration, secure password hashing (BCrypt), and authentication. It issues stateless JSON Web Tokens (JWTs) that are used by the API Gateway to authorize access to the rest of the DevFlow ecosystem.

## 🛠️ Tech Stack
* **Java 21**
* **Spring Boot 3.5.x**
* **Spring Security**
* **io.jsonwebtoken (JJWT)**
* **Spring Data JPA & PostgreSQL**
* **Eureka Client**

## 🔌 API Endpoints
*Note: These endpoints should be accessed through the API Gateway (Port 8080).*

### 1. Register a New User
* **URL:** `POST /api/auth/register`
* **Body:**
  ```json
  {
    "email": "developer@devflow.com",
    "password": "superSecretPassword"
  }