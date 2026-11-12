# RuangAI — Anti-Abuse & Promotional Credit Protection System

## 1. The Sybil & Credit Exploitation Threat Model

In consumer generative AI apps offering free promotional credits (e.g., "Dapatkan 3 Kredit Render Gratis Saat Mendaftar"), malicious actors exploit the system via the following loop:
1. Register with disposable Gmail or temp mail.
2. Claim 3 free rendering credits.
3. Generate 3 room makeovers.
4. Delete the account to purge database records.
5. Register a fresh account on the same smartphone.
6. Re-claim 3 free credits indefinitely.

This abuse depletes infrastructure budgets and skews product analytics.

---

## 2. Multi-Layered Defense Architecture

RuangAI deploys 5 defense layers without creating unnecessary friction for legitimate users:

```
+-----------------------------------------------------------------------------------+
| LAYER 1: APP INTEGRITY & ATTESTATION                                              |
| - Firebase App Check (reCAPTCHA Enterprise / Play Integrity)                      |
| - Blocks modified APKs, emulators, bot scripts, and rooted devices                |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| LAYER 2: ISOLATED PROMOTION ELIGIBILITY LEDGER                                    |
| - Table: promotion_claims(device_hardware_hash, promotion_code)                   |
| - Independent from users table; NOT deleted when user account is deleted           |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
| LAYER 3: MULTI-SIGNAL RISK ENGINE                                                 |
| - Device Velocity (claims per device in 30 days)                                  |
| - IP Subnet Velocity & Cloud Proxy Detection                                      |
| - Google Sign-In Account Age & Authenticity                                       |
+-----------------------------------------------------------------------------------+
                                          |
                   +----------------------+----------------------+
                   |                                             |
            [Score < 0.4: Low Risk]                    [Score >= 0.4: Elevated]
                   |                                             |
                   v                                             v
       (Instant Credit Grant)                        (Step-Up Verification:
                                                      WhatsApp OTP / CAPTCHA)
```

---

## 3. The Isolated Promotion Ledger (Decoupled from User Identity)

To comply with Indonesian Personal Data Protection Law (UU PDP) while eliminating free-credit re-claiming:
1. The user profile, name, email, and room photos are completely erased upon user deletion.
2. The `promotion_claims` record contains **no personal information**:
   - `device_hardware_hash`: SHA-256 HMAC of hardware identifiers salted with a private server secret.
   - `promotion_code`: e.g., `WELCOME_3_CREDITS`.
   - `claimed_at`: Timestamp.
3. Because the hash is mathematically irreversible and contains zero PII, keeping it in an isolated audit ledger complies fully with UU PDP legitimate interest exemptions for fraud prevention.
4. When a newly registered account requests welcome credits, the server checks:
   ```sql
   SELECT EXISTS(
     SELECT 1 FROM promotion_claims 
     WHERE device_hardware_hash = $1 
     AND promotion_code = 'WELCOME_3_CREDITS'
   );
   ```
   If `true`, the user is welcomed as a returning device and given access to free tier chat and room scanning, but the 3 free render credits are withheld.

---

## 4. Rate Limiting & Concurrency Throttling

| Operation | Sliding Window Limit | Action upon Exceeding |
| :--- | :--- | :--- |
| **Account Registration** | Max 2 accounts per device per 30 days | Block registration; prompt to log in with existing account. |
| **Welcome Promo Claim** | Exactly 1 claim per physical device lifetime | Return friendly notice: "Promo perangkat telah digunakan". |
| **Room Vision Scans** | Max 10 uploads per hour per user | HTTP 429: "Batas pemindaian tercapai, coba lagi dalam 1 jam". |
| **Concurrent Inpainting** | Max 1 active inpainting job per user | Block secondary job until first job completes or fails. |
| **Chat Consultant** | Max 30 messages per hour (Free Tier) | Suggest upgrading to Interior Plus or waiting. |

---

## 5. Legitimate Account Deletion & Privacy Balance

When a user taps "Hapus Akun Saya" (Delete My Account):
- `users`: Soft-deleted then hard-deleted after 7 days grace period.
- `room_assets` & `generated_designs`: Physical images deleted from S3/Cloud Storage buckets.
- `ai_credit_wallets`: Closed and zeroed.
- `promotion_claims`: Pseudonymous device hash retained exclusively for promotional idempotency.
