# RuangAI — AI Orchestration & Service Architecture

## 1. Architectural Philosophy: Pragmatic Orchestrator vs. Multi-Agent Mesh

A common failure mode in generative AI startups is premature multi-agent complexity—spawning 6 or 7 autonomous LLM agents that chat with one another, each consuming hundreds of tokens per turn, introducing non-deterministic loops, compounding latency, and inflating operational cost by 500% to 1,000%.

RuangAI adopts a **Pragmatic Orchestrator + Deterministic Services** model for Phase 1 & 2:

```
                                 [USER REQUEST]
                                        |
                                        v
                       +----------------------------------+
                       |     CENTRAL AI ORCHESTRATOR      |
                       |  - Intent Classifier             |
                       |  - Model Tier Selector           |
                       |  - Token & Budget Limiter        |
                       |  - Tool & Service Dispatcher     |
                       +----------------------------------+
                                        |
      +-----------------+---------------+----------------+----------------+
      |                 |                                |                |
      v                 v                                v                v
[Room Vision Svc] [Design Consultant Svc]       [Product Match Svc] [Visual Inpaint Svc]
(Multimodal Flash)  (Fast Conversational)       (Deterministic DB)  (Diffusion Inpaint)
  - Room type        - Style taxonomy             - Category/Color    - Gated by Credit
  - Objects          - Color palette              - Price range IDR   - Background worker
  - Spatial bounds   - Project memory             - Shopee/Tokopedia  - Failure refund
```

### Why This Is Superior:
1. **Zero Agent-to-Agent Loop Risk:** The central orchestrator decides the single tool or service needed for the user's intent. Services return structured JSON directly to the orchestrator.
2. **Predictable Latency:** A user question like "Can you make this Japandi style?" takes 1 fast LLM call (~800ms), rather than 4 cascaded agents taking 15 seconds.
3. **Strict Cost Control:** Each service has fixed token input/output budgets and model tiers. Expensive vision and inpainting models are only invoked upon explicit trigger.

---

## 2. Specialized Services & Responsibilities

### Service 1: Room Vision Service (`room-vision`)
- **Trigger:** Only executed once upon room photo upload, or when a user explicitly uploads an additional room angle.
- **Model:** Multimodal Vision Model (e.g., Gemini 2.5/3.0 Flash Vision).
- **Input:** Compressed room photo (1280px max edge) + System Prompt.
- **Output (Strict JSON Schema):**
  ```json
  {
    "room_type": "living_room | bedroom | kost_studio | kitchen | workspace",
    "lighting": {
      "natural_source": "window_right",
      "time_of_day": "daylight",
      "intensity": "moderate"
    },
    "walls": {
      "dominant_color": "#F3EFE0",
      "material": "plaster_painted",
      "condition": "good"
    },
    "flooring": {
      "material": "ceramic_tile_white",
      "color": "#FFFFFF"
    },
    "detected_items": [
      {
        "category": "sofa",
        "current_state": "fabric_grey_worn",
        "position": "center_left",
        "recommendation": "replace"
      },
      {
        "category": "coffee_table",
        "current_state": "laminate_wood_small",
        "position": "center",
        "recommendation": "upgrade"
      }
    ],
    "structural_anchors": ["door_left", "window_right", "ac_unit_top_wall"]
  }
  ```
- **Caching:** Output is saved to `room_analysis` table. Subsequent style variations reuse this cached analysis.

### Service 2: Interior Design Consultant (`design-consultant`)
- **Trigger:** User sends a chat message or selects a design style preset.
- **Model:** Fast Text LLM (Tier 1 Flash).
- **Input:** User message + Pruned project memory (last 6 conversation turns + room analysis summary).
- **System Instructions:** Acts as an expert Indonesian interior consultant ("Arsitek & Desainer RuangAI"). Knows popular styles (Japandi, Scandinavian, Industrial, Minimalist, Tropical Modern, Korean Cozy Kost). Considers Indonesian climatic realities (humidity, ventilation) and realistic budgets (IDR).
- **Capabilities:**
  - Suggests color palettes with Hex codes.
  - Recommends space-saving furniture arrangements for compact rooms.
  - Produces structured **Design Briefs** ready for the visualization worker.

### Service 3: Visual Inpainting & Redesign Service (`visual-inpaint`)
- **Trigger:** User taps "Generate Ruang Impian" (explicit credit consumption).
- **Execution:** Dispatched to background worker queue.
- **Input:**
  - Original room image.
  - Structural anchor mask (protecting doors, windows, structural boundaries).
  - Design style prompt synthesized from the Design Brief (e.g., "Warm Japandi living room, light oakwood coffee table, beige linen sofa, warm minimalist lighting, architectural preservation").
- **Output:** High-resolution photorealistic transformed image URL.

### Service 4: Product Discovery & Matching Service (`product-match`)
- **Trigger:** Generated visualization completed, or user taps an item hotspot pin.
- **Engine:** Hybrid Deterministic & Vector Matcher.
  1. **Visual Feature Extraction:** Extracts item bounding boxes and descriptors (`category: "sofa_3_seater"`, `style: "japandi"`, `color: "cream_beige"`, `material: "linen_wood"`).
  2. **Catalog Index Filtering:** Queries verified Indonesian catalog database using strict attribute filters (Price $\le \text{user budget}$, in-stock, minimum rating $\ge 4.5$).
  3. **Ranking:** Sorts by visual relevance score, sales volume, and verified merchant tier (Shopee Mall / Star Seller, Tokopedia Official Store).
- **Output:** Top 3 real purchasable products with title, price in IDR, ratings, thumbnail, and deep-link affiliate URL.

---

## 3. Project Context & Conversation Memory Management

To prevent context bloat and runaway token costs:
1. **Summary-plus-Sliding-Window:** The conversation history retains only the **last 6 messages** verbatim.
2. **Persistent Project State Card:** An immutable state object is injected into the system prompt:
   ```
   [PROJECT CONTEXT]
   Room Type: Kamar Kost (3x4m)
   Target Style: Japandi Minimalist
   Budget Cap: IDR 2,500,000
   Kept Items: Existing Single Bed Frame
   Disliked Colors: Bright Neon, Dark Industrial
   ```
3. This guarantees constant token overhead (~1,200 tokens total) regardless of whether the user chats for 5 turns or 50 turns.
