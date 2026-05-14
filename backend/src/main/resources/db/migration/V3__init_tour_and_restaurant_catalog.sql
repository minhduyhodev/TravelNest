CREATE TABLE tours (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name_vi VARCHAR(255) NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    slug VARCHAR(300) NOT NULL,
    description_vi TEXT NULL,
    description_en TEXT NULL,
    duration_days TINYINT NOT NULL DEFAULT 1,
    duration_nights TINYINT NOT NULL DEFAULT 0,
    destination_vi VARCHAR(150) NOT NULL,
    destination_en VARCHAR(150) NOT NULL,
    departure_point_vi VARCHAR(255) NULL,
    departure_point_en VARCHAR(255) NULL,
    max_guests INT NOT NULL DEFAULT 20,
    min_guests INT NOT NULL DEFAULT 1,
    includes_vi TEXT NULL,
    includes_en TEXT NULL,
    excludes_vi TEXT NULL,
    excludes_en TEXT NULL,
    requirements_vi TEXT NULL,
    requirements_en TEXT NULL,
    thumbnail_url VARCHAR(500) NULL,
    avg_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_tours_slug (slug),
    INDEX idx_tours_destination (destination_vi),
    INDEX idx_tours_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tour_itineraries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    day_number TINYINT NOT NULL,
    title_vi VARCHAR(255) NOT NULL,
    title_en VARCHAR(255) NOT NULL,
    description_vi TEXT NULL,
    description_en TEXT NULL,
    meals_vi VARCHAR(100) NULL,
    meals_en VARCHAR(100) NULL,
    accommodation_vi VARCHAR(150) NULL,
    accommodation_en VARCHAR(150) NULL,
    PRIMARY KEY (id),
    INDEX idx_tour_itineraries_tour_id (tour_id),
    CONSTRAINT fk_tour_itineraries_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tour_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_tour_images_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE tour_slots (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tour_id BIGINT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    departure_time TIME NULL,
    price_per_person DECIMAL(15, 2) NOT NULL,
    total_slots INT NOT NULL,
    booked_slots INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    note_vi VARCHAR(255) NULL,
    note_en VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_tour_slots_tour_id (tour_id),
    INDEX idx_tour_slots_start_date (start_date),
    CONSTRAINT fk_tour_slots_tour FOREIGN KEY (tour_id) REFERENCES tours(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE restaurants (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name_vi VARCHAR(255) NOT NULL,
    name_en VARCHAR(255) NOT NULL,
    slug VARCHAR(300) NOT NULL,
    description_vi TEXT NULL,
    description_en TEXT NULL,
    cuisine_type_vi VARCHAR(100) NULL,
    cuisine_type_en VARCHAR(100) NULL,
    address VARCHAR(255) NOT NULL,
    district VARCHAR(100) NOT NULL,
    province VARCHAR(100) NOT NULL,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    phone VARCHAR(20) NULL,
    email VARCHAR(255) NULL,
    open_time TIME NOT NULL DEFAULT '10:00:00',
    close_time TIME NOT NULL DEFAULT '22:00:00',
    price_range VARCHAR(20) NULL,
    cancel_policy_vi TEXT NULL,
    cancel_policy_en TEXT NULL,
    thumbnail_url VARCHAR(500) NULL,
    avg_rating DECIMAL(3, 2) NOT NULL DEFAULT 0.00,
    total_reviews INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_restaurants_slug (slug),
    INDEX idx_restaurants_district (district),
    INDEX idx_restaurants_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE restaurant_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_restaurant_images_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    name_vi VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_menu_categories_restaurant_id (restaurant_id),
    CONSTRAINT fk_menu_categories_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE menu_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL,
    name_vi VARCHAR(150) NOT NULL,
    name_en VARCHAR(150) NOT NULL,
    description_vi TEXT NULL,
    description_en TEXT NULL,
    price DECIMAL(15, 2) NOT NULL,
    image_url VARCHAR(500) NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    is_deleted TINYINT(1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_menu_items_category_id (category_id),
    INDEX idx_menu_items_restaurant_id (restaurant_id),
    CONSTRAINT fk_menu_items_category FOREIGN KEY (category_id) REFERENCES menu_categories(id) ON DELETE CASCADE,
    CONSTRAINT fk_menu_items_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE restaurant_tables (
    id BIGINT NOT NULL AUTO_INCREMENT,
    restaurant_id BIGINT NOT NULL,
    table_code VARCHAR(20) NOT NULL,
    capacity TINYINT NOT NULL,
    zone_vi VARCHAR(50) NULL,
    zone_en VARCHAR(50) NULL,
    is_active TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_restaurant_tables_code (restaurant_id, table_code),
    CONSTRAINT fk_restaurant_tables_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO tours (
    name_vi, name_en, slug, description_vi, description_en, duration_days, duration_nights,
    destination_vi, destination_en, departure_point_vi, departure_point_en, max_guests, min_guests,
    includes_vi, includes_en, excludes_vi, excludes_en, requirements_vi, requirements_en,
    avg_rating, total_reviews, status
) VALUES
(
    'Ha Giang Loop Escape',
    'Ha Giang Loop Escape',
    'ha-giang-loop-escape',
    'Hanh trinh vong cung cao nguyen voi diem dung ngam canh, homestay va doi ho tro.',
    'Three-day highland circuit with curated viewpoints, village stays, and support crew.',
    3,
    2,
    'Ha Giang',
    'Ha Giang',
    'Ha Giang City',
    'Ha Giang City',
    18,
    1,
    'Ma Pi Leng Pass, Ethnic village homestay, Photo support, Breakfast and dinner',
    'Ma Pi Leng Pass, Ethnic village homestay, Photo support, Breakfast and dinner',
    'Personal travel insurance',
    'Personal travel insurance',
    'Suitable for riders comfortable with mountain roads',
    'Suitable for riders comfortable with mountain roads',
    9.50,
    121,
    'ACTIVE'
),
(
    'Mekong Floating Market Day',
    'Mekong Floating Market Day',
    'mekong-floating-market-day',
    'Chuyen di trong ngay qua cho noi, vuon trai cay va diem dung nau an dia phuong.',
    'A one-day river journey through floating markets, fruit gardens, and local cooking stops.',
    1,
    0,
    'Can Tho',
    'Can Tho',
    'Ninh Kieu Pier',
    'Ninh Kieu Pier',
    22,
    1,
    'Cai Rang market, Boat breakfast, Fruit orchard visit, Local kitchen demo',
    'Cai Rang market, Boat breakfast, Fruit orchard visit, Local kitchen demo',
    'Personal shopping expenses',
    'Personal shopping expenses',
    'Best suited for early risers and light walking',
    'Best suited for early risers and light walking',
    8.70,
    83,
    'ACTIVE'
),
(
    'Lan Ha Bay Slow Cruise',
    'Lan Ha Bay Slow Cruise',
    'lan-ha-bay-slow-cruise',
    'Hai ngay du thuyen qua cac vung nuoc yen, co kayak va tham hang.',
    'Two-day bay cruise with kayaking, cave access, and a quieter route away from crowded piers.',
    2,
    1,
    'Hai Phong',
    'Hai Phong',
    'Got Ferry Terminal',
    'Got Ferry Terminal',
    16,
    1,
    'Kayak session, Sunset deck dinner, Cave visit, Small-group cruise',
    'Kayak session, Sunset deck dinner, Cave visit, Small-group cruise',
    'Personal beverages outside meal plan',
    'Personal beverages outside meal plan',
    'Bring light luggage and comfortable footwear',
    'Bring light luggage and comfortable footwear',
    9.10,
    98,
    'ACTIVE'
);

INSERT INTO tour_itineraries (
    tour_id, day_number, title_vi, title_en, description_vi, description_en, meals_vi, meals_en, accommodation_vi, accommodation_en
) VALUES
((SELECT id FROM tours WHERE slug = 'ha-giang-loop-escape'), 1, 'City to Yen Minh', 'City to Yen Minh', 'Khoi hanh tu thanh pho va di qua cac diem ngam canh noi bat.', 'Depart from the city and pass signature viewpoints on the way to Yen Minh.', 'Breakfast, Dinner', 'Breakfast, Dinner', 'Village homestay', 'Village homestay'),
((SELECT id FROM tours WHERE slug = 'ha-giang-loop-escape'), 2, 'Dong Van to Meo Vac', 'Dong Van to Meo Vac', 'Vuot deo Ma Pi Leng va tham quan ban dia phuong.', 'Cross Ma Pi Leng Pass and visit local villages.', 'Breakfast, Dinner', 'Breakfast, Dinner', 'Mountain guesthouse', 'Mountain guesthouse'),
((SELECT id FROM tours WHERE slug = 'ha-giang-loop-escape'), 3, 'Valley route return', 'Valley route return', 'Tro ve Ha Giang qua cung duong thung lung.', 'Return to Ha Giang via the valley route.', 'Breakfast', 'Breakfast', NULL, NULL),
((SELECT id FROM tours WHERE slug = 'mekong-floating-market-day'), 1, 'Sunrise market ride', 'Sunrise market ride', 'Len thuyen luc som, tham cho noi va dung an sang tren song.', 'Board early, visit the floating market, and stop for breakfast on the river.', 'Breakfast, Lunch', 'Breakfast, Lunch', NULL, NULL),
((SELECT id FROM tours WHERE slug = 'lan-ha-bay-slow-cruise'), 1, 'Harbor to hidden coves', 'Harbor to hidden coves', 'Len du thuyen, an trua va kayak tai vung nuoc yen.', 'Board the cruise, enjoy lunch, and kayak through quiet coves.', 'Lunch, Dinner', 'Lunch, Dinner', 'Cruise cabin', 'Cruise cabin'),
((SELECT id FROM tours WHERE slug = 'lan-ha-bay-slow-cruise'), 2, 'Morning paddle and brunch return', 'Morning paddle and brunch return', 'Cheo kayak buoi sang va tro ve ben truoc buoi chieu.', 'Morning paddle before returning to the harbor for brunch.', 'Brunch', 'Brunch', NULL, NULL);

INSERT INTO tour_slots (
    tour_id, start_date, end_date, departure_time, price_per_person, total_slots, booked_slots, status, note_vi, note_en
) VALUES
((SELECT id FROM tours WHERE slug = 'ha-giang-loop-escape'), '2026-06-01', '2026-06-03', '06:00:00', 4290000, 18, 7, 'OPEN', 'Khoi hanh dau thang 6', 'Early June departure'),
((SELECT id FROM tours WHERE slug = 'ha-giang-loop-escape'), '2026-06-08', '2026-06-10', '06:00:00', 4290000, 18, 9, 'OPEN', 'Khoi hanh hang tuan', 'Weekly departure'),
((SELECT id FROM tours WHERE slug = 'mekong-floating-market-day'), '2026-06-05', '2026-06-05', '06:00:00', 1450000, 22, 11, 'OPEN', 'Khoi hanh moi sang', 'Morning departure'),
((SELECT id FROM tours WHERE slug = 'mekong-floating-market-day'), '2026-06-06', '2026-06-06', '06:00:00', 1450000, 22, 13, 'OPEN', 'Khoi hanh cuoi tuan', 'Weekend departure'),
((SELECT id FROM tours WHERE slug = 'lan-ha-bay-slow-cruise'), '2026-06-12', '2026-06-13', '11:30:00', 3890000, 16, 6, 'OPEN', 'Du thuyen nhom nho', 'Small-group cruise'),
((SELECT id FROM tours WHERE slug = 'lan-ha-bay-slow-cruise'), '2026-06-19', '2026-06-20', '11:30:00', 3890000, 16, 8, 'OPEN', 'Khoi hanh cuoi tuan', 'Weekend sailing');

INSERT INTO restaurants (
    name_vi, name_en, slug, description_vi, description_en, cuisine_type_vi, cuisine_type_en,
    address, district, province, phone, email, open_time, close_time, price_range,
    cancel_policy_vi, cancel_policy_en, avg_rating, total_reviews, status
) VALUES
(
    'Ember Riverside Grill',
    'Ember Riverside Grill',
    'ember-riverside-grill',
    'Nha hang nuong hai san ben song voi terrace thoang va goc ban rieng cho buoi toi.',
    'Fire-grilled seafood and sharing plates with a breezy riverfront terrace for evening bookings.',
    'Seafood and grill',
    'Seafood and grill',
    '18 Bach Dang',
    'Hai Chau',
    'Da Nang',
    '02363881111',
    'hello@embergrill.vn',
    '11:00:00',
    '22:30:00',
    'PREMIUM',
    'Giu ban 15 phut sau gio hen. Huy truoc 2 gio de tranh mat cho.',
    'Tables are held for 15 minutes. Please cancel at least two hours in advance.',
    9.00,
    142,
    'ACTIVE'
),
(
    'Lantern Garden Bistro',
    'Lantern Garden Bistro',
    'lantern-garden-bistro',
    'Khu vuon an toi voi mon Viet hien dai, phu hop cho cap doi va buoi toi yen tinh.',
    'Garden dining with modern Vietnamese tasting plates, ideal for quiet reservations and couples.',
    'Modern Vietnamese',
    'Modern Vietnamese',
    '42 Tran Phu',
    'Cam Chau',
    'Hoi An',
    '02353992222',
    'book@lanterngarden.vn',
    '10:30:00',
    '21:30:00',
    'MID',
    'Co the doi gio trong pham vi con cho trong ngay.',
    'Reservation times can be adjusted when same-day capacity is still available.',
    8.80,
    97,
    'ACTIVE'
),
(
    'Skyline Noodle Kitchen',
    'Skyline Noodle Kitchen',
    'skyline-noodle-kitchen',
    'Quan noodle tren cao voi khung gio dat ban nhanh cho bua trua va nhom nho.',
    'High-floor noodle bar with quick reservation windows for lunch meetings and small groups.',
    'Noodles and comfort food',
    'Noodles and comfort food',
    '88 Nguyen Hue',
    'District 1',
    'Ho Chi Minh City',
    '02839112222',
    'team@skylinekitchen.vn',
    '09:00:00',
    '21:00:00',
    'BUDGET',
    'Ban giu trong 10 phut. Uu tien khach dat truoc gio cao diem.',
    'Reservations are held for 10 minutes. Advance booking is recommended for peak hours.',
    8.60,
    118,
    'ACTIVE'
);

INSERT INTO menu_categories (restaurant_id, name_vi, name_en, sort_order) VALUES
((SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'), 'Signature grill', 'Signature grill', 1),
((SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'), 'Seafood sharing', 'Seafood sharing', 2),
((SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'), 'Garden tasting', 'Garden tasting', 1),
((SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'), 'Dessert and coffee', 'Dessert and coffee', 2),
((SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'), 'House noodles', 'House noodles', 1),
((SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'), 'Small plates', 'Small plates', 2);

INSERT INTO menu_items (
    category_id, restaurant_id, name_vi, name_en, description_vi, description_en, price, sort_order
) VALUES
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill') AND name_en = 'Signature grill'),
 (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'),
 'Charred squid skewers', 'Charred squid skewers', 'Muc nuong xien than than.', 'Charred squid skewers finished over live fire.', 380000, 1),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill') AND name_en = 'Seafood sharing'),
 (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'),
 'Signature river prawns', 'Signature river prawns', 'Tom song nuong phan lon.', 'Large river prawns for sharing.', 520000, 2),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill') AND name_en = 'Seafood sharing'),
 (SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'),
 'Lemongrass clams', 'Lemongrass clams', 'Ngheu xa nong.', 'Clams in lemongrass broth.', 410000, 3),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro') AND name_en = 'Garden tasting'),
 (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'),
 'Hoi An rose dumplings', 'Hoi An rose dumplings', 'Ban bao hoa hong Hoi An.', 'Hoi An rose dumplings with herbs.', 320000, 1),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro') AND name_en = 'Garden tasting'),
 (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'),
 'Claypot aubergine', 'Claypot aubergine', 'Ca tim kho niu dat.', 'Claypot aubergine with savory glaze.', 360000, 2),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro') AND name_en = 'Dessert and coffee'),
 (SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'),
 'Coconut coffee panna cotta', 'Coconut coffee panna cotta', 'Panna cotta ca phe dua.', 'Coconut coffee panna cotta.', 340000, 3),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen') AND name_en = 'House noodles'),
 (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'),
 'Lemongrass beef noodles', 'Lemongrass beef noodles', 'Mi bo sa chanh dac trung.', 'Signature lemongrass beef noodles.', 180000, 1),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen') AND name_en = 'Small plates'),
 (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'),
 'Crispy spring rolls', 'Crispy spring rolls', 'Cha gio gion an kem.', 'Crispy spring rolls for sharing.', 210000, 2),
((SELECT id FROM menu_categories WHERE restaurant_id = (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen') AND name_en = 'Small plates'),
 (SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'),
 'Black sesame ice cream', 'Black sesame ice cream', 'Kem me den.', 'Black sesame ice cream.', 190000, 3);

INSERT INTO restaurant_tables (restaurant_id, table_code, capacity, zone_vi, zone_en) VALUES
((SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'), 'T01', 2, 'Terrace', 'Terrace'),
((SELECT id FROM restaurants WHERE slug = 'ember-riverside-grill'), 'T02', 4, 'Riverfront', 'Riverfront'),
((SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'), 'G01', 2, 'Garden', 'Garden'),
((SELECT id FROM restaurants WHERE slug = 'lantern-garden-bistro'), 'G02', 4, 'Lantern court', 'Lantern court'),
((SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'), 'W01', 2, 'Window', 'Window'),
((SELECT id FROM restaurants WHERE slug = 'skyline-noodle-kitchen'), 'W02', 6, 'Meeting table', 'Meeting table');
