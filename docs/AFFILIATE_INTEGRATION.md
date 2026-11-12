# RuangAI — Affiliate & E-Commerce Integration Architecture

## 1. Indonesian E-Commerce Ecosystem & Affiliate Landscape

Indonesia's e-commerce landscape is dominated by three primary platforms with mature affiliate partner programs:

| Platform | Affiliate Program | Commission Structure (Home & Living) | API Access & Sourcing Strategy |
| :--- | :--- | :--- | :--- |
| **Shopee Indonesia** | Shopee Affiliate Program | 2.5% – 10% (Capped per transaction / higher for Shopee Mall) | Shopee Open Platform & Affiliate Open API (Authorized partner keys, sub-id tracking). |
| **Tokopedia / TikTok Shop** | TikTok Shop by Tokopedia Affiliate | 3% – 12% (Varies by merchant campaign & creator tiers) | TikTok Shop Partner Center / Tokopedia Open API. |
| **Lazada Indonesia** | Lazada Affiliate Partner Program | 3% – 8% | Open Platform Affiliate API & deep link generator. |
| **Direct Brands** (Dekoruma, IKEA, Informa) | Direct B2B / Involve Asia / Accesstrade | 4% – 7% flat CPS | Affiliate aggregator feeds & CSV catalog imports. |

---

## 2. Independent Affiliate Integration Layer (Adapter Pattern)

To avoid tight coupling to any single marketplace, RuangAI employs an extensible **Adapter Interface**:

```
                              +---------------------------+
                              |   AffiliateIntegration    |
                              |         Interface         |
                              +---------------------------+
                                            ^
         +-----------------+----------------+-----------------+
         |                 |                |                 |
+-----------------+ +----------------+ +---------------+ +------------------+
|  ShopeeAdapter  | | TokopediaAdap. | | LazadaAdapter | | GenericAdapter   |
| (API / Feeds)   | | (Partner API)  | | (Partner API) | | (Verified Catalog|
+-----------------+ +----------------+ +---------------+ +------------------+
```

### Core Interface Contract (TypeScript / Kotlin)

```typescript
export interface AffiliateAdapter {
  readonly marketplaceCode: 'shopee' | 'tokopedia' | 'lazada' | 'generic';
  
  // Searches verified catalog matching visual attributes
  searchProducts(query: ProductSearchQuery): Promise<MarketplaceProduct[]>;
  
  // Retrieves live price, stock status, and ratings
  getProductDetails(externalId: string): Promise<ProductDetail>;
  
  // Constructs trackable affiliate link with sub-IDs
  generateAffiliateLink(params: AffiliateLinkParams): Promise<string>;
  
  // Ingests conversion webhook or reports
  parseConversionReport(payload: any): Promise<AffiliateConversionRecord[]>;
}

export interface AffiliateLinkParams {
  originalProductUrl: string;
  projectId: string;
  userHash: string;
  slotOrPinId: string;
}
```

---

## 3. Strict Compliance & Anti-Scraping Policy

1. **Zero Unauthorized Scraping:**
   RuangAI strictly prohibits HTML screen scraping or reverse-engineering marketplace mobile APIs. Such practices violate platform terms of service, risk IP blacklisting, break without notice on CSS changes, and provide unverified product data.
2. **Phase 1 Ingestion Strategy:**
   - Use official **Shopee Open Platform / Affiliate Open API** and **TikTok Shop Partner API** where authorized credentials exist.
   - For merchants without public search APIs, RuangAI operates a **Curated Verified Product Catalog** pre-seeded with top-rated items from Shopee Mall, Star Sellers, and Tokopedia Official Stores, enriched with deep affiliate redirection links.
3. **Transparent Consumer Disclosures:**
   In compliance with Indonesian Consumer Protection regulations (UU Perlindungan Konsumen No. 8/1999) and digital advertising standards:
   - Every product card displays a clear badge: *"Tautan Afiliasi"* (Affiliate Link).
   - Info tooltip: *"RuangAI dapat menerima komisi dari pembelian yang dilakukan melalui tautan ini tanpa biaya tambahan bagi Anda."*

---

## 4. Sub-ID Attribution Architecture

Outbound affiliate URLs encapsulate encrypted tracking parameters:

```
https://api.ruangai.id/v1/affiliate/redirect?pid=SKU-10293&proj=4a71&slot=sofa_center
```

When clicked, the backend resolves the product and builds the marketplace affiliate URL:

```
https://shope.ee/an_link?sub_id1=proj_4a71&sub_id2=usr_9b1deb4d&sub_id3=pin_sofa_center
```

- `sub_id1`: Project ID (enables analytics on which room type generates the highest sales).
- `sub_id2`: SHA-256 hash of User ID (for conversion attribution without exposing PII).
- `sub_id3`: Visual hotspot slot (e.g., Sofa, Coffee Table, Pendant Light) to evaluate which furniture categories convert best.
