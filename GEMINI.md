# RUANGAI — PERMANENT ENGINEERING RULES & CTO DIRECTIVES

This document defines the non-negotiable architectural, security, cost, and operational rules for the RuangAI platform. All engineers and AI development agents must strictly observe these rules across every development turn.

---

## 1. Core Engineering Tenets

1. **Architecture Before Implementation:**
   Never write production code without an approved architectural plan or modifying established design documents first. Any change in data models, state flows, or external integrations must update `/docs/` accordingly.

2. **No Mock Fallbacks for Core Differentiators:**
   The product value is real visual commerce. Product matching, affiliate tracking, and room analysis must use real schemas, realistic catalogs, and verifiable APIs or real database entries—never fake dummy strings or random generators disguised as AI.

3. **Single Orchestrator Over Multi-Agent Mesh (MVP Rule):**
   Do not build an overly complex 7-agent independent mesh for the MVP. Use **one central orchestrator** with specialized, decoupled services (Room Vision, Design Consultation, Product Discovery, Budget Optimizer, Visualization). This keeps latency low, prevents infinite tool-calling loops, and minimizes token waste.

---

## 2. AI Cost & Credit Management (Strict Priority)

1. **Credit Management Precedes Image Generation:**
   Under no circumstances shall image generation or editing endpoints be implemented without the Credit Wallet, Reservation, Atomic Deduction, and Refund-on-Failure systems already in place.
2. **Model Tiering by Computational Cost:**
   - **Tier 1 (Lightweight / Flash):** Used for conversational chat, style taxonomy, basic suggestions, and metadata parsing.
   - **Tier 2 (Multimodal Vision):** Used only when a room photo or furniture image needs geometric/visual feature extraction. Cached aggressively per room asset.
   - **Tier 3 (High-Fidelity Generation / Inpainting):** Reserved strictly for explicit user-requested visualization jobs; strictly gated by user credit reservations.
3. **Credit Lifecycle Idempotency:**
   Every credit-consuming operation requires a unique `idempotency_key` and job reservation. If the inference fails, the reserved credit is automatically returned to the user's available balance.
4. **Never Expose AI Keys to Mobile Clients:**
   Mobile apps interact exclusively with backend endpoints or securely authenticated Firebase AI Logic via App Check attestation. No API keys shall ever be embedded in client-side code.

---

## 3. Commerce & Affiliate Integrity

1. **No Unauthorized Web Scraping:**
   All marketplace integrations (Shopee, Tokopedia / TikTok Shop, Lazada, Blibli, etc.) must adhere to official affiliate APIs, developer partner programs, or authorized product data feeds. If an official API is unavailable, implement a generic adapter interface with verified sample catalogs until authorized credentials are provided.
2. **Transparent Affiliate Disclosure:**
   In compliance with advertising regulations, all product recommendations with affiliate monetization must feature a transparent disclosure badge (e.g., "Afiliasi / Komisi diperoleh").
3. **Sub-ID Attribution Tracking:**
   Every generated outbound affiliate link must incorporate project ID, user hash, and recommendation slot ID into Sub-IDs (`sub_id1`, `sub_id2`, `sub_id3`) for granular conversion tracking.

---

## 4. Anti-Abuse & Data Privacy (UU PDP Compliance)

1. **Account Deletion Is Not Promotion Reset:**
   Device integrity fingerprints, promotion claim hashes, and transaction audit trails reside in an isolated **Promotion Eligibility Ledger**. Deleting a user profile removes personal identifiers and room assets according to Indonesian Personal Data Protection Law (UU PDP), but preserves irreversible cryptographic hashes to prevent re-claiming promotional free credits.
2. **Device Attestation:**
   Android builds must validate App Check and Google Play Integrity before granting promotional credits.
3. **Never Trust Client State:**
   Credit balances, subscription statuses, product prices, and promotional eligibility must always be verified and computed server-side.

---

## 5. Mobile & Jetpack Compose Standards

1. **Material Design 3 (M3):**
   Strictly adhere to M3 color tokens, dynamic theming, generous 8dp-grid spacing, and 48dp touch targets.
2. **Edge-to-Edge & Accessibility:**
   Mandatory `enableEdgeToEdge()` with proper `WindowInsets` handling.
3. **Navigation & State Management:**
   Use standard Jetpack `ViewModel`, `StateFlow`, and type-safe navigation.
4. **Offline Resilience & Error Handling:**
   Provide clear UI states: `Loading`, `Empty`, `Success`, `Error`, and `CreditDepleted`.
