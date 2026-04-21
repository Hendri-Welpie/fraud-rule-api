# 🚨 Fraud Rule Engine API

A production-grade Spring Boot microservice that processes categorized transaction events and flags potential fraud using configurable rules.

## ✨ Features

- ✅ **Real-time Transaction Validation** - Evaluate transactions against configurable fraud rules
- 🛑 **Fraud Detection** - Flag suspicious or fraudulent activities with risk scoring
- ⚡ **High Performance** - Parallel rule evaluation using Java 21 virtual threads
- 📊 **Velocity Checking** - Rate limiting and transaction frequency monitoring via Redis
- 💾 **Persistent Storage** - PostgreSQL with JSONB for flexible rule storage
- 🔄 **Reactive Architecture** - Built with Spring WebFlux for non-blocking operations
- 📈 **Observability** - Metrics, health checks, and distributed tracing support
- 🐳 **Container Ready** - Docker and Docker Compose support
- 🔧 **Fault Tolerance** - Circuit breaker and rate limiter for resilience
- 🔐 **Toggleable Security** - Spring Security with HTTP Basic auth, can be enabled/disabled via config
- 📝 **Audit Trail** - Database-backed audit logging of all API operations
- 🔒 **PII Masking** - Automatic masking of sensitive data (account IDs, IPs, device IDs) in logs
- 📊 **Custom Metrics** - Prometheus metrics for transaction processing and fraud detection
- ☸️ **Kubernetes Ready** - Health probes and horizontal scaling support
- 🔍 **Distributed Tracing** - OpenTelemetry integration for end-to-end observability

---

## 📦 Tech Stack

| Technology          | Purpose                                              |
|---------------------|------------------------------------------------------|
| **Java 21**         | Runtime with virtual threads for high concurrency    |
| **Spring Boot 3.5** | Application framework with WebFlux                   |
| **Spring WebFlux**  | Reactive web framework for non-blocking I/O          |
| **PostgreSQL 16**   | Primary data store with JSONB support                |
| **Redis 7**         | Caching and velocity tracking                        |
| **Flyway**          | Database migrations                                  |
| **MapStruct**       | Object mapping                                       |
| **OpenTelemetry**   | Distributed tracing and observability                |
| **Resilience4j**    | Circuit breaker and rate limiter for fault tolerance |
| **Spring Security** | Toggleable HTTP Basic authentication                 |
| **Micrometer**      | Metrics collection for Prometheus                    |
| **Docker**          | Containerization and orchestration                   |
| **Maven**           | Build and dependency management                      |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client                                   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Fraud Rule API                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │
│  │  Controller  │──│   Service    │──│  Fraud Evaluator     │   │
│  │    Layer     │  │    Layer     │  │  (Strategy Pattern)  │   │
│  └──────────────┘  └──────────────┘  └──────────────────────┘   │
│         │                │                      │                │
│         ▼                ▼                      ▼                │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │              Condition Evaluator Factory                  │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────────────┐ │   │
│  │  │ EQUALS  │ │   GT    │ │   LT    │ │ INCLUDE + more  │ │   │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────────────┘ │   │
│  └──────────────────────────────────────────────────────────┘   │
└─────────────────────────┬───────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │PostgreSQL│   │   Redis  │   │ Metrics  │
    │ (JSONB)  │   │  Cache   │   │Prometheus│
    └──────────┘   └──────────┘   └──────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) or [Rancher Desktop](https://rancherdesktop.io/)
- Java 21+ (for local development)
- Maven 3.9+ (for local development)

### Quick Start with Docker Compose

```bash
# Clone the repository
git clone <repository-url>
cd fraud-rule-api

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f fraud-rule-api

# Stop all services
docker-compose down
```

### Local Development

```bash
# Start dependencies only
docker-compose up -d postgres redis

# Run the application locally
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 🔧 Configuration

The application is highly configurable via `application.yml`:

```yaml
app:
  fraud:
    evaluation:
      parallel-threshold: 5      # Rules threshold for parallel evaluation
      timeout-ms: 5000          # Evaluation timeout
      max-concurrent-evaluations: 100
    velocity:
      enabled: true
      window-seconds: 60        # Time window for rate limiting
      max-transactions: 10      # Max transactions per window
    risk:
      high-threshold: 80        # Risk score for CRITICAL severity
      medium-threshold: 50      # Risk score for HIGH severity
      low-threshold: 20         # Risk score for MEDIUM severity
    amount:
      high-value-threshold: 1000000.00
      suspicious-threshold: 500000.00

# Resilience4j configuration for fault tolerance
resilience4j:
  circuitbreaker:
    instances:
      fraudService:
        slidingWindowSize: 100
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
  ratelimiter:
    instances:
      fraudApi:
        limitRefreshPeriod: 1s
        limitForPeriod: 100000  # 100k TPS limit

# OpenTelemetry configuration for distributed tracing
otel:
  service:
    name: fraud-rule-api
  traces:
    exporter: otlp
  metrics:
    exporter: prometheus
```

### Custom Metrics

The application exposes the following custom Prometheus metrics:

| Metric                                           | Description                                           | Type    |
|--------------------------------------------------|-------------------------------------------------------|---------|
| `fraud_transaction_processed_total`              | Total number of transactions processed                | Counter |
| `fraud_detection_total`                          | Total number of fraud detections                      | Counter |
| `fraud_velocity_check_total`                     | Total number of velocity checks performed             | Counter |
| `fraud_evaluation_duration`                      | Time taken to evaluate fraud rules                    | Timer   |
| `resilience4j_circuitbreaker_state`              | Circuit breaker state (0=closed, 1=open, 2=half-open) | Gauge   |
| `resilience4j_ratelimiter_available_permissions` | Available rate limiter permissions                    | Gauge   |

### Environment Variables

| Variable                 | Description                      | Default                                   |
|--------------------------|----------------------------------|-------------------------------------------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile            | `default`                                 |
| `SPRING_DATASOURCE_URL`  | PostgreSQL connection URL        | `jdbc:postgresql://postgres:5432/frauddb` |
| `SPRING_DATA_REDIS_HOST` | Redis hostname                   | `redis`                                   |
| `APP_SECURITY_ENABLED`   | Enable/disable RBAC security     | `false`                                   |

---

## 🔐 Security (RBAC)

Security is **disabled by default** so anyone can clone the repo and start immediately. When needed, activate the `secure` Spring profile to enable Role-Based Access Control (RBAC) with HTTP Basic authentication.

### Roles

| Role        | Fraud API (Read) | Submit Transactions | Rules API (Read) | Rules API (Write) |
|-------------|:----------------:|:-------------------:|:----------------:|:-----------------:|
| **ADMIN**   |        ✅         |          ✅          |        ✅         |         ✅         |
| **ANALYST** |        ✅         |          ✅          |        ✅         |         ❌         |

### Default Users (when security is enabled)

| Username   | Password   | Role      |
|------------|------------|-----------|
| `admin`    | `admin`    | ADMIN     |
| `analyst`  | `analyst`  | ANALYST   |

### How to Enable Security

**Local development:**
```bash
# Without security (default) - just works
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# With security
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,secure
```

**Docker Compose:**
```bash
# Without security (default) - just works
docker-compose up -d

# With security
docker-compose -f docker-compose.yml -f docker-compose.secure.yml up -d
```

**Testing with security enabled:**
```bash
# As admin (full access)
curl -u admin:admin http://localhost:9080/v1/api/fraud/flag-items

# As analyst (read-only fraud + submit transactions)
curl -u analyst:analyst http://localhost:9080/v1/api/fraud/flag-items

# Analyst CANNOT manage rules (returns 403)
curl -u analyst:analyst -X POST http://localhost:9080/api/v1/rules -d '{}' -H 'Content-Type: application/json'
```

### Custom Users

Add users in `application-secure.yml` or via environment variables:

```yaml
app:
  security:
    enabled: true
    users:
      - username: admin
        password: admin
        role: ADMIN
      - username: analyst
        password: analyst
        role: ANALYST
      - username: viewer
        password: viewer123
        role: ANALYST
```

---

## 📝 Audit Trail

All API operations (except actuator/swagger endpoints) are automatically logged to the `fraud.audit_trail` database table. Each audit entry includes:

| Field             | Description                               |
|-------------------|-------------------------------------------|
| `trace_id`        | Request trace ID from `X-Trace-Id` header |
| `principal`       | Authenticated user or "anonymous"         |
| `http_method`     | HTTP method (GET, POST, etc.)             |
| `uri`             | Request URI path                          |
| `response_status` | HTTP response status code                 |
| `timestamp`       | When the request was processed            |

---

## 🔒 PII Masking in Logs

Sensitive data is automatically masked in all log output:

| Field                 | Example Input      | Masked Output     |
|-----------------------|--------------------|-------------------|
| `account_id`          | `123456`           | `***`             |
| `user_id`             | `789`              | `***`             |
| `beneficiary_account` | `987654`           | `***`             |
| `ip_address`          | `192.168.1.100`    | `***.***.***.***` |
| `device_id`           | `device-12345`     | `***`             |
| `geo_location`        | `-33.9249,18.4241` | `***`             |

---

## 🧪 API Usage

All requests support distributed tracing via `X-Trace-Id` header.

### 1. Validate a Transaction

```bash
curl -X POST 'http://localhost:9080/v1/api/fraud/transactions' \
  -H 'Content-Type: application/json' \
  -H 'X-Trace-Id: unique-trace-id' \
  -d '{
    "transaction_id": "TXN-001",
    "account_id": 123456,
    "user_id": 789,
    "currency": "ZAR",
    "amount": 50000.00,
    "timestamp": "2026-04-14T10:30:00",
    "transaction_type": "TRANSFER",
    "channel": "WEB",
    "merchant_id": "M001",
    "merchant_name": "Online Store",
    "beneficiary_account": 987654,
    "ip_address": "192.168.1.100",
    "device_id": "device-12345",
    "geo_location": "-33.9249,18.4241",
    "status": "PENDING"
  }'
```

**Response:**
```json
{
  "transactionId": "TXN-001",
  "isFraud": true,
  "riskScore": 75,
  "severity": "HIGH",
  "matchedRules": ["rule-0001", "VELOCITY_CHECK"],
  "processingTimeMs": 45
}
```

### 2. Get Flagged Items (Paginated)

```bash
curl 'http://localhost:9080/v1/api/fraud/flag-items?page=0&size=10' \
  -H 'X-Trace-Id: unique-trace-id'
```

### 3. Get Fraud by Account

```bash
curl 'http://localhost:9080/v1/api/fraud/account/123456' \
  -H 'X-Trace-Id: unique-trace-id'
```

### 4. Get Fraud by Severity

```bash
curl 'http://localhost:9080/v1/api/fraud/severity/HIGH?page=0&size=10' \
  -H 'X-Trace-Id: unique-trace-id'
```

### 5. Manage Rules

```bash
# Create a new rule
curl -X POST 'http://localhost:9080/api/v1/rules' \
  -H 'Content-Type: application/json' \
  -d '{
    "ruleId": "high-value-rule",
    "name": "High Value Transfer Rule",
    "condition": {...},
    "actions": [...]
  }'

# Get all rules
curl 'http://localhost:9080/api/v1/rules'

# Get active rule
curl 'http://localhost:9080/api/v1/rules/active'
```

---

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:9080/actuator/health
```

### Prometheus Metrics

```bash
curl http://localhost:9080/actuator/prometheus
```

### Available Endpoints

| Endpoint                     | Description                |
|------------------------------|----------------------------|
| `/actuator/health`           | Health status              |
| `/actuator/health/liveness`  | Kubernetes liveness probe  |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/metrics`          | Application metrics        |
| `/actuator/prometheus`       | Prometheus format metrics  |

---

## 📮 Postman Collection

A comprehensive Postman collection is available in the `postman/` directory with all possible API scenarios:

### Collection Structure

```
Fraud Rule Engine API.postman_collection.json
├── Fraud API
│   ├── Validate Transaction
│   │   ├── Normal Transaction (No Fraud)
│   │   ├── High Value Transaction (Fraud)
│   │   ├── Velocity Limit Exceeded (Fraud)
│   │   ├── Multiple Rules Triggered (Fraud)
│   │   └── Invalid Request (Error)
│   ├── Get Flagged Items
│   │   ├── Get All Flagged Items (Paginated)
│   │   └── Get Specific Flagged Item
│   ├── Get Fraud by Account
│   │   └── Get Fraud Items for Account
│   └── Get Fraud by Severity
│       ├── Get High Severity Frauds
│       ├── Get Medium Severity Frauds
│       └── Get Low Severity Frauds
└── Rules API
    ├── Create Rule
    │   ├── Simple Rule (Amount > 1000)
    │   ├── Complex Rule (AND condition)
    │   ├── Velocity Rule
    │   └── Invalid Rule (Error)
    ├── Get Rules
    │   ├── Get All Rules
    │   ├── Get Specific Rule
    │   └── Get Active Rule
    ├── Update Rule
    │   └── Update Existing Rule
    └── Delete Rule
        └── Delete Existing Rule
```

### How to Use

1. **Import the Collection**: Import `postman/Fraud Rule Engine API.postman_collection.json` into Postman
2. **Set Collection Variables**:
   - `base_url`: `http://localhost:9080`
   - `trace_id`: `test-trace-123`
   - `auth_username`: `admin` (only needed when security is enabled)
   - `auth_password`: `admin` (only needed when security is enabled)
3. **Without Security**: All requests work out of the box — the server ignores the Basic Auth header when security is disabled
4. **With Security**: The collection uses Basic Auth at the collection level. Switch user by changing `auth_username`/`auth_password` variables
5. **Run Scenarios**: Execute requests in order or use the Collection Runner

### Key Scenarios Covered

- **Transaction Validation**: Various fraud scenarios with different rule combinations
- **Error Handling**: Invalid requests, missing fields, malformed JSON
- **Pagination**: Different page sizes and sorting options
- **Rule Management**: CRUD operations with valid and invalid rule definitions
- **Performance Testing**: Rate limiting and circuit breaker scenarios

---

## 🏛️ Design Patterns & SOLID Principles

### Design Patterns Used

| Pattern             | Implementation                                       |
|---------------------|------------------------------------------------------|
| **Strategy**        | Condition evaluators for different comparison types  |
| **Factory**         | `ConditionEvaluatorFactory` for evaluator management |
| **Template Method** | `AbstractConditionEvaluator` base class              |
| **Repository**      | Spring Data R2DBC reactive repositories              |
| **Builder**         | Lombok `@Builder` for object construction            |

### SOLID Principles

- **S**ingle Responsibility: Each service/evaluator has one purpose
- **O**pen/Closed: New condition types via new evaluators without modifying existing code
- **L**iskov Substitution: All evaluators are interchangeable
- **I**nterface Segregation: Separate API interfaces for Fraud and Rules
- **D**ependency Inversion: Dependencies injected via interfaces

---

## 📁 Project Structure

```
src/main/java/org/project/fraudruleapi/
├── Application.java
├── fraud/
│   ├── controller/
│   ├── entity/
│   ├── evaluator/
│   │   └── strategy/          # Strategy pattern evaluators
│   ├── mapper/
│   ├── model/
│   ├── repository/            # Includes custom R2DBC DatabaseClient impl
│   └── service/
├── rules/
│   ├── controller/
│   ├── entity/
│   ├── mapper/
│   ├── model/
│   ├── repository/
│   └── service/
└── shared/
    ├── audit/
    ├── cache/
    ├── config/
    ├── converter/
    ├── enums/
    ├── exception/
    ├── filter/
    ├── log/
    ├── scheduler/
    ├── security/
    ├── util/
    └── validator/
```

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

---

## 📜 License

This project is licensed under the **MIT License**.
