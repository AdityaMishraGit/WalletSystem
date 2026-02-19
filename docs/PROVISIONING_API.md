# Provisioning API Documentation

API for discovering provisioning services and bundles, and for executing provisioning transactions (paying for a service bundle from a wallet).

**Base URL:** `http://localhost:8085/api/v1`  
**Content-Type:** `application/json`

---

## Overview

| Endpoint | Method | Description |
|----------|--------|-------------|
| Create provisioning service | `POST /provisioning-services` | Create a new provisioning service |
| Update provisioning service | `PUT /provisioning-services/{id}` | Update an existing provisioning service by id |
| List provisioning services | `GET /provisioning-services` | Get available services with optional filters |
| Create service bundle | `POST /provisioning-services/bundles` | Add a new bundle to a provisioning service (body includes `provisioningServiceId`) |
| Update service bundle | `PUT /provisioning-services/bundles/{id}` | Update an existing bundle by id |
| List service bundles | `GET /provisioning-services/bundles` | Get bundles (fixed amount) with optional filters (amount, amountFilter, serviceId, etc.) |
| Execute provisioning | `POST /provisioning/execute` | Debit wallet and credit FEE account for a selected bundle |

**How to add bundles to a provisioning service:**  
1. Create or pick a **provisioning service** (e.g. **POST /provisioning-services** or **GET /provisioning-services**).  
2. **POST /provisioning-services/bundles** with body `provisioningServiceId` (that service’s id), `name`, `code`, `fixedAmount`, and optional `subcategory`, `validityDays`, `description`, `status`.  
3. List bundles with **GET /provisioning-services/bundles?serviceId=&lt;id&gt;** or use the returned bundle in **POST /provisioning/execute**.

**Typical flow (end-to-end):**  
1. Create a service → **POST /provisioning-services**.  
2. Add bundles to it → **POST /provisioning-services/bundles** (one or more times) with the same `provisioningServiceId`.  
3. List bundles → **GET /provisioning-services/bundles**.  
4. Execute a purchase → **POST /provisioning/execute** with `userId`, `bundleId`, and optional `provisioningReference`.

---

## 1. Create provisioning service

**POST** `/provisioning-services`

Creates a new provisioning service. The `code` must be unique across all services.

### Request

**Headers:** `Content-Type: application/json`

**Body:**

| Field         | Type   | Required | Description |
|---------------|--------|----------|-------------|
| `type`        | string | Yes      | One of: `TV_RECHARGE`, `PREPAID_RECHARGE`, `POSTPAID_RECHARGE`, `SUBSCRIPTION`, `DATA_BUNDLE`, `UTILITY_BILL` |
| `name`        | string | Yes      | Display name (e.g. "DSTv", "Airtel Prepaid") |
| `code`        | string | Yes      | Unique service code |
| `subcategory` | string | No       | Subcategory (e.g. entertainment, mobile) |
| `description` | string | No       | Optional description |
| `status`      | string | No       | `ACTIVE` or `INACTIVE`. Defaults to `ACTIVE` if omitted |

### Response 201 Created

Same shape as a single provisioning service in the list response: `id`, `type`, `name`, `code`, `subcategory`, `description`, `status`, `createdAt`.

### Error responses

| Status | Condition |
|--------|-----------|
| **400** | Invalid request body or duplicate `code` |

### Example

```bash
curl -s -X POST "http://localhost:8085/api/v1/provisioning-services" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TV_RECHARGE",
    "name": "DSTv",
    "code": "DSTV",
    "subcategory": "entertainment",
    "description": "Satellite TV",
    "status": "ACTIVE"
  }'
```

---

## 2. Update provisioning service

**PUT** `/provisioning-services/{id}`

Updates an existing provisioning service by id. All fields in the body are required (full replace).

### Request

**Path:** `id` – UUID of the provisioning service to update.

**Headers:** `Content-Type: application/json`

**Body:**

| Field         | Type   | Required | Description |
|---------------|--------|----------|-------------|
| `type`        | string | Yes      | Service type (same values as create) |
| `name`        | string | Yes      | Display name |
| `code`        | string | Yes      | Unique service code (can be changed) |
| `subcategory` | string | No       | Subcategory |
| `description` | string | No       | Description |
| `status`      | string | Yes      | `ACTIVE` or `INACTIVE` |

### Response 200 OK

Updated provisioning service object: `id`, `type`, `name`, `code`, `subcategory`, `description`, `status`, `createdAt`.

### Error responses

| Status | Condition |
|--------|-----------|
| **400** | Invalid request body or service not found |

### Example

```bash
curl -s -X PUT "http://localhost:8085/api/v1/provisioning-services/550e8400-e29b-41d4-a716-446655440001" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TV_RECHARGE",
    "name": "DSTv Updated",
    "code": "DSTV",
    "subcategory": "entertainment",
    "description": "Satellite TV provider",
    "status": "ACTIVE"
  }'
```

---

## 3. List provisioning services

**GET** `/provisioning-services`

Returns provisioning services (e.g. TV recharge, prepaid, subscriptions). Optional filters narrow the list by type and subcategory.

### Request

| Query parameter | Type   | Required | Description |
|-----------------|--------|----------|-------------|
| `serviceType`   | string | No       | Filter by type. One of: `TV_RECHARGE`, `PREPAID_RECHARGE`, `POSTPAID_RECHARGE`, `SUBSCRIPTION`, `DATA_BUNDLE`, `UTILITY_BILL` |
| `subcategory`   | string | No       | Filter by subcategory (e.g. `entertainment`, `mobile`) |

### Response 200 OK

Array of provisioning service objects.

| Field         | Type   | Description |
|---------------|--------|-------------|
| `id`          | string | UUID of the service |
| `type`        | string | One of the service types above |
| `name`        | string | Display name (e.g. "DSTv", "Airtel Prepaid") |
| `code`        | string | Service code |
| `subcategory` | string | Subcategory |
| `description` | string | Optional description |
| `status`      | string | e.g. `ACTIVE` |
| `createdAt`   | string | ISO-8601 timestamp |

### Example

```bash
# All active services
curl -s "http://localhost:8085/api/v1/provisioning-services"

# Only TV / subscription types
curl -s "http://localhost:8085/api/v1/provisioning-services?serviceType=TV_RECHARGE"
curl -s "http://localhost:8085/api/v1/provisioning-services?serviceType=SUBSCRIPTION"

# By subcategory
curl -s "http://localhost:8085/api/v1/provisioning-services?subcategory=entertainment"
```

### Example response body

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "type": "TV_RECHARGE",
    "name": "DSTv",
    "code": "DSTV",
    "subcategory": "entertainment",
    "description": "Satellite TV",
    "status": "ACTIVE",
    "createdAt": "2025-01-15T10:00:00Z"
  }
]
```

---

## 4. Create service bundle

**POST** `/provisioning-services/bundles`

Adds a new bundle to a provisioning service. The bundle is linked to the service via `provisioningServiceId`. Each bundle has a **fixed amount** (price).

### Request

**Headers:** `Content-Type: application/json`

**Body:**

| Field                   | Type    | Required | Description |
|-------------------------|---------|----------|-------------|
| `provisioningServiceId` | string  | Yes      | UUID of the provisioning service this bundle belongs to |
| `name`                  | string  | Yes      | Bundle display name (e.g. "DSTv Compact", "Netflix Basic") |
| `code`                  | string  | Yes      | Bundle code (unique per service) |
| `fixedAmount`           | decimal | Yes      | Fixed price/amount (must be ≥ 0) |
| `subcategory`           | string  | No       | Subcategory |
| `validityDays`          | integer | No       | Validity in days (e.g. 30 for monthly) |
| `description`           | string  | No       | Optional description |
| `status`                | string  | No       | `ACTIVE` or `INACTIVE`. Defaults to `ACTIVE` if omitted |

### Response 201 Created

Service bundle object: `id`, `provisioningServiceId`, `name`, `code`, `fixedAmount`, `subcategory`, `validityDays`, `description`, `status`, `effectiveAmount`, `createdAt`.

### Error responses

| Status | Condition |
|--------|-----------|
| **400** | Invalid request or provisioning service not found |

### Example

```bash
curl -s -X POST "http://localhost:8085/api/v1/provisioning-services/bundles" \
  -H "Content-Type: application/json" \
  -d '{
    "provisioningServiceId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "DSTv Compact",
    "code": "DSTV-COMPACT",
    "fixedAmount": 50.00,
    "subcategory": "entertainment",
    "validityDays": 30,
    "status": "ACTIVE"
  }'
```

---

## 5. Update service bundle

**PUT** `/provisioning-services/bundles/{id}`

Updates an existing service bundle by id. Full replace; all body fields required (same shape as create, plus `status` required).

**Path:** `id` – UUID of the bundle to update.

**Body:** Same fields as Create service bundle; `status` is required.

**Response 200:** Updated bundle object. **400** – Invalid request or bundle/provisioning service not found.

---

## 6. List service bundles

**GET** `/provisioning-services/bundles`

Returns service bundles. Each bundle has a **fixed amount** (price). You can filter by service type, subcategory, service ID, and by amount: pass `amount` and `amountFilter` to get bundles whose fixed amount is **greater than** or **less than** the given value.

### Request

| Query parameter | Type    | Required | Description |
|-----------------|---------|----------|-------------|
| `serviceType`   | string  | No       | Filter by provisioning service type (e.g. `TV_RECHARGE`, `SUBSCRIPTION`) |
| `amount`        | decimal | No       | Amount threshold. Used with `amountFilter` to filter by bundle fixed amount |
| `amountFilter`  | string  | No       | `GREATER_THAN` = bundles with fixed_amount **&gt;** amount; `LESS_THAN` = fixed_amount **&lt;** amount. Ignored if `amount` is not provided |
| `subcategory`   | string  | No       | Filter by bundle subcategory |
| `serviceId`     | uuid    | No       | Restrict to bundles belonging to this provisioning service ID |

### Response 200 OK

Array of service bundle objects.

| Field                  | Type    | Description |
|------------------------|---------|-------------|
| `id`                   | string  | UUID of the bundle |
| `provisioningServiceId`| string  | UUID of the parent service |
| `name`                 | string  | Display name (e.g. "DSTv Compact") |
| `code`                 | string  | Bundle code |
| `amountMin`            | number  | Optional; null when fixed amount is set |
| `amountMax`            | number  | Optional; null when fixed amount is set |
| `fixedAmount`          | number  | **Price to charge** (fixed amount for the bundle) |
| `subcategory`          | string  | Subcategory |
| `validityDays`         | number  | Optional validity in days |
| `description`          | string  | Optional description |
| `status`               | string  | e.g. `ACTIVE` |
| `createdAt`            | string  | ISO-8601 timestamp |
| `effectiveAmount`      | number  | Effective charge amount (same as `fixedAmount` when set) |

### Amount filter behaviour

- **`amount=50`** + **`amountFilter=GREATER_THAN`** → bundles with `fixed_amount > 50`
- **`amount=100`** + **`amountFilter=LESS_THAN`** → bundles with `fixed_amount < 100`
- Omit both `amount` and `amountFilter` to return all bundles (subject to other filters).  
- If only `amount` is sent (no `amountFilter`), no amount filter is applied.

### Examples

```bash
# All bundles
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles"

# Bundles for a specific service (use service id from list services)
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceId=550e8400-e29b-41d4-a716-446655440001"

# Bundles with fixed amount greater than 50
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?amount=50&amountFilter=GREATER_THAN"

# Bundles with fixed amount less than 100
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?amount=100&amountFilter=LESS_THAN"

# Combine: subscription bundles under 20
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceType=SUBSCRIPTION&amount=20&amountFilter=LESS_THAN"
```

### Example response body

```json
[
  {
    "id": "650e8400-e29b-41d4-a716-446655440001",
    "provisioningServiceId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "DSTv Compact",
    "code": "DSTV-COMPACT",
    "amountMin": null,
    "amountMax": null,
    "fixedAmount": 50.00,
    "subcategory": "entertainment",
    "validityDays": 30,
    "description": "Compact package",
    "status": "ACTIVE",
    "createdAt": "2025-01-15T10:00:00Z",
    "effectiveAmount": 50.00
  }
]
```

---

## 7. Execute provisioning transaction

**POST** `/provisioning/execute`

Debits the user wallet and credits the FEE account for the selected service bundle. Creates a transaction with type `PROVISIONING` and stores `serviceBundleId` and `provisioningReference` on the transaction.

### Request

**Headers:** `Content-Type: application/json`

**Body:**

| Field                   | Type   | Required | Description |
|-------------------------|--------|----------|-------------|
| `userId`                | string | Yes      | User ID (unique per wallet); primary key for transactional APIs |
| `bundleId`              | string | Yes      | UUID of the service bundle (from list bundles) |
| `provisioningReference` | string | No       | Customer reference (e.g. phone number, subscription ID, meter number) |

### Response 200 OK

Transaction object.

| Field                  | Type   | Description |
|------------------------|--------|-------------|
| `transactionId`        | string | UUID of the created transaction |
| `transactionType`      | string | `PROVISIONING` |
| `status`               | string | e.g. `COMPLETED` |
| `referenceId`          | string | Internal reference for this posting |
| `createdAt`            | string | ISO-8601 timestamp |
| `serviceBundleId`      | string | UUID of the bundle (same as request `bundleId`) |
| `provisioningReference`| string | Value sent in the request, if any |

### Error responses

| Status | Condition |
|--------|-----------|
| **400** | Wallet not found, bundle not found, bundle inactive, or invalid amount |
| **422** | Insufficient balance in wallet |

Error body: `{"error": "message"}`

### Example

```bash
curl -s -X POST "http://localhost:8085/api/v1/provisioning/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "bundleId": "650e8400-e29b-41d4-a716-446655440001",
    "provisioningReference": "+27123456789"
  }'
```

### Example response body

```json
{
  "transactionId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "transactionType": "PROVISIONING",
  "status": "COMPLETED",
  "referenceId": "provisioning-a1b2c3d4-...",
  "createdAt": "2025-02-19T14:30:00Z",
  "serviceBundleId": "650e8400-e29b-41d4-a716-446655440001",
  "provisioningReference": "+27123456789"
}
```

---

## Reference: Enums and types

### Provisioning service types

| Value              | Description        |
|--------------------|--------------------|
| `TV_RECHARGE`      | TV / set-top recharge |
| `PREPAID_RECHARGE` | Prepaid mobile/data  |
| `POSTPAID_RECHARGE`| Postpaid billing    |
| `SUBSCRIPTION`     | Recurring subscription (e.g. streaming) |
| `DATA_BUNDLE`      | Data bundles        |
| `UTILITY_BILL`     | Utility payments    |

### Amount filter (for list bundles)

| Value          | Description |
|----------------|-------------|
| `GREATER_THAN` | Return bundles with `fixed_amount` **&gt;** the given `amount` |
| `LESS_THAN`    | Return bundles with `fixed_amount` **&lt;** the given `amount` |

---

## Ledger behaviour

- **Double-entry:** Each provisioning transaction posts **DEBIT** to the user’s wallet account and **CREDIT** to the FEE account for the bundle’s fixed amount.
- **Transaction record:** The `transaction` row has `txn_type = 'PROVISIONING'`, `service_bundle_id`, and `provisioning_reference` set.
- **Balance:** Wallet balance is derived from ledger entries; after a successful call the wallet balance decreases by the bundle’s fixed amount.

---

## See also

- **API.md** – Full wallet ledger API (wallets, cash-in/out, transfer, balance, system accounts).
- **PROVISIONING_WORKFLOW.md** – Workflow and domain model for provisioning.
