# 📬 Notification Service

A production-grade, event-driven **Notification Microservice** built with **Java 17 + Spring Boot 3.5**. The service is designed around multiple industry-standard GoF design patterns — **Observer**, **Decorator**, **Factory**, and **Strategy** — and uses **Apache Kafka** for async notification dispatch, **PostgreSQL** for persistence, **Netflix Eureka** for service discovery, and **Resilience4j** for fault tolerance with a custom retry mechanism.

---

## 📐 Architecture Overview

```
Client
  │
  ▼
POST /api/v1/notification/send
  │
  ▼
NotificationController
  │
  ▼
NotificationService (Spring @Service)
  │  uses Factory to build INotification
  │  optionally wraps with Decorator (TimestampNotification)
  │
  ▼
ConcreteObservable.notifyObservers()
  │  fetches all registered Observers from PostgreSQL
  │  creates a NotificationEngine per Observer via NotificationEngineFactory
  │
  ▼
NotificationEngine.update()
  │  builds NotificationEvent (id, name, email, phone, text, timestamp)
  │  publishes to Kafka topic: "notification"
  │
  ▼
[Kafka Topic: notification — 3 partitions]
  │
  ▼
EmailStrategy (@KafkaListener)
  │  @Retry(name = "notificationRetry") — 3 attempts, 200ms wait
  │  fallback: sendNotificationFallback()
  │
  ▼
EmailService → JavaMailSender → Gmail SMTP
```

The service runs on port **8084** with context path `/api/v1/notification` and registers itself with a **Eureka Server** at `localhost:8761`.

---

## 🔷 Low-Level Design (LLD)

The codebase is composed of five interlocking design patterns:

### 1. Factory Pattern — `NotificationFactory` & `NotificationMessageFactory`

Two-level factory hierarchy for clean object construction:

| Interface | Implementation | Role |
|---|---|---|
| `INotificationFactory` | `SimpleNotificationFactory` | Creates `INotification` instances |
| `INotificationMessageFactory` | `SimpleNotificationMessageFactory` | Creates plain `NotiMessageDto` |
| `INotificationMessageFactory` | `TimestampNotificationMessageFactory` | Creates `NotiMessageDto` with `createdAt` pre-set |

`SimpleNotificationFactory` is a Spring `@Component` that produces a `SimpleNotification` by delegating DTO creation to the injected message factory.

### 2. Decorator Pattern — `NotificationCRUD`

Allows dynamic enrichment of a notification's content without modifying the base class:

```
INotification  (interface)
    └── SimpleNotification             ← concrete leaf; holds NotiMessageDto
    └── INotificationDecorator         ← extends INotification (marker interface)
            └── TimestampNotification  ← wraps any INotification, sets createdAt = LocalDateTime.now()
```

`TimestampNotification` follows the classic decorator: it accepts an `INotification` in its constructor, delegates `getContent()` to the wrapped object, copies the DTO, and stamps it with the current time. Additional decorators (e.g. `SignatureDecorator`) can be chained the same way.

### 3. Observer Pattern — `NotificationObserver`

The observer list is **database-backed** rather than held in memory — a key design decision for a distributed microservice:

```
IObservable  (interface)
    └── ConcreteObservable (@Service)
            ├── addObserver()     → saves Observer entity to PostgreSQL
            ├── removeObserver()  → deletes by id from PostgreSQL
            └── notifyObservers() → fetches List<Observer> from repo,
                                    creates NotificationEngine per observer
                                    via NotificationEngineFactory, calls update()

IObserver  (interface)
    └── NotificationEngine
            └── update() → builds NotificationEvent, publishes to Kafka

IObserverFactory  (interface)
    └── NotificationEngineFactory (@Component)
            └── create() → injects KafkaTemplate + topic name into NotificationEngine
```

> **Design note (from source comments):** Rather than holding a `List<IObserver>` in memory, `ConcreteObservable` composes an `ObserverRepository`. `List<Observer> list = repository.findAll()` replaces the in-memory list at notify time — making observers persistent and survivable across service restarts.

### 4. Strategy Pattern — `NotificationStrategy`

Each delivery channel implements `INotificationStrategy` and is simultaneously a **Kafka consumer**:

```
INotificationStrategy  (interface)
    └── sendNotification(NotificationEvent event)

        └── EmailStrategy (@Service, @KafkaListener)
                ├── listens on topic: "notification", groupId: "notification-service"
                ├── @Retry(name = "notificationRetry") — Resilience4j retry
                ├── delegates to EmailService → JavaMailSender
                └── fallback: sendNotificationFallback() logs the failure
```

New channels (SMS, Push, WhatsApp) can be added by implementing `INotificationStrategy` and adding a `@KafkaListener` — zero changes to existing code.

### 5. Global Exception Handling — `GlobalExceptionHandler`

`@RestControllerAdvice` with three handlers: `Exception` (500), `IllegalArgumentException` (400), and `RuntimeException` (500). All return a structured `ErrorResponse` with `timestamp`, `status`, `error`, and `message` fields.

---

## 🛠️ Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5.x |
| Service Discovery | Netflix Eureka Client (Spring Cloud 2025.0.0) |
| Async Messaging | Apache Kafka (`spring-kafka`) |
| Kafka Topic | `notification` — 3 partitions, replication factor 1 |
| Database | PostgreSQL + Spring Data JPA (`ddl-auto: create-drop`) |
| Fault Tolerance | Resilience4j Retry |
| Email Delivery | Spring Boot Mail → Gmail SMTP (STARTTLS, port 587) |
| Validation | Jakarta Validation (`@NotNull`, `@Email`, `@Pattern`) |
| Object Mapping | ModelMapper 3.2.0 |
| Boilerplate | Lombok (`@Data`, `@RequiredArgsConstructor`, `@Slf4j`) |
| Build | Maven (Maven Wrapper included) |
| Tests | JUnit 5, Spring Boot Test, spring-kafka-test |

---

## 📁 Project Structure

```
notification-service/
├── src/
│   ├── main/
│   │   ├── java/com/shashank/notification_service/
│   │   │   │
│   │   │   ├── Advice/
│   │   │   │   └── GlobalExceptionHandler.java        # @RestControllerAdvice; handles Exception,
│   │   │   │                                          #   IllegalArgumentException, RuntimeException
│   │   │   │
│   │   │   ├── Config/
│   │   │   │   ├── AppConfig.java                     # @Bean ModelMapper
│   │   │   │   └── KafkaConfig.java                   # Creates "notification" NewTopic (3 partitions)
│   │   │   │
│   │   │   ├── Controllers/
│   │   │   │   └── NotificationController.java        # REST endpoints (POST /create, /create/timestamp,
│   │   │   │                                          #   /send, /create/observer)
│   │   │   │
│   │   │   ├── DTO/
│   │   │   │   ├── NotiMessageDto.java                # id, text, createdAt
│   │   │   │   ├── NotificationEvent.java             # Kafka message payload (id, name, email,
│   │   │   │   │                                      #   phone, text, timestamp)
│   │   │   │   ├── ObserverDto.java                   # id, name, email, phone
│   │   │   │   ├── RequestObserverDto.java            # Validated registration DTO (@Email, @Pattern)
│   │   │   │   └── ErrorResponse.java                 # Standard error envelope
│   │   │   │
│   │   │   ├── Entites/
│   │   │   │   ├── Notification.java                  # JPA: table "Notifications" (id, text, createAt)
│   │   │   │   └── Observer.java                      # JPA: table "Observers" (id, name, email, phone)
│   │   │   │
│   │   │   ├── NotificationCRUD/                      # ── DECORATOR PATTERN ──
│   │   │   │   ├── INotification.java                 # Interface: getContent() → NotiMessageDto
│   │   │   │   ├── SimpleNotification.java            # Concrete leaf; delegates to INotificationMessageFactory
│   │   │   │   └── NotificationDecorators/
│   │   │   │       ├── INotificationDecorator.java    # Marker interface extends INotification
│   │   │   │       └── TimestampNotification.java     # Wraps INotification; stamps createdAt
│   │   │   │
│   │   │   ├── NotificationFactory/                   # ── FACTORY PATTERN ──
│   │   │   │   ├── INotificationFactory.java          # getNotification(text, factory) → INotification
│   │   │   │   ├── SimpleNotificationFactory.java     # @Component; produces SimpleNotification
│   │   │   │   └── NotificationMessageFactory/
│   │   │   │       ├── INotificationMessageFactory.java          # getNotificationMessage(text) → NotiMessageDto
│   │   │   │       ├── SimpleNotificationMessageFactory.java     # @Component; plain NotiMessageDto
│   │   │   │       └── TimestampNotificationMessageFactory.java  # @Component; NotiMessageDto + LocalDateTime.now()
│   │   │   │
│   │   │   ├── NotificationObserver/                  # ── OBSERVER PATTERN ──
│   │   │   │   ├── Observable/
│   │   │   │   │   ├── IObservable.java               # addObserver / removeObserver / notifyObservers
│   │   │   │   │   └── ConcreteObservable.java        # @Service; DB-backed observer list
│   │   │   │   ├── Observer/
│   │   │   │   │   ├── IObserver.java                 # update()
│   │   │   │   │   └── NotificationEngine.java        # Builds NotificationEvent; publishes to Kafka
│   │   │   │   └── ObserverFactory/
│   │   │   │       ├── IObserverFactory.java          # create(observable, name, email, phone, notification)
│   │   │   │       └── NotificationEngineFactory.java # @Component; injects KafkaTemplate + topic name
│   │   │   │
│   │   │   ├── NotificationStrategy/                  # ── STRATEGY PATTERN ──
│   │   │   │   ├── INotificationStrategy.java         # sendNotification(NotificationEvent)
│   │   │   │   ├── KafkaConsumers/
│   │   │   │   │   └── EmailStrategy.java             # @KafkaListener + @Retry; delegates to EmailService
│   │   │   │   └── CoreServices/
│   │   │   │       └── EmailService.java              # JavaMailSender wrapper
│   │   │   │
│   │   │   ├── Repositories/
│   │   │   │   ├── NotificationRepo.java              # JpaRepository<Notification, Long>
│   │   │   │   └── ObserverRepository.java            # JpaRepository<Observer, Long>
│   │   │   │
│   │   │   ├── Services/
│   │   │   │   └── NotificationService.java           # Orchestrates Factory + Decorator + Observable;
│   │   │   │                                          #   persists Notification to DB
│   │   │   └── NotificationServiceApplication.java    # Spring Boot entry point
│   │   │
│   │   └── resources/
│   │       ├── application.yml                        # Active config (do NOT commit credentials)
│   │       └── application-example.yml               # Safe template with placeholder values
│   │
│   └── test/
│       └── java/com/shashank/notification_service/
│           └── NotificationServiceApplicationTests.java
│
├── pom.xml
├── mvnw / mvnw.cmd
└── .gitignore
```

---

## ⚙️ Prerequisites

- **Java 17+**
- **Maven 3.8+** (or use the included `./mvnw`)
- **Apache Kafka** with **Zookeeper** (local or Docker)
- **PostgreSQL** database named `NotificationDB`
- A running **Eureka Server** on port `8761`
- A **Gmail account** with an App Password (for SMTP)

---

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/shashank-varshney21/Notification-Service.git
cd Notification-Service
```

### 2. Start Infrastructure via Docker Compose

Create a `docker-compose.yml` in the project root:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: NotificationDB
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on: [zookeeper]
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
```

```bash
docker-compose up -d
```

### 3. Configure `application.yml`

Copy the provided template and fill in your credentials:

```bash
cp src/main/resources/application-example.yml src/main/resources/application.yml
```

```yaml
server:
  port: 8084
  servlet:
    context-path: /api/v1/notification

kafka:
  topic:
    notification: notification

spring:
  application:
    name: notification-service
  datasource:
    url: jdbc:postgresql://localhost:5432/NotificationDB
    username: postgres
    password: your_db_password
  jpa:
    hibernate:
      ddl-auto: create-drop     # change to "update" in production
    properties:
      format_sql: true
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.LongSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: ${spring.application.name}
      key-deserializer: org.apache.kafka.common.serialization.LongDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-16-digit-app-password    # no spaces
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
    fetch-registry: true
    register-with-eureka: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

resilience4j:
  retry:
    configs:
      default:
        maxRetryAttempts: 3
        waitDuration: 10s
    instances:
      notificationRetry:
        baseConfig: default
        waitDuration: 200ms
```

> **Gmail App Password:** Enable 2-Step Verification on your Google account, then generate a 16-character App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords). Enter it without spaces.

> ⚠️ **Never commit `application.yml` with real credentials.** Use `application-example.yml` as the committed template.

### 4. Start the Eureka Server

This service expects a Eureka Server running at `localhost:8761`. Minimal setup:

**`pom.xml` dependency:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

**Main class:**
```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**`application.yml`:**
```yaml
server.port: 8761
spring.application.name: eureka-server
eureka.client.register-with-eureka: false
eureka.client.fetch-registry: false
```

### 5. Build & Run

```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

Or run the JAR directly:

```bash
./mvnw clean package -DskipTests
java -jar target/notification-service-0.0.1-SNAPSHOT.jar
```

The service starts at: `http://localhost:8084/api/v1/notification`

---

## 📡 API Reference

All endpoints are prefixed with `/api/v1/notification`.

### Register an Observer (subscriber)

```http
POST /create/observer
Content-Type: application/json

{
  "name": "Shashank Varshney",
  "email": "user@example.com",
  "phone": "9876543210"
}
```

**Response:** `200 OK` — `"SUCCESS"`

Persists the observer to the `Observers` table. All registered observers will receive future `POST /send` calls.

---

### Create a Simple Notification

```http
POST /create
Content-Type: text/plain

Your order has been shipped!
```

**Response:** `200 OK`
```json
{
  "id": null,
  "text": "Your order has been shipped!",
  "createdAt": null
}
```

Creates and persists a `Notification` record. Does **not** send it yet.

---

### Create a Timestamp-Decorated Notification

```http
POST /create/timestamp
Content-Type: text/plain

Your order has been shipped!
```

**Response:** `200 OK`
```json
{
  "id": null,
  "text": "Your order has been shipped!",
  "createdAt": "2026-04-08T10:30:00.123456"
}
```

Wraps the notification with `TimestampNotification` decorator — `createdAt` is stamped at construction time via `LocalDateTime.now()`.

---

### Send a Notification (triggers full pipeline)

```http
POST /send
Content-Type: application/json

{
  "id": null,
  "text": "Your order has been shipped!",
  "createdAt": null
}
```

**Response:** `200 OK` — `"Notifications queued"`

This triggers the complete pipeline:
1. Builds an `INotification` via `SimpleNotificationFactory`
2. Calls `ConcreteObservable.notifyObservers(notification)`
3. Fetches all registered `Observer` records from PostgreSQL
4. Creates a `NotificationEngine` per observer via `NotificationEngineFactory`
5. Each engine's `update()` publishes a `NotificationEvent` to the Kafka `notification` topic
6. `EmailStrategy` consumes the event and dispatches an email via Gmail SMTP

---

## 🔄 Kafka Deep Dive

### Topic Configuration

| Property | Value |
|---|---|
| Topic name | `notification` |
| Partitions | 3 |
| Replication factor | 1 |
| Producer key serializer | `LongSerializer` |
| Producer value serializer | `JsonSerializer` |
| Consumer key deserializer | `LongDeserializer` |
| Consumer value deserializer | `JsonDeserializer` |
| Consumer group ID | `notification-service` |

The topic is auto-created on startup via the `KafkaConfig` `@Bean` (`NewTopic`).

### `NotificationEvent` Payload (Kafka message)

```json
{
  "id": 42,
  "name": "Shashank Varshney",
  "email": "user@example.com",
  "phone": "9876543210",
  "text": "Your order has been shipped!",
  "timestamp": "2026-04-08T10:30:00.123456"
}
```

`NotificationEngine.update()` uses `KafkaTemplate.send(topic, id, event).whenComplete(...)` for async send with success/failure logging.

---

## 🔁 Resilience4j Retry

`EmailStrategy` is annotated with `@Retry(name = "notificationRetry")`. If email dispatch fails (e.g. SMTP timeout), Resilience4j retries automatically before triggering the fallback.

```yaml
resilience4j:
  retry:
    configs:
      default:
        maxRetryAttempts: 3
        waitDuration: 10s
    instances:
      notificationRetry:
        baseConfig: default
        waitDuration: 200ms    # overrides the default 10s for this instance
```

On exhaustion, `sendNotificationFallback(NotificationEvent event, Throwable throwable)` fires and logs the failure via `@Slf4j`.

---

## 🧩 Design Patterns — Quick Reference

| Pattern | Key Classes | Java idiom |
|---|---|---|
| **Factory** | `INotificationFactory`, `SimpleNotificationFactory`, `INotificationMessageFactory`, `SimpleNotificationMessageFactory`, `TimestampNotificationMessageFactory` | Spring `@Component` with named bean qualifier |
| **Decorator** | `INotification`, `SimpleNotification`, `INotificationDecorator`, `TimestampNotification` | Explicit `new TimestampNotification(wrapped)` — intentional, not Spring-managed |
| **Observer** | `IObservable`, `ConcreteObservable`, `IObserver`, `NotificationEngine`, `IObserverFactory`, `NotificationEngineFactory` | DB-backed observer list via `JpaRepository` instead of an in-memory `List` |
| **Strategy** | `INotificationStrategy`, `EmailStrategy` | `@KafkaListener` acts as the runtime trigger for each channel strategy |

---

## 🗃️ Database Schema

Two JPA-managed entities with tables auto-created by Hibernate:

**Notifications**

| Column | Type | Notes |
|---|---|---|
| Id | BIGINT (PK) | `@GeneratedValue(IDENTITY)` |
| text | VARCHAR | Notification message body |
| createAt | TIMESTAMP | Set when using `TimestampNotification` decorator |

**Observers**

| Column | Type | Notes |
|---|---|---|
| id | BIGINT (PK) | `@GeneratedValue(IDENTITY)` |
| name | VARCHAR | Full name |
| email | VARCHAR | Validated with `@Email` |
| phone | VARCHAR | Validated: `^[0-9]{10}$` |

> ⚠️ `ddl-auto: create-drop` drops and recreates schema on every restart. Switch to `update` or `validate` + Flyway/Liquibase before deploying to any persistent environment.

---

## 🧪 Running Tests

```bash
./mvnw test
```

Integration tests use the embedded Kafka broker provided by `spring-kafka-test` — no external Kafka instance required.

---

## 🔒 Security Notes

- The actual `application.yml` should **never be committed** with real credentials. Rotate any credentials currently in the repo immediately.
- Add `application.yml` to `.gitignore` and rely on `application-example.yml` as the committed reference.
- Consider externalising secrets via **Spring Cloud Config**, environment variables, or a vault solution like **AWS Secrets Manager** / **HashiCorp Vault**.

```bash
# .gitignore — add this line
src/main/resources/application.yml
```

---

## 🗺️ Roadmap

- [ ] `SMSStrategy` — Twilio / AWS SNS integration
- [ ] `PushNotificationStrategy` — Firebase Cloud Messaging
- [ ] Dead Letter Queue (DLQ) handling for undeliverable Kafka events
- [ ] `SignatureDecorator` implementation (from LLD diagram)
- [ ] Swagger / OpenAPI 3 documentation
- [ ] Flyway/Liquibase migrations + `ddl-auto: validate`
- [ ] Docker multi-stage build + Kubernetes manifests
- [ ] Distributed tracing with Micrometer + Zipkin

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit: `git commit -m 'feat: add your feature'`
4. Push: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 👤 Author

**Shashank Varshney**
[github.com/shashank-varshney21](https://github.com/shashank-varshney21)

---

## 📄 License

This project is open source. Add a `LICENSE` file (MIT recommended) to make it official.
