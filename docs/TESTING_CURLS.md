# Wallet Ledger – Testing cURL Examples

Base URL: **http://localhost:8080/api/v1**

Ensure the application is running (`mvn spring-boot:run`) and PostgreSQL is up before running these commands.

---

## 1. Create Wallet (User onboarding)

**POST** `/wallets` – Creates a new wallet and its USER_WALLET_ACCOUNT.

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-123","currency":"USD"}'
```

Optional: omit `currency` (defaults to USD):

```bash
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"user-456"}'
```

**Example response (200):**
```json
{"walletId":"...","userId":"user-123","status":"ACTIVE","currency":"USD","createdAt":"..."}
```

---

## 2. Cash-In

**POST** `/cashin` – Credits the wallet from settlement (DEBIT settlement, CREDIT user_wallet).

Replace `{walletId}` with a wallet UUID from Create Wallet.

```bash
curl -X POST http://localhost:8080/api/v1/cashin \
  -H "Content-Type: application/json" \
  -d '{"walletId":"{walletId}","amount":100.00}'
```

**Example response (200):**
```json
{"transactionId":"...","transactionType":"CASH_IN","status":"COMPLETED","referenceId":"...","createdAt":"..."}
```

---

## 3. Cash-Out (Reserve withdrawal)

**POST** `/cashout` – Reserves withdrawal (DEBIT user_wallet, CREDIT withdrawal_pending).

```bash
curl -X POST http://localhost:8080/api/v1/cashout \
  -H "Content-Type: application/json" \
  -d '{"walletId":"{walletId}","amount":25.50}'
```

**Example response (200):**
```json
{"transactionId":"...","transactionType":"WITHDRAWAL_RESERVE","status":"COMPLETED","referenceId":"...","createdAt":"..."}
```

**422** – Insufficient balance.

---

## 4. Transfer (P2P)

**POST** `/transfer` – Transfers amount from one wallet to another (locks sender; DEBIT sender, CREDIT receiver).

Replace `{fromWalletId}` and `{toWalletId}` with wallet UUIDs.

```bash
curl -X POST http://localhost:8080/api/v1/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId":"{fromWalletId}","toWalletId":"{toWalletId}","amount":10.00}'
```

**Example response (200):**
```json
{"transactionId":"...","transactionType":"TRANSFER","status":"COMPLETED","referenceId":"...","createdAt":"..."}
```

**422** – Insufficient balance.

---

## 5. Get Balance

**GET** `/balance/{walletId}` – Returns current balance from ledger.

```bash
curl -s http://localhost:8080/api/v1/balance/{walletId}
```

**Example response (200):**
```json
{"walletId":"...","balance":100.00}
```

**404** – Wallet not found.

---

## 6. Transaction History

**GET** `/transactions/{walletId}` – Returns transactions for this wallet's account, newest first.

```bash
curl -s http://localhost:8080/api/v1/transactions/{walletId}
```

**Example response (200):** Array of transaction objects.

**404** – Wallet not found.

---

## 7. Reversal

**POST** `/reversal` – Creates a compensating REVERSAL transaction for an original transaction (by its reference id).

Replace `{originalReferenceId}` with the `referenceId` of a previous transaction (e.g. from Cash-In, Transfer, or Cash-Out response).

```bash
curl -X POST http://localhost:8080/api/v1/reversal \
  -H "Content-Type: application/json" \
  -d '{"originalReferenceId":"{originalReferenceId}"}'
```

**Example response (200):**
```json
{"transactionId":"...","transactionType":"REVERSAL","status":"COMPLETED","referenceId":"...","createdAt":"..."}
```

**400** – Original transaction not found.

---

## Full flow (copy-paste sequence)

Run in order; replace `WALLET_A` and `WALLET_B` with the `walletId` values from step 1 and 2.

```bash
# 1. Create two wallets
curl -s -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"alice","currency":"USD"}'
# Save walletId as WALLET_A

curl -s -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json" \
  -d '{"userId":"bob","currency":"USD"}'
# Save walletId as WALLET_B

# 2. Cash-in to wallet A
curl -s -X POST http://localhost:8080/api/v1/cashin \
  -H "Content-Type: application/json" \
  -d '{"walletId":"WALLET_A","amount":200.00}'

# 3. Get balance (wallet A)
curl -s http://localhost:8080/api/v1/balance/WALLET_A

# 4. Transfer from A to B
curl -s -X POST http://localhost:8080/api/v1/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId":"WALLET_A","toWalletId":"WALLET_B","amount":50.00}'

# 5. Get balances (both)
curl -s http://localhost:8080/api/v1/balance/WALLET_A
curl -s http://localhost:8080/api/v1/balance/WALLET_B

# 6. Transaction history (wallet A)
curl -s http://localhost:8080/api/v1/transactions/WALLET_A

# 7. Cash-out from wallet A (reserve)
curl -s -X POST http://localhost:8080/api/v1/cashout \
  -H "Content-Type: application/json" \
  -d '{"walletId":"WALLET_A","amount":20.00}'

# 8. Reversal (use a referenceId from a previous transaction response)
curl -s -X POST http://localhost:8080/api/v1/reversal \
  -H "Content-Type: application/json" \
  -d '{"originalReferenceId":"PASTE_REFERENCE_ID_HERE"}'
```

---

## Error responses

| Status | Meaning                          |
|--------|----------------------------------|
| 400    | Bad request (e.g. invalid UUID)  |
| 404    | Resource not found (e.g. wallet) |
| 422    | Unprocessable (e.g. insufficient balance, invalid posting) |
| 500    | Internal error                   |

Error body: `{"error": "message"}`

Example – insufficient balance (422):

```bash
curl -s -X POST http://localhost:8080/api/v1/transfer \
  -H "Content-Type: application/json" \
  -d '{"fromWalletId":"{walletId}","toWalletId":"{otherWalletId}","amount":999999.00}'
```

---

## Pretty-print JSON (optional)

Pipe through `jq` if installed:

```bash
curl -s http://localhost:8080/api/v1/balance/{walletId} | jq
```
