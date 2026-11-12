# RuangAI — Database Design & Schema Specification

## 1. Entity-Relationship Overview

RuangAI's data model is organized into 6 core domains:
1. **Identity & Security:** Users, Sessions, Device attestation, Anti-abuse promotion ledger.
2. **Credits & Monetization:** Wallets, Atomic reservations, Transactions, Subscriptions.
3. **Room Projects & Assets:** Rooms, Photos, Structural analysis cache, Design revisions.
4. **Product Catalog & Sourcing:** Marketplace products, Categories, Prices in IDR, Verified merchants.
5. **Recommendations & Shopping Lists:** Interactive design pins, Product matches, User shopping lists.
6. **Affiliate & Analytics:** Outbound clicks, Sub-ID attribution, Conversion records.

---

## 2. Table Schemas & Relational Definitions (PostgreSQL DDL)

```sql
-- ============================================================================
-- DOMAIN 1: IDENTITY & ANTI-ABUSE
-- ============================================================================

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(100),
    avatar_url TEXT,
    locale VARCHAR(10) DEFAULT 'id_ID',
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    deleted_at TIMESTAMPTZ -- Soft-deletion for UU PDP compliance
);

-- Device integrity and App Check registry
CREATE TABLE user_devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    device_hardware_hash VARCHAR(64) NOT NULL, -- SHA-256 of hardware integrity signal
    platform VARCHAR(20) NOT NULL, -- 'android' or 'ios'
    app_version VARCHAR(20),
    app_check_valid BOOLEAN DEFAULT TRUE,
    last_seen_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_user_device UNIQUE (user_id, device_hardware_hash)
);

-- ISOLATED PROMOTION ELIGIBILITY LEDGER
-- Note: Does NOT reference users(id) via foreign key so it survives user account deletion!
CREATE TABLE promotion_claims (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_hardware_hash VARCHAR(64) NOT NULL,
    promotion_code VARCHAR(50) NOT NULL, -- e.g., 'WELCOME_FREE_CREDITS_V1'
    claimed_at TIMESTAMPTZ DEFAULT NOW(),
    client_ip_hash VARCHAR(64),
    risk_score NUMERIC(3,2) DEFAULT 0.0,
    CONSTRAINT uq_device_promotion UNIQUE (device_hardware_hash, promotion_code)
);
CREATE INDEX idx_promotion_device ON promotion_claims(device_hardware_hash);

-- ============================================================================
-- DOMAIN 2: CREDIT WALLET & MONETIZATION
-- ============================================================================

CREATE TABLE ai_credit_wallets (
    user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    balance INT NOT NULL DEFAULT 3, -- Initial promo credits upon verified signup
    reserved_balance INT NOT NULL DEFAULT 0, -- Credits locked in active jobs
    lifetime_purchased INT NOT NULL DEFAULT 0,
    lifetime_consumed INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT chk_positive_balance CHECK (balance >= 0),
    CONSTRAINT chk_positive_reserve CHECK (reserved_balance >= 0)
);

CREATE TYPE credit_txn_type AS ENUM ('PROMOTION', 'PURCHASE', 'RESERVE', 'CAPTURE', 'REFUND');
CREATE TYPE credit_txn_status AS ENUM ('PENDING', 'COMPLETED', 'CANCELLED');

CREATE TABLE ai_credit_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_id UUID, -- Associated AI render job
    idempotency_key VARCHAR(64) UNIQUE NOT NULL,
    amount INT NOT NULL, -- Negative for deduction/reservation, positive for top-up/refund
    transaction_type credit_txn_type NOT NULL,
    status credit_txn_status NOT NULL DEFAULT 'COMPLETED',
    description VARCHAR(255),
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_credit_txn_user ON ai_credit_transactions(user_id, created_at DESC);

-- ============================================================================
-- DOMAIN 3: ROOM PROJECTS & ASSETS
-- ============================================================================

CREATE TABLE room_projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL DEFAULT 'Proyek Ruang Baru',
    room_type VARCHAR(50) NOT NULL, -- 'living_room', 'bedroom', 'kost_studio', etc.
    target_style VARCHAR(50),       -- 'japandi', 'scandinavian', 'industrial', etc.
    budget_cap_idr BIGINT,          -- User's maximum budget in Rupiah
    is_archived BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_room_projects_user ON room_projects(user_id, created_at DESC);

CREATE TABLE room_assets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES room_projects(id) ON DELETE CASCADE,
    asset_type VARCHAR(30) NOT NULL, -- 'original_photo', 'mask', 'depth_map'
    storage_url TEXT NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Cached room understanding so Vision model is only run once per room asset
CREATE TABLE room_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asset_id UUID UNIQUE NOT NULL REFERENCES room_assets(id) ON DELETE CASCADE,
    detected_room_type VARCHAR(50),
    detected_lighting JSONB,
    wall_materials JSONB,
    floor_materials JSONB,
    structural_anchors JSONB, -- Coordinates of windows, doors, AC
    existing_items JSONB,     -- List of detected furniture
    raw_vision_response JSONB,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE generated_designs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES room_projects(id) ON DELETE CASCADE,
    prompt_used TEXT NOT NULL,
    style_applied VARCHAR(50) NOT NULL,
    render_image_url TEXT NOT NULL,
    before_asset_id UUID REFERENCES room_assets(id),
    render_credit_cost INT DEFAULT 1,
    inference_duration_ms INT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_designs_project ON generated_designs(project_id, created_at DESC);

-- ============================================================================
-- DOMAIN 4: MARKETPLACE PRODUCTS & RECOMMENDATIONS
-- ============================================================================

CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(100) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL, -- 'sofa', 'coffee_table', 'rug', 'lighting'
    design_style VARCHAR(50),      -- 'japandi', 'scandinavian', 'minimalist'
    price_idr BIGINT NOT NULL,
    original_marketplace VARCHAR(50) NOT NULL, -- 'shopee', 'tokopedia', 'lazada'
    marketplace_product_id VARCHAR(100) NOT NULL,
    store_name VARCHAR(150),
    store_badge VARCHAR(50),       -- 'Official Store', 'Star+', 'Mall'
    rating NUMERIC(2,1) DEFAULT 4.8,
    review_count INT DEFAULT 0,
    main_image_url TEXT NOT NULL,
    deep_link_url TEXT NOT NULL,
    dimensions_cm JSONB,           -- {"width": 180, "depth": 85, "height": 75}
    color_hex VARCHAR(7),          -- e.g. '#D4C3A3'
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_products_category_price ON products(category, price_idr);
CREATE INDEX idx_products_style ON products(design_style);

-- Interactive hotspot pins on generated redesign images
CREATE TABLE design_product_pins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    design_id UUID NOT NULL REFERENCES generated_designs(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    pin_label VARCHAR(100) NOT NULL, -- e.g. 'Sofa 3-Seater Minimalis'
    pos_x_percent NUMERIC(5,2) NOT NULL, -- Pin coordinate on image (0-100%)
    pos_y_percent NUMERIC(5,2) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ============================================================================
-- DOMAIN 5: SHOPPING LISTS & BUDGET
-- ============================================================================

CREATE TABLE shopping_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID UNIQUE NOT NULL REFERENCES room_projects(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    total_estimated_idr BIGINT DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE shopping_list_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shopping_list_id UUID NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INT DEFAULT 1,
    is_purchased BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uq_list_product UNIQUE (shopping_list_id, product_id)
);

-- ============================================================================
-- DOMAIN 6: AFFILIATE TRACKING & ATTRIBUTION
-- ============================================================================

CREATE TABLE affiliate_clicks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    project_id UUID REFERENCES room_projects(id) ON DELETE SET NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    marketplace VARCHAR(50) NOT NULL,
    sub_id_1 VARCHAR(100), -- project_id
    sub_id_2 VARCHAR(100), -- user_id_hash
    sub_id_3 VARCHAR(100), -- pin_id or slot
    redirect_url TEXT NOT NULL,
    ip_hash VARCHAR(64),
    user_agent TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
CREATE INDEX idx_affiliate_clicks_perf ON affiliate_clicks(marketplace, created_at DESC);
```

---

## 3. Data Retention & Privacy Strategy (UU PDP Compliant)

1. **User Deletion Procedure:**
   - Triggers cascading deletion of `room_projects`, `room_assets`, `room_analysis`, `generated_designs`, and `shopping_lists`.
   - Object storage blobs for room photos are enqueued for purge within 48 hours.
   - `promotion_claims` retains only irreversible SHA-256 device hashes, ensuring malicious users cannot farm welcome credits repeatedly while respecting personal data erasure.
2. **Database Indexing Strategy:**
   - Fast user-project lookups: composite `(user_id, created_at DESC)`.
   - Fast budget-range product discovery: composite `(category, price_idr)`.
   - Idempotency lookups: unique index on `ai_credit_transactions(idempotency_key)`.
