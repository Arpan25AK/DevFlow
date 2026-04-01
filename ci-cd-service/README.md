# Review Service

The Review Service is a core microservice responsible for handling all user-generated reviews, ratings, and feedback within the platform. It manages the lifecycle of a review (creation, retrieval, updating, and moderation) and provides aggregated rating data to other services.

## 🛠 Tech Stack

* **Framework:** Java Spring Boot
* **API/Communication:** gRPC & Protocol Buffers (Internal) / REST (External facing)
* **Database:** Relational SQL (PostgreSQL/MySQL)
* **Containerization:** Docker
* **Build Tool:** Maven/Gradle

## 📋 Prerequisites

Before you begin, ensure you have the following installed locally:
* Java Development Kit (JDK) 17 or higher
* Docker & Docker Compose
* Git
* Your preferred IDE (IntelliJ IDEA, Eclipse, etc.)

## 🚀 Local Development Setup

**1. Clone the repository:**
```bash
git clone <repository-url>/review-service.git
cd review-service
