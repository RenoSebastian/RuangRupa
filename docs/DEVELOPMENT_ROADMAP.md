# RuangAI — Development Roadmap & Implementation Milestones

## 1. Roadmap Overview & Phasing

RuangAI is developed across four phased horizons, prioritizing the shortest path to a high-converting, monetizable MVP before layering complex 3D or multi-party ecosystems:

```
[PHASE 1: THE CORE CONVERTING MVP]
Room Scan -> AI Vision -> Consultation -> Credit Wallet -> Visualization -> Product Match -> Affiliate
                                  |
                                  v
[PHASE 2: SMART INTERIOR COMMERCE]
Interactive 2D Try-On -> Realistic Product Inpainting -> Indonesian QRIS/E-Wallet -> Subscriptions
                                  |
                                  v
[PHASE 3: ADVANCED 3D & AR VISUALIZATION]
ARCore Surface Detection -> 3D Furniture Placement -> Room Dimension Estimation
                                  |
                                  v
[PHASE 4: BUSINESS ECOSYSTEM & B2B]
Retailer Partner Portal -> Interior Designer Marketplace -> White-Label API
```

---

## 2. Phase 1 — MVP Detailed Milestone Breakdown

### Milestone 1.1: Foundations & Security (Turn 1 - 2)
- Set up project metadata, strings, and Android package identity.
- Establish clean architecture folders: `domain/`, `data/`, `ui/`, `di/`.
- Implement `ai_credit_wallets` and `ai_credit_transactions` database schemas with atomic reservation logic.
- Register isolated promotion ledger logic (`promotion_claims`).

### Milestone 1.2: Room Scanning & Local Pre-Processing (Turn 3)
- Integrate Android Photo Picker (zero-permission) and Camera capture.
- Implement on-device downscaling (1280px max edge, JPEG 85) and EXIF orientation correction.
- Build Room Project creation screen with Room Type selector (`Living Room`, `Bedroom`, `Kamar Kost`, `Workspace`, `Kitchen`).

### Milestone 1.3: Room Vision Analysis & Project Memory (Turn 4)
- Connect Room Vision Service using Multimodal Gemini Flash.
- Extract structured room metadata: room type, lighting, wall/floor colors, and detected furniture items.
- Cache extraction in `room_analysis` so the vision model is never re-run unnecessarily.
- Implement Project State Memory card for conversation context.

### Milestone 1.4: AI Design Consultation & Style Taxonomy (Turn 5)
- Build Conversational Consultant UI with pre-set style chips:
  - *Japandi*, *Scandinavian*, *Minimalist*, *Industrial*, *Tropical Modern*, *Retro 80s*, *Modern Luxury*.
- Fast chat responses with Indonesian interior design persona.
- Automatic synthesis of actionable **Design Briefs** for visualization.

### Milestone 1.5: Credit Wallet UI & Reservation Pipeline (Turn 6)
- Build Credit Wallet badge and bottom sheet showing Available vs Reserved balance.
- Enforce the Credit-First Rule: lock 1 credit in reservation before dispatching generation requests.
- Implement auto-refund mechanism on simulated or actual render failures.

### Milestone 1.6: Photorealistic Visualization & Before/After Slider (Turn 7)
- Connect inpainting worker to transform the room based on the Design Brief while preserving structural anchors.
- Build custom Jetpack Compose **Before/After Split Slider** allowing touch-drag comparison between the original photo and the redesigned room.

### Milestone 1.7: Product Discovery & Hotspot Pins (Turn 8)
- Render interactive hotspot pins over redesigned furniture pieces.
- Implement hybrid product matcher mapping detected furniture to verified Indonesian marketplace catalog items with IDR prices, real store names, ratings, and dimensions.
- Build Project Shopping List summing total budget against user's spending limit.

### Milestone 1.8: Affiliate Engine & Attribution (Turn 9)
- Construct secure affiliate redirect links with Sub-IDs (`project_id`, `user_hash`, `slot_id`).
- Deep-link to Shopee/Tokopedia apps.
- Prominent advertising transparency badges (*Tautan Afiliasi*).

---

## 3. Phase 2 — Smart Interior Commerce

1. **Virtual Try-Before-You-Buy (Level 1):**
   - 2D canvas drag, pinch-to-zoom, and rotation of furniture PNG cutouts directly inside the user's room photo without invoking expensive AI inference.
2. **Virtual Try-Before-You-Buy (Level 2):**
   - AI-assisted inpainting of a specific chosen product into the designated room quadrant with realistic shadows and perspective (consumes 1 render credit).
3. **Indonesian Payment Integration:**
   - Midtrans / Xendit integration for instant QRIS, GoPay, OVO, and ShopeePay credit top-ups (IDR 9,900 for 3 credits, IDR 29,000 for 10 credits).
4. **Subscription Management:**
   - Interior Plus (IDR 49,000/month) recurring access.

---

## 4. Phase 3 & 4 — Future Horizons

- **Phase 3:** ARCore surface tracking and 3D GLTF asset placement for certified high-ticket furniture items.
- **Phase 4:** Self-service merchant portal for Indonesian furniture brands (Dekoruma, Informa, local Jepara teak artisans) to upload 3D models and promote inventory directly in AI design generation results.
