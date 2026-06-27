# Reconcilia — Multi-Bank Statement Reconciliation Engine

A Spring Boot 3 backend that ingests CSV bank statements from multiple bank formats,
normalises them into a unified schema, persists them to PostgreSQL, and exposes a
REST API for upload.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     REST Layer                          │
│          POST /api/statements/upload                    │
│               StatementController                       │
└────────────────────────┬────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────┐
│                   Service Layer                         │
│             StatementIngestionService                   │
│  1. Select parser  2. Parse  3. Validate  4. Deduplicate│
│  5. Bulk-save                                           │
└───────────┬────────────────────────────────┬────────────┘
            │                                │
┌───────────▼───────────┐      ┌─────────────▼────────────┐
│    Strategy Pattern   │      │    Repository Layer       │
│  BankStatementParser  │      │   TransactionRepository   │
│  ┌─────────────────┐  │      │  (Spring Data JPA)        │
│  │ AlphaBankParser │  │      └─────────────┬────────────┘
│  ├─────────────────┤  │                    │
│  │  BetaBankParser │  │      ┌─────────────▼────────────┐
│  └─────────────────┘  │      │        PostgreSQL         │
│  BankStatementParser  │      │  (Flyway schema mgmt)    │
│      Factory          │      └──────────────────────────┘
└───────────────────────┘
```

### Key Design Decisions

| Concern | Decision | Rationale |
|---|---|---|
| Parser extensibility | Strategy pattern | Adding Bank Gamma = 1 new class + 1 enum value |
| Schema management | Flyway (not `ddl-auto: create`) | Controlled, versioned migrations |
| Duplicate detection | Single `SELECT … IN (…)` before INSERT | Avoids N round-trips and DB exceptions |
| Integration tests | Testcontainers + real PostgreSQL | Catches PostgreSQL-specific behaviour H2 silently ignores |
| Bean Validation | Validates every parsed row before any DB write | Full error list returned in one 400 response |

---

## Prerequisites

| Tool | Minimum version |
|---|---|
| Java | 17 |
| Maven | 3.8 |
| Docker + Docker Compose | Docker Desktop 4.x / Engine 24+ |

> **Docker must be running** both for `docker-compose up` (local dev) and for
> `mvn verify` (Testcontainers spins up its own throwaway PostgreSQL container).

---

## Running Locally

### 1 — Start PostgreSQL

```bash
docker-compose up -d
```

Starts `postgres:16-alpine` on port **5432** with:
- Database: `reconcilia`
- Username: `reconcilia`
- Password: `reconcilia`

### 2 — Run the application

```bash
mvn spring-boot:run
```

Flyway automatically applies `V1__create_transactions_table.sql` on first start.

### 3 — Open Swagger UI

```
http://localhost:8080/swagger-ui.html
```

Use the **POST /api/statements/upload** endpoint to upload the sample CSV files
found at `src/test/resources/csv/`.

---

## Running Integration Tests

Docker must be running. Testcontainers pulls `postgres:16-alpine` (cached after
the first pull), applies Flyway migrations, runs all `*IT.java` tests, then
stops and removes the container.

```bash
# Integration tests only (via failsafe plugin)
mvn verify

# Skip integration tests (unit tests only)
mvn test
```

---

## API Reference

### `POST /api/statements/upload`

**Content-Type:** `multipart/form-data`

| Parameter | Type | Required | Description |
|---|---|---|---|
| `file` | file | ✅ | CSV bank statement |
| `bank` | string | ✅ | `ALPHA` or `BETA` (case-insensitive) |

**Success response `200 OK`:**
```json
{
  "bank": "ALPHA",
  "fileName": "alpha_jan2024.csv",
  "savedCount": 10,
  "duplicateCount": 0
}
```

**Error response `400 Bad Request`** (e.g., validation failures):
```json
{
  "message": "Validation failed for 2 row(s)",
  "details": [
    "Row 3 [accountId]: Account ID must not be blank",
    "Row 7 [referenceNumber]: Reference number must not be blank"
  ]
}
```

---

## CSV Format Reference

### Alpha Bank — split debit/credit columns

```
account_id,date,description,reference,debit,credit
ACC001,2024-01-15,Salary Deposit,ALPHA-REF-001,,4250.00
ACC001,2024-01-16,Grocery Store,ALPHA-REF-002,156.75,
```

- **date format:** `yyyy-MM-dd`
- **debit / credit:** exactly one is populated per row; the other is empty
- **stored amount:** `credit − debit` (positive = credit, negative = debit)

### Beta Bank — signed amount + type column

```
account_id,date,description,reference,amount,type
ACC002,2024-01-15,Payroll,BETA-REF-001,5500.00,CR
ACC002,2024-01-16,Supermarket,BETA-REF-002,210.30,DR
```

- **date format:** `yyyy-MM-dd`
- **type:** `CR` (credit) or `DR` (debit)
- **stored amount:** positive for CR, negated for DR

Both formats produce the same unified `Transaction` schema in the database.

---

## Adding a New Bank Format

> Total effort: **1 enum value + 1 class**

1. Add a value to `SourceBank.java`:
   ```java
   public enum SourceBank { ALPHA, BETA, GAMMA }  // ← add GAMMA
   ```

2. Create the parser (Spring auto-registers it via `@Component`):
   ```java
   @Component
   public class GammaBankParser implements BankStatementParser {

       @Override
       public SourceBank supportedBank() { return SourceBank.GAMMA; }

       @Override
       public List<Transaction> parse(MultipartFile file) throws IOException {
           // implement parsing logic here
       }
   }
   ```

3. Add a sample CSV to `src/test/resources/csv/` and a test case to
   `StatementIngestionIT`.

`BankStatementParserFactory` collects all `BankStatementParser` beans automatically —
**no changes to the factory, service, or controller are required**.

---

## Project Structure

```
reconcilia-backend/
├── src/main/java/com/reconcilia/
│   ├── ReconciliaApplication.java
│   ├── controller/       StatementController.java
│   ├── dto/              IngestionResult.java
│   ├── entity/           Transaction.java  SourceBank.java  TransactionStatus.java
│   ├── exception/        GlobalExceptionHandler.java  (+ 4 exception classes)
│   ├── parser/           BankStatementParser.java  AlphaBankParser.java
│   │                     BetaBankParser.java  BankStatementParserFactory.java
│   ├── repository/       TransactionRepository.java
│   └── service/          StatementIngestionService.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/     V1__create_transactions_table.sql
├── src/test/
│   ├── java/com/reconcilia/
│   │   ├── AbstractIntegrationTest.java   ← Testcontainers base
│   │   └── integration/  StatementIngestionIT.java
│   └── resources/csv/    alpha_sample.csv  beta_sample.csv
├── docker-compose.yml
└── pom.xml
```
