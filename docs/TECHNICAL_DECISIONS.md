# RuangAI — Architectural Decision Records (ADRs)

This document explains the rationale and trade-offs behind the primary engineering decisions for RuangAI.

---

## ADR-001: Centralized Pragmatic Orchestrator vs. Autonomous Multi-Agent Mesh

- **Status:** Accepted
- **Context:** The product vision describes 7 logical agent responsibilities (Room Vision, Design Consultant, Visualization, Product Discovery, Budget Optimizer, Affiliate, Project Memory).
- **Decision:** Implement **one central orchestrator** that delegates to dedicated stateless services rather than creating 7 autonomous LLM agents that communicate peer-to-peer.
- **Rationale:**
  - Autonomous multi-agent conversations introduce non-deterministic loops, catastrophic token bloat, high latencies (>20 seconds for basic chat), and complex debugging.
  - In our architecture, the Central Orchestrator classifies user intent and dispatches to deterministic services (e.g., Product Match is a fast SQL/vector query, not an LLM chat).
  - This reduces AI inference costs by up to 80% while improving responsiveness to under 1.5 seconds for conversational interactions.

---

## ADR-002: Build Credit Management Before Image Generation

- **Status:** Accepted
- **Context:** Developers often prioritize image generation first because it creates flashy demos.
- **Decision:** The Credit Wallet, Reservation, Atomic Deduction, and Auto-Refund systems must be developed and validated before any image generation pipeline is wired up.
- **Rationale:**
  - High-resolution diffusion inpainting costs ~Rp 500 per image.
  - Exposing an inpainting endpoint without an atomic reservation ledger leaves the system vulnerable to rapid balance exploitation, concurrency race conditions, and uncontrolled provider debt.
  - Building the credit ledger first guarantees that every rendered pixel is financially accounted for.

---

## ADR-003: Isolated Promotion Eligibility Ledger Decoupled from User Table

- **Status:** Accepted
- **Context:** Users can delete their accounts and re-register using a new email to farm welcome promotional credits.
- **Decision:** Store promotion claim records in an isolated table (`promotion_claims`) keyed by salted SHA-256 device hardware hashes, decoupled from the `users` table.
- **Rationale:**
  - Complying with UU PDP requires that personal data (email, name, room photos) be deleted upon user request.
  - However, retaining an irreversible pseudonymous hash of a device's promotion eligibility does not constitute personal data under UU PDP fraud prevention exemptions.
  - When the user recreates their account on the same phone, the system recognizes the hardware hash and withholds the welcome promotional credit while allowing standard usage.

---

## ADR-004: Hybrid Deterministic & Vector Product Matching

- **Status:** Accepted
- **Context:** Matching an item depicted in an AI-generated room with real marketplace inventory.
- **Decision:** Combine deterministic category, price range, and color filtering with visual embedding cosine similarity.
- **Rationale:**
  - Pure vector search often hallucinates completely wrong items (e.g., matching a beige sofa with a beige curtain or an expensive IDR 15,000,000 designer item when the user's budget is IDR 2,000,000).
  - Deterministic filtering guarantees: category = `sofa`, price $\le \text{budget}$, minimum merchant rating $\ge 4.5$.
  - Vector similarity then ranks the filtered items by aesthetic style and visual harmony.

---

## ADR-005: Client-Side Image Pre-Processing & Compression

- **Status:** Accepted
- **Context:** Modern mobile phones shoot 12MP to 50MP images (5MB – 15MB).
- **Decision:** Compress and downscale room photos on the mobile device to a maximum dimension of 1280px with JPEG quality 85 (~350KB) before uploading.
- **Rationale:**
  - Saves 95% of upload bandwidth, especially critical on Indonesian mobile networks (4G/LTE in sub-urban areas).
  - Multimodal vision models (Gemini Flash Vision) operate optimally at ~1024-1280px resolution; higher resolutions provide negligible geometric gains while consuming far more input tokens.
