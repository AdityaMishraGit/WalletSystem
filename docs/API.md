# Wallet Ledger API Documentation

Base URL: `http://localhost:8080/api/v1`

Interactive docs (Swagger UI): **http://localhost:8080/swagger-ui.html**

**Implemented:** Ledger posting engine (Phase 2), transfer and cash-in (Phase 3), withdrawal reserve (Phase 4), integration tests (Phase 7). Balance is derived from `ledger_entry.balance_after` only.

---

## 1. Create Wallet (User onboarding)

**POST** `/wallets`

Creates a new wallet and its USER_WALLET_ACCOUNT.

### Request

| Header       | Type   | Required | Description        |
|--------------|--------|----------|--------------------|
| Content-Type | string | Yes      | `application/json` |

**Body:**

```json
{
  "userId": "string (required)",
  "currency": "string (optional, default: USD)"
}
```

### Response 200 OK

```json
{
  "walletId": "uuid",
  "userId": "string",
  "status": "ACTIVE",
  "currency": "USD",
  "createdAt": "ISO-8601 timestamp"
}
```

### Example

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123","currency":"USD"}'
```

---

## 2. Cash-In

**POST** `/cashin`

Credits the wallet from settlement. Double-entry: DEBIT settlement_account, CREDIT user_wallet.

### Request

| Header       | Type   | Required | Description        |
|--------------|--------|----------|--------------------|
| Content-Type | string | Yes      | `application/json` |

**Body:**

```json
{
  "walletId": "uuid (required)",
  "amount": "decimal >= 0.01 (required)"
}
```

### Response 200 OK

```json
{
  "transactionId": "uuid",
  "transactionType": "CASH_IN",
  "status": "COMPLETED",
  "referenceId": "string",
  "createdAt": "ISO-8601 timestamp"
}
```

**404** – Wallet not found. **422** – Invalid posting.

---

## 3. Cash-Out (Reserve)

**POST** `/cashout`

Step 1 of withdrawal: DEBIT user_wallet, CREDIT withdrawal_pending.

### Request

**Body:** `{"walletId": "uuid", "amount": "decimal >= 0.01"}`

### Response 200 OK

Transaction with `transactionType`: `WITHDRAWAL_RESERVE`. **422** – Insufficient balance.

---

## 4. Transfer (P2P)

**POST** `/transfer`

Locks sender account; DEBIT sender, CREDIT receiver.

### Request

**Body:** `{"fromWalletId": "uuid", "toWalletId": "uuid", "amount": "decimal >= 0.01"}`

### Response 200 OK

Transaction with `transactionType`: `TRANSFER`. **422** – Insufficient balance.

---

## 5. Get Balance

**GET** `/balance/{walletId}`

Returns current balance from latest `ledger_entry.balance_after`.

### Response 200 OK

```json
{
  "walletId": "uuid",
  "balance": "decimal"
}
```

**404** – Wallet not found.

---

## 6. Transaction History

**GET** `/transactions/{walletId}`

Returns transactions that touch this wallet's account, newest first.

### Response 200 OK

Array of transaction objects (`transactionId`, `transactionType`, `status`, `referenceId`, `createdAt`).

---

## 7. Reversal

**POST** `/reversal`

Creates a compensating REVERSAL transaction for a given original (by reference id).

### Request

**Body:** `{"originalReferenceId": "string (required)"}`

### Response 200 OK

Transaction with `transactionType`: `REVERSAL`. **400** – Original transaction not found.

---

## Error responses

| Status | Meaning                |
|--------|------------------------|
| 400    | Bad request            |
| 404    | Resource not found     |
| 422    | Unprocessable (business rule) |
| 500    | Internal error         |

Error body: `{"error": "message"}`
