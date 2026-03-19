
### 📞 2. The Eureka Server Documentation
Navigate into your `eureka-server/` folder, create a **`README.md`** file, and paste this in:

```markdown
# 🗺️ Eureka Service Registry (The Phonebook)

**Port:** `8761`

## 📖 Overview
The Eureka Server is the central service registry for the DevFlow ecosystem. Because microservices dynamically spin up and down (changing IP addresses and ports), they cannot rely on hardcoded URLs to communicate. 

Instead, every service registers itself with this Eureka Server upon boot. When Service A needs to talk to Service B, it queries this registry to get the live, routing-ready address.

## 🛠️ Tech Stack
* **Java 21**
* **Spring Boot 3.5.x**
* **Spring Cloud Netflix Eureka Server**

## 🚀 How to Run
1. Ensure your Docker infrastructure (specifically network components if applicable) is running.
2. Run the `EurekaServerApplication.java` main method.
3. Access the live dashboard at: [http://localhost:8761](http://localhost:8761)

## ⚙️ Key Configurations
This server is configured to run standalone. In `application.yml`, it is explicitly told **not** to register with itself to prevent infinite loop registry errors:
```yaml
eureka.client.register-with-eureka: false
eureka.client.fetch-registry: false
```
```

