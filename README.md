# Enterprise E-Commerce Microservices Platform

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk17/)
[![Spring Boot 3.2.x](https://img.shields.io/badge/Spring_Boot-3.2.x-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud 2023.0.1](https://img.shields.io/badge/Spring_Cloud-2023.0.1-6DB33F?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-3.x-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)](https://kafka.apache.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-6.0-47A248?style=for-the-badge&logo=mongodb&logoColor=white)](https://www.mongodb.com/)
[![Redis](https://img.shields.io/badge/Redis-7.x-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

A production-ready, enterprise-grade E-Commerce platform built from scratch utilizing a highly scalable, resilient **Microservices Architecture**. This repository features service-discovery, API gateway-level security, role-based access control (RBAC), database connection pooling, distributed caching, async event-driven messaging, and comprehensive containerization.

---

## 📸 Platform Showcase & Architecture

![Platform Overview & Architecture Banner](ecom_microservices_thumbnail.png)

### Logical Workflow Diagram

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

## 🌟 Key Features

### 🛡️ 1. Identity & Security (User Service)
*   **Stateless Session Authentication**: Uses secure JSON Web Tokens (JWT) for user verification and auth flow.
*   **Token Rotation Strategy**: Implements double-token patterns (short-lived Access Tokens, long-lived Refresh Tokens).
*   **Role-Based Access Control (RBAC)**: Fine-grained authorizations protecting admin endpoints (e.g., product creation, order status management).
*   **Blacklist / Revocation Flow**: Secure logout invalidates refresh tokens and session histories.

### 📦 2. Scalable Product Catalog & Caching (Product Service)
*   **High Performance Cataloging**: Categories and products are fully paginated, sorted, and filtered on demand.
*   **Redis Caching Layers**: Reduces database hits by caching hot categories (TTL: 10 mins) and product catalog listings (TTL: 30 mins) with automatic cache eviction on product updates.
*   **Soft Deletes**: Ensures data integrity by marking items as inactive rather than deleting records permanently.

### 💳 3. ACID Transactions & Sync Integrations (Order Service)
*   **Synchronous Stock Checks**: Uses Declarative Spring Cloud OpenFeign client to communicate with `Product Service` during checkout, verifying stock and reducing quantities in a single transaction.
*   **Price Snapshotting**: Freezes product pricing details at the moment of checkout, ensuring historical order data remains intact when product listing prices change.
*   **Order Resiliency**: Implements rollbacks and stock recovery when orders are cancelled.

### 🔔 4. Event-Driven Asynchronous Messages (Notification Service)
*   **Kafka Messaging Backbone**: Decoupled notification processing using Apache Kafka topics.
*   **MongoDB Registry**: Preserves notification history, tracking order creation, updates, and cancellations asynchronously without impacting main checkout latency.

### 🔀 5. Enterprise Infrastructure & Orchestration
*   **Netflix Eureka Discovery**: High-availability service registration registry to make microservices location-transparent.
*   **Spring Cloud API Gateway**: Central entryway handles cross-cutting concerns: gateway routing, CORS headers, security verification, and Redis-backed rate limiting.
*   **HikariCP Database Connection Pool**: Tuned connection properties for fast PostgreSQL execution.
*   **Dockerization & Dependency Health Checks**: All databases and microservices are containerized. Docker compose starts services in a strictly healthy order using container health checks.

---

## 📂 Codebase Directory Layout

```text
ecommerce-microservices/
├── api-gateway/            # Spring Cloud Gateway routing & JWT security verification
├── discovery-server/       # Eureka Discovery Server registry
├── user-service/           # User administration, authentication, and JWT lifecycle
├── product-service/        # Product inventory catalog & Redis cache management
├── order-service/          # Order placement, price snapshotting & Feign client integrations
├── notification-service/   # Async Kafka consumer & MongoDB notification history logs
├── docker-compose.yml      # Complete infrastructure configuration (PostgreSQL, MongoDB, Kafka, Redis)
├── .env.example            # Configuration variables template
└── README.md               # Documentation
```

---

## 🛠️ Technology Stack Breakdown

| Component | Technology | Detail / Version |
| :--- | :--- | :--- |
| **Runtime Environment** | Java 17 | OpenJDK 17 with Spring Boot 3.2.x |
| **Build Automation** | Maven | Monorepo structure, separate `pom.xml` per service module |
| **Discovery Registry** | Spring Cloud Eureka | Netflix Eureka discovery client & server |
| **API Routing & Filter** | Spring Cloud Gateway | Path routing, CORS filter configurations, and JWT authorization |
| **Secure Token Auth** | Spring Security + JWT | HMAC-SHA256 signing, Token-based Stateless authorization |
| **Relational Database** | PostgreSQL 15 | Separate databases for Users, Products, and Orders |
| **Document Database** | MongoDB 6.0 | Stores notification records with dynamic JSON models |
| **Distributed Caching** | Redis 7.x | Caching product listings and managing API rate limits |
| **Event Broker** | Apache Kafka 3.x | Confluent Community Kafka & Zookeeper orchestration |
| **Connection Pooling** | HikariCP | Maximum pool size = 10, minimum idle = 5, timeout = 30s |
| **API Playground** | OpenAPI 3 / Swagger | Interactive routes playground at `/swagger-ui.html` for each service |
| **Containerization** | Docker | Multi-stage Dockerfiles coordinated via Docker Compose |

---

## 🚀 Getting Started

### 📋 Prerequisites
Ensure the following tools are set up on your machine:
*   [Java 17 JDK](https://openjdk.org/projects/jdk17/) installed and on system path
*   [Apache Maven 3.8+](https://maven.apache.org/)
*   [Docker & Docker Desktop](https://www.docker.com/products/docker-desktop/) (running)

---

### 🐳 Option A: Running with Docker Compose (Recommended)

Docker Compose initializes all service dependencies (PostgreSQL, Redis, MongoDB, Kafka, Zookeeper) and microservices in a single command.

#### 1. Configure the Environment
Copy the example environment template to create your `.env` file:
```bash
cp .env.example .env
```

> [!NOTE]
> Open the `.env` file and customize the database passwords, JWT secrets, and ports if required. The defaults are ready to run out of the box.

#### 2. Start the Microservices
Build and run the entire stack in detached mode:
```bash
docker compose up -d --build
```

#### 3. Monitor Health Check Progress
The containers utilize Docker health checks to boot up in the correct order. You can verify that all instances are fully healthy:
```bash
docker compose ps
```

To view logs for all services or a specific container:
```bash
docker compose logs -f
# Or view a specific container
docker compose logs -f api-gateway
```

#### 4. Stop the Environment
To tear down the containers and preserve persistent database volumes:
```bash
docker compose down
```
To delete the containers along with the persistent database volumes:
```bash
docker compose down -v
```

---

### 💻 Option B: Running Locally (For Active Development)

If you prefer to run the microservices locally to debug in your IDE:

#### 1. Spin Up Only the Middleware Services
Use Docker Compose to run only the databases, cache, and messaging queue:
```bash
docker compose up -d postgres-user postgres-product postgres-order mongodb redis zookeeper kafka
```

#### 2. Configure Local Environment Variables
Create a local `.env` file at the root or inject variables into your IDE runner:
*   Use `localhost` instead of container names (e.g., `redis` -> `localhost`, `kafka` -> `localhost`).
*   Ensure the databases, Redis, and Kafka correspond to the mapped ports (Postgres User: `5433`, Postgres Product: `5434`, Postgres Order: `5435`, Redis: `6379`, MongoDB: `27017`, Zookeeper: `2181`, Kafka: `9092`).

#### 3. Build the Core Services
Compile and package the microservices using Maven:
```bash
mvn clean install -DskipTests
```

#### 4. Start Services in Order
For proper routing and registration, run the modules in this sequence:

```bash
# Terminal 1: Start Discovery Server (Registry)
cd discovery-server && mvn spring-boot:run

# Terminal 2: Start API Gateway (Router & Filter)
cd api-gateway && mvn spring-boot:run

# Terminal 3: Start User Management & Auth
cd user-service && mvn spring-boot:run

# Terminal 4: Start Product Catalog
cd product-service && mvn spring-boot:run

# Terminal 5: Start Order Processing
cd order-service && mvn spring-boot:run

# Terminal 6: Start Notification Logs Consumer
cd notification-service && mvn spring-boot:run
```

---

## 📡 API Reference Directory

All microservice APIs are routed through the **API Gateway** on Port `8080`.

<details>
<summary>🔑 Authentication & User Management Service (Port 8081 via Gateway 8080)</summary>

| HTTP Method | Route Endpoint | Authentication | Allowed Roles | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/register` | Public | All | Register a new user account. |
| `POST` | `/api/v1/auth/login` | Public | All | Log in to receive Access Token & Refresh Token. |
| `POST` | `/api/v1/auth/refresh-token` | Public | All | Exchange refresh token for a new access token. |
| `POST` | `/api/v1/auth/logout` | Secure | All | Revokes active refresh token and logs user out. |
| `GET` | `/api/v1/users/profile` | Secure | USER, ADMIN | Retrieve active profile details. |
| `PUT` | `/api/v1/users/profile` | Secure | USER, ADMIN | Edit profile information. |
| `GET` | `/api/v1/users` | Secure | ADMIN | Retrieve a paginated list of all users. |
| `DELETE` | `/api/v1/users/{id}` | Secure | ADMIN | Remove user profile by ID. |

</details>

<details>
<summary>🛍️ Product Catalog & Inventory Service (Port 8082 via Gateway 8080)</summary>

| HTTP Method | Route Endpoint | Authentication | Allowed Roles | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/products` | Public | All | Get active products (paginated, sorted, filtered by category). |
| `GET` | `/api/v1/products/{id}` | Public | All | Retrieve specific product details by ID. |
| `POST` | `/api/v1/products` | Secure | ADMIN | Create a new catalog product. |
| `PUT` | `/api/v1/products/{id}` | Secure | ADMIN | Update product details. |
| `DELETE` | `/api/v1/products/{id}` | Secure | ADMIN | Soft delete product from search catalog. |
| `GET` | `/api/v1/categories` | Public | All | Retrieve product categories. |
| `POST` | `/api/v1/categories` | Secure | ADMIN | Register a new product category. |

</details>

<details>
<summary>💳 Order Transaction Service (Port 8083 via Gateway 8080)</summary>

| HTTP Method | Route Endpoint | Authentication | Allowed Roles | Description |
| :--- | :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/orders` | Secure | USER, ADMIN | Place a new order (verifies stock & triggers checkout). |
| `GET` | `/api/v1/orders` | Secure | USER, ADMIN | View order history for logged-in user. |
| `GET` | `/api/v1/orders/{id}` | Secure | USER, ADMIN | View order details and statuses. |
| `DELETE` | `/api/v1/orders/{id}` | Secure | USER, ADMIN | Cancel an order (restores inventory & publishes event). |
| `PUT` | `/api/v1/orders/{id}/status` | Secure | ADMIN | Update status (`PENDING`, `CONFIRMED`, `SHIPPED`, etc.). |
| `GET` | `/api/v1/orders/admin/all` | Secure | ADMIN | Retrieve all system orders (paginated). |

</details>

<details>
<summary>🔔 Notification Service (Port 8084 via Gateway 8080)</summary>

| HTTP Method | Route Endpoint | Authentication | Allowed Roles | Description |
| :--- | :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/notifications/my` | Secure | USER, ADMIN | Fetch database logs of notifications sent to the user. |

</details>

---

## ⚡ Asynchronous Event-Driven Architectures

The system uses event queues to pass orders and system changes asynchronously. The following JSON schemas are used across Kafka topics:

### 1. Topic: `order.created`
Fired immediately upon checkout. Consumed by `Notification Service`.
```json
{
  "orderId": "5fa23d11-5465-4700-9831-cd9a77ef1c08",
  "userId": "1b988f0a-3cc9-482a-bc91-236b325201fa",
  "userEmail": "customer@example.com",
  "items": [
    {
      "productId": "88aa90be-e04f-4aef-bbdf-1123490bcf88",
      "productName": "Ultra-Wide Gaming Monitor",
      "unitPrice": 499.99,
      "quantity": 1
    }
  ],
  "totalAmount": 499.99,
  "createdAt": "2026-06-03T23:40:00"
}
```

### 2. Topic: `order.status.updated`
Fired when administrators transition order state.
```json
{
  "orderId": "5fa23d11-5465-4700-9831-cd9a77ef1c08",
  "userId": "1b988f0a-3cc9-482a-bc91-236b325201fa",
  "userEmail": "customer@example.com",
  "oldStatus": "CONFIRMED",
  "newStatus": "SHIPPED",
  "updatedAt": "2026-06-03T23:45:00"
}
```

### 3. Topic: `order.cancelled`
Fired when a user cancels an order.
```json
{
  "orderId": "5fa23d11-5465-4700-9831-cd9a77ef1c08",
  "userId": "1b988f0a-3cc9-482a-bc91-236b325201fa",
  "userEmail": "customer@example.com",
  "reason": "Cancelled by user request",
  "cancelledAt": "2026-06-03T23:50:00"
}
```

---

## 🧪 Testing Suite & Quality Assurance

The codebase includes integration tests that verify database operations and communications.

To run the verification suite across the entire project:
```bash
mvn clean test
```

### Key Test Coverage Highlights:
*   **User Service**: Checks hashing algorithms, validation tokens, authentication, session blacklisting, and profile management.
*   **Product Service**: Verifies Redis caching operations, data retrieval speeds, serialization, and catalog filtering logic.
*   **Order Service**: Validates OpenFeign communication, transactional integrity, and product inventory updates.

---

## ❓ FAQ & Troubleshooting

#### 1. Why are my services failing to start when running `docker compose up`?
Verify that no other processes on your host system are using the required ports (e.g. Postgres on `5433`/`5434`/`5435`, Redis on `6379`, Mongo on `27017`, or Gateway on `8080`). You can kill existing processes or edit the ports in `.env` and `docker-compose.yml`.

#### 2. How long does Eureka take to register the microservices?
After the microservices start, they can take up to 30 seconds to appear on Eureka dashboard (`http://localhost:8761`). If you try to route requests through API Gateway too early, you might receive `503 Service Unavailable`.

#### 3. How do I test the APIs?
Import the provided Postman collection file `ecommerce_microservices.postman_collection.json` into Postman. It contains pre-configured requests for register, login, profile updates, product creation, order checks, and cancellation flows.

---

## 👥 Contributors & Author
*   **Rimjhim Krishna** - [@rimjhimkrishna](https://github.com/rimjhimkrishna)
