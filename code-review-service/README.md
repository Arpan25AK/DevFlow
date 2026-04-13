# Code Review Service

The **Code Review Service** is a core microservice within the DevFlow ecosystem. It manages the submission, storage, and status tracking of code reviews for pull requests.

This service demonstrates both **synchronous** and **asynchronous** microservice communication by validating repositories via HTTP (OpenFeign) and broadcasting notification events via message broker (Kafka).

## 🚀 Tech Stack
* **Java 21**
* **Spring Boot 3.x**
* **Spring Data JPA & Hibernate**
* **PostgreSQL** (Database)
* **Spring Cloud OpenFeign** (Synchronous REST communication)
* **Apache Kafka** (Asynchronous event streaming)
* **Netflix Eureka Client** (Service Discovery)

---

## 🏗️ Architecture & Integration
This service does not operate in isolation. It relies on the DevFlow ecosystem to function securely and efficiently:

1. **API Gateway & Auth Service:** All incoming requests are routed through the API Gateway, which validates the JWT token and securely injects the `X-User-Id` header. The Code Review Service uses this header to identify the `reviewerId` without trusting the frontend.
2. **Repository Service (Synchronous):** Before saving a review, this service makes an OpenFeign call to the Repository Service to ensure the provided `repositoryId` actually exists.
3. **Notification Service (Asynchronous):** Upon successfully saving a code review, this service fires a `"Review Created"` event to the `code-review-events` Kafka topic. The Notification Service listens to this topic to send emails to the code authors.

---

## 🛠️ Setup & Installation

### 1. Prerequisites
Ensure your infrastructure is running via Docker Compose:
* PostgreSQL (Port: 5432)
* Kafka & Zookeeper (Port: 9092, 2181)
* Eureka Server (Port: 8761)

### 2. Database Creation
Before starting the service, you must create its dedicated database. Run the following command in your terminal to create it inside your running Postgres container:

```bash
docker exec -it devflow-postgres psql -U devflow -d postgres -c "CREATE DATABASE code_review_db;"
Note: The application has a built-in @PostConstruct configuration to handle the Asia/Kolkata timezone mapping to prevent JDBC connection crashes.

3. Running the Service
You can run the application via your IDE or using the Maven wrapper:

Bash
./mvnw spring-boot:run
The service will start on Port 8085 and automatically register with the Eureka server.

🔌 API Endpoints
All requests should be made through the API Gateway (Port 8080) to ensure proper JWT authentication.

Create a Code Review
POST http://localhost:8080/api/reviews

Headers Required (via Gateway):

Authorization: Bearer <your-jwt-token>

Request Body:

JSON
{
  "repositoryId": 1,
  "pullrequestId": "550e8400-e29b-41d4-a716-446655440000",
  "authorId": "123e4567-e89b-12d3-a456-426614174000",
  "comments": "The code looks great, but please add some unit tests for this new logic."
}
Responses:

201 Created: Returns the saved Code Review object and triggers a Kafka event.

400 Bad Request: If the repositoryId does not exist in the Repository Service.

403 Forbidden / 401 Unauthorized: If the JWT token is missing or invalid.