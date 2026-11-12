# RuangAI — Security Architecture & Data Privacy (UU PDP Compliance)

## 1. Regulatory Context (Indonesian PDP Law - UU No. 27 Tahun 2022)

Because RuangAI handles photos of users' private domestic spaces (bedrooms, living rooms, homes), trust and privacy are paramount. The system is designed to adhere strictly to the Indonesian Personal Data Protection Law (UU PDP):

1. **Principle of Specific Purpose (Pasal 27):** Room images and visual features are processed solely for generating interior design recommendations and product matches. They are never sold to data brokers or used for unrelated commercial tracking.
2. **Right to Erasure / Deletion (Pasal 43):** Users possess the right to permanently purge their room photos, design generations, chat transcripts, and personal profiles at any time through in-app self-service controls.
3. **Security Measures (Pasal 35):** Technical and organizational safeguards must prevent unauthorized access, leaks, or tampering with user room photographs.

---

## 2. Zero-Trust Client Architecture & Secrets Management

1. **No Embedded Client Secrets:**
   - The Android client binary contains **zero third-party AI keys**, marketplace developer secrets, or database master credentials.
   - All external API calls (Gemini, Imagen, Shopee Affiliate, Tokopedia) are routed through authenticated backend micro-gateways.
2. **Android Network Security Config:**
   - Cleartext HTTP traffic is strictly disabled (`cleartextTrafficPermitted="false"`).
   - TLS 1.3 encryption is enforced for all REST, WebSocket, and asset storage communication.
3. **Short-Lived Signed URLs for Room Images:**
   - Room photos uploaded to Cloud Storage buckets are not publicly readable.
   - The client uploads and reads assets via **pre-signed URLs with a 15-minute expiration window**.

---

## 3. Account & Room Data Deletion Procedure

When a user initiates account deletion in Settings:
1. **Immediate Revocation:** Auth session is invalidated immediately.
2. **Cascading Asset Purge:**
   - SQL triggers delete references in `room_projects`, `room_assets`, `room_analysis`, `generated_designs`, and `shopping_lists`.
   - A background worker purges the user's private folder in Object Storage (`gs://ruangai-assets/{user_id}/*`).
3. **Audit Log Retention:**
   - Financial ledger entries (`ai_credit_transactions` and payment records) are retained in an anonymized archive for 5 years as required by Indonesian tax and financial compliance laws.
