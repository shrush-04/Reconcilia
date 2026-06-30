# Reconcilia — Multi-Bank Statement Reconciliation Engine

> A backend-first fintech system that automates transaction reconciliation across multiple bank accounts — the same core problem that enterprise treasury platforms solve at scale.

---

## 📌 One-Paragraph Description

**Recruiter-friendly:**
Reconcilia is a full-stack Java application that automates the process of reconciling bank statements from multiple banks. It reads CSV files from different banks — each with a different format — converts them into one standard structure, and automatically identifies which transactions match, which are missing, and which appear more than once. This is the exact workflow that treasury teams at large corporations perform manually every day. Reconcilia makes it automatic, accurate, and auditable.

**Technical:**
Reconcilia is a Spring Boot 3 reconciliation engine that ingests multi-format CSV bank statements, normalises them via a Strategy-pattern parser layer into a unified PostgreSQL-backed transaction schema, and executes a configurable date-tolerance matching algorithm to classify every transaction as `MATCHED`, `UNMATCHED`, or `DUPLICATE` across bank sources. The system is integration-tested with Testcontainers against a real PostgreSQL instance and exposes a documented REST API (Swagger/OpenAPI). A React 19 + Tailwind CSS frontend provides a live upload console and results dashboard.

---

## What Problem It Solves

Large corporates manage cash across dozens or hundreds of bank accounts, each bank delivering statements in a different CSV format. Reconciling these manually is time-consuming, error-prone, and a bottleneck for treasury operations. Reconcilia automates:
- Format normalisation across structurally different bank CSVs
- Duplicate transaction detection within and across bank sources
- Cross-bank transaction matching with configurable date tolerance
- Clear audit output: every transaction is classified, nothing is silently ignored

This is the core domain of platforms like **Omniscient's Liquidice®**, which reconciles cash positions across 350+ banks globally.

---

## ✨ Key Features

- **Multi-format CSV ingestion** — handles banks with split debit/credit columns vs. signed amount + type columns
- **Pluggable bank parser architecture** — Strategy pattern; new bank format = one new class, zero changes elsewhere
- **Configurable reconciliation engine** — date-tolerance window is tunable per reconciliation run
- **Transaction classification** — every record is labelled `MATCHED`, `UNMATCHED`, or `DUPLICATE`
- **REST API with Swagger docs** — all endpoints documented and testable via `/swagger-ui.html`
- **Versioned database migrations** — Flyway manages schema evolution cleanly
- **Real-database integration tests** — Testcontainers spins up actual PostgreSQL for each test run
- **React frontend** — live upload console and reconciliation results dashboard

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring MVC |
| Database | PostgreSQL, Flyway (migrations) |
| Testing | JUnit 5, Testcontainers |
| API Docs | Swagger / OpenAPI 3 |
| Frontend | React 19, Tailwind CSS |
| Build | Maven |

---

## 🏗 Architecture & Design

```
┌─────────────────────────────────────────────────────┐
│                   React Frontend                     │
│         Upload Console │ Results Dashboard           │
└──────────────────────┬──────────────────────────────┘
                       │ REST API
┌──────────────────────▼──────────────────────────────┐
│               Spring Boot API Layer                  │
│    StatementController │ ReconciliationController    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                  Service Layer                       │
│   ParserService (Strategy) │ ReconciliationService   │
└────────────┬─────────────────────────┬──────────────┘
             │                         │
┌────────────▼──────────┐  ┌──────────▼──────────────┐
│   Parser Strategy     │  │  Reconciliation Engine   │
│  BankAParser          │  │  - Duplicate detection   │
│  BankBParser          │  │  - Cross-bank matching   │
│  BankCParser          │  │  - Date tolerance logic  │
│  [plug in new bank]   │  └─────────────────────────┘
└───────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              PostgreSQL (via Flyway)                 │
│         transactions │ reconciliation_results        │
└─────────────────────────────────────────────────────┘
```

### Key Design Decision — Strategy Pattern

Each bank delivers CSVs in a different schema. Rather than writing `if/else` chains for each format, every bank parser implements a common `BankStatementParser` interface. The `ParserFactory` selects the right parser at runtime based on the bank identifier. **Adding a new bank requires exactly one new class — no changes to the service, controller, or factory.**

```java
// Adding a new bank is this simple:
public class BankDParser implements BankStatementParser {
    @Override
    public List<Transaction> parse(MultipartFile file) { ... }
}
```

---

## ⚙️ How It Works

1. User uploads a CSV bank statement via the React frontend (or directly via the API)
2. The `ParserFactory` identifies the bank format and selects the correct `BankStatementParser`
3. The parser normalises the CSV into a unified `Transaction` schema and persists to PostgreSQL
4. User triggers reconciliation via the API; the `ReconciliationService` runs the matching engine
5. Every transaction is classified as `MATCHED`, `UNMATCHED`, or `DUPLICATE`
6. Results are stored and queryable; the React dashboard renders classification breakdown

---

## 📦 Installation

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Node.js 18+ (for frontend)
- Maven 3.8+

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/shrush-04/reconcilia.git
cd reconcilia

# Configure database connection
cp src/main/resources/application.example.properties src/main/resources/application.properties
# Edit application.properties with your PostgreSQL credentials

# Run Flyway migrations and start the server
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Frontend Setup

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173`

---




## 📁 Folder Structure

```
reconcilia/
├── src/
│   ├── main/
│   │   ├── java/com/reconcilia/
│   │   │   ├── controller/         # REST controllers
│   │   │   ├── service/            # Business logic
│   │   │   ├── parser/             # Strategy-pattern bank parsers
│   │   │   │   ├── BankStatementParser.java   # Interface
│   │   │   │   ├── BankAParser.java
│   │   │   │   ├── BankBParser.java
│   │   │   │   └── ParserFactory.java
│   │   │   ├── model/              # Domain models
│   │   │   ├── repository/         # Spring Data JPA repositories
│   │   │   └── reconciliation/     # Matching engine
│   │   └── resources/
│   │       ├── db/migration/       # Flyway SQL migrations
│   │       └── application.properties
│   └── test/
│       └── java/com/reconcilia/    # Testcontainers integration tests
├── frontend/                       # React 19 + Tailwind
│   ├── src/
│   │   ├── components/
│   │   └── pages/
│   └── package.json
├── docs/
│   └── architecture.md
└── pom.xml
```

---

## 🧠 Reconciliation Logic

The matching engine runs in three passes:

**Pass 1 — Duplicate Detection**
Within each bank's uploaded records, transactions with identical amount, date (within tolerance), and reference are flagged as `DUPLICATE`. This catches double-uploads and genuine bank-side duplicates.

**Pass 2 — Cross-Bank Matching**
Transactions from different banks are compared. A match requires: same amount, same currency, and transaction dates within the configured tolerance window (default: ±1 day). Matched pairs are labelled `MATCHED`.

**Pass 3 — Unmatched Flagging**
Any transaction not resolved in Pass 1 or 2 is labelled `UNMATCHED` and surfaced for manual review.

**Why date tolerance matters:**
Real bank statements often record the same transaction on slightly different dates (value date vs. transaction date). A hard date equality check would incorrectly flag genuine matches as unmatched. The configurable tolerance window solves this — the same approach used in production treasury systems.

---

## 🖼 Screenshots


| Screen | Description |
|---|---|
| `docs/screenshots/upload.png` | Statement upload console |
| `docs/screenshots/results.png` | Reconciliation results dashboard |
| `docs/screenshots/swagger.png` | Swagger API documentation |

---


## 🔮 Future Improvements

- Support for additional file formats (XLSX, MT940 SWIFT format)
- Scheduled auto-reconciliation via cron triggers
- Role-based access control (admin vs. viewer)
- Email/webhook alerts for unmatched transaction threshold breaches
- Export reconciliation report as PDF

---

## 💡 Why This Project Matters

Manual bank reconciliation is one of the most resource-intensive operations in corporate treasury. A finance team managing 50 bank accounts across multiple currencies can spend days per month on this process. Reconcilia demonstrates that this workflow can be automated with clean architecture, a domain-aware matching algorithm, and a well-tested backend — the same engineering principles behind enterprise treasury platforms.

---

## 👩‍💻 My Contribution

I designed and built this project independently. The key decision I am most proud of is the Strategy-pattern parser architecture — I recognised early that adding per-bank `if/else` logic would become unmaintainable as the number of bank formats grew, and chose a pluggable interface instead. I also chose Testcontainers over H2 for integration testing after realising that in-memory databases mask PostgreSQL-specific behaviour in constraint enforcement and SQL compatibility. Both decisions reflect how I approach real engineering problems: design for change, test against reality.

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

*Built by [Shrushti Mangalekar](https://www.linkedin.com/in/shrushti-mangalekar-bbb016248/) · [GitHub](https://github.com/shrush-04)*
