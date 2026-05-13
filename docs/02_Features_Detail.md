# 03_Features_Detail.md — Database Design & API Reference (MySQL Relational)

> **Tài liệu kim chỉ nam cho thiết kế Database Schema và API**
> 
> ⚠️ **IMPORTANT**: Dùng **MySQL 8.0+** (Relational Database), KHÔNG phải MongoDB
> - MySQL: Structured TABLES, ACID transactions, strong consistency, FK constraints
> - MongoDB: Document collections, eventual consistency, flexible schema (NOT used here)
>
> Spring Boot 3.x · JPA/Hibernate · RESTful API
> Mọi luồng nghiệp vụ, logic và edge cases được mô tả tại đây.

---

## Mục lục

- [0. Tại sao MySQL không phải MongoDB?](#0-tại-sao-mysql-không-phải-mongodb)
- [1. Tổng quan 6 Tables chính](#1-tổng-quan-6-tables-chính)
- [2. Table 1: Hotels — Khách sạn](#2-table-1-hotels--khách-sạn)
- [3. Table 2: Tours — Du lịch](#3-table-2-tours--du-lịch)
- [4. Table 3: Restaurants — Nhà hàng](#4-table-3-restaurants--nhà-hàng)
- [5. Table 4: Orders & Order Items — Đơn hàng](#5-table-4-orders--order-items--đơn-hàng)
- [6. Table 5: Bookings — Đặt dịch vụ](#6-table-5-bookings--đặt-dịch-vụ)
- [7. Table 6: Payments & Refunds — Thanh toán](#7-table-6-payments--refunds--thanh-toán)
- [8. Normalization Strategy (1NF–3NF)](#8-normalization-strategy-1nf3nf)
- [9. Reference vs Embedded Pattern](#9-reference-vs-embedded-pattern)
- [10. Edge Cases & Error Handling](#10-edge-cases--error-handling)

---

## 0. Tại sao MySQL không phải MongoDB?

### ⚠️ Sửa lại: Thuật ngữ "Collection"

**TRƯỚC (Sai):**
```
"Collection" = MongoDB documents (not relational)
❌ Collection 1: Hotels
❌ Collection 2: Tours
```

**SAU (Đúng):**
```
"Table" = MySQL relational structure (correct term)
✅ Table 1: hotels
✅ Table 2: tours
```

**Giải thích:**
- **MongoDB**: Dùng từ "Collection" (document-based, schemaless)
- **MySQL**: Dùng từ "Table" (relational, schema-based, constraints)

### So sánh: MySQL vs MongoDB

| Tiêu chí | MySQL (Relational) | MongoDB (Document) |
|---|---|---|
| **Thuật ngữ** | TABLE | COLLECTION |
| **Cấu trúc** | Rows + Columns | Documents (JSON) |
| **Schema** | Cứng (STRICT) | Linh hoạt |
| **ACID** | ✅ ACID (strong) | ⚠️ Eventual consistency |
| **FK Constraint** | ✅ Tự động kiểm tra | ❌ Phải code xử lý |
| **Normalization** | ✅ 1NF-3NF | ❌ Denormalization |
| **Transaction** | ✅ Multi-table transaction | ⚠️ Limited multi-doc |
| **Query Performance** | ✅ JOIN (optimized) | ⚠️ Embedding overhead |
| **Use Case** | E-commerce, Banking | Real-time analytics |

### Tại sao project này chọn MySQL?

```
✅ Strong consistency: Payment/Order data MUST be consistent
✅ ACID transactions: Booking + Payment = atomic
✅ FK constraints: Prevent orphaned records
✅ Normalization: Avoid data duplication (audit trail important)
✅ Cost: Cheaper than specialized databases

❌ MongoDB không phù hợp:
  ❌ Eventual consistency → Payment mismatch risk
  ❌ No native FK → Manual validation
  ❌ Embedding → Duplicate data (price changes)
  ❌ Complex transactions → Many reconciliation jobs
```

### ✅ Kết luận

**Dùng MySQL, KHÔNG MongoDB**

Trong tài liệu này:
- ✅ "Table" = MySQL table (CORRECT)
- ❌ "Collection" = MongoDB term (REMOVE)
- ✅ "Relational" = MySQL design
- ✅ "Foreign Key" = MySQL FK constraint

---

## 1. Tổng quan 6 Tables chính (MySQL Relational)

### Architecture Overview

```
PROJECT USES: MySQL 8.0+ RELATIONAL DATABASE
NOT MongoDB (Document Store)

6 Main Tables:
├─ Hotels ecosystem (hotels, room_types, hotel_images, hotel_amenities)
├─ Tours ecosystem (tours, tour_slots, tour_itineraries, tour_images)
├─ Restaurants ecosystem (restaurants, menu_items, restaurant_tables)
├─ Orders ecosystem (orders, order_items)
├─ Bookings ecosystem (bookings, hotel_bookings, tour_bookings, restaurant_bookings)
└─ Payments ecosystem (payments, refunds)

Total: 38 tables (including Auth tables: users, roles, permissions, etc.)
```

### Kiến trúc Relational (Chi tiết)

```sql
┌─────────────────────────────────────────────────┐
│ 6 CORE TABLES (MySQL Relational)                │
├─────────────────────────────────────────────────┤
│                                                 │
│ TABLE 1: HOTELS (Khách sạn)                    │
│ ├─ hotels (primary)                            │
│ ├─ room_types (1-M relationship)               │
│ ├─ hotel_images (1-M relationship)             │
│ └─ hotel_amenities (M-M mapping)               │
│                                                 │
│ TABLE 2: TOURS (Du lịch)                       │
│ ├─ tours (primary)                             │
│ ├─ tour_slots (1-M, dynamic slots)             │
│ ├─ tour_itineraries (1-M)                      │
│ └─ tour_images (1-M)                           │
│                                                 │
│ TABLE 3: RESTAURANTS (Nhà hàng)                │
│ ├─ restaurants (primary)                       │
│ ├─ menu_categories (1-M)                       │
│ ├─ menu_items (1-M, hierarchical)              │
│ └─ restaurant_tables (1-M, inventory)          │
│                                                 │
│ TABLE 4: ORDERS (Đơn hàng)                     │
│ ├─ orders (primary, master)                    │
│ └─ order_items (1-M, snapshot pattern)         │
│                                                 │
│ TABLE 5: BOOKINGS (Đặt dịch vụ)                │
│ ├─ bookings (parent, polymorphic)              │
│ ├─ hotel_bookings (1-1 with bookings)          │
│ ├─ tour_bookings (1-1 with bookings)           │
│ └─ restaurant_bookings (1-1 with bookings)     │
│                                                 │
│ TABLE 6: PAYMENTS (Thanh toán)                 │
│ ├─ payments (primary, 1-1 with orders)         │
│ └─ refunds (M-1 with payments)                 │
│                                                 │
└─────────────────────────────────────────────────┘
```

### Relationships Matrix

| Table | Type | Parent | Child | Reason |
|---|---|---|---|---|
| hotels ← room_types | 1-M | hotels.id | room_types.hotel_id | Each hotel has many room types |
| hotels ← hotel_images | 1-M | hotels.id | images.hotel_id | Media gallery |
| tours ← tour_slots | 1-M | tours.id | slots.tour_id | Dynamic availability |
| restaurants ← menu_items | 1-M | restaurants.id | items.restaurant_id | Hierarchical menu |
| orders ← order_items | 1-M | orders.id | items.order_id | Snapshot pattern |
| orders ← bookings | 1-M | orders.id | bookings.order_id | Multiple bookings per order |
| bookings ← hotel_bookings | 1-1 | bookings.id | hotel_bookings.booking_id | Polymorphic (joined) |
| bookings ← tour_bookings | 1-1 | bookings.id | tour_bookings.booking_id | Polymorphic (joined) |
| bookings ← restaurant_bookings | 1-1 | bookings.id | rest_bookings.booking_id | Polymorphic (joined) |
| payments ← orders | 1-1 | orders.id | payments.order_id | One payment per order |
| refunds ← payments | M-1 | payments.id | refunds.payment_id | Multiple refunds (rare) |

### Normalization Level: 1NF–3NF

| Table | 1NF | 2NF | 3NF | Status |
|---|:-:|:-:|:-:|---|
| hotels | ✅ | ✅ | ✅ | Fully normalized (denormalized fields OK) |
| room_types | ✅ | ✅ | ✅ | Fully normalized |
| tours | ✅ | ✅ | ✅ | Fully normalized |
| tour_slots | ✅ | ✅ | ✅ | Fully normalized + Dynamic |
| restaurants | ✅ | ✅ | ✅ | Fully normalized |
| menu_items | ✅ | ✅ | ✅ | Hierarchical + Normalized |
| orders | ✅ | ✅ | ✅ | 3NF + Snapshot (intentional) |
| order_items | ✅ | ✅ | ✅ | 3NF + Snapshot fields (audit trail) |
| bookings | ✅ | ✅ | ✅ | Polymorphic (joined inheritance) |
| payments | ✅ | ✅ | ✅ | JSON gateway_response OK (flexible) |
| refunds | ✅ | ✅ | ✅ | Fully normalized |

---

## 2. Table 1: Hotels — Khách sạn

### 2.1. Schema Design (MySQL)

#### `hotels` — Primary table

```sql
CREATE TABLE hotels (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_vi         VARCHAR(255) NOT NULL,          -- Bilingual
    name_en         VARCHAR(255) NOT NULL,
    slug            VARCHAR(300) UNIQUE NOT NULL,   -- SEO URL
    description_vi  TEXT,
    description_en  TEXT,
    
    -- Location (Embedded: doesn't need separate table)
    address         VARCHAR(255) NOT NULL,
    district        VARCHAR(100) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    latitude        DECIMAL(10,8),
    longitude       DECIMAL(11,8),
    
    -- Contact
    phone           VARCHAR(20),
    email           VARCHAR(255),
    
    -- Policies (Embedded: standard for all guests)
    check_in_time   TIME DEFAULT '14:00:00',
    check_out_time  TIME DEFAULT '12:00:00',
    cancel_policy_vi TEXT,
    cancel_policy_en TEXT,
    
    -- Denormalized counters (Cache fields)
    avg_rating      DECIMAL(3,2) DEFAULT 0.00,     -- Recalculate every 5 min
    total_reviews   INT DEFAULT 0,
    
    -- Media
    thumbnail_url   VARCHAR(500),
    
    -- Status
    star_rating     TINYINT DEFAULT 3,              -- 1-5
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    is_deleted      TINYINT(1) DEFAULT 0,           -- Soft delete
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_slug (slug),
    INDEX idx_province (province),
    INDEX idx_status (status),
    INDEX idx_star (star_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `room_types` — Child table (1-M with hotels)

```sql
CREATE TABLE room_types (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    hotel_id        BIGINT NOT NULL,                -- FK
    
    name_vi         VARCHAR(150) NOT NULL,
    name_en         VARCHAR(150) NOT NULL,
    description_vi  TEXT,
    description_en  TEXT,
    
    -- Room details
    max_guests      TINYINT DEFAULT 2,
    area_sqm        DECIMAL(6,2),
    bed_type_vi     VARCHAR(100),
    bed_type_en     VARCHAR(100),
    
    -- Pricing (Snapshot on order)
    base_price      DECIMAL(15,2) NOT NULL,         -- Frozen at booking time
    weekend_price   DECIMAL(15,2),
    total_rooms     INT DEFAULT 1,                  -- Inventory count
    
    -- Media
    thumbnail_url   VARCHAR(500),
    
    -- Status
    is_deleted      TINYINT(1) DEFAULT 0,
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE,
    INDEX idx_hotel_id (hotel_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2.2. Normalization Check: Hotels

#### ✅ 1NF — Atomic Values

```
✅ PASS: Mỗi column có 1 giá trị (không array/list)
  - address, district, province = 3 separate fields ✅
  - Images = separate hotel_images table ✅
  - Amenities = separate hotel_amenities + mapping ✅

❌ WRONG (violates 1NF):
  ❌ room_types JSON = ["Deluxe", "Standard"] (lưu array)
  ❌ amenities = "WiFi,AC,Pool" (repeating values)
```

#### ✅ 2NF — No Partial Dependency

```
✅ PASS: Non-key attrs depend on FULL PK only
  - avg_rating depends on hotels.id ✅ (not on hotel.name)
  - name_vi depends on hotels.id ✅
  
❌ WRONG (violates 2NF):
  ❌ composite key (hotel_id, room_id) with field depending on hotel_id only
```

#### ✅ 3NF — No Transitive Dependency

```
✅ PASS: Non-key attrs don't depend on other non-key attrs
  - province is user input (not derived) ✅
  - avg_rating is derived (recalculated) - OK ✅
  
❌ WRONG (violates 3NF):
  ❌ hotels.province_name depends on hotels.province
     → Create lookup table: provinces(id, name)
  ✅ FIX: Store province as VARCHAR (user input)
```

#### ✅ Denormalization Justification

```
✅ ALLOWED denormalization (for performance):

1. avg_rating, total_reviews
   Reason: Expensive to calculate per request
   Strategy: Recalculate every 5 minutes (async job)
   Query: SELECT AVG(rating), COUNT(*) FROM reviews WHERE hotel_id = ?
   Store in: hotels.avg_rating, hotels.total_reviews
   Invalidation: When new review added

2. thumbnail_url
   Reason: Performance (avoid JOIN to images)
   Strategy: Copy from hotel_images.image_url
   Keep: Most recent image

3. cancel_policy_vi/en
   Reason: Policy is read-only, rarely changes
   Strategy: Embed as TEXT (no separate table needed)
```

### 2.3. Reference vs Embedded — Hotels Table

| Field | Pattern | Reason |
|---|---|---|
| `room_types` | **REFERENCE** (Separate table) | 1-M relationship, dynamic, frequently updated |
| `hotel_images` | **REFERENCE** (Separate table) | Media gallery, 1-M, can be reordered |
| `hotel_amenities` | **REFERENCE** (M-M mapping) | Many-to-many, frequently filtered |
| `address, district, province` | **EMBEDDED** | Location data, atomic, no normalization needed |
| `check_in_time, check_out_time` | **EMBEDDED** | Hotel policy, same for all guests, rarely changes |
| `cancel_policy_vi/en` | **EMBEDDED** | Audit trail (snapshot at booking), rarely updated |
| `avg_rating, total_reviews` | **REFERENCE** | Denormalized counter cache (recalculated) |
| `thumbnail_url` | **EMBEDDED** (or cache) | Convenience field, copied from images |

---

## 3. Table 2: Tours — Du lịch

### 3.1. Schema Design (MySQL)

#### `tours` — Primary table

```sql
CREATE TABLE tours (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_vi         VARCHAR(255) NOT NULL,
    name_en         VARCHAR(255) NOT NULL,
    slug            VARCHAR(300) UNIQUE NOT NULL,
    
    description_vi  TEXT,
    description_en  TEXT,
    
    -- Duration
    duration_days   TINYINT DEFAULT 1,
    duration_nights TINYINT DEFAULT 0,
    
    -- Route
    destination_vi  VARCHAR(150) NOT NULL,
    destination_en  VARCHAR(150) NOT NULL,
    departure_point_vi VARCHAR(255),
    departure_point_en VARCHAR(255),
    
    -- Capacity
    max_guests      INT DEFAULT 20,
    min_guests      INT DEFAULT 1,
    
    -- Details (Embedded as TEXT, not as separate table)
    includes_vi     TEXT,                           -- e.g., "Accommodation, Meals, Guide"
    includes_en     TEXT,
    excludes_vi     TEXT,
    excludes_en     TEXT,
    requirements_vi TEXT,
    requirements_en TEXT,
    
    -- Denormalized counters
    avg_rating      DECIMAL(3,2) DEFAULT 0.00,
    total_reviews   INT DEFAULT 0,
    
    -- Media
    thumbnail_url   VARCHAR(500),
    
    -- Status
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    is_deleted      TINYINT(1) DEFAULT 0,
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_slug (slug),
    INDEX idx_destination (destination_vi),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `tour_slots` — Child table (1-M, CRITICAL for booking)

```sql
CREATE TABLE tour_slots (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    tour_id         BIGINT NOT NULL,                -- FK
    
    -- Date range
    start_date      DATE NOT NULL,
    end_date        DATE NOT NULL,
    departure_time  TIME,
    
    -- Pricing (Snapshot at booking time = immutable)
    price_per_person DECIMAL(15,2) NOT NULL,
    
    -- Capacity tracking (DYNAMIC, updated on each booking)
    total_slots     INT NOT NULL,                   -- Max capacity
    booked_slots    INT DEFAULT 0,                  -- Current reservations
    
    -- Status
    status          VARCHAR(20) DEFAULT 'OPEN',    -- OPEN | FULL | CANCELLED | COMPLETED
    note_vi         VARCHAR(255),
    note_en         VARCHAR(255),
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE,
    INDEX idx_tour_id (tour_id),
    INDEX idx_start_date (start_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3.2. Business Logic: Tour Booking (Availability + Race Condition)

#### Check Real-time Availability

```java
GET /api/tours/{tour_id}/slots?start_date=2026-06-01&end_date=2026-06-30

Logic:
1. SELECT ts.* FROM tour_slots ts
   WHERE ts.tour_id = ? 
     AND ts.start_date >= ? 
     AND ts.end_date <= ?
     AND ts.status IN ('OPEN', 'FULL')
   ORDER BY ts.start_date ASC

2. For each slot:
   - available_slots = total_slots - booked_slots
   - Show: {slot_id, start_date, end_date, available_slots, price_per_person}

3. Edge cases:
   - IF status = 'CANCELLED' → available = 0
   - IF booked_slots > total_slots (error) → Log alert
   - IF booked_slots = total_slots → auto UPDATE status = 'FULL'
```

#### Create Tour Booking (Race Condition Prevention)

```
POST /api/bookings/tours
{
  "tour_slot_id": 789,
  "num_adults": 2,
  "num_children": 1,
  "contact_name": "John Doe",
  "contact_phone": "0901234567",
  "contact_email": "john@example.com"
}

⚠️ RACE CONDITION RISK: 2 users book last slot simultaneously

✅ SOLUTION: Pessimistic Locking in Transaction

START TRANSACTION (SERIALIZABLE isolation)
  
  1. Lock: SELECT ts.* FROM tour_slots ts
            WHERE ts.id = ? 
            FOR UPDATE  ← Lock this row until COMMIT
     
     Now other requests WAIT for this transaction to finish
  
  2. Validate:
     - IF ts.status = 'CANCELLED' → ROLLBACK (error 400)
     - IF ts.booked_slots >= ts.total_slots → ROLLBACK (409 Conflict)
     - IF (ts.total_slots - ts.booked_slots) < (num_adults + num_children)
       → ROLLBACK (409 Conflict "Not enough slots")
  
  3. Create records:
     a. INSERT INTO orders (total_amount = price_per_person × (num_adults + num_children))
        → order_id = 123
     
     b. INSERT INTO order_items (order_id, service_type='TOUR', variant_id=tour_slot_id)
        → Snapshot: price_per_person at booking time
     
     c. INSERT INTO bookings (order_id, service_type='TOUR', status='PENDING_CONFIRMATION')
        → booking_id = 456
     
     d. INSERT INTO tour_bookings (booking_id, tour_slot_id, num_adults, num_children)
     
     e. UPDATE tour_slots SET booked_slots = booked_slots + (num_adults + num_children)
        WHERE id = tour_slot_id
        → IF new booked_slots >= total_slots → UPDATE status = 'FULL'
  
  4. Assign STAFF:
     - SELECT s.* FROM users s
       WHERE s.role = 'STAFF'
       ORDER BY (SELECT COUNT(*) FROM bookings WHERE staff_id = s.id) ASC
       LIMIT 1
     - UPDATE bookings SET staff_id = s.id
  
  5. Send notification:
     - Email to STAFF: "New booking needs confirmation"
     - Email to customer: "Booking created, awaiting confirmation"

COMMIT ← Release lock
```

### 3.3. Normalization Check: Tours

#### ✅ Snapshot in tour_bookings

```sql
CREATE TABLE tour_bookings (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id      BIGINT UNIQUE NOT NULL,
    tour_id         BIGINT NOT NULL,
    tour_slot_id    BIGINT NOT NULL,
    
    num_adults      INT DEFAULT 1,
    num_children    INT DEFAULT 0,
    price_per_person DECIMAL(15,2) NOT NULL,  ← SNAPSHOT (immutable)
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (tour_id) REFERENCES tours(id),
    FOREIGN KEY (tour_slot_id) REFERENCES tour_slots(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Why snapshot?**
```
Scenario: Admin updates tour price from $100 → $150

Option A (NO Snapshot):
  ❌ tour_bookings.price_per_person = FK to tour_slots.price_per_person
  ❌ When query old booking: Shows $150 (WRONG!)
  ❌ Invoice mismatch: Charged $100 but shows $150
  ❌ No audit trail

Option B (WITH Snapshot):
  ✅ tour_bookings.price_per_person = 100.00 (copied at booking time)
  ✅ When query old booking: Shows $100 (CORRECT!)
  ✅ Invoice accurate: Charged $100, shown $100
  ✅ Audit trail preserved
```

---

## 4. Table 3: Restaurants — Nhà hàng

### 4.1. Schema Design (MySQL)

#### `restaurants` — Primary table

```sql
CREATE TABLE restaurants (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name_vi         VARCHAR(255) NOT NULL,
    name_en         VARCHAR(255) NOT NULL,
    slug            VARCHAR(300) UNIQUE NOT NULL,
    
    description_vi  TEXT,
    description_en  TEXT,
    
    -- Cuisine
    cuisine_type_vi VARCHAR(100),
    cuisine_type_en VARCHAR(100),
    
    -- Location (Embedded)
    address         VARCHAR(255) NOT NULL,
    district        VARCHAR(100) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    latitude        DECIMAL(10,8),
    longitude       DECIMAL(11,8),
    
    -- Contact
    phone           VARCHAR(20),
    email           VARCHAR(255),
    
    -- Hours (Embedded)
    open_time       TIME DEFAULT '10:00:00',
    close_time      TIME DEFAULT '22:00:00',
    
    -- Price range
    price_range     VARCHAR(20),                    -- BUDGET | MID | PREMIUM | LUXURY
    
    -- Policy (Embedded, audit trail)
    cancel_policy_vi TEXT,
    cancel_policy_en TEXT,
    
    -- Denormalized counters
    avg_rating      DECIMAL(3,2) DEFAULT 0.00,
    total_reviews   INT DEFAULT 0,
    
    -- Media
    thumbnail_url   VARCHAR(500),
    
    -- Status
    status          VARCHAR(20) DEFAULT 'ACTIVE',
    is_deleted      TINYINT(1) DEFAULT 0,
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_slug (slug),
    INDEX idx_district (district),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `restaurant_tables` — Table reservation inventory

```sql
CREATE TABLE restaurant_tables (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    restaurant_id   BIGINT NOT NULL,                -- FK
    
    table_code      VARCHAR(20) NOT NULL,          -- "T01", "T02"
    capacity        TINYINT NOT NULL,              -- Max guests per table
    zone_vi         VARCHAR(50),                   -- "Hành lang", "Gần cửa sổ"
    zone_en         VARCHAR(50),
    is_active       TINYINT(1) DEFAULT 1,
    
    PRIMARY KEY (id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE,
    UNIQUE KEY uq_table_code (restaurant_id, table_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4.2. Business Logic: Restaurant Booking

#### Check Table Availability (2-hour overlap detection)

```
GET /api/restaurants/{rest_id}/availability?date=2026-06-15&time=19:00&num_guests=4

Logic:
  1. SELECT rt.* FROM restaurant_tables rt
     WHERE rt.restaurant_id = ? 
       AND rt.capacity >= ? 
       AND rt.is_active = 1

  2. For each table:
     SELECT COUNT(*) as conflict_count
     FROM restaurant_bookings rb
     WHERE rb.restaurant_id = ?
       AND rb.table_id = rt.id
       AND rb.booking_date = ?
       AND ABS(TIMESTAMPDIFF(MINUTE, rb.booking_time, ?)) < 120  ← 2-hour window
     
     IF conflict_count > 0 → table NOT available
  
  3. Return: available_tables[] sorted by capacity
```

---

## 5. Table 4: Orders & Order Items — Đơn hàng (Snapshot Pattern)

### 5.1. Schema Design

#### `orders` — Master order record

```sql
CREATE TABLE orders (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_code      VARCHAR(30) UNIQUE NOT NULL,   -- "TN-20260512-00123"
    user_id         BIGINT NOT NULL,
    
    -- Financial
    subtotal        DECIMAL(15,2) NOT NULL,        -- SUM(order_items.subtotal)
    discount_amount DECIMAL(15,2) DEFAULT 0.00,    -- Voucher discount
    total_amount    DECIMAL(15,2) NOT NULL,        -- subtotal - discount
    
    -- Voucher (Embedded snapshot for audit)
    voucher_id      BIGINT,                        -- FK for lookup
    voucher_code    VARCHAR(50),                   -- Snapshot: code value at booking time
    
    -- Notes
    special_requests TEXT,
    
    -- Status
    status          VARCHAR(30) DEFAULT 'PENDING', -- PENDING | PAID | COMPLETED | CANCELLED
    
    -- Language
    lang            VARCHAR(5) DEFAULT 'vi',
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_order_code (order_code),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `order_items` — Line items (SNAPSHOT PATTERN - Critical!)

```sql
CREATE TABLE order_items (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT NOT NULL,                -- FK
    
    -- Service reference
    service_type    VARCHAR(20) NOT NULL,          -- HOTEL | TOUR | RESTAURANT
    service_id      BIGINT NOT NULL,               -- Reference to specific service
    
    -- Snapshot fields (IMMUTABLE after insert - for audit trail)
    service_name    VARCHAR(255) NOT NULL,         -- Snapshot: "Hilton Hanoi" at booking time
    variant_id      BIGINT,                        -- room_type_id | tour_slot_id | table_id
    variant_name    VARCHAR(255),                  -- Snapshot: "Deluxe Room" at booking time
    
    -- Pricing snapshot (IMMUTABLE - critical for invoice accuracy)
    unit_price      DECIMAL(15,2) NOT NULL,        -- Snapshot: price at booking time
    quantity        INT DEFAULT 1,
    subtotal        DECIMAL(15,2) NOT NULL,        -- unit_price × quantity
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_service (service_type, service_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5.2. ✅ Why Snapshot Pattern is CORRECT for Orders

```
Scenario: Customer books hotel on 2026-05-12 at $150/night
         Admin changes room price to $200 on 2026-05-15

WITHOUT Snapshot:
❌ order_items.unit_price = FK to room_types.base_price
❌ When querying order: Shows $200 (WRONG!)
❌ Invoice shows: $200 × 2 nights = $400 (Customer paid $300!)
❌ Dispute: "I was charged $300 but invoice shows $400"
❌ No audit trail of original price

WITH Snapshot (CORRECT):
✅ order_items.unit_price = 150.00 (copied at booking)
✅ order_items.service_name = "Hilton Hanoi" (copied at booking)
✅ order_items.variant_name = "Deluxe Room" (copied at booking)
✅ When querying order: Shows $150 × 2 = $300 (CORRECT!)
✅ Invoice accurate: $150 × 2 = $300
✅ Payment matches: Customer charged $300, invoice $300
✅ Audit trail: Can see all historical prices

Conclusion: Snapshot denormalization is INTENTIONAL for business integrity
```

### 5.3. Normalization: Orders Table

```
✅ 1NF: All fields atomic
  ✅ service_name is VARCHAR (single value)
  ✅ unit_price is DECIMAL (single value)
  ✅ No arrays/lists

✅ 2NF: Non-key attributes depend on PK only
  ✅ service_name depends on order_items.id
  ✅ unit_price depends on order_items.id
  ✅ NOT dependent on composite key subset

✅ 3NF: No transitive dependencies
  ✅ Snapshot fields are historical state (not transitive)
  ✅ They capture PAST value at booking time
  ✅ Changes in source table don't affect snapshot

Verdict: 3NF COMPLIANT (snapshots are acceptable denormalization)
```

---

## 6. Table 5: Bookings — Đặt dịch vụ (Polymorphic Design)

### 6.1. Polymorphic Pattern (Joined Inheritance)

#### Why Polymorphic?

```
Problem: 3 booking types (Hotel, Tour, Restaurant) with DIFFERENT fields

❌ BAD: Single table
  CREATE TABLE bookings (
    id, order_id, user_id,
    check_in_date, check_out_date,  ← NULL for tours/restaurants
    num_adults, num_children,       ← NULL for hotels
    table_id,                         ← NULL for tours
    ...300 columns, mostly NULL
  )
  Wasteful, hard to query

✅ GOOD: Polymorphic (Joined Inheritance)
  TABLE bookings (generic fields)
  TABLE hotel_bookings (hotel-specific 1-1)
  TABLE tour_bookings (tour-specific 1-1)
  TABLE restaurant_bookings (restaurant-specific 1-1)
  
  Query: SELECT b.*, hb.* FROM bookings b
         LEFT JOIN hotel_bookings hb ON b.id = hb.booking_id
         WHERE b.service_type = 'HOTEL'
```

#### `bookings` — Parent table (Shared fields)

```sql
CREATE TABLE bookings (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_code    VARCHAR(30) UNIQUE NOT NULL,   -- "BK-20260512-00456"
    
    -- References
    order_id        BIGINT NOT NULL,
    order_item_id   BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    
    -- Discriminator (tells which child table to query)
    service_type    VARCHAR(20) NOT NULL,          -- HOTEL | TOUR | RESTAURANT
    service_id      BIGINT NOT NULL,               -- Generic FK
    
    -- Contact snapshot (at booking time)
    contact_name    VARCHAR(150) NOT NULL,
    contact_phone   VARCHAR(20) NOT NULL,
    contact_email   VARCHAR(255) NOT NULL,
    
    -- Booking details
    guest_count     INT DEFAULT 1,
    special_requests TEXT,
    
    -- Status (State machine)
    status          VARCHAR(30) DEFAULT 'PENDING_CONFIRMATION',
    
    -- Staff assignment
    staff_id        BIGINT,                        -- Assigned STAFF
    staff_note      TEXT,                          -- Internal notes
    
    -- Timestamps (State transitions)
    confirmed_at    DATETIME,
    cancelled_at    DATETIME,
    cancel_reason   TEXT,
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_code (booking_code),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_staff_id (staff_id),
    INDEX idx_service_type (service_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `hotel_bookings` — Child table (1-1 with bookings)

```sql
CREATE TABLE hotel_bookings (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id      BIGINT UNIQUE NOT NULL,        -- 1-1 relationship
    
    -- Service FKs
    hotel_id        BIGINT NOT NULL,
    room_type_id    BIGINT NOT NULL,
    
    -- Hotel-specific fields
    check_in_date   DATE NOT NULL,
    check_out_date  DATE NOT NULL,
    num_nights      INT NOT NULL,
    num_rooms       INT DEFAULT 1,
    price_per_night DECIMAL(15,2) NOT NULL,        -- Snapshot
    
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (hotel_id) REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `tour_bookings` & `restaurant_bookings` (Similar)

---

## 7. Table 6: Payments & Refunds — Thanh toán

### 7.1. Payment Gateway Integration

#### `payments` — Payment records (1-1 with orders)

```sql
CREATE TABLE payments (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id        BIGINT UNIQUE NOT NULL,        -- 1-1 with orders
    payment_code    VARCHAR(50) UNIQUE NOT NULL,   -- "PAY-20260512-00789"
    
    -- Gateway
    gateway         VARCHAR(20) NOT NULL,          -- VNPAY | MOMO
    gateway_txn_id  VARCHAR(100),                  -- VNPay transaction ID
    gateway_order_id VARCHAR(100),                 -- MoMo order ID
    
    -- Financial
    amount          DECIMAL(15,2) NOT NULL,
    currency        VARCHAR(5) DEFAULT 'VND',
    
    -- Status
    status          VARCHAR(20) DEFAULT 'PENDING', -- PENDING | SUCCESS | FAILED | REFUNDED
    paid_at         DATETIME,
    
    -- Gateway response (Flexible JSON for different APIs)
    gateway_response JSON,                         -- Raw response
    
    -- Client
    ip_address      VARCHAR(50),
    
    -- Audit
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_code (payment_code),
    FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_gateway_txn (gateway_txn_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `refunds` — Refund tracking

```sql
CREATE TABLE refunds (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    payment_id      BIGINT NOT NULL,
    booking_id      BIGINT,                        -- Optional
    refund_code     VARCHAR(50) UNIQUE NOT NULL,   -- "RF-20260512-00999"
    
    -- Financial
    amount          DECIMAL(15,2) NOT NULL,
    reason          TEXT,
    
    -- Status
    status          VARCHAR(20) DEFAULT 'PENDING', -- PENDING | PROCESSING | SUCCESS | FAILED
    
    -- Gateway refund
    gateway_refund_id VARCHAR(100),
    gateway_response JSON,
    
    -- Approval workflow
    requested_by    BIGINT NOT NULL,
    processed_by    BIGINT,
    requested_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at    DATETIME,
    
    -- Notes
    note            TEXT,
    
    PRIMARY KEY (id),
    FOREIGN KEY (payment_id) REFERENCES payments(id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE SET NULL,
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_payment_id (payment_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 8. Normalization Strategy Matrix (1NF–3NF)

```
✅ ALL 11 Core Tables: FULLY NORMALIZED (1NF–3NF)

| Table | 1NF | 2NF | 3NF | Denormalization | Reason |
|---|:-:|:-:|:-:|---|---|
| users | ✅ | ✅ | ✅ | None | Pure relational |
| hotels | ✅ | ✅ | ✅ | avg_rating (cache) | Read optimization |
| room_types | ✅ | ✅ | ✅ | None | Pure relational |
| tours | ✅ | ✅ | ✅ | avg_rating (cache) | Read optimization |
| tour_slots | ✅ | ✅ | ✅ | price_per_person (snapshot) | Audit trail |
| restaurants | ✅ | ✅ | ✅ | avg_rating (cache) | Read optimization |
| menu_items | ✅ | ✅ | ✅ | None | Pure relational |
| orders | ✅ | ✅ | ✅ | voucher_code (snapshot) | Audit trail |
| order_items | ✅ | ✅ | ✅ | service_name, unit_price (snapshot) | CRITICAL audit trail |
| bookings | ✅ | ✅ | ✅ | contact_name (snapshot) | Audit trail |
| payments | ✅ | ✅ | ✅ | gateway_response (JSON) | Flexibility |
| refunds | ✅ | ✅ | ✅ | None | Pure relational |
```

---

## 9. Reference vs Embedded Pattern (CORRECTED)

### ✅ REFERENCE Pattern (Separate Tables)

```
Use when:
✅ 1-M or M-M relationships exist
✅ Data frequently queried independently
✅ Data changes independently (UPDATE/DELETE separately)
✅ Need referential integrity (FK constraints)

Examples:
✅ room_types ← hotels (1-M)
✅ hotel_images ← hotels (1-M, media)
✅ tour_slots ← tours (1-M, dynamic)
✅ menu_items ← restaurants (1-M)
```

### ✅ EMBEDDED Pattern (Inline Fields)

```
Use when:
✅ Data is snapshot (captured at point-in-time)
✅ Data is immutable after insert (audit trail)
✅ No need for independent queries
✅ Rarely or never updated
✅ Specific to parent record only

Examples:
✅ order_items.unit_price (snapshot at booking)
✅ order_items.service_name (snapshot at booking)
✅ bookings.contact_name (snapshot at booking)
✅ payments.gateway_response (immutable JSON)
✅ hotels.check_in_time (hotel policy, static)
✅ hotels.cancel_policy_vi/en (audit trail)
```

### ✅ DENORMALIZED Cache (Counter Cache)

```
Use when:
✅ Derived field (calculated, not literal)
✅ Expensive to calculate per request
✅ Acceptable to be eventually consistent
✅ Recalculate periodically (async job)

Examples:
✅ hotels.avg_rating (recalculate every 5 min from reviews)
✅ hotels.total_reviews (counter cache)
✅ tour_slots.booked_slots (update on each booking)

Implementation:
- Scheduled job: Every 5 minutes
  SELECT AVG(r.rating), COUNT(*) FROM reviews r
  WHERE r.service_type = 'HOTEL' AND r.service_id = ?
- UPDATE hotels SET avg_rating = ?, total_reviews = ? WHERE id = ?
- Index on hotels.avg_rating for sorting
```

### ❌ WRONG Patterns

```
❌ Embedding arrays (violates 1NF):
  CREATE TABLE hotels (
    id BIGINT,
    room_types JSON,  ← WRONG: should be separate table
    amenities JSON    ← WRONG: should be M-M mapping
  )
  Fix: Create separate tables + FK

❌ Transitive dependency (violates 3NF):
  CREATE TABLE hotels (
    id BIGINT,
    province_id BIGINT,
    province_name VARCHAR(100)  ← WRONG: depends on province_id
  )
  Fix: province_name in province lookup table OR store "Hanoi" string directly

❌ Snapshot without versioning:
  CREATE TABLE orders (
    id BIGINT,
    service_id BIGINT,
    service_name VARCHAR(255)  ← Where does this come from?
  )
  Should clearly document: "Snapshot of service.name at booking time"
```

---

## 10. Edge Cases & Error Handling

### 10.1 Race Condition: Overbooking

#### Scenario: 2 customers book last tour slot simultaneously

```
WITHOUT Pessimistic Lock (UNSAFE):
1. User A: SELECT available = 1
2. User B: SELECT available = 1
3. User A: Book → available = 0
4. User B: Book → available = -1 ❌ OVERBOOKING!

WITH Pessimistic Lock (SAFE):
START TRANSACTION (SERIALIZABLE)
  SELECT ts.booked_slots FROM tour_slots ts
  WHERE ts.id = ?
  FOR UPDATE  ← Locks row, other requests WAIT
  
  1. User A acquires lock
  2. User B waits...
  3. User A: CHECK booked_slots < total_slots ✅
  4. User A: UPDATE booked_slots = booked_slots + 2
  5. User A: COMMIT (releases lock)
  6. User B acquires lock
  7. User B: CHECK booked_slots < total_slots ❌ FULL
  8. User B: ROLLBACK (409 Conflict)

JPA Implementation:
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT ts FROM TourSlot ts WHERE ts.id = ?1")
Optional<TourSlot> findByIdForUpdate(Long id);
```

### 10.2 Race Condition: Double Payment

```
Scenario: User clicks "Pay" button twice (network retry)

WITHOUT Protection:
❌ Two payment records created
❌ Two charges to credit card
❌ Reconciliation nightmare

WITH UNIQUE Constraint + Idempotency:
UNIQUE(order_id, gateway)

1. First payment: INSERT {order_id: 123, gateway: 'VNPAY'} ✅
2. Second payment: INSERT {order_id: 123, gateway: 'VNPAY'}
   → Constraint violation (duplicate)
   → Application catches DataIntegrityViolationException
   → Returns existing payment

Code:
try {
  paymentRepo.save(payment);
} catch (DataIntegrityViolationException e) {
  // Already exists
  return paymentRepo.findByOrderIdAndGateway(order_id, gateway);
}
```

### 10.3 Cancellation Policy Window

```
Scenario: Customer cancels hotel booking, want to know refund amount

Query:
SELECT b.created_at, hb.check_in_date, h.cancel_policy_vi
FROM bookings b
JOIN hotel_bookings hb ON b.id = hb.booking_id
JOIN hotels h ON hb.hotel_id = h.id
WHERE b.id = ?

Policy:
- Full refund if cancelled 2 days before check-in
- 50% refund if cancelled 1 day before check-in
- No refund if cancelled within 1 day

Calculation:
days_until_checkin = DATEDIFF(hb.check_in_date, NOW())
IF days_until_checkin >= 2:
  refund_amount = order.total_amount (100%)
ELSE IF days_until_checkin >= 1:
  refund_amount = order.total_amount * 0.5 (50%)
ELSE:
  refund_amount = 0
```

---

## Summary: MySQL vs MongoDB Clarification

```
✅ PROJECT USES: MySQL 8.0+ (Relational Database)

Terminology:
- MySQL: TABLE (rows + columns)
- MongoDB: COLLECTION (documents)

This project:
- ✅ Table 1: hotels
- ✅ Table 2: tours
- ✅ Table 3: restaurants
- ✅ Table 4: orders & order_items
- ✅ Table 5: bookings (with child: hotel_bookings, tour_bookings, restaurant_bookings)
- ✅ Table 6: payments & refunds

NOT MongoDB collections!
```

---

> **Last Updated**: 2026-05-12  
> **Version**: 2.0 (CORRECTED: MySQL focus, no "Collection" term)  
> **Maintained By**: Senior Architecture Team  
> **Review Cycle**: On change
