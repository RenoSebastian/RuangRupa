# RuangAI — Product Requirements Document (PRD)

**Document Version:** 1.0.0  
**Status:** Approved for Foundation & MVP  
**Target Platform:** Android (Native Jetpack Compose) + Scalable Cloud Services  
**Primary Market:** Indonesia (IDR Currency, Indonesian Language & Culture)  

---

## 1. Target Audience & User Personas

### Persona A: Sarah (24, First-Jobber / Kost Resident)
- **Context:** Rents a 3x4m boarding room (kamar kost) in South Jakarta.
- **Budget:** IDR 1,500,000 - 3,000,000 total makeover budget.
- **Goal:** Wants her room to look cozy, organized, and aesthetic (Korean Japandi / Minimalist style) for WFH and unwinding.
- **Pain Point:** Small space, cannot repaint walls or drill fixtures, limited budget. Needs affordable multifunctional furniture and non-damaging adhesive decor from Shopee/Tokopedia.
- **RuangAI Journey:** Snaps photo of messy kost room $\rightarrow$ Selects "Kamar Kost / Studio" & "Japandi Murah" $\rightarrow$ Sets budget max IDR 2,500,000 $\rightarrow$ Receives redesigned visual $\rightarrow$ Taps shopping list with under-bed organizers, aesthetic bedsheet, warm lamp $\rightarrow$ Buys directly via Shopee links.

### Persona B: Dimas & Anya (31 & 29, Young Couple / New Homeowners)
- **Context:** Recently bought a 36/72 cluster house in BSD/Tangerang. Empty living room and master bedroom.
- **Budget:** IDR 10,000,000 - 25,000,000.
- **Goal:** Modern Scandinavian or Warm Tropical living room with a comfortable 3-seater sofa, TV credenza, and indoor planter.
- **Pain Point:** Overwhelmed by showroom prices; worried that furniture bought online will clash in color or size.
- **RuangAI Journey:** Uploads living room photo $\rightarrow$ Tests "Scandinavian" vs "Modern Luxury" with AI Consultant $\rightarrow$ Virtual Try-On of specific Tokopedia sofa in the left corner $\rightarrow$ Exports complete shopping list with estimated total expenditure.

### Persona C: Budi (42, Property Investor / Airbnb Host)
- **Context:** Furnishing a studio apartment in Bali/Bandung for short-term rental.
- **Budget:** IDR 15,000,000.
- **Goal:** High visual appeal to maximize Airbnb booking rates and reviews.
- **RuangAI Journey:** Uses high-resolution rendering credits to generate 3 alternative theme proposals (Industrial Chic, Tropical Boho, Japandi) to present to partners, then sources identical items from certified retailers.

---

## 2. User Journeys & Core Workflows

### Journey 1: Room Scan & AI Analysis (Free / Tier 1)
1. User taps "Scan Room" (Camera photo or Gallery upload).
2. App performs local pre-processing (exif orientation check, resize to max 1280px on long edge, JPEG quality 85).
3. System executes Room Vision Agent:
   - Detects room category: `Living Room`, `Bedroom`, `Kost Studio`, `Kitchen`, `Workspace`, `Dining`.
   - Identifies existing furniture: `Sofa (fabric, grey)`, `Coffee table (wood)`, `Curtains (dark blue)`.
   - Analyzes lighting: `Natural daylight from right window`, `Warm overhead light`.
   - Identifies structural anchors: `Doors`, `Windows`, `Air Conditioner position`.
4. User receives interactive room analysis summary card with detected items and suggested quick-makeover ideas.

### Journey 2: AI Interior Consultation & Style Selection
1. User enters natural conversation with RuangAI Consultant.
2. User can choose a quick preset style pill (Japandi, Scandinavian, Industrial, Minimalist, Tropical, Retro 80s, Luxury) or type custom requests (e.g., "Ubah kamarku jadi tema kamar hotel mewah tapi budget hemat").
3. AI asks clarifying questions if needed (Budget range, keep existing mattress?, preferred color palette).
4. AI generates a contextual design brief outlining changes to wall color, furniture replacements, lighting mood, and accent decor.

### Journey 3: Contextual Visualization & Before/After (Gated by Credits)
1. User reviews design brief and taps "Generate Ruang Impian (1 Credit)".
2. System checks user's Credit Wallet:
   - If balance $\ge 1$: atomically reserves 1 credit (`status = RESERVED`).
   - If balance $< 1$: opens Credit Top-Up sheet (e.g., 3 credits for IDR 9,900 via QRIS/GoPay).
3. Backend processes image inpainting/redesign while preserving architectural anchors.
4. On success: marks credit deduction as `COMPLETED` and saves rendered design asset.
5. Mobile UI presents an interactive **Before/After Split Slider** comparing the original room with the AI redesign.
6. If the rendering job fails: credit reservation is automatically refunded (`status = REFUNDED`) with an informative error message.

### Journey 4: AI Product Discovery & Shopping List
1. The rendered design contains interactive hotspot pins over key furniture pieces (Sofa, Coffee Table, Pendant Lamp, Area Rug).
2. Tapping a pin executes Product Discovery:
   - Extracts visual features (category, color hex, material, style).
   - Searches verified Indonesian marketplace catalog index (Shopee, Tokopedia, etc.).
   - Returns top 3 matching products with real title, price in IDR, thumbnail, merchant rating, and store name.
3. User can swap alternative products, adjust price filters, or add to "Daftar Belanja Ruang" (Project Shopping List).
4. Shopping list calculates real-time total budget against user's target spending.

### Journey 5: Affiliate Checkout & Attribution
1. User taps "Beli di Shopee" or "Beli di Tokopedia".
2. Backend generates an encrypted affiliate redirect link containing:
   - Merchant platform identifier.
   - Project ID & Recommendation Slot ID.
   - Anonymous user hash for conversion attribution.
3. App deep-links to the marketplace application (or opens secure in-app browser if app not installed).
4. Click event is logged in `affiliate_clicks` table for attribution analytics.

---

## 3. Functional Requirements Matrix

| ID | Feature Module | Requirement Description | Priority |
| :--- | :--- | :--- | :--- |
| **FR-01** | User Authentication | Google Sign-In via Credential Manager + Email Login. Session persistence. | **MVP (P0)** |
| **FR-02** | Room Camera & Upload | Camera capture, photo picker, on-device image compression & rotation fix. | **MVP (P0)** |
| **FR-03** | Room Vision Analysis | Automatic room type, objects, lighting, and architectural anchor detection. | **MVP (P0)** |
| **FR-04** | AI Design Consultation | Context-aware chat with project memory; pre-built style taxonomy. | **MVP (P0)** |
| **FR-05** | Credit Wallet System | Atomic credit reservation, deduction, top-up, refund, and balance check. | **MVP (P0)** |
| **FR-06** | 2D Visualization Engine | Redesigns room respecting perspective and anchors. | **MVP (P0)** |
| **FR-07** | Before/After Comparison | Interactive split-screen drag slider comparing original vs redesigned room. | **MVP (P0)** |
| **FR-08** | Product Discovery | Extracts items from redesign and matches with real marketplace items in IDR. | **MVP (P0)** |
| **FR-09** | Shopping List Manager | Aggregates matched furniture into a project budget shopping list with totals. | **MVP (P0)** |
| **FR-10** | Affiliate Link Engine | Secure sub-ID generation and deep link routing with disclosure badges. | **MVP (P0)** |
| **FR-11** | Anti-Abuse Protection | App Check attestation, Play Integrity, device promotion claim ledger. | **MVP (P0)** |
| **FR-12** | Virtual Try-On (Level 1) | 2D drag, scale, rotate placement of specific product PNG into room photo. | **Phase 2 (P1)**|
| **FR-13** | Realistic AI Inpainting | AI inpainting integrating product with realistic shadows and perspective. | **Phase 2 (P1)**|
| **FR-14** | Payment Gateway | Indonesian payment gateway (Midtrans / Xendit: QRIS, GoPay, ShopeePay). | **Phase 2 (P1)**|
| **FR-15** | 3D / AR Visualization | ARCore plane detection & 3D GLTF furniture model placement. | **Phase 3 (P2)**|
| **FR-16** | Retailer B2B Portal | Furniture store dashboard for uploading verified catalogs and tracking sales.| **Phase 4 (P3)**|

---

## 4. Non-Functional Requirements (NFRs)

- **Performance & Latency:**
  - AI Consultation chat first-token response: $< 1.5$ seconds.
  - Room Vision Analysis: $< 3$ seconds.
  - Visualization Rendering: $< 20$ seconds (with animated progress feedback).
  - App cold launch time: $< 1.8$ seconds on mid-range Android devices.
- **Security & Integrity:**
  - Zero hardcoded API keys in mobile APK.
  - Firebase App Check / Play Integrity required for credit operations.
  - Encrypted communications (HTTPS / TLS 1.3).
- **Cost Efficiency:**
  - Ordinary chat messages restricted to Tier 1 Flash models ($< \$0.0001$ per query).
  - Image generation strictly metered by credit reservations.
- **Privacy Compliance:**
  - Indonesian Personal Data Protection Law (UU No. 27 Tahun 2022 / UU PDP).
  - Explicit consent for room photo processing.
  - User-initiated account and photo deletion with complete unlinking.
- **Accessibility:**
  - Minimum touch target size $\ge 48\text{dp}$.
  - Support for high-contrast colors and dynamic font scaling.
