# TravelNest — Database Schema

> **MySQL 8.0+ · Spring Boot 3.x / JPA / Hibernate**
> Tài liệu quy chuẩn — Source of Truth cho tầng dữ liệu.

---

## Mục lục

- [1. Nguyên tắc thiết kế](#1-nguyên-tắc-thiết-kế)
- [2. Tổng quan ERD](#2-tổng-quan-erd)
- [3. Nhóm bảng: Auth & Users](#3-nhóm-bảng-auth--users)
- [4. Nhóm bảng: Dịch vụ (Services)](#4-nhóm-bảng-dịch-vụ-services)
- [5. Nhóm bảng: Order & Booking (Hybrid Flow)](#5-nhóm-bảng-order--booking-hybrid-flow)
- [6. Nhóm bảng: Payment & Refund](#6-nhóm-bảng-payment--refund)
- [7. Nhóm bảng: Review](#7-nhóm-bảng-review)
- [8. Nhóm bảng: Voucher & Discount](#8-nhóm-bảng-voucher--discount)
- [9. Nhóm bảng: Config & CMS](#9-nhóm-bảng-config--cms)
- [10. Enum Reference](#10-enum-reference)
- [11. Indexing Strategy](#11-indexing-strategy)

---

## 1. Nguyên tắc thiết kế

| Nguyên tắc | Áp dụng |
|---|---|
| **Soft delete** | Mọi bảng chính dùng `is_deleted TINYINT(1)` thay vì DELETE thật |
| **Audit fields** | Mọi bảng có `created_at`, `updated_at` (auto-managed bởi JPA) |
| **UUID vs BIGINT** | PK dùng `BIGINT AUTO_INCREMENT` — hiệu năng JOIN tốt hơn UUID trong MySQL |
| **Charset** | `utf8mb4` toàn bộ — hỗ trợ emoji, tiếng Việt có dấu đầy đủ |
| **Engine** | `InnoDB` toàn bộ — hỗ trợ FK, transaction ACID |
| **Giá tiền** | `DECIMAL(15,2)` — tránh lỗi làm tròn của FLOAT/DOUBLE |
| **Enum** | Dùng `VARCHAR(50)` thay vì MySQL ENUM — dễ migrate, JPA mapping clean hơn |
| **i18n** | Các trường text có `_vi` / `_en` suffix cho bilingual content |

---

## 2. Tổng quan ERD

```
users ──< orders ──< order_items ──> (service_type + service_id)
              |                               |
              └──< payments                  v
                        |              bookings
                        └──< refunds       |
                                     ┌─────┼─────┐
                                     v     v     v
                               hotel_ tour_ rest_
                               bookings bookings bookings
                                     |     |     |
                               room_ tour_ rest_
                               types slots tables
                                 |     |     |
                              hotels tours restaurants
```

---

## 3. Nhóm bảng: Auth & Users

### `users`

```sql
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    email           VARCHAR(255)    NOT NULL,
    password_hash   VARCHAR(255)    NULL,
    full_name       VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20)     NULL,
    avatar_url      VARCHAR(500)    NULL,
    date_of_birth   DATE            NULL,
    gender          VARCHAR(10)     NULL,           -- MALE | FEMALE | OTHER
    role            VARCHAR(20)     NOT NULL DEFAULT 'CUSTOMER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    email_verified  TINYINT(1)      NOT NULL DEFAULT 0,
    preferred_lang  VARCHAR(5)      NOT NULL DEFAULT 'vi',
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- role: CUSTOMER | STAFF | ADMIN
-- status: ACTIVE | BANNED | UNVERIFIED | INACTIVE
```

---

### `social_accounts`

```sql
CREATE TABLE social_accounts (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    provider        VARCHAR(30)     NOT NULL,   -- GOOGLE | FACEBOOK
    provider_id     VARCHAR(255)    NOT NULL,
    provider_email  VARCHAR(255)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_social_provider (provider, provider_id),
    INDEX idx_social_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `otp_tokens`

```sql
CREATE TABLE otp_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    token       VARCHAR(10)     NOT NULL,
    type        VARCHAR(30)     NOT NULL,   -- EMAIL_VERIFY | PASSWORD_RESET
    expires_at  DATETIME        NOT NULL,
    is_used     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_otp_user_type (user_id, type),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `user_addresses`

```sql
CREATE TABLE user_addresses (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL,
    label           VARCHAR(50)     NULL,
    full_name       VARCHAR(150)    NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    address_line    VARCHAR(255)    NOT NULL,
    ward            VARCHAR(100)    NULL,
    district        VARCHAR(100)    NOT NULL,
    province        VARCHAR(100)    NOT NULL,
    is_default      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_addr_user (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `roles` (RBAC)

```sql
CREATE TABLE roles (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL,
    description VARCHAR(255)    NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default roles
INSERT INTO roles (name, description) VALUES
('CUSTOMER', 'Khách đặt dịch vụ'),
('STAFF', 'Nhân viên vận hành'),
('ADMIN', 'Quản trị viên hệ thống');
```

---

### `permissions` (RBAC)

```sql
CREATE TABLE permissions (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)    NOT NULL,
    description VARCHAR(255)    NULL,
    resource    VARCHAR(50)     NOT NULL,
    action      VARCHAR(50)     NOT NULL,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_permission (resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert permissions
INSERT INTO permissions (name, description, resource, action) VALUES
('hotels:read', 'Xem danh sách khách sạn', 'hotels', 'read'),
('hotels:create', 'Tạo khách sạn', 'hotels', 'create'),
('hotels:update', 'Cập nhật khách sạn', 'hotels', 'update'),
('hotels:delete', 'Xóa khách sạn', 'hotels', 'delete'),
('tours:read', 'Xem danh sách tour', 'tours', 'read'),
('tours:create', 'Tạo tour', 'tours', 'create'),
('tours:update', 'Cập nhật tour', 'tours', 'update'),
('tours:delete', 'Xóa tour', 'tours', 'delete'),
('restaurants:read', 'Xem danh sách nhà hàng', 'restaurants', 'read'),
('restaurants:create', 'Tạo nhà hàng', 'restaurants', 'create'),
('restaurants:update', 'Cập nhật nhà hàng', 'restaurants', 'update'),
('restaurants:delete', 'Xóa nhà hàng', 'restaurants', 'delete'),
('bookings:read', 'Xem booking', 'bookings', 'read'),
('bookings:confirm', 'Xác nhận booking', 'bookings', 'confirm'),
('bookings:cancel', 'Hủy booking', 'bookings', 'cancel'),
('bookings:refund', 'Xử lý hoàn tiền', 'bookings', 'refund'),
('users:read', 'Xem danh sách user', 'users', 'read'),
('users:manage', 'Quản lý user', 'users', 'manage'),
('users:ban', 'Ban/Unban user', 'users', 'ban'),
('dashboard:read', 'Xem dashboard thống kê', 'dashboard', 'read'),
('reports:export', 'Export báo cáo', 'reports', 'export'),
('vouchers:manage', 'Quản lý voucher', 'vouchers', 'manage'),
('banners:manage', 'Quản lý banner', 'banners', 'manage'),
('system:config', 'Cấu hình hệ thống', 'system', 'config');
```

---

### `role_permissions` (M-M Mapping)

```sql
CREATE TABLE role_permissions (
    role_id         BIGINT  NOT NULL,
    permission_id   BIGINT  NOT NULL,

    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id)       REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- CUSTOMER permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'CUSTOMER' AND p.resource IN ('hotels', 'tours', 'restaurants') AND p.action = 'read'

UNION ALL

-- STAFF permissions  
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'STAFF' AND p.resource IN ('hotels', 'tours', 'restaurants', 'bookings', 'dashboard') 
  AND p.action IN ('read', 'confirm', 'cancel')

UNION ALL

-- ADMIN permissions (full access)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';
```

---

### `user_roles` (M-M User-Role Mapping)

```sql
CREATE TABLE user_roles (
    user_id     BIGINT  NOT NULL,
    role_id     BIGINT  NOT NULL,

    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 4. Nhóm bảng: Dịch vụ (Services)

### 4.1. Hotel

#### `hotels`

```sql
CREATE TABLE hotels (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    name_vi             VARCHAR(255)    NOT NULL,
    name_en             VARCHAR(255)    NOT NULL,
    slug                VARCHAR(300)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    star_rating         TINYINT         NOT NULL DEFAULT 3,
    address             VARCHAR(255)    NOT NULL,
    ward                VARCHAR(100)    NULL,
    district            VARCHAR(100)    NOT NULL,
    province            VARCHAR(100)    NOT NULL,
    latitude            DECIMAL(10,8)   NULL,
    longitude           DECIMAL(11,8)   NULL,
    phone               VARCHAR(20)     NULL,
    email               VARCHAR(255)    NULL,
    check_in_time       TIME            NOT NULL DEFAULT '14:00:00',
    check_out_time      TIME            NOT NULL DEFAULT '12:00:00',
    cancel_policy_vi    TEXT            NULL,
    cancel_policy_en    TEXT            NULL,
    thumbnail_url       VARCHAR(500)    NULL,
    avg_rating          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    total_reviews       INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_hotel_slug (slug),
    INDEX idx_hotel_province (province),
    INDEX idx_hotel_status (status),
    INDEX idx_hotel_star (star_rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `hotel_images`

```sql
CREATE TABLE hotel_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id    BIGINT          NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    caption_vi  VARCHAR(255)    NULL,
    caption_en  VARCHAR(255)    NULL,
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_hotel_images_hotel (hotel_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `hotel_amenities` & `hotel_amenity_map`

```sql
CREATE TABLE hotel_amenities (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name_vi     VARCHAR(100)    NOT NULL,
    name_en     VARCHAR(100)    NOT NULL,
    icon        VARCHAR(100)    NULL,

    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE hotel_amenity_map (
    hotel_id    BIGINT  NOT NULL,
    amenity_id  BIGINT  NOT NULL,

    PRIMARY KEY (hotel_id, amenity_id),
    FOREIGN KEY (hotel_id)   REFERENCES hotels(id) ON DELETE CASCADE,
    FOREIGN KEY (amenity_id) REFERENCES hotel_amenities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `room_types`

```sql
CREATE TABLE room_types (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    hotel_id            BIGINT          NOT NULL,
    name_vi             VARCHAR(150)    NOT NULL,
    name_en             VARCHAR(150)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    max_guests          TINYINT         NOT NULL DEFAULT 2,
    area_sqm            DECIMAL(6,2)    NULL,
    bed_type_vi         VARCHAR(100)    NULL,
    bed_type_en         VARCHAR(100)    NULL,
    base_price          DECIMAL(15,2)   NOT NULL,
    weekend_price       DECIMAL(15,2)   NULL,
    total_rooms         INT             NOT NULL DEFAULT 1,
    thumbnail_url       VARCHAR(500)    NULL,
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_roomtype_hotel (hotel_id),
    FOREIGN KEY (hotel_id) REFERENCES hotels(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `room_type_images`

```sql
CREATE TABLE room_type_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_type_id    BIGINT          NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 4.2. Tour

#### `tours`

```sql
CREATE TABLE tours (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    name_vi             VARCHAR(255)    NOT NULL,
    name_en             VARCHAR(255)    NOT NULL,
    slug                VARCHAR(300)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    duration_days       TINYINT         NOT NULL DEFAULT 1,
    duration_nights     TINYINT         NOT NULL DEFAULT 0,
    destination_vi      VARCHAR(150)    NOT NULL,
    destination_en      VARCHAR(150)    NOT NULL,
    departure_point_vi  VARCHAR(255)    NULL,
    departure_point_en  VARCHAR(255)    NULL,
    max_guests          INT             NOT NULL DEFAULT 20,
    min_guests          INT             NOT NULL DEFAULT 1,
    includes_vi         TEXT            NULL,
    includes_en         TEXT            NULL,
    excludes_vi         TEXT            NULL,
    excludes_en         TEXT            NULL,
    requirements_vi     TEXT            NULL,
    requirements_en     TEXT            NULL,
    thumbnail_url       VARCHAR(500)    NULL,
    avg_rating          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    total_reviews       INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_tour_slug (slug),
    INDEX idx_tour_destination (destination_vi),
    INDEX idx_tour_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `tour_itineraries`

```sql
CREATE TABLE tour_itineraries (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    tour_id             BIGINT          NOT NULL,
    day_number          TINYINT         NOT NULL,
    title_vi            VARCHAR(255)    NOT NULL,
    title_en            VARCHAR(255)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    meals_vi            VARCHAR(100)    NULL,
    meals_en            VARCHAR(100)    NULL,
    accommodation_vi    VARCHAR(150)    NULL,
    accommodation_en    VARCHAR(150)    NULL,

    PRIMARY KEY (id),
    INDEX idx_itinerary_tour (tour_id),
    FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `tour_images`

```sql
CREATE TABLE tour_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    tour_id     BIGINT          NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `tour_slots`

```sql
CREATE TABLE tour_slots (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    tour_id             BIGINT          NOT NULL,
    start_date          DATE            NOT NULL,
    end_date            DATE            NOT NULL,
    departure_time      TIME            NULL,
    price_per_person    DECIMAL(15,2)   NOT NULL,
    total_slots         INT             NOT NULL,
    booked_slots        INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
    note_vi             VARCHAR(255)    NULL,
    note_en             VARCHAR(255)    NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_slot_tour (tour_id),
    INDEX idx_slot_date (start_date),
    FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### 4.3. Restaurant

#### `restaurants`

```sql
CREATE TABLE restaurants (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    name_vi             VARCHAR(255)    NOT NULL,
    name_en             VARCHAR(255)    NOT NULL,
    slug                VARCHAR(300)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    cuisine_type_vi     VARCHAR(100)    NULL,
    cuisine_type_en     VARCHAR(100)    NULL,
    address             VARCHAR(255)    NOT NULL,
    district            VARCHAR(100)    NOT NULL,
    province            VARCHAR(100)    NOT NULL,
    latitude            DECIMAL(10,8)   NULL,
    longitude           DECIMAL(11,8)   NULL,
    phone               VARCHAR(20)     NULL,
    email               VARCHAR(255)    NULL,
    open_time           TIME            NOT NULL DEFAULT '10:00:00',
    close_time          TIME            NOT NULL DEFAULT '22:00:00',
    price_range         VARCHAR(20)     NULL,   -- BUDGET | MID | PREMIUM | LUXURY
    cancel_policy_vi    TEXT            NULL,
    cancel_policy_en    TEXT            NULL,
    thumbnail_url       VARCHAR(500)    NULL,
    avg_rating          DECIMAL(3,2)    NOT NULL DEFAULT 0.00,
    total_reviews       INT             NOT NULL DEFAULT 0,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_restaurant_slug (slug),
    INDEX idx_rest_district (district),
    INDEX idx_rest_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `restaurant_images`

```sql
CREATE TABLE restaurant_images (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    restaurant_id   BIGINT          NOT NULL,
    image_url       VARCHAR(500)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `menu_categories` & `menu_items`

```sql
CREATE TABLE menu_categories (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    restaurant_id   BIGINT          NOT NULL,
    name_vi         VARCHAR(100)    NOT NULL,
    name_en         VARCHAR(100)    NOT NULL,
    sort_order      INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    category_id     BIGINT          NOT NULL,
    restaurant_id   BIGINT          NOT NULL,
    name_vi         VARCHAR(150)    NOT NULL,
    name_en         VARCHAR(150)    NOT NULL,
    description_vi  TEXT            NULL,
    description_en  TEXT            NULL,
    price           DECIMAL(15,2)   NOT NULL,
    image_url       VARCHAR(500)    NULL,
    is_available    TINYINT(1)      NOT NULL DEFAULT 1,
    sort_order      INT             NOT NULL DEFAULT 0,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_menu_category (category_id),
    FOREIGN KEY (category_id)   REFERENCES menu_categories(id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `restaurant_tables`

```sql
CREATE TABLE restaurant_tables (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    restaurant_id   BIGINT          NOT NULL,
    table_code      VARCHAR(20)     NOT NULL,
    capacity        TINYINT         NOT NULL,
    zone_vi         VARCHAR(50)     NULL,
    zone_en         VARCHAR(50)     NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,

    PRIMARY KEY (id),
    UNIQUE KEY uq_table_code (restaurant_id, table_code),
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 5. Nhóm bảng: Order & Booking (Hybrid Flow)

> **Luồng:** `Cart → Order (PENDING) → Payment → Order (PAID) → Bookings (CONFIRMED) → Staff xử lý`

### `orders`

```sql
CREATE TABLE orders (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    order_code      VARCHAR(30)     NOT NULL,   -- "TN-20260512-00123"
    user_id         BIGINT          NOT NULL,
    subtotal        DECIMAL(15,2)   NOT NULL,
    discount_amount DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(15,2)   NOT NULL,
    voucher_id      BIGINT          NULL,
    voucher_code    VARCHAR(50)     NULL,       -- Snapshot
    special_requests TEXT           NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING',
    lang            VARCHAR(5)      NOT NULL DEFAULT 'vi',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_order_code (order_code),
    INDEX idx_order_user (user_id),
    INDEX idx_order_status (status),
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `order_items`

```sql
CREATE TABLE order_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    order_id        BIGINT          NOT NULL,
    service_type    VARCHAR(20)     NOT NULL,   -- HOTEL | TOUR | RESTAURANT
    service_id      BIGINT          NOT NULL,
    service_name    VARCHAR(255)    NOT NULL,   -- Snapshot
    variant_id      BIGINT          NULL,       -- room_type_id | tour_slot_id | table_id
    variant_name    VARCHAR(255)    NULL,       -- Snapshot
    unit_price      DECIMAL(15,2)   NOT NULL,   -- Snapshot giá lúc đặt
    quantity        INT             NOT NULL DEFAULT 1,
    subtotal        DECIMAL(15,2)   NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_oi_order (order_id),
    INDEX idx_oi_service (service_type, service_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **Tại sao snapshot tên/giá?** Nếu Admin sửa giá dịch vụ sau khi đặt, lịch sử đơn hàng phải giữ nguyên giá trị gốc.

---

### `bookings`

```sql
CREATE TABLE bookings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_code    VARCHAR(30)     NOT NULL,   -- "BK-20260512-00456"
    order_id        BIGINT          NOT NULL,
    order_item_id   BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    service_type    VARCHAR(20)     NOT NULL,   -- HOTEL | TOUR | RESTAURANT
    service_id      BIGINT          NOT NULL,
    contact_name    VARCHAR(150)    NOT NULL,
    contact_phone   VARCHAR(20)     NOT NULL,
    contact_email   VARCHAR(255)    NOT NULL,
    guest_count     INT             NOT NULL DEFAULT 1,
    special_requests TEXT           NULL,
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING_CONFIRMATION',
    staff_id        BIGINT          NULL,
    staff_note      TEXT            NULL,       -- Internal note
    confirmed_at    DATETIME        NULL,
    cancelled_at    DATETIME        NULL,
    cancel_reason   TEXT            NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_booking_code (booking_code),
    INDEX idx_booking_order (order_id),
    INDEX idx_booking_user (user_id),
    INDEX idx_booking_staff (staff_id),
    INDEX idx_booking_service (service_type, service_id),
    INDEX idx_booking_status (status),
    FOREIGN KEY (order_id)      REFERENCES orders(id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id),
    FOREIGN KEY (user_id)       REFERENCES users(id),
    FOREIGN KEY (staff_id)      REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `hotel_bookings`

```sql
CREATE TABLE hotel_bookings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL UNIQUE,
    hotel_id        BIGINT          NOT NULL,
    room_type_id    BIGINT          NOT NULL,
    check_in_date   DATE            NOT NULL,
    check_out_date  DATE            NOT NULL,
    num_nights      INT             NOT NULL,
    num_rooms       INT             NOT NULL DEFAULT 1,
    price_per_night DECIMAL(15,2)   NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (booking_id)   REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (hotel_id)     REFERENCES hotels(id),
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `tour_bookings`

```sql
CREATE TABLE tour_bookings (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id          BIGINT          NOT NULL UNIQUE,
    tour_id             BIGINT          NOT NULL,
    tour_slot_id        BIGINT          NOT NULL,
    num_adults          INT             NOT NULL DEFAULT 1,
    num_children        INT             NOT NULL DEFAULT 0,
    price_per_person    DECIMAL(15,2)   NOT NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (booking_id)   REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (tour_id)      REFERENCES tours(id),
    FOREIGN KEY (tour_slot_id) REFERENCES tour_slots(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `restaurant_bookings`

```sql
CREATE TABLE restaurant_bookings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL UNIQUE,
    restaurant_id   BIGINT          NOT NULL,
    table_id        BIGINT          NULL,
    booking_date    DATE            NOT NULL,
    booking_time    TIME            NOT NULL,
    num_guests      INT             NOT NULL DEFAULT 1,
    seating_pref_vi VARCHAR(100)    NULL,
    seating_pref_en VARCHAR(100)    NULL,

    PRIMARY KEY (id),
    FOREIGN KEY (booking_id)    REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
    FOREIGN KEY (table_id)      REFERENCES restaurant_tables(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 6. Nhóm bảng: Payment & Refund

### `payments`

```sql
CREATE TABLE payments (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    order_id            BIGINT          NOT NULL,
    payment_code        VARCHAR(50)     NOT NULL,
    gateway             VARCHAR(20)     NOT NULL,   -- VNPAY | MOMO
    gateway_txn_id      VARCHAR(100)    NULL,
    gateway_order_id    VARCHAR(100)    NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    currency            VARCHAR(5)      NOT NULL DEFAULT 'VND',
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    paid_at             DATETIME        NULL,
    gateway_response    JSON            NULL,
    ip_address          VARCHAR(50)     NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_code (payment_code),
    INDEX idx_payment_order (order_id),
    INDEX idx_payment_gateway_txn (gateway_txn_id),
    INDEX idx_payment_status (status),
    FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

### `refunds`

```sql
CREATE TABLE refunds (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    payment_id          BIGINT          NOT NULL,
    booking_id          BIGINT          NULL,
    refund_code         VARCHAR(50)     NOT NULL,
    amount              DECIMAL(15,2)   NOT NULL,
    reason              TEXT            NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    gateway_refund_id   VARCHAR(100)    NULL,
    gateway_response    JSON            NULL,
    requested_by        BIGINT          NOT NULL,
    processed_by        BIGINT          NULL,
    requested_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at        DATETIME        NULL,
    note                TEXT            NULL,

    PRIMARY KEY (id),
    UNIQUE KEY uq_refund_code (refund_code),
    INDEX idx_refund_payment (payment_id),
    INDEX idx_refund_booking (booking_id),
    FOREIGN KEY (payment_id)   REFERENCES payments(id),
    FOREIGN KEY (booking_id)   REFERENCES bookings(id) ON DELETE SET NULL,
    FOREIGN KEY (requested_by) REFERENCES users(id),
    FOREIGN KEY (processed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 7. Nhóm bảng: Review

### `reviews`

```sql
CREATE TABLE reviews (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL,
    service_type    VARCHAR(20)     NOT NULL,
    service_id      BIGINT          NOT NULL,
    rating          TINYINT         NOT NULL,
    rating_service  TINYINT         NULL,
    rating_location TINYINT         NULL,
    rating_value    TINYINT         NULL,
    rating_clean    TINYINT         NULL,
    comment         TEXT            NULL,
    is_approved     TINYINT(1)      NOT NULL DEFAULT 1,
    helpful_count   INT             NOT NULL DEFAULT 0,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_review_service (service_type, service_id),
    INDEX idx_review_user (user_id),
    FOREIGN KEY (booking_id) REFERENCES bookings(id),
    FOREIGN KEY (user_id)    REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `review_images`

```sql
CREATE TABLE review_images (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    review_id   BIGINT          NOT NULL,
    image_url   VARCHAR(500)    NOT NULL,
    sort_order  INT             NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `review_helpful_votes`

```sql
CREATE TABLE review_helpful_votes (
    user_id     BIGINT      NOT NULL,
    review_id   BIGINT      NOT NULL,
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, review_id),
    FOREIGN KEY (user_id)   REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 8. Nhóm bảng: Voucher & Discount

### `vouchers`

```sql
CREATE TABLE vouchers (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    code                VARCHAR(50)     NOT NULL,
    name_vi             VARCHAR(150)    NOT NULL,
    name_en             VARCHAR(150)    NOT NULL,
    description_vi      TEXT            NULL,
    description_en      TEXT            NULL,
    discount_type       VARCHAR(20)     NOT NULL,   -- PERCENTAGE | FIXED_AMOUNT
    discount_value      DECIMAL(15,2)   NOT NULL,
    max_discount_amount DECIMAL(15,2)   NULL,       -- Giảm tối đa (cho PERCENTAGE)
    min_order_amount    DECIMAL(15,2)   NOT NULL DEFAULT 0.00,
    applies_to          VARCHAR(20)     NOT NULL DEFAULT 'ALL',
    total_quantity      INT             NULL,
    used_quantity       INT             NOT NULL DEFAULT 0,
    per_user_limit      INT             NOT NULL DEFAULT 1,
    is_combo            TINYINT(1)      NOT NULL DEFAULT 0,  -- 1 = chỉ áp khi đặt combo
    valid_from          DATETIME        NOT NULL,
    valid_until         DATETIME        NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    is_deleted          TINYINT(1)      NOT NULL DEFAULT 0,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_voucher_code (code),
    INDEX idx_voucher_status (status),
    INDEX idx_voucher_valid (valid_from, valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `voucher_usages`

```sql
CREATE TABLE voucher_usages (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    voucher_id  BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    order_id    BIGINT      NOT NULL,
    used_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_vu_voucher (voucher_id),
    INDEX idx_vu_user (user_id),
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id),
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (order_id)   REFERENCES orders(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 9. Nhóm bảng: Config & CMS

### `destinations`

```sql
CREATE TABLE destinations (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name_vi         VARCHAR(100)    NOT NULL,
    name_en         VARCHAR(100)    NOT NULL,
    slug            VARCHAR(120)    NOT NULL,
    description_vi  TEXT            NULL,
    description_en  TEXT            NULL,
    thumbnail_url   VARCHAR(500)    NULL,
    sort_order      INT             NOT NULL DEFAULT 0,
    is_featured     TINYINT(1)      NOT NULL DEFAULT 0,
    is_deleted      TINYINT(1)      NOT NULL DEFAULT 0,

    PRIMARY KEY (id),
    UNIQUE KEY uq_dest_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `banners`

```sql
CREATE TABLE banners (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    title_vi            VARCHAR(150)    NULL,
    title_en            VARCHAR(150)    NULL,
    image_url           VARCHAR(500)    NOT NULL,
    link_url            VARCHAR(500)    NULL,
    position            VARCHAR(30)     NOT NULL DEFAULT 'HOME_HERO',
    sort_order          INT             NOT NULL DEFAULT 0,
    open_new_tab        TINYINT(1)      NOT NULL DEFAULT 0,
    start_date          DATETIME        NULL,
    end_date            DATETIME        NULL,
    click_count         INT             NOT NULL DEFAULT 0,
    impression_count    INT             NOT NULL DEFAULT 0,
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    INDEX idx_banner_position (position, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### `system_configs`

```sql
CREATE TABLE system_configs (
    config_key      VARCHAR(100)    NOT NULL,
    config_value    TEXT            NOT NULL,
    description     VARCHAR(255)    NULL,
    updated_by      BIGINT          NULL,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (config_key),
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO system_configs (config_key, config_value, description) VALUES
('site_name_vi',             'TravelNest',           'Tên thương hiệu tiếng Việt'),
('site_name_en',             'TravelNest',           'Brand name in English'),
('support_email',            'support@travelnest.vn','Email hỗ trợ khách hàng'),
('support_phone',            '1900-xxxx',            'Hotline hỗ trợ'),
('vnpay_enabled',            'true',                 'Bật/tắt VNPay'),
('momo_enabled',             'true',                 'Bật/tắt MoMo'),
('max_advance_booking_days', '365',                  'Đặt trước tối đa bao nhiêu ngày');
```

---

## 10. Enum Reference

> Tất cả enum lưu dạng `VARCHAR` trong DB. Định nghĩa tập trung tại đây.

| Bảng | Cột | Giá trị hợp lệ |
|---|---|---|
| `users` | `role` | `CUSTOMER` · `STAFF` · `ADMIN` |
| `users` | `status` | `ACTIVE` · `BANNED` · `UNVERIFIED` · `INACTIVE` |
| `users` | `gender` | `MALE` · `FEMALE` · `OTHER` |
| `hotels` · `tours` · `restaurants` | `status` | `ACTIVE` · `INACTIVE` · `DRAFT` |
| `tour_slots` | `status` | `OPEN` · `FULL` · `CANCELLED` · `COMPLETED` |
| `orders` | `status` | `PENDING` · `PAID` · `PARTIALLY_CANCELLED` · `COMPLETED` · `CANCELLED` · `REFUNDED` |
| `bookings` | `status` | `PENDING_CONFIRMATION` · `CONFIRMED` · `IN_PROGRESS` · `COMPLETED` · `CANCELLED` · `NO_SHOW` |
| `payments` | `status` | `PENDING` · `SUCCESS` · `FAILED` · `CANCELLED` · `REFUNDED` · `PARTIAL_REFUNDED` |
| `payments` | `gateway` | `VNPAY` · `MOMO` |
| `refunds` | `status` | `PENDING` · `PROCESSING` · `SUCCESS` · `FAILED` |
| `vouchers` | `discount_type` | `PERCENTAGE` · `FIXED_AMOUNT` |
| `vouchers` | `applies_to` | `ALL` · `HOTEL` · `TOUR` · `RESTAURANT` |
| `order_items` · `bookings` | `service_type` | `HOTEL` · `TOUR` · `RESTAURANT` |
| `banners` | `position` | `HOME_HERO` · `HOME_MIDDLE` · `CATEGORY_TOP` |
| `otp_tokens` | `type` | `EMAIL_VERIFY` · `PASSWORD_RESET` |

---

## 11. Indexing Strategy

| Query pattern thường gặp | Index |
|---|---|
| Login — tìm user theo email | `users.email` UNIQUE |
| Lọc khách sạn theo tỉnh | `hotels.province` |
| Lọc tour theo ngày khởi hành | `tour_slots.start_date` |
| Booking của 1 user | `bookings.user_id` |
| Staff xem booking cần xử lý | `bookings.status` |
| Thanh toán theo order | `payments.order_id` |
| Callback gateway | `payments.gateway_txn_id` |
| Kiểm tra voucher code | `vouchers.code` UNIQUE |
| Review theo dịch vụ | `reviews.service_type + service_id` |
| Banner theo vị trí | `banners.position + is_active` |

---

## Tóm tắt số bảng

| Nhóm | Số bảng |
|---|---|
| Auth & Users | 8 |
| Hotel | 5 |
| Tour | 4 |
| Restaurant | 5 |
| Order & Booking | 6 |
| Payment & Refund | 2 |
| Review | 3 |
| Voucher | 2 |
| Config & CMS | 3 |
| **Tổng** | **38** |

---

## Ghi chú phiên bản

| Phiên bản | Ngày | Ghi chú |
|---|---|---|
| v1.0 | 2026-05-12 | Khởi tạo — MySQL 8.0, Hybrid Booking Flow, Bilingual |

---

> **Lưu ý triển khai:**
> - Dùng **Flyway** để version control schema — tạo `V1__init_schema.sql` từ doc này.
> - Mọi thay đổi schema phải tạo migration file mới (`V2__add_xxx.sql`), không sửa file cũ.
> - Bật `spring.jpa.hibernate.ddl-auto=validate` trên production — Flyway quản lý schema, không để Hibernate tự tạo.
> - `orders` có FK đến `vouchers` — tạo bảng `vouchers` **trước** `orders` khi chạy migration.
