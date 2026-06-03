# Warehouse Inventory Reservation System

## 1. Challenge Chosen

**Challenge 1 — Warehouse Inventory Reservation System**

Chosen because it involves interesting concurrency problems (preventing overselling under simultaneous load), a clear state machine, and maps cleanly to well-known design patterns.

---

## 2. Architecture Overview

```
controller/       — HTTP layer; translates requests/responses
service/          — Business logic interfaces + implementations
  ReservationService.java / ReservationServiceImpl.java
  InventoryService.java   / InventoryServiceImpl.java
  factory/        — ReservationFactory (Factory Pattern)
  state/          — State pattern for reservation lifecycle
    ReservationState.java / ReservationStateFactory.java
    PendingState.java / ConfirmedState.java / CancelledState.java
repository/       — Spring Data JPA repositories
entity/           — JPA entities + ReservationStatus enum
dto/              — Request/response records
exception/        — Domain exceptions + GlobalExceptionHandler (centralised error mapping)
```

The service layer contains all business logic and depends only on interfaces (repositories, factory). Controllers are thin and delegate to services. The state package encapsulates lifecycle rules.

---

## 3. Design Patterns

### State Pattern
**Location:** `service/state/`

Each reservation status maps to a state object (`PendingState`, `ConfirmedState`, `CancelledState`) that implements `ReservationState`. Each state knows which transitions are valid and throws `InvalidReservationStateException` for illegal ones. `ReservationStateFactory` resolves the current state from the entity's status.

```
PENDING  → confirm() → CONFIRMED  (terminal)
PENDING  → cancel()  → CANCELLED  (terminal)
```

### Factory Pattern
**Location:** `service/factory/ReservationFactory`

`ReservationFactory.create(CreateReservationRequest)` encapsulates the construction of a `Reservation` aggregate — setting status, timestamp, and building all child `ReservationItem` entities. The service does not directly instantiate reservation entities.

---

## 4. SOLID Principles

| Principle | Where |
|---|---|
| **Single Responsibility** | Controllers only handle HTTP; services only handle business logic; factories only build objects |
| **Open/Closed** | Adding a new state requires a new class, not modifying existing states |
| **Liskov Substitution** | `PendingState`, `ConfirmedState`, `CancelledState` are interchangeable via `ReservationState` |
| **Interface Segregation** | `ReservationService` and `InventoryService` are separate narrow interfaces |
| **Dependency Inversion** | `ReservationServiceImpl` depends on `InventoryRepository` (interface), not a concrete class |

---

## 5. Database Design

### products
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| sku | VARCHAR(50) | NOT NULL, UNIQUE |
| name | VARCHAR(255) | NOT NULL |
| description | TEXT | nullable |

### inventory
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| sku | VARCHAR(50) | NOT NULL, UNIQUE, FK → products(sku) |
| total_stock | INTEGER | NOT NULL, DEFAULT 0, CHECK >= 0 |
| available_stock | INTEGER | NOT NULL, DEFAULT 0, CHECK >= 0 |
| reserved_stock | INTEGER | NOT NULL, DEFAULT 0, CHECK >= 0 |
| version | BIGINT | NOT NULL, DEFAULT 0 — optimistic lock |

### reservations
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| order_id | VARCHAR(100) | NOT NULL, UNIQUE — idempotency key |
| status | VARCHAR(20) | NOT NULL — PENDING / CONFIRMED / CANCELLED |
| created_at | TIMESTAMP | NOT NULL |
| version | BIGINT | NOT NULL, DEFAULT 0 — optimistic lock |

### reservation_items
| Column | Type | Constraints |
|---|---|---|
| id | BIGSERIAL | PRIMARY KEY |
| reservation_id | BIGINT | NOT NULL, FK → reservations(id) |
| sku | VARCHAR(50) | NOT NULL |
| quantity | INTEGER | NOT NULL, CHECK > 0 |

**Concurrency safety:** The `reserve()` path issues a `SELECT … FOR UPDATE` (pessimistic write lock) on each `inventory` row. SKUs are locked in alphabetical order to prevent deadlocks when a reservation covers multiple SKUs. The `version` column on both `inventory` and `reservations` provides an optimistic locking safety net.

**Duplicate-order protection:** `order_id` carries a `UNIQUE` constraint and is treated as an idempotency key. `reserve()` first checks whether the order already exists and rejects duplicates with `409 DUPLICATE` before touching any stock. The rare case where two identical `orderId` requests race past that pre-check is caught by the unique constraint — the loser's insert fails with `DataIntegrityViolationException`, its transaction (including the stock deduction) rolls back, and it is surfaced as the same `409 DUPLICATE`. A retried or duplicated submission can therefore never create a second reservation or double-deduct stock.

**Migrations:** Liquibase SQL changesets only, in `src/main/resources/db/changelog/sql/`. Seed data (3 products + matching inventory rows) is applied in `005_seed_data.sql`.

---

## 6. How to Run

Requirements: Docker and Docker Compose.

```bash
docker compose up
```

The app starts at `http://localhost:8080`. Liquibase migrations and seed data run automatically on startup.

### Example requests

```bash
# Reserve inventory
curl -X POST http://localhost:8080/api/v1/reservations \
  -H 'Content-Type: application/json' \
  -d '{"orderId":"ORD-1001","items":[{"sku":"A100","quantity":5},{"sku":"B200","quantity":3}]}'

# Confirm a reservation
curl -X POST http://localhost:8080/api/v1/reservations/1/confirm

# Cancel a reservation
curl -X POST http://localhost:8080/api/v1/reservations/1/cancel

# Get a reservation
curl http://localhost:8080/api/v1/reservations/1

# Get inventory for a SKU
curl http://localhost:8080/api/v1/inventory/A100
```

---

## 7. How to Run Tests

Requires Docker (Testcontainers spins up a real PostgreSQL container).

```bash
./gradlew test
```

### Unit tests (`service/ReservationServiceImplTest`)
- No Spring context; repositories are mocked via Mockito
- Covers: insufficient stock rejection, SKU not found, duplicate `orderId` rejection, all valid and invalid state transitions, stock restoration on cancel

### Integration tests (`integration/ReservationConcurrencyIntegrationTest`)
- Full Spring Boot context + real PostgreSQL via Testcontainers
- **Oversell:** two concurrent `reserve` requests for the same SKU where combined quantity (60 + 60 = 120) exceeds stock (100) → asserts exactly one succeeds, one is rejected with `InsufficientStockException`, and final stock is correct
- **Duplicate order:** five concurrent `reserve` requests with the same `orderId` → asserts exactly one succeeds, the other four are rejected with `DuplicateException`, only one row is created, and stock is deducted only once

---

## 8. Trade-offs

| Decision | Trade-off |
|---|---|
| Pessimistic locking (`SELECT FOR UPDATE`) on inventory | Guarantees correctness at the cost of reduced throughput under high contention on a single SKU |
| Alphabetical SKU lock ordering | Prevents deadlocks at the cost of a minor sort per request |
| Seed data in Liquibase | Convenient for demos; in production, seed data would be managed separately |
| `@Version` on both `Inventory` and `Reservation` | Adds a safety net against concurrent updates that bypass the pessimistic lock path |
