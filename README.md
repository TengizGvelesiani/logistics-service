# VanOpt Logistics Service

A Spring Boot backend that solves the **van loading problem**: given a list of shipments (each with volume and revenue) and a maximum van capacity, it selects the combination that **maximizes total revenue** without exceeding capacity. Every optimization request and its result are stored in PostgreSQL for audit.

---

## Table of Contents

- [Quick Start](#quick-start)
- [Technology Stack](#technology-stack)
- [Step 1: Start the database (Docker Compose)](#step-1-start-the-database-docker-compose)
- [Step 2: Build](#step-2-build)
- [Step 3: Run the application](#step-3-run-the-application)
- [Prerequisites](#prerequisites)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)

---

## Quick Start

```bash
# 1. Start the database (required first)
docker compose up -d

# 2. Build (run one of the next two lines - Mac or Windows)
./gradlew build
.\gradlew.bat build

# 3. Run the JAR (run one of the next two lines - Mac or Windows)
java -jar build/libs/logistics-service-0.0.1-SNAPSHOT.jar
java -jar build\libs\logistics-service-0.0.1-SNAPSHOT.jar
```

Then open **http://localhost:8080/** for the web UI, or call the API (see [API Reference](#api-reference)).

---

## Technology Stack

| Layer             | Technology |
|-------------------|------------|
| Language         | Java 25    |
| Framework        | Spring Boot 4 |
| Data             | Spring Data JPA, PostgreSQL |
| Migrations       | Flyway     |
| Build            | Gradle (wrapper in repo) |
| Database runtime | Docker Compose (PostgreSQL only); app runs as a plain JAR |

---

## Step 1: Start the database (Docker Compose)

The database must be running before you start the application. From the project root:

```bash
docker compose up -d
```

This starts PostgreSQL 15. Connection details:

| Setting   | Value       |
|-----------|-------------|
| Host      | `localhost` |
| Port      | `5432`      |
| Database  | `vanopt_db` |
| User      | `user`      |
| Password  | `password`  |

To change these, edit `src/main/resources/application.properties`.

**Schema:** Flyway runs on first app startup and creates the tables from `src/main/resources/db/migration/`. No manual SQL needed.

---

## Step 2: Build

Builds the project, runs tests, and produces the executable JAR.

**macOS / Linux**

```bash
./gradlew build
```

**Windows (PowerShell or cmd)**

```bash
.\gradlew.bat build
```

Output JAR: `build/libs/logistics-service-0.0.1-SNAPSHOT.jar` (use `build\libs\` on Windows).

---

## Step 3: Run the application

After the database is running (Step 1), start the app with the JAR.

**macOS / Linux**

```bash
java -jar build/libs/logistics-service-0.0.1-SNAPSHOT.jar
```

**Windows (PowerShell or cmd)**

```bash
java -jar build\libs\logistics-service-0.0.1-SNAPSHOT.jar
```

The API is at **http://localhost:8080**. REST base path: **http://localhost:8080/api/v1**. Web UI: **http://localhost:8080/**.

**Alternative (no JAR):** Run with Gradle instead of the JAR: `./gradlew bootRun` (macOS/Linux) or `.\gradlew.bat bootRun` (Windows).

---

## Prerequisites

| Requirement              | Notes |
|--------------------------|--------|
| **Java 25**              | Or the version set in `build.gradle`. |
| **Gradle**               | Use the wrapper: `./gradlew` (macOS/Linux) or `.\gradlew.bat` (Windows). |
| **Docker & Docker Compose** | Required to run PostgreSQL (Step 1). |

---

## API Reference

**Base URL:** `http://localhost:8080/api/v1`

---

### POST `/optimize` — Run optimization

Sends the van capacity and list of available shipments. The service runs the 0/1 knapsack algorithm, saves both input and result, and returns the selected shipments and totals.

**Request body**

| Field                | Type   | Required | Description                          |
|----------------------|--------|----------|--------------------------------------|
| `maxVolume`          | number | Yes      | Van capacity (e.g. dm³). Must be > 0. |
| `availableShipments` | array  | Yes      | List of `{ name, volume, revenue }`. |

Each shipment: `name` (string), `volume` (positive integer), `revenue` (non‑negative number).

**Example request**

```bash
curl -X POST http://localhost:8080/api/v1/optimize \
  -H "Content-Type: application/json" \
  -d '{
    "maxVolume": 15,
    "availableShipments": [
      {"name": "Parcel A", "volume": 5, "revenue": 120},
      {"name": "Parcel B", "volume": 10, "revenue": 200},
      {"name": "Parcel C", "volume": 3, "revenue": 80},
      {"name": "Parcel D", "volume": 8, "revenue": 160}
    ]
  }'
```

**Example response (200 OK)**

```json
{
  "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "selectedShipments": [
    {"name": "Parcel A", "volume": 5, "revenue": 120},
    {"name": "Parcel B", "volume": 10, "revenue": 200}
  ],
  "totalVolume": 15,
  "totalRevenue": 320,
  "createdAt": "2025-06-01T10:00:00Z"
}
```

**When no shipment fits (200 OK)**

If nothing fits within `maxVolume`, the response has empty `selectedShipments`, `totalVolume: 0`, and `totalRevenue: 0`.

```bash
curl -X POST http://localhost:8080/api/v1/optimize \
  -H "Content-Type: application/json" \
  -d '{"maxVolume": 2, "availableShipments": [{"name": "Large", "volume": 10, "revenue": 100}]}'
```

**Invalid input (400 Bad Request)**

Example: negative `maxVolume` or empty `availableShipments`. The response body describes the validation errors.

```bash
curl -X POST http://localhost:8080/api/v1/optimize \
  -H "Content-Type: application/json" \
  -d '{"maxVolume": -1, "availableShipments": []}'
```

---

### GET `/optimizations/{requestId}` — Get one run (audit)

Returns the full result of a single optimization run by the `requestId` returned from `POST /optimize`.

**Example request**

```bash
curl -s http://localhost:8080/api/v1/optimizations/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Example response (200 OK)**

Same shape as the POST response: `requestId`, `selectedShipments`, `totalVolume`, `totalRevenue`, `createdAt`.

**Not found (404)**

If `requestId` does not exist:

```bash
curl -s http://localhost:8080/api/v1/optimizations/00000000-0000-0000-0000-000000000000
```

---

### GET `/optimizations` — List runs (paginated)

Returns a page of past optimization runs. Each item includes `requestId`, `totalVolume`, `totalRevenue`, and `createdAt` (no per-shipment detail).

**Query parameters**

| Parameter | Type | Default | Description        |
|-----------|------|---------|--------------------|
| `page`    | int  | `0`     | Zero-based page.   |
| `size`    | int  | `20`    | Page size.         |

**Example request**

```bash
curl -s "http://localhost:8080/api/v1/optimizations?page=0&size=20"
```

**Example response (200 OK)**

```json
{
  "content": [
    {
      "requestId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "totalVolume": 15,
      "totalRevenue": 320,
      "createdAt": "2025-06-01T10:00:00Z"
    }
  ],
  "pageable": {},
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "empty": false
}
```

---

## Database Schema

The schema has **four tables**: two for the **input** of each run and two for the **output**. A single **request ID (UUID)** ties input and output together for the same run.

### Tables overview

| Table                 | Entity           | Description |
|-----------------------|------------------|-------------|
| **optimized_outputs** | OptimizedOutput  | One row per run: `id`, `request_id` (UUID), `total_volume`, `total_revenue`, `created_at`. |
| **selected_shipments**| SelectedShipment | Chosen shipments for that run; FK to `optimized_outputs.id`; columns: `name`, `volume`, `revenue`. |
| **optimization_inputs**| OptimizationInput | Input for that run: `request_id`, `max_volume`, `created_at`. |
| **input_shipments**   | InputShipment    | Available shipments offered; FK to `optimization_inputs.id`; columns: `name`, `volume`, `revenue`. |

**Flow:** For each `POST /optimize`, the service stores the **input** (max volume + available shipments) in `optimization_inputs` and `input_shipments`, and the **output** (selected shipments + totals) in `optimized_outputs` and `selected_shipments`. Both sides use the same `request_id` so you can correlate them for audit.

### Indexes

| Index / column | Purpose |
|----------------|--------|
| `optimized_outputs.request_id` (unique) | Lookup by `requestId` for `GET /optimizations/{requestId}`. |
| `optimized_outputs.created_at` (DESC)   | List runs newest first for `GET /optimizations`. |
| `selected_shipments.optimized_output_id`| Join to load selected shipments for a run. |
| `optimization_inputs.request_id` (unique)| Find input by the same UUID. |
| `optimization_inputs.created_at`        | Used by the cleanup job (delete old input). |
| `input_shipments.optimization_input_id` | Join input shipments to their run. |

### Data retention

A scheduled job (see `vanopt.input-retention-days` and `vanopt.cleanup-cron` in `application.properties`) deletes **OptimizationInput** rows older than 30 days (and, by FK cascade, their `input_shipments`). **OptimizedOutput** and **selected_shipments** are kept for long-term audit.

