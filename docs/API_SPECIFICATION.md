# RuangAI — REST API Specification (OpenAPI 3.1 Aligned)

**Base URL:** `https://api.ruangai.id/v1`  
**Security Schemes:** `Bearer <FirebaseIdToken>` + Header `X-Firebase-AppCheck: <Token>`

---

## 1. Authentication & Security Endpoints

### `POST /auth/session`
Synchronizes the authenticated user and registers device attestation.
- **Headers:** `Authorization: Bearer <Token>`, `X-Firebase-AppCheck: <Token>`
- **Request Body:**
  ```json
  {
    "device_hardware_hash": "a1b2c3d4e5f6...64chars",
    "platform": "android",
    "app_version": "1.0.0"
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "user_id": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
    "email": "user@gmail.com",
    "display_name": "Dimas Pratama",
    "credit_balance": 3,
    "eligible_for_welcome_promo": false
  }
  ```

### `POST /promotions/claim`
Claims welcome promotional render credits. Protected by the isolated device promotion ledger.
- **Request Body:**
  ```json
  {
    "promotion_code": "WELCOME_3_CREDITS",
    "device_hardware_hash": "a1b2c3d4e5f6...64chars"
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "success": true,
    "credits_granted": 3,
    "new_balance": 3,
    "message": "Selamat! 3 kredit render gratis berhasil ditambahkan ke dompet Anda."
  }
  ```
- **Response `403 Forbidden`:**
  ```json
  {
    "error_code": "DEVICE_PROMOTION_ALREADY_CLAIMED",
    "message": "Perangkat ini telah menggunakan promo kredit selamat datang sebelumnya."
  }
  ```

---

## 2. Wallet & Credits Management

### `GET /wallet`
Returns user balance and transaction ledger.
- **Response `200 OK`:**
  ```json
  {
    "balance": 2,
    "reserved_balance": 1,
    "available_balance": 1,
    "currency": "CREDITS",
    "transactions": [
      {
        "id": "c1a2...",
        "amount": -1,
        "type": "RESERVE",
        "status": "COMPLETED",
        "description": "Render Ruang Tamu - Tema Japandi",
        "created_at": "2026-11-12T10:15:00Z"
      },
      {
        "id": "c1a1...",
        "amount": 3,
        "type": "PROMOTION",
        "status": "COMPLETED",
        "description": "Bonus Selamat Datang",
        "created_at": "2026-11-12T10:00:00Z"
      }
    ]
  }
  ```

---

## 3. Room Projects & Vision Analysis

### `POST /projects`
Creates a new interior design project.
- **Request Body:**
  ```json
  {
    "title": "Makeover Kamar Kost",
    "room_type": "kost_studio",
    "target_style": "japandi",
    "budget_cap_idr": 2500000
  }
  ```
- **Response `201 Created`:**
  ```json
  {
    "project_id": "4a71...",
    "title": "Makeover Kamar Kost",
    "created_at": "2026-11-12T10:05:00Z"
  }
  ```

### `POST /projects/{project_id}/photos`
Uploads a room photo, generates object storage URL, and synchronously extracts room features.
- **Content-Type:** `multipart/form-data` (`image`: binary JPEG, max 5MB)
- **Response `200 OK`:**
  ```json
  {
    "asset_id": "8f12...",
    "storage_url": "https://storage.ruangai.id/rooms/8f12.jpg",
    "analysis": {
      "detected_room_type": "kost_studio",
      "dominant_wall_color": "#F4F0E8",
      "floor_type": "ceramic_tile_white",
      "lighting": "Natural daylight from window on right side",
      "detected_items": [
        {"category": "bed", "state": "single_bed_unmade", "position": "center"},
        {"category": "desk", "state": "small_study_desk", "position": "left"}
      ],
      "makeover_suggestions": [
        "Tambahkan sprei warna beige netral untuk kesan bersih Japandi",
        "Gunakan rak ambalan kayu di atas meja belajar untuk menghemat ruang",
        "Pasang lampu meja warm white (3000K) untuk suasana malam yang hangat"
      ]
    }
  }
  ```

---

## 4. AI Consultation & Chat

### `POST /projects/{project_id}/chat`
Sends a message to the RuangAI Design Consultant. Uses Tier 1 Fast LLM with pruned context.
- **Request Body:**
  ```json
  {
    "message": "Bisa tolong rekomendasikan sofa 2-seater yang cocok buat kamar kost ini? Budget di bawah 1,5 juta."
  }
  ```
- **Response `200 OK`:**
  ```json
  {
    "reply": "Tentu! Untuk kamar kost ukuran 3x4 meter, sofa 2-seater berdesain kaki ramping (slim wooden legs) sangat ideal agar lantai tetap terlihat luas. Saya rekomendasikan sofa kain fabric warna beige atau abu muda dengan rangka kayu jati belanda yang senada dengan tema Japandi Anda.",
    "suggested_actions": [
      {"label": "Lihat Rekomendasi Sofa Shopee", "action_type": "SEARCH_PRODUCTS", "query": "sofa 2 seater japandi"},
      {"label": "Simulasikan di Pojok Kiri", "action_type": "TRY_ON", "slot": "corner_left"}
    ]
  }
  ```

---

## 5. Visualization & Asynchronous Inpainting

### `POST /projects/{project_id}/designs/generate`
Atomically reserves 1 credit and enqueues photorealistic redesign job.
- **Headers:** `Idempotency-Key: <UUID>`
- **Request Body:**
  ```json
  {
    "base_asset_id": "8f12...",
    "style": "japandi",
    "prompt_override": "Warm Japandi with minimalist oak shelving and neutral linen decor",
    "preserve_anchors": ["window", "door"]
  }
  ```
- **Response `202 Accepted`:**
  ```json
  {
    "job_id": "e839...",
    "status": "QUEUED",
    "credits_reserved": 1,
    "remaining_balance": 2,
    "estimated_seconds": 15
  }
  ```

### `GET /jobs/{job_id}`
Checks status of asynchronous generation job.
- **Response `200 OK` (Completed):**
  ```json
  {
    "job_id": "e839...",
    "status": "COMPLETED",
    "result": {
      "design_id": "d109...",
      "render_image_url": "https://storage.ruangai.id/renders/d109.jpg",
      "before_image_url": "https://storage.ruangai.id/rooms/8f12.jpg",
      "detected_hotspot_pins": [
        {"pin_id": "p1", "label": "Sofa 2 Dudukan Japandi", "pos_x": 42.5, "pos_y": 68.0, "category": "sofa"},
        {"pin_id": "p2", "label": "Meja Kopi Kayu Bulat", "pos_x": 55.0, "pos_y": 75.2, "category": "coffee_table"}
      ]
    }
  }
  ```

---

## 6. Product Discovery & Affiliate Routing

### `GET /products/search`
Queries verified marketplace catalog matching visual categories.
- **Parameters:** `category=sofa&style=japandi&max_price_idr=1500000`
- **Response `200 OK`:**
  ```json
  {
    "products": [
      {
        "id": "prod_1",
        "title": "Sofa Minimalis 2 Seater Scandinavian Japandi Cream",
        "price_idr": 1289000,
        "formatted_price": "Rp 1.289.000",
        "rating": 4.9,
        "review_count": 842,
        "store_name": "Dekoruma Official Store",
        "store_badge": "Official Store",
        "marketplace": "shopee",
        "thumbnail_url": "https://images.ruangai.id/catalog/sofa_japandi_cream.jpg",
        "dimensions": "135 x 75 x 72 cm",
        "affiliate_click_url": "https://api.ruangai.id/v1/affiliate/redirect?pid=prod_1&proj_id=4a71"
      }
    ]
  }
  ```

### `GET /affiliate/redirect`
Generates tracked affiliate referral with sub-IDs and redirects to Shopee/Tokopedia.
- **HTTP Response:** `302 Found` $\rightarrow$ Target Marketplace Deep Link.
