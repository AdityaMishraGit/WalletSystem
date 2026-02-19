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
  "userId": "string (required, unique per wallet)",
  "amount": "decimal >= 0.01 (required)"
}
```

**Note:** `userId` is unique per wallet; it is the primary key for all transactional APIs.

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

**400** – Wallet not found for userId. **422** – Invalid posting.

---

## 3. Cash-Out (Reserve)

**POST** `/cashout`

Step 1 of withdrawal: DEBIT user_wallet, CREDIT withdrawal_pending.

### Request

**Body:** `{"userId": "string (required)", "amount": "decimal >= 0.01"}`

### Response 200 OK

Transaction with `transactionType`: `WITHDRAWAL_RESERVE`. **422** – Insufficient balance.

---

## 4. Transfer (P2P)

**POST** `/transfer`

Locks sender account; DEBIT sender, CREDIT receiver.

### Request

**Body:** `{"fromUserId": "string (required)", "toUserId": "string (required)", "amount": "decimal >= 0.01"}`

### Response 200 OK

Transaction with `transactionType`: `TRANSFER`. **422** – Insufficient balance.

---

## 5. Get Balance

**GET** `/balance/{userId}`

Returns current balance from latest `ledger_entry.balance_after`. `userId` is unique per wallet.

### Response 200 OK

```json
{
  "userId": "string",
  "walletId": "uuid",
  "balance": "decimal"
}
```

**400** – Wallet not found for userId.

---

## 6. Transaction History

**GET** `/transactions/{userId}`

Returns transaction history for this user's wallet. **Both credit and debit legs** are returned: each transaction produces one response item per ledger entry (e.g. one DEBIT leg and one CREDIT leg), so you see the full double-entry view. Fields include **userId**, **accountId** (which account this leg belongs to), **amount**, **direction** (DEBIT/CREDIT), **balanceAfter** and full transaction details. Newest transactions first. `userId` is unique per wallet.

### Response 200 OK

Array of transaction history items. Each item is **one leg** of a transaction (credit or debit). A single transaction typically appears as two items: one DEBIT and one CREDIT.

| Field | Type | Description |
|-------|------|-------------|
| `userId` | string | User ID (unique per wallet) |
| `accountId` | string | Account ID for this leg (identifies which account was debited or credited) |
| `transactionId` | string | Transaction UUID |
| `transactionType` | string | CASH_IN, CASH_OUT, TRANSFER, PROVISIONING, REVERSAL, etc. |
| `status` | string | COMPLETED, PENDING, etc. |
| `referenceId` | string | Reference ID |
| `createdAt` | string | ISO-8601 timestamp |
| `serviceBundleId` | string | For PROVISIONING: bundle ID; null otherwise |
| `provisioningReference` | string | For PROVISIONING: e.g. phone number; null otherwise |
| `amount` | decimal | Amount for this leg (debit or credit) |
| `direction` | string | **DEBIT** = amount deducted from this account, **CREDIT** = amount added to this account |
| `balanceAfter` | decimal | Balance of **this account** after this transaction was applied |

---

## 7. Reversal

**POST** `/reversal`

Creates a compensating REVERSAL transaction for a given original (by reference id).

### Request

**Body:** `{"originalReferenceId": "string (required)"}`

### Response 200 OK

Transaction with `transactionType`: `REVERSAL`. **400** – Original transaction not found.

---

## 8. System accounts (balance and credit)

System accounts (SETTLEMENT_ACCOUNT, WITHDRAWAL_PENDING_ACCOUNT, FEE_ACCOUNT, REVERSAL_ACCOUNT, SYSTEM_MASTER_ACCOUNT) are seeded with an initial balance of 1,000,000,000 each (except SYSTEM_MASTER_ACCOUNT which nets to 0). You can check their balances and add more funds via these endpoints.

### List all system account balances

**GET** `/system-accounts/balance`

Returns balance for each system account.

**Response 200 OK:** Array of `{ "accountType": "string", "accountId": "uuid", "balance": "decimal" }`.

### Get one system account balance

**GET** `/system-accounts/balance/{accountType}`

**Path:** `accountType` = `SYSTEM_MASTER_ACCOUNT` | `SETTLEMENT_ACCOUNT` | `WITHDRAWAL_PENDING_ACCOUNT` | `FEE_ACCOUNT` | `REVERSAL_ACCOUNT`.

**Response 200 OK:** `{ "accountType": "string", "accountId": "uuid", "balance": "decimal" }`.

### Add amount to a system account

**POST** `/system-accounts/credit`

Credits the given system account (double-entry: DEBIT SYSTEM_MASTER_ACCOUNT, CREDIT target). Use for SETTLEMENT_ACCOUNT, WITHDRAWAL_PENDING_ACCOUNT, FEE_ACCOUNT, or REVERSAL_ACCOUNT (not SYSTEM_MASTER_ACCOUNT).

**Body:** `{ "accountType": "SETTLEMENT_ACCOUNT", "amount": 500.00 }`

**Response 200 OK:** Transaction object (`transactionType`: `SYSTEM_CREDIT`).

---

## 9. Provisioning (service bundles)

Provisioning allows paying for service bundles (e.g. TV recharge, prepaid, subscriptions) from the wallet. Double-entry: DEBIT user_wallet, CREDIT FEE_ACCOUNT. The `transaction` row stores `service_bundle_id` and `provisioning_reference`.

### Create provisioning service

**POST** `/provisioning-services`

Creates a new provisioning service. Code must be unique.

**Body:** `{ "type": "TV_RECHARGE" | "PREPAID_RECHARGE" | "POSTPAID_RECHARGE" | "SUBSCRIPTION" | "DATA_BUNDLE" | "UTILITY_BILL", "name": "string (required)", "code": "string (required)", "subcategory": "string (optional)", "description": "string (optional)", "status": "ACTIVE | INACTIVE (optional, default ACTIVE)" }`

**Response 201:** Provisioning service object (`id`, `type`, `name`, `code`, `subcategory`, `description`, `status`, `createdAt`). **400** – Invalid request or duplicate code.

### Update provisioning service

**PUT** `/provisioning-services/{id}`

Updates an existing provisioning service by id. Full replace; all body fields required.

**Body:** `{ "type", "name", "code", "subcategory", "description", "status" }` (all required except optional subcategory/description)

**Response 200:** Updated provisioning service object. **400** – Invalid request or service not found.

### List provisioning services

**GET** `/provisioning-services`

Returns provisioning services with optional filters.

**Query parameters:**

| Parameter     | Type   | Required | Description                                                                 |
|---------------|--------|----------|-----------------------------------------------------------------------------|
| serviceType   | string | No       | Filter by type: `TV_RECHARGE`, `PREPAID_RECHARGE`, `POSTPAID_RECHARGE`, `SUBSCRIPTION`, `DATA_BUNDLE`, `UTILITY_BILL` |
| subcategory   | string | No       | Filter by subcategory                                                       |

**Response 200 OK:** Array of `{ "id", "type", "name", "code", "subcategory", "description", "status", "createdAt" }`.

### List service bundles

**GET** `/provisioning-services/bundles`

Returns service bundles. Each bundle has a **fixed amount** (price). Optional filters include an amount threshold: pass `amount` and `amountFilter` to get bundles whose fixed amount is **greater than** or **less than** the given amount.

**Query parameters:**

| Parameter     | Type    | Required | Description                                                                 |
|---------------|---------|----------|-----------------------------------------------------------------------------|
| serviceType   | string  | No       | Filter by provisioning service type                                        |
| amount        | decimal | No       | Amount threshold; used with `amountFilter` to filter by bundle fixed amount |
| amountFilter  | string  | No       | `GREATER_THAN` = bundles with fixed_amount **>** amount; `LESS_THAN` = fixed_amount **<** amount. Ignored if `amount` is not provided. |
| subcategory   | string  | No       | Filter by bundle subcategory                                               |
| serviceId     | uuid    | No       | Filter by provisioning service ID                                          |

**Examples:**

- `?amount=50&amountFilter=GREATER_THAN` → bundles with fixed amount &gt; 50  
- `?amount=100&amountFilter=LESS_THAN` → bundles with fixed amount &lt; 100  
- Omit `amount` and `amountFilter` to return all bundles (subject to other filters).

**Response 200 OK:** Array of bundle objects including `id`, `provisioningServiceId`, `name`, `code`, `fixedAmount`, `subcategory`, `validityDays`, `effectiveAmount`, `status`, `createdAt`.

### Create service bundle

**POST** `/provisioning-services/bundles`

Add a new bundle to a provisioning service. Body must include `provisioningServiceId` (UUID of the service), `name`, `code`, `fixedAmount`, and optionally `subcategory`, `validityDays`, `description`, `status`.

**Response 201:** Service bundle object. **400** – Invalid request or provisioning service not found.

### Update service bundle

**PUT** `/provisioning-services/bundles/{id}`

Update an existing service bundle by id. Body: `provisioningServiceId`, `name`, `code`, `fixedAmount`, `subcategory`, `validityDays`, `description`, `status`.

**Response 200:** Updated bundle object. **400** – Bundle or service not found.

### Execute provisioning transaction

**POST** `/provisioning/execute`

Debits the user wallet and credits FEE_ACCOUNT for the selected bundle. Transaction type: `PROVISIONING`.

**Body:** `{ "userId": "string (required, unique per wallet)", "bundleId": "uuid (required)", "provisioningReference": "string (optional)" }`

**Response 200 OK:** Transaction object including `transactionId`, `transactionType`: `PROVISIONING`, `status`, `referenceId`, `serviceBundleId`, `provisioningReference`, `createdAt`.

**400** – Wallet or bundle not found, bundle inactive, or invalid amount. **422** – Insufficient balance.

---

## Error responses

| Status | Meaning                |
|--------|------------------------|
| 400    | Bad request            |
| 404    | Resource not found     |
| 422    | Unprocessable (business rule) |
| 500    | Internal error         |

Error body: `{"error": "message"}`
