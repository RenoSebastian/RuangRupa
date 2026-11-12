# RuangAI — AI Cost Controller & Credit Management Engine

## 1. The Cost Challenge in Generative Commerce

Uncontrolled AI consumption is the number one cause of insolvency in generative AI startups. Typical pitfalls include:
- Running heavy multimodal models on every conversational chat message.
- Re-generating full 1024x1024 room images for minor textual questions.
- Uncached room analysis causing repeated visual feature extractions.
- Lack of atomic credit reservations allowing users to spam concurrent requests on zero balance.

RuangAI treats **Cost Management as a First-Class System Architecture Component**, governed by the **AI Cost Controller**.

---

## 2. Multi-Tier Model Routing

| Workload Tier | Typical Operations | Model Selection | Estimated Cost per Request (USD) | Estimated Cost in IDR (USD 1 = Rp 16.000) | User Fee Mechanism |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Tier 1 (Conversation)** | Style advice, budget breakdown, shopping list queries, clarification questions. | Gemini 2.5 / 3.0 Flash (Text-only) | ~\$0.0001 (1K tokens in/out) | **Rp 1,6 per chat** | **Free tier allowance** (up to 30 chats/day) |
| **Tier 2 (Vision Extraction)** | Room type classification, object detection, lighting and material analysis. | Gemini 2.5 / 3.0 Flash Vision (Multimodal) | ~\$0.0008 (1 image + 500 tokens out) | **Rp 12,8 per scan** | **Free tier allowance** (cached per photo upload) |
| **Tier 3 (Photorealistic Redesign)** | Diffusion-based contextual room inpainting & style transformation. | Imagen 3 / SDXL Inpainting Worker | ~\$0.025 – \$0.040 per render | **Rp 400 – Rp 640 per render** | **Gated by Credits** (1 Credit = Rp 2.360 – Rp 3.300 retail) |

### Gross Margin on Render Credits:
- Retail Price: **IDR 9,900 for 3 credits** $\approx$ **IDR 3,300 per render credit**.
- Provider Cost: **~IDR 500 per high-resolution render**.
- **Gross Profit Margin: ~84.8%** (Provides healthy buffer for cloud hosting, payment fees, and free tier chat allowances).

---

## 3. The Atomic Credit Reservation Lifecycle

To eliminate race conditions and negative balances when users double-tap or send concurrent requests:

```
[Available Balance >= 1] 
          |
          v
[1. Atomic Reservation]  ---> balance = balance - 1, reserved = reserved + 1
          |                   (Executed inside SQL TRANSACTION with row lock)
          v
[2. AI Job Execution]
          |
   +------+------+
   |             |
(Success)     (Failure / Timeout)
   |             |
   v             v
[3a. Final Capture]          [3b. Auto-Refund]
- reserved = reserved - 1    - reserved = reserved - 1
- lifetime_consumed += 1     - balance = balance + 1
- txn_status = COMPLETED     - txn_status = REFUNDED
```

### SQL Atomic Transaction Implementation:
```sql
-- Atomic Credit Reservation
BEGIN;
SELECT balance, reserved_balance 
FROM ai_credit_wallets 
WHERE user_id = $1 
FOR UPDATE;

-- Validate sufficient available balance
-- If balance < 1, rollback and throw INSUFFICIENT_CREDITS

UPDATE ai_credit_wallets 
SET balance = balance - 1, 
    reserved_balance = reserved_balance + 1,
    updated_at = NOW()
WHERE user_id = $1;

INSERT INTO ai_credit_transactions 
(id, user_id, job_id, idempotency_key, amount, transaction_type, status, description)
VALUES (gen_random_uuid(), $1, $2, $3, -1, 'RESERVE', 'PENDING', 'Reservasi kredit render');

COMMIT;
```

---

## 4. Upstream Optimization & Caching Rules

1. **Client-Side Image Optimization:**
   - Raw 12MP/48MP smartphone photos (8MB - 15MB) are resized on-device to a maximum edge of **1280px** with JPEG quality 85 (~350KB).
   - This saves over 95% bandwidth and reduces multimodal token processing costs.
2. **Analysis Result Caching:**
   - The output of the Room Vision analysis is saved to `room_analysis`.
   - If the user generates 5 different style variations (Japandi, Scandinavian, Modern Luxury), the vision extraction is **never re-run**.
3. **Product Search Result Caching:**
   - Product catalog queries are normalized and cached in Redis with a 24-hour TTL.
4. **Token Pruning & Context Compression:**
   - Chat history older than 6 turns is automatically compressed into a 3-line project summary card.
