# RuangAI — System Architecture Blueprint

## 1. High-Level Architectural Overview

RuangAI adopts a **Mobile-First Client + Modular Backend Monolith + Asynchronous AI Worker Queue** architecture.

```
+-----------------------------------------------------------------------------------+
|                           CLIENT LAYER (Android Native)                           |
|  - Jetpack Compose M3 UI (Camera, Chat, Before/After Slider, Interactive Pins)     |
|  - Local Room Pre-processor (Exif, Resizer, Compressor, 2D Canvas Try-On)          |
|  - Security: Play Integrity API + Firebase App Check SDK                          |
+-----------------------------------------------------------------------------------+
                                          |
                                          | HTTPS / REST / SSE / TLS 1.3
                                          v
+-----------------------------------------------------------------------------------+
|                         GATEWAY & AUTHENTICATION LAYER                            |
|  - Firebase Auth (Google Sign-In via Credential Manager)                          |
|  - App Check Token Verification & Rate Limiting                                   |
|  - Device Promotion Fingerprint Evaluator                                         |
+-----------------------------------------------------------------------------------+
                                          |
                                          v
+-----------------------------------------------------------------------------------+
|                     RUANGAI BACKEND SERVICE (Modular Core)                        |
|                                                                                   |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | User & Wallet Svc  |  | Room Project Svc     |  | AI Orchestration Svc     |  |
|  | - Credit Ledger    |  | - Room Assets        |  | - Tool Calling Controller |  |
|  | - Atomic Reserve   |  | - Design Preferences |  | - Model Tier Router       |  |
|  | - Refund Handler   |  | - History & Metadata |  | - Prompt Context Builder  |  |
|  +--------------------+  +----------------------+  +---------------------------+  |
|                                                                                   |
|  +--------------------+  +----------------------+  +---------------------------+  |
|  | Catalog & Match Svc|  | Affiliate Engine     |  | Payment Gateway Adapter   |  |
|  | - Hybrid Search    |  | - Sub-ID Generator   |  | - Midtrans / Xendit       |  |
|  | - Visual Feature   |  | - Click Tracker      |  | - QRIS / E-Wallet         |  |
|  | - IDR Price Filter |  | - Disclosure Badges  |  | - Webhook Callback Handler|  |
|  +--------------------+  +----------------------+  +---------------------------+  |
+-----------------------------------------------------------------------------------+
           |                                                      |
           | Job Enqueue (BullMQ / Redis)                         |
           v                                                      v
+--------------------------------------+      +-------------------------------------+
|        ASYNCHRONOUS AI WORKERS       |      |          DATA & ASSET LAYER         |
|  - Vision Analysis Worker            |      |  - Primary DB: PostgreSQL /         |
|  - Image Inpainting / Gen Worker     |      |    Firestore for Realtime State     |
|  - Product Embedding Worker          |      |  - Object Storage: S3 / GCS         |
|  - Safety & Content Filter Worker    |      |  - Redis: Job Queue & Cache         |
+--------------------------------------+      |  - Vector Index: pgvector           |
                                              +-------------------------------------+
```

---

## 2. Component Breakdown

### A. Mobile Client Layer (Android)
- **Language & Framework:** 100% Kotlin with Jetpack Compose (Declarative M3).
- **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture (Domain, Data, UI).
- **Core Libraries:**
  - `androidx.compose.material3`: Design components and responsive layouts.
  - `androidx.camera` / Activity Photo Picker: Zero-permission visual media access.
  - `coil-compose`: Asynchronous image caching, memory management, and cross-fading.
  - `kotlinx-coroutines` & `StateFlow`: Reactive UI state handling.
  - `Firebase App Check` & `Play Integrity`: Device authenticity and anti-tamper verification.

### B. Gateway & Anti-Abuse Filter
- Validates that incoming requests contain valid App Check tokens.
- Checks client requests against the rate limiter (sliding window algorithm).
- Inspects device promotion hashes to prevent repeated free-credit farming upon reinstall or account recreate.

### C. Backend Modular Core
Instead of unmanageable microservices, the backend operates as a high-cohesion **Modular Monolith**:
1. **Wallet & Credit Service:** Handles credit balances, atomic reservations (`SELECT ... FOR UPDATE`), deductions, and refunds with zero-trust client rules.
2. **Room Project Service:** Organizes uploaded room photos, analysis metadata, generated design versions, and user preferences.
3. **AI Orchestration Service:** Coordinates model calls, prompt building, context window pruning, and cost recording.
4. **Product Catalog & Matching Service:** Maps recognized furniture categories to real marketplace product listings using category, color, material, and price heuristics.
5. **Affiliate Engine:** Encrypts project and user attributes into URL parameters, handles redirect endpoints, and records outbound referral telemetry.

### D. Asynchronous Worker Queue
- Heavy operations (high-resolution image inpainting, multimodal vector embedding, batch product catalog syncing) are placed on an asynchronous queue (Redis + BullMQ / Cloud PubSub).
- The client receives an immediate `job_id` and tracks status via Server-Sent Events (SSE) or polling.
- Prevents HTTP connection timeout and isolates AI service failures from client availability.

---

## 3. Communication & Data Flow

### The Zero-Trust Credit Reservation Flow
To protect unit economics and prevent fraudulent generations:

```
[Client]                [Backend / Wallet]               [Worker / AI Engine]
   |                             |                                  |
   |-- 1. Request Redesign ----->|                                  |
   |   (room_id, style, prompt)  |-- 2. Check Credit Balance        |
   |                             |-- 3. Atomic Reserve (1 Credit)   |
   |                             |      (status = 'RESERVED')       |
   |                             |-- 4. Enqueue Gen Job ----------->|
   |<-- 5. Return job_id --------|                                  |-- 6. Execute AI Gen
   |                                                                |   (Image Inpaint)
   |-- 7. Poll / Listen (job_id) ---------------------------------->|
   |                                                                |
   |                        [CASE A: SUCCESS]                       |
   |                             |                                  |
   |                             |<-- 8a. Job Completed ------------|
   |                             |-- 9a. Deduct Credit (COMPLETED)  |
   |<-- 10a. Render Result ------|                                  |
   |                                                                |
   |                        [CASE B: FAILURE]                       |
   |                             |                                  |
   |                             |<-- 8b. Job Failed (Timeout/Error)|
   |                             |-- 9b. Auto-Refund (REFUNDED)     |
   |<-- 10b. Error + Credit OK --|                                  |
```

---

## 4. Scalability, Caching & Resilience

1. **Vision Result Caching:**
   When a user uploads a room, the Room Vision analysis (room type, structural boundaries, existing items) is stored in the database. When the user tests different styles (e.g., Japandi $\rightarrow$ Industrial $\rightarrow$ Tropical), the vision model is **never re-run on the original photo**. Only the design prompt and inpainting worker run.
2. **Product Catalog Caching:**
   Common furniture query results (e.g., "Sofa 3 Dudukan Minimalis Abu-Abu") are cached in Redis with a 24-hour TTL, saving thousands of catalog search calls.
3. **Idempotency Keys:**
   All mutations (credit purchases, credit deductions, project creation) require client-generated UUID idempotency keys to eliminate double-charges on network reconnection.
