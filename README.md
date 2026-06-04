# Enterprise E-Commerce Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk17/)
[![Spring Boot 3.2.x](https://img.shields.io/badge/Spring_Boot-3.2.x-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2023.0.1](https://img.shields.io/badge/Spring_Cloud-2023.0.1-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.x-231F20?style=flat&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0-47A248?style=flat&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=flat&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)

A production-ready, enterprise-grade E-Commerce platform built from scratch utilizing a highly scalable **Microservices Architecture**. This repository features service-discovery, API gateway-level security, role-based access control (RBAC), database connection pooling, distributed caching, async event-driven messaging, and Dockerization.

---

## 1. System Architecture

```text
                       [ CLIENTS (Postman / Browser) ]
                                      |
                                      v (Port 8080)
                       +-------------------------------+
                       |          API GATEWAY          |  <--- (JWT Verification / Rate Limiting)
                       +-------------------------------+
                                 /    |    \
          +---------------------+     |     +-------------------------+
          | (Load Balancing)          |                               |
          v                           v                               v
   +--------------+            +--------------+                +--------------+
   | USER-SERVICE |            | PROD-SERVICE |                | ORDER-SERVICE|
   | (Port 8081)  |            | (Port 8082)  |                | (Port 8083)  |
   +--------------+            +--------------+                +--------------+
          |                           |                               |
          | (JPA)                     | (JPA + Redis Cache)           | (JPA + OpenFeign Sync Client)
          v                           v                               v
    [ postgres_user ]           [ postgres_prod ]               [ postgres_order ]
      (Port 5433)                 (Port 5434)                     (Port 5435)
                                                                      |
                                                                      | (Async Kafka Event)
                                                                      v
                                                             [ KAFKA BROKER (9092) ]
                                                                      |
                                                                      v (Kafka Listener)
                                                             +------------------+
                                                             |   NOTIF-SERVICE  |
                                                             |    (Port 8084)   |
                                                             +------------------+
                                                                      |
                                                                      v (MongoDB)
                                                                [ mongo_notif ]
                                                                  (Port 27017)
```

---

## 2. Microservices Directory

| Service Name | Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| **Discovery Server** | `8761` | *N/A* | Service registration and lookup registry via Netflix Eureka. |
| **API Gateway** | `8080` | *N/A* | Gateway routing, CORS config, rate limiting (Redis), and JWT authentication. |
| **User Service** | `8081` | PostgreSQL | Registration, login session management, JWT/refresh-token rotation, and profiles. |
| **Product Service** | `8082` | PostgreSQL + Redis | Category cataloging, product search/filtering, Redis caching, and stock management. |
| **Order Service** | `8083` | PostgreSQL | Placing orders, calculating pricing snapshots, Feign client inventory checks, and Kafka event logs. |
| **Notification Service** | `8084` | MongoDB | Consuming Kafka events and preserving history logs of order updates. |

---

## 3. Technology Stack

| Component | Technology | Detail |
| :--- | :--- | :--- |
| **Runtime Environment** | Java 17 | JDK 17 with Spring Boot 3.2.5 |
| **Build Automation** | Maven | Separate `pom.xml` per service, managed globally |
| **Discovery & Routing** | Spring Cloud | Spring Cloud Gateway & Netflix Eureka |
| **Communication** | OpenFeign & Kafka | Sync Feign calls (Order -> Product) & Async Kafka events (Order -> Notification) |
| **Authentication** | Spring Security + JWT | Stateless sessions, token signing via shared secret key |
| **Caching Layer** | Redis | Caching catalogs (TTL: 10 mins) and single products (TTL: 30 mins) |
| **Database Pool** | HikariCP | Maximum pool size = 10, minimum idle = 5, timeout = 30000ms |
| **Documentation** | OpenAPI / Swagger | Interactive routes playground at `/swagger-ui.html` for each service |
| **DevOps & Containers** | Docker | Multi-stage Dockerfiles coordinated via Docker Compose |

---

## 4. Prerequisites

Before running the application, make sure you have the following installed:
- [Java 17 JDK](https://openjdk.org/projects/jdk17/)
- [Apache Maven 3.8+](https://maven.apache.org/)
- [Docker & Docker Desktop](https://www.docker.com/products/docker-desktop/)

---

## 5. How to Run with Docker Compose

1. Clone the repository and navigate to the project directory:
   ```bash
   cd ecommerce-microservices
   ```

2. Copy the template `.env.example` file to `.env`:
   ```bash
   cp .env.example .env
   ```

3. Build and launch all services in the background:
   ```bash
   docker compose up -d --build
   ```

4. Verify that all containers are healthy:
   ```bash
   docker compose ps
   ```

---

## 6. How to Run Locally

If you want to run the microservices locally outside of Docker containers:

1. Ensure Postgres, MongoDB, Redis, and Zookeeper/Kafka are running on their default ports.
2. Update the `.env` settings to target `localhost` (e.g. `USER_DB_URL=jdbc:postgresql://localhost:5433/user_db`).
3. Run each service module using Maven:
   ```bash
   # In terminal 1: Discovery Server
   cd discovery-server && mvn spring-boot:run
   
   # In terminal 2: API Gateway
   cd api-gateway && mvn spring-boot:run

   # In terminal 3: User Service
   cd user-service && mvn spring-boot:run

   # In terminal 4: Product Service
   cd product-service && mvn spring-boot:run

   # In terminal 5: Order Service
   cd order-service && mvn spring-boot:run

   # In terminal 6: Notification Service
   cd notification-service && mvn spring-boot:run
   ```

---

## 7. API Endpoints Directory

All secure endpoints require the `Authorization: Bearer <token>` header. Public endpoints can be accessed without a token.

### User & Auth Service (Port 8081 via Gateway Port 8080)
- `POST /api/v1/auth/register` (Public) - Create a new user.
- `POST /api/v1/auth/login` (Public) - Obtain Access Token (15m expiry) and Refresh Token (7d expiry).
- `POST /api/v1/auth/refresh-token` (Public) - Rotate tokens before expiration.
- `POST /api/v1/auth/logout` (Secure) - Revokes/destroys refresh token session.
- `GET /api/v1/users/profile` (Secure) - Get profile details.
- `PUT /api/v1/users/profile` (Secure) - Update profile info.
- `GET /api/v1/users` (Secure - ADMIN) - Get paginated list of users.
- `DELETE /api/v1/users/{id}` (Secure - ADMIN) - Delete a user.

### Product Catalog Service (Port 8082 via Gateway Port 8080)
- `GET /api/v1/products` (Public) - Get active products (paginated, sorted, filtered by category).
- `GET /api/v1/products/{id}` (Public) - Get product details.
- `POST /api/v1/products` (Secure - ADMIN) - Create new product.
- `PUT /api/v1/products/{id}` (Secure - ADMIN) - Update product details.
- `DELETE /api/v1/products/{id}` (Secure - ADMIN) - Soft delete product.
- `GET /api/v1/categories` (Public) - View categories.
- `POST /api/v1/categories` (Secure - ADMIN) - Create new category.

### Order Processing Service (Port 8083 via Gateway Port 8080)
- `POST /api/v1/orders` (Secure) - Places an order, checks stock synchronously, reduces stock, and logs a Kafka event.
- `GET /api/v1/orders` (Secure) - View personal order history.
- `GET /api/v1/orders/{id}` (Secure) - View order status and items list.
- `DELETE /api/v1/orders/{id}` (Secure) - Cancels order (restores stock in DB, emits cancellation event).
- `PUT /api/v1/orders/{id}/status` (Secure - ADMIN) - Update status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED).
- `GET /api/v1/orders/admin/all` (Secure - ADMIN) - Get all orders paginated.

### Notification History Service (Port 8084 via Gateway Port 8080)
- `GET /api/v1/notifications/my` (Secure) - List notification logs matching user ID.

---

## 8. Apache Kafka Topics

Async messaging relies on three topics:

1. **`order.created`**: Fired when a new checkout finishes.
   ```json
   {
     "orderId": "uuid-here",
     "userId": "uuid-here",
     "userEmail": "customer@example.com",
     "items": [{"productId": "uuid", "productName": "Laptop", "unitPrice": 1000.0, "quantity": 2}],
     "totalAmount": 2000.00,
     "createdAt": "2026-06-03T23:40:00"
   }
   ```
2. **`order.status.updated`**: Emitted when admin updates an order's status.
   ```json
   {
     "orderId": "uuid-here",
     "userId": "uuid-here",
     "userEmail": "customer@example.com",
     "oldStatus": "CONFIRMED",
     "newStatus": "SHIPPED",
     "updatedAt": "2026-06-03T23:45:00"
   }
   ```
3. **`order.cancelled`**: Emitted when a customer cancels their order.
   ```json
   {
     "orderId": "uuid-here",
     "userId": "uuid-here",
     "userEmail": "customer@example.com",
     "reason": "Cancelled by user",
     "cancelledAt": "2026-06-03T23:50:00"
   }
   ```

---

## 9. Testing Suite

To compile and verify all unit and integration tests:

Run the following command at the root folder `ecommerce-microservices`:
```bash
mvn clean test
```

This runs tests on:
- **User Service**: `AuthServiceTest`, `UserServiceTest`, `AuthControllerTest`
- **Product Service**: `ProductServiceTest`, `CategoryServiceTest`, `ProductControllerTest`
- **Order Service**: `OrderServiceTest`, `OrderControllerTest`
