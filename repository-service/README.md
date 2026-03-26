# DevFlow: Repository Service (The Vault) 🗄️

The Repository Service is the core storage and version control metadata manager for the DevFlow microservice ecosystem. It acts as "The Vault," responsible for securely storing project metadata in PostgreSQL and managing physical code assets within a self-hosted MinIO object storage warehouse.

Additionally, this service operates within an Event-Driven Architecture, broadcasting real-time lifecycle events via Apache Kafka to decoupled services (like the Notification Service).

## 🚀 Tech Stack
* **Framework:** Java 21, Spring Boot 3.5
* **Database:** PostgreSQL (Metadata: Project Name, Owner, Visibility)
* **Object Storage:** MinIO (S3-Compatible file storage)
* **Messaging:** Apache Kafka (Event broadcasting)
* **Discovery:** Netflix Eureka Client
* **ORM:** Spring Data JPA / Hibernate

## 🏗️ Architecture & Flow
1. **Metadata Management:** Validates and stores repository details in PostgreSQL.
2. **Dynamic Provisioning:** Automatically initializes the `devflow-repos` bucket in MinIO on startup.
3. **Event Broadcasting:** Emits a JSON payload to the `project-lifecycle` Kafka topic whenever a new repository is initialized.
4. **File Streaming:** Directly streams `multipart/form-data` uploads into the MinIO warehouse without overloading server RAM.

## 📡 API Endpoints

All endpoints are routed through the API Gateway at `http://localhost:8080/api/repositories`.

### Project Management
* **Create Repository**
    * `POST /create`
    * Body (JSON): `{"name": "string", "ownerEmail": "string", "description": "string", "isPrivate": boolean}`
    * *Emits Kafka Event on success.*

* **Get User Repositories**
    * `GET /getrepos/{ownerEmail}`
    * Returns: A JSON array of all projects owned by the user.

* **Check Repository Existence**
    * `GET /repoexists/{ownerEmail}/{name}`
    * Returns: `true` or `false`.

### File Operations (MinIO)
* **Upload File**
    * `POST /upload/{ownerEmail}/{projectName}`
    * Body: `form-data` with key `file` (type: File).
    * *Streams the file directly into `ownerEmail/projectName/filename` inside MinIO.*

* **List Files (Inventory)**
    * `GET /files/{ownerEmail}/{projectName}`
    * Returns: A JSON array of file names currently stored in the repository.

* **Download File**
    * `GET /download/{ownerEmail}/{projectName}?fileName={exact_file_name}`
    * Returns: The physical file as a downloadable attachment (`application/octet-stream`).

## ⚙️ Local Development Setup
This service relies on the core infrastructure defined in the global `docker-compose.yml`. Ensure the following containers are running before starting the service:
1. `devflow-postgres` (Port 5432)
2. `devflow-minio` (Ports 9000, 9001)
3. `kafka` & `zookeeper` (Port 9092)
4. `eureka-server` (Port 8761)

**To run the service:**
```bash
./mvnw spring-boot:run