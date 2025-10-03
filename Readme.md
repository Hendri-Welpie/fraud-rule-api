# 🚨 Fraud Rule API

The **Fraud Rule API** is a Spring Boot microservice that evaluates financial transactions against pre-configured fraud rules.  
It allows you to:

- ✅ Validate transactions in real-time
- 🛑 Flag suspicious or fraudulent activities
- 💾 Store transaction and fraud results in PostgreSQL
- ⚡ Run seamlessly inside **Docker Compose** with supporting services

---

## 📦 Requirements

Before running the application, ensure you have one of the following installed:

- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Rancher Desktop](https://rancherdesktop.io/)

---

## 🚀 Getting Started

### 1. Run Services with Docker Compose

Spin up all dependencies with:

```bash
docker-compose up -d
```

The following services will be started:

- **Postgres** (frauddb) – stores transactions & flagged frauds
- **Redis** – caching for fast rule lookups
- **PGAdmin** – database GUI for PostgreSQL

---

### 2. Connect to PGAdmin

Open [http://localhost:8050/browser/](http://localhost:8050/browser/)

🔑 Login credentials:
- **Username**: `admin@admin.com`
- **Password**: `password`

➡️ Add a new connection with the following details:

- **Name**: Any (e.g., `FraudDB`)
- **Host**: `postgres`
- **Database**: `frauddb`
- **Username**: `postgres`
- **Password**: `postgres`

Once connected, you can explore the **frauddb** schema and stored transactions.

---

## 🧪 API Usage

You can use **Postman** or `curl` to interact with the API.  
All requests require a custom header:

```http
X-Trace-Id: <your-unique-trace-id>
```

---

### 📌 1. Validate a Transaction

This endpoint validates a transaction, stores it, and flags it if suspicious.

```bash
curl --location 'http://localhost:9080/v1/api/fraud/transactions' \
--header 'X-Trace-Id: 4c5e36a1-089b-4c7b-8517-b2fcbac941db' \
--header 'Content-Type: application/json' \
--data '{
  "transaction_id": "32432",
  "account_id": 123123,
  "user_id": 12321,
  "currency": "ZAR",
  "amount": 340000234.00,
  "timestamp": "2025-09-27T15:20:11.285477Z",
  "transaction_type": "TRANSFER",
  "channel": "WEB",
  "merchant_id": "32432",
  "merchant_name": "Ben",
  "beneficiary_account": 32142347,
  "ip_address": "192.168.1.45",
  "device_id": "device-98765",
  "geo_location": "-33.9249,18.4241",
  "status": "PENDING"
}'
```

---

### 📌 2. Get All Flagged Items

Retrieve all transactions currently flagged as fraud:

```bash
curl --location 'http://localhost:9080/v1/api/fraud/flag-items' \
--header 'X-Trace-Id: 27a1c44f-9fa7-4eb0-a921-0a736a01fcc9'
```

---

### 📌 3. Get a Specific Flagged Item

Search for a flagged item by ID:

```bash
curl --location 'http://localhost:9080/v1/api/fraud/flag-item/5' \
--header 'X-Trace-Id: 97766d29-001f-4d2e-9e33-02c1d12cb532'
```

---

## 🛠️ Tech Stack

- **Java 25** (Spring Boot Reactive API)
- **PostgreSQL** (transaction + fraud data persistence)
- **Redis** (fast in-memory storage)
- **Docker & Docker Compose** (containerized environment)
- **PGAdmin** (DB management UI)

---

## 📊 System Architecture

```
               +-------------+
               |   Client    |
               | (Postman)   |
               +------+------+
                      |
                      v
             +--------+---------+
             |  Fraud Rule API  |
             | (Spring Boot)    |
             +---+----------+---+
                 |          |
         +-------+--+   +---+------+
         | Postgres |   |   Redis  |
         | frauddb  |   | cache    |
         +----------+   +----------+
```

---

## 🧑‍💻 Development Notes

- All endpoints require `X-Trace-Id` for distributed tracing
- Transactions are stored in **Postgres**
- Fraud rules are evaluated **synchronously** but scalable with **Redis caching**

---

## 📜 License

This project is licensed under the **MIT License** – free to use, modify, and distribute.  