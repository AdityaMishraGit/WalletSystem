# Provisioning Services – Workflow Plan

## Overview

Provisioning services let users spend wallet balance to purchase **service bundles** (e.g. TV recharge, prepaid recharge, subscriptions). Each transaction debits the user’s wallet and credits the ledger account used for provisioning (e.g. FEE_ACCOUNT or a dedicated PROVISIONING_ACCOUNT), and records the **bundle** and **customer reference** (e.g. phone number, subscription ID) on the transaction.

---

## 1. Domain Concepts

| Concept | Description |
|--------|-------------|
| **Provisioning service type** | Category of service: `TV_RECHARGE`, `PREPAID_RECHARGE`, `POSTPAID_RECHARGE`, `SUBSCRIPTION`, `DATA_BUNDLE`, `UTILITY_BILL`. |
| **Provisioning service** | A concrete provider/product (e.g. "DSTv", "Airtel Prepaid", "Netflix"). Has a **type**, **name**, **subcategory**. |
| **Service bundle** | A purchasable option under a service (e.g. "DSTv Compact – 50 USD", "Airtel 5 GB – 10 USD"). Has **amount** (fixed or min/max range), **subcategory**, optional validity. |
| **Provisioning transaction** | A ledger transaction with type `PROVISIONING`, linked to a **service_bundle_id** and **provisioning_reference** (customer identifier: phone, account id, etc.). |

---

## 2. Workflow: Executing a Provisioning Transaction

```
┌─────────────┐     POST /provisioning/execute      ┌──────────────────────┐
│   Client    │ ──────────────────────────────────►│  Provisioning API   │
│             │  { walletId, bundleId, reference } │                     │
└─────────────┘                                    └──────────┬───────────┘
                                                               │
                     ┌─────────────────────────────────────────▼─────────────────────────────────────────┐
                     │ 1. Resolve bundle (amount, service). Fail if not found or inactive.                │
                     │ 2. Resolve user wallet & account. Fail if wallet not found.                        │
                     │ 3. Resolve FEE_ACCOUNT (or PROVISIONING_ACCOUNT) as credit side.                    │
                     │ 4. Post double-entry: DEBIT user_wallet, CREDIT fee/provisioning (bundle amount).  │
                     │ 5. Persist transaction with txn_type=PROVISIONING, service_bundle_id,              │
                     │    provisioning_reference.                                                         │
                     │ 6. Return transaction response.                                                     │
                     └───────────────────────────────────────────────────────────────────────────────────┘
```

- **Idempotency:** Use a unique `reference_id` per request (e.g. client-generated or `provisioning-{walletId}-{bundleId}-{reference}-{timestamp}`) so duplicate requests can be detected if you add idempotency later.
- **Failure handling:** If balance is insufficient, the posting engine returns 422. If bundle is invalid or wallet not found, return 400/404.

---

## 3. Workflow: Querying Services and Bundles

- **GET /provisioning-services**  
  List provisioning services. Optional filters: **serviceType**, **subcategory**.

- **GET /provisioning-services/bundles**  
  List bundles with filters:
  - **serviceType** – e.g. `TV_RECHARGE`
  - **amountMin** / **amountMax** – filter by bundle amount (fixed or range)
  - **subcategory** – e.g. "entertainment", "mobile"
  - **serviceId** – restrict to one service

- Responses support pagination (e.g. `page`, `size`) if needed.

---

## 4. Transaction Table Extension

The **transaction** table is extended for provisioning:

| Column | Type | Description |
|--------|------|-------------|
| `service_bundle_id` | UUID NULL | FK to `service_bundle.id`. Set when `txn_type = 'PROVISIONING'`. |
| `provisioning_reference` | VARCHAR(512) NULL | Customer reference (phone number, subscription id, etc.). |

- For non-provisioning transactions, both columns remain NULL.
- Ledger entries are unchanged; only the transaction row carries provisioning context.

---

## 5. Data Model (New Tables)

- **provisioning_service**  
  `id` (PK), `type` (e.g. TV_RECHARGE), `name`, `code`, `subcategory`, `description`, `status`, `created_at`.

- **service_bundle**  
  `id` (PK), `provisioning_service_id` (FK), `name`, `code`, `amount_min`, `amount_max`, `fixed_amount` (nullable), `subcategory`, `validity_days`, `description`, `status`, `created_at`.

- **transaction** (existing)  
  Add `service_bundle_id`, `provisioning_reference`; add `PROVISIONING` to `chk_txn_type`.

---

## 6. API Summary

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | /api/v1/provisioning-services | List services (filter by type, subcategory). |
| GET | /api/v1/provisioning-services/bundles | List bundles (filter by serviceType, amountMin, amountMax, subcategory, serviceId). |
| POST | /api/v1/provisioning/execute | Execute provisioning: debit wallet, credit fee/provisioning account, create PROVISIONING transaction with bundle + reference. |

---

## 7. Double-Entry for Provisioning

- **DEBIT** user’s `USER_WALLET_ACCOUNT` (bundle amount).
- **CREDIT** `FEE_ACCOUNT` (or a dedicated provisioning account) (same amount).
- One transaction, two legs; `transaction` row holds `service_bundle_id` and `provisioning_reference`.
