You are a senior fintech backend architect.

Design and implement a production-grade Wallet Ledger System using Java 22 (OpenJDK 22) + Spring Boot 5 (Reactive WebFlux) + PostgreSQL.

The system must behave like a banking core ledger and MUST NOT store balance as a mutable column.
Balance must always be derived from immutable double-entry ledger records.

Follow strict financial correctness, concurrency safety, and auditability.

---

## CORE FUNCTIONAL REQUIREMENTS

The system supports:

1. User onboarding (create wallet account)
2. Cash-In (add money from bank/provider)
3. Cash-Out (withdraw money to bank)
4. Wallet Transfer (P2P transfer)
5. Balance enquiry
6. Transaction history
7. Reversal support

---

## CRITICAL ACCOUNTING RULES

Use DOUBLE ENTRY ACCOUNTING:

Every transaction produces TWO ledger postings:

DEBIT account
CREDIT account

Money must never be created or destroyed.

System accounts must exist:

* SYSTEM_MASTER_ACCOUNT
* USER_WALLET_ACCOUNT
* SETTLEMENT_ACCOUNT
* WITHDRAWAL_PENDING_ACCOUNT
* FEE_ACCOUNT
* REVERSAL_ACCOUNT

Balance must be computed using ledger_entry.balance_after snapshot.

---

## TECHNICAL CONSTRAINTS

Architecture style:

* Clean Architecture + Domain Driven Design
* Hexagonal Ports/Adapters
* Pessimistic locking for debit operations

Stack:

* Spring Boot WebFlux
* R2DBC Postgres
* Flyway migrations
* Lombok
* MapStruct
* Testcontainers
* JUnit5

---

## DATA MODEL (STRICT)

wallet
wallet_id
user_id
status
currency
created_at

account
account_id
account_type
wallet_id (nullable for system accounts)
status

transaction
txn_id
txn_type
status
reference_id
created_at

ledger_entry
entry_id
txn_id
account_id
direction (DEBIT/CREDIT)
amount
balance_after
created_at

---

## BUSINESS RULES

TRANSFER:

* Must lock sender account (SELECT FOR UPDATE)
* Prevent negative balance
* Atomic posting

CASH IN:
DEBIT settlement_account
CREDIT user_wallet

CASH OUT:
STEP 1 reserve funds:
DEBIT user_wallet
CREDIT withdrawal_pending

STEP 2 on bank success:
DEBIT withdrawal_pending
CREDIT settlement_account

REVERSAL:
Create compensating transaction, never delete rows

---

## API REQUIREMENTS

POST /wallets
POST /cashin
POST /cashout
POST /transfer
GET /balance/{walletId}
GET /transactions/{walletId}

---

## NON FUNCTIONAL REQUIREMENTS

* Serializable financial consistency
* No race conditions
* Auditable trail
* Horizontal scalable
* Exactly once ledger posting
* Retry safe

---

## IMPLEMENTATION PLAN

Generate the project in phases:

PHASE 1:
Create domain entities and database migrations

PHASE 2:
Implement ledger posting engine

PHASE 3:
Implement transfer and cash-in flows

PHASE 4:
Implement withdrawal with reservation

PHASE 7:
Integration tests proving no double debit under concurrency

---

Start by generating project structure and domain layer only.
Do not implement controllers yet.

---

## Implementation summary (all phases)

- **Phase 1–2:** Domain entities, Flyway migrations, ledger posting engine (double-entry, balance from `ledger_entry.balance_after`).
- **Phase 3:** Transfer (with `SELECT FOR UPDATE` on sender) and cash-in flows.
- **Phase 4:** Cash-out: reserve (DEBIT user, CREDIT withdrawal_pending) and settle (DEBIT withdrawal_pending, CREDIT settlement).
- **Phase 7:** Integration test (Testcontainers Postgres) proving no double debit under concurrent transfers.

**API documentation**
- **OpenAPI (Swagger UI):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Markdown:** [docs/API.md](docs/API.md) — request/response for every endpoint.

---

## Run on local

**Maven (use local settings)**

A `settings-local.xml` is included for local runs. Use it so Maven uses the right repo and profile:

```bash
# Build
mvn -s settings-local.xml clean install

# Run tests (Postgres via Testcontainers)
mvn -s settings-local.xml test

# Start the application (requires Postgres)
mvn -s settings-local.xml spring-boot:run
```

Alternatively, copy the file to your Maven user config (backup any existing one first):

```bash
cp settings-local.xml ~/.m2/settings.xml
mvn clean spring-boot:run
```

**Requirements**

- OpenJDK 22 (or compatible JDK 22)
- PostgreSQL 15+ (e.g. `localhost:5432`, database `wallet_ledger`, user/password `postgres`)
