# Postman collections

## Onboard 50 Users

**File:** `Onboard-50-Users.postman_collection.json`

Creates **50 wallets** (user onboarding) with unique alphanumeric user IDs: `usr_01`, `usr_02`, … `usr_50`. Each request calls **POST** `/api/v1/wallets` with `userId` and `currency` (USD).

**How to use:** Import the collection, set `baseUrl` if needed (default `http://localhost:8080/api/v1`), then run the collection or the folder. All 50 requests run in sequence. If a userId already exists, the API returns 400 (duplicate).

---

## Provisioning Transactions (100 per user × 50 users)

**File:** `Provisioning-Transactions-100-Per-User.postman_collection.json`

Runs **5000 provisioning transactions**: **100 transactions per user** for each of the 50 users (`usr_01`–`usr_50`), using **different provisioning services/bundles** (DSTv, Airtel, Netflix, DataCo, UtilityCo) at random.

**Prerequisites**

1. **Onboard 50 Users** collection has been run (50 wallets exist).
2. **Provisioning Services & Bundles** collection has been run (services and 20 bundles exist).
3. Each user has **wallet balance** (e.g. run cash-in for `usr_01`–`usr_50` so they can pay for bundles).

**How to run**

1. **Import** the collection and set `baseUrl` if needed.
2. **Run folder 1 once:** “1. Setup - Get bundle IDs” → run the **List bundles** request once. This saves all bundle IDs to the collection variable `bundleIds`.
3. **Run folder 2 with 5000 iterations:** Open **Collection Runner**, select the collection, choose **only** the folder **“2. Execute provisioning (100 per user × 50 users)”**, set **Iterations** to **5000**, then Run. Each iteration runs one **Execute provisioning** request (userId round-robins usr_01…usr_50, bundleId is random from the loaded bundles).

**Result:** 5000 transactions = 100 per user, spread across the onboarded bundles and services. If you see 422 (e.g. insufficient balance), ensure users have been credited via cash-in.

---

## Provisioning Services & Bundles

**File:** `Provisioning-Services-and-Bundles.postman_collection.json`

Creates **5 provisioning services** and **20 bundles** across multiple service types for testing and demo data.

### Contents

1. **Create Provisioning Services (5)**
   - DSTv (TV_RECHARGE)
   - Airtel Prepaid (PREPAID_RECHARGE)
   - Netflix (SUBSCRIPTION)
   - DataCo (DATA_BUNDLE)
   - UtilityCo (UTILITY_BILL)

   Each request has a **Test** script that saves the returned `id` into a collection variable (`serviceId_dstv`, `serviceId_airtel`, etc.) for use in bundle requests.

2. **Create Bundles (20)**
   - **DSTv:** Compact (50), Compact Plus (75), Premium (120), Family (95)
   - **Airtel:** 10, 25, 50, 100
   - **Netflix:** Basic (9.99), Standard (15.99), Premium (19.99)
   - **DataCo:** 1GB (5), 5GB (15), 10GB (25), 20GB (40), 50GB (80)
   - **UtilityCo:** Electricity 50, Electricity 100, Water 30

3. **List (verify)**  
   GET services and GET bundles to confirm data.

### How to use

1. **Import** the collection in Postman (File → Import → select the JSON file).
2. **Base URL:** Collection variable `baseUrl` is set to `http://localhost:8080/api/v1`. Change it if your API runs elsewhere.
3. **Run order:** Run the full collection (or run folder **1** then folder **2**). Services must be created first so their IDs are stored for bundle creation.
4. **Duplicate codes:** If you run the collection more than once, create-service or create-bundle requests may return 400 (duplicate code). Use a fresh DB or different codes for repeated runs.

### One-off run

- **Runner:** Collection → Run → Run Provisioning Services & Bundles. All requests run in sequence; variables set in folder 1 are used in folder 2.
