# DevFlow: Notification Service (The Sentinel) ✉️

The Notification Service acts as the "Sentinel" of the DevFlow microservice ecosystem. It is a purely event-driven background worker that monitors platform activity and dispatches real-time alerts to users.

Unlike other services in DevFlow, this service has **no REST API endpoints**. It operates entirely by listening to Apache Kafka streams and executing asynchronous tasks, keeping the rest of the platform completely decoupled and blazing fast.

## 🚀 Tech Stack
* **Framework:** Java 21, Spring Boot 3.5
* **Messaging:** Apache Kafka (Consumer)
* **Email Client:** Spring Boot Starter Mail (JavaMailSender)
* **Templating:** Thymeleaf (Dynamic HTML Emails)
* **Discovery:** Netflix Eureka Client

## 🏗️ Architecture & Flow
1. **The Listener:** Constantly polls the `project-lifecycle` Kafka topic using the `notification-group` consumer group.
2. **The Interceptor:** Catches JSON payloads emitted by other services (e.g., when the Repository Service creates a new MinIO vault).
3. **The Engine:** Parses the JSON event into a Java Record (`ProjectEvent`).
4. **The Templater:** Injects dynamic variables (Developer Email, Project Name) into a modern HTML template using Thymeleaf.
5. **The Dispatcher:** Authenticates with Google's SMTP servers via a secure App Password and transmits the HTML email.

## ⚙️ Local Development Setup
This service requires the core event-driven infrastructure to be online. Ensure the following containers/services are running:
1. `kafka` & `zookeeper` (Port 9092)
2. `eureka-server` (Port 8761)

## ##**To Run This Service**
./mvnw spring-boot:run

**Crucial Security Note:**
To run this service locally, you must provide a valid Google App Password in the `application.yaml`. **Never commit your actual App Password to version control.**
```yaml
spring:
  mail:
    username: your_email@gmail.com
    password: YOUR_16_DIGIT_APP_PASSWORD

You can keep the credentials as it is or can replace it with a place
holder to apply it in run config

