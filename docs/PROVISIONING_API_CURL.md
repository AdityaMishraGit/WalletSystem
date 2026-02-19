# Provisioning API – cURL Examples

Base URL: **http://localhost:8085/api/v1**  
All request bodies use `Content-Type: application/json`.

---

## 1. Provisioning services

### Create provisioning service

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

**Type** must be one of: `TV_RECHARGE`, `PREPAID_RECHARGE`, `POSTPAID_RECHARGE`, `SUBSCRIPTION`, `DATA_BUNDLE`, `UTILITY_BILL`.  
**Status** optional; defaults to `ACTIVE` if omitted.

---

### Update provisioning service

```bash
# Replace {id} with the provisioning service UUID
curl -s -X PUT "http://localhost:8085/api/v1/provisioning-services/{id}" \
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

Example with a real UUID:

```bash
curl -s -X PUT "http://localhost:8085/api/v1/provisioning-services/550e8400-e29b-41d4-a716-446655440001" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "TV_RECHARGE",
    "name": "DSTv",
    "code": "DSTV",
    "subcategory": "entertainment",
    "description": "",
    "status": "ACTIVE"
  }'
```

---

### List provisioning services

```bash
# All active services
curl -s "http://localhost:8085/api/v1/provisioning-services"

# Filter by service type
curl -s "http://localhost:8085/api/v1/provisioning-services?serviceType=TV_RECHARGE"
curl -s "http://localhost:8085/api/v1/provisioning-services?serviceType=SUBSCRIPTION"

# Filter by subcategory
curl -s "http://localhost:8085/api/v1/provisioning-services?subcategory=entertainment"

# Both filters
curl -s "http://localhost:8085/api/v1/provisioning-services?serviceType=TV_RECHARGE&subcategory=entertainment"
```

---

## 2. Service bundles

### Create service bundle

Attach a new bundle to a provisioning service by setting `provisioningServiceId` to that service’s UUID (from create or list).

```bash
# Replace {provisioning-service-uuid} with the service id from POST/GET provisioning-services
curl -s -X POST "http://localhost:8085/api/v1/provisioning-services/bundles" \
  -H "Content-Type: application/json" \
  -d '{
    "provisioningServiceId": "{provisioning-service-uuid}",
    "name": "DSTv Compact",
    "code": "DSTV-COMPACT",
    "fixedAmount": 50.00,
    "subcategory": "entertainment",
    "validityDays": 30,
    "description": "Compact package",
    "status": "ACTIVE"
  }'
```

Example with a real service UUID:

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

### Update service bundle

```bash
# Replace {bundle-id} with the service bundle UUID
curl -s -X PUT "http://localhost:8085/api/v1/provisioning-services/bundles/{bundle-id}" \
  -H "Content-Type: application/json" \
  -d '{
    "provisioningServiceId": "550e8400-e29b-41d4-a716-446655440001",
    "name": "DSTv Compact Plus",
    "code": "DSTV-COMPACT",
    "fixedAmount": 55.00,
    "subcategory": "entertainment",
    "validityDays": 30,
    "description": "Compact package",
    "status": "ACTIVE"
  }'
```

---

### List service bundles

```bash
# All bundles
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles"

# Bundles for one service (use service id from list provisioning-services)
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceId=550e8400-e29b-41d4-a716-446655440001"

# Bundles with fixed amount > 50
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?amount=50&amountFilter=GREATER_THAN"

# Bundles with fixed amount < 100
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?amount=100&amountFilter=LESS_THAN"

# By service type
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceType=SUBSCRIPTION"

# Combined: subscription bundles under 20
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceType=SUBSCRIPTION&amount=20&amountFilter=LESS_THAN"
```

**amountFilter** values: `GREATER_THAN`, `LESS_THAN`.

---

## 3. Execute provisioning (pay for bundle)

Debit a wallet and credit the FEE account for the chosen bundle. Use `userId` (unique per wallet) and `bundleId` (from list/create bundles).

```bash
curl -s -X POST "http://localhost:8085/api/v1/provisioning/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-123",
    "bundleId": "650e8400-e29b-41d4-a716-446655440001",
    "provisioningReference": "+27123456789"
  }'
```

- **userId**: User ID (unique per wallet); primary key for transactional APIs.  
- **bundleId**: UUID of the service bundle (from list or create bundles).  
- **provisioningReference**: Optional (e.g. phone number, subscription id).

---

## Quick flow (copy-paste)

```bash
# 1. Create a provisioning service
curl -s -X POST "http://localhost:8085/api/v1/provisioning-services" \
  -H "Content-Type: application/json" \
  -d '{"type":"SUBSCRIPTION","name":"Netflix","code":"NETFLIX","status":"ACTIVE"}'

# 2. Copy "id" from response, then create a bundle (replace SERVICE_ID below)
SERVICE_ID="<paste-service-id-here>"
curl -s -X POST "http://localhost:8085/api/v1/provisioning-services/bundles" \
  -H "Content-Type: application/json" \
  -d "{\"provisioningServiceId\":\"$SERVICE_ID\",\"name\":\"Netflix Basic\",\"code\":\"NETFLIX-BASIC\",\"fixedAmount\":9.99,\"status\":\"ACTIVE\"}"

# 3. List bundles (optional)
curl -s "http://localhost:8085/api/v1/provisioning-services/bundles?serviceId=$SERVICE_ID"

# 4. Execute provisioning (replace USER_ID with the user's id, BUNDLE_ID with bundle UUID)
curl -s -X POST "http://localhost:8085/api/v1/provisioning/execute" \
  -H "Content-Type: application/json" \
  -d '{"userId":"USER_ID","bundleId":"BUNDLE_ID","provisioningReference":"user@example.com"}'
```

---

## Error responses

- **400** – Bad request (e.g. invalid body, duplicate code, service/bundle not found).  
- **422** – Business rule (e.g. insufficient balance on execute).  

Response body shape: `{"error": "message"}`.
