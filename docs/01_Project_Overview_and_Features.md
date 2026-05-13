# TravelNest — Project Overview & Features

> **Nền tảng Đặt Dịch Vụ Du Lịch Tích Hợp**
> _(All-in-one Travel Service Booking — Hotel · Tour · Restaurant)_
> Tài liệu quy chuẩn — Source of Truth cho toàn bộ quá trình phát triển.

---

## Mục lục (Table of Contents)

- [1. Tổng quan dự án](#1-tổng-quan-dự-án)
- [2. Tech Stack](#2-tech-stack)
- [3. Kiến trúc hệ thống](#3-kiến-trúc-hệ-thống)
  - [2.6. Frontend Setup & Workflow](#26-frontend-setup--workflow)
  - [3.1. Frontend SPA Architecture](#31-frontend-spa-architecture)
- [4. Quy chuẩn UI/UX & Design System](#4-quy-chuẩn-uiux--design-system)
- [5. Chiến lược Render (Web)](#5-chiến-lược-render-web)
- [6. Chiến lược i18n (Đa ngôn ngữ)](#6-chiến-lược-i18n-đa-ngôn-ngữ)
- [7. Phân tích Roles & Tính năng](#7-phân-tích-roles--tính-năng)
  - [7.1. USER (Khách đặt dịch vụ)](#71-user-khách-đặt-dịch-vụ)
  - [7.2. STAFF (Nhân viên vận hành)](#72-staff-nhân-viên-vận-hành)
  - [7.3. ADMIN (Quản trị viên)](#73-admin-quản-trị-viên)

---

## 1. Tổng quan dự án

### 1.1. Mục tiêu

Xây dựng **nền tảng đặt dịch vụ du lịch tích hợp (All-in-one Travel Booking Platform)**, tự vận hành (agency model), hỗ trợ **Web App**, với các mục tiêu cốt lõi:

- **Đa dịch vụ trên 1 nền tảng**: Khách hàng có thể đặt Khách sạn, Tour du lịch và Nhà hàng trong cùng một tài khoản.
- **Frontend đơn giản & hiệu năng tốt**: Tối ưu Core Web Vitals, bundle size và trải nghiệm client-side.
- **Thanh toán online tích hợp**: Hỗ trợ VNPay và MoMo — chuẩn thị trường Việt Nam.
- **Bilingual (Việt – Anh)**: Phục vụ cả khách nội địa lẫn khách quốc tế.
- **Kiến trúc mở rộng**: RESTful API chuẩn.

### 1.2. Phạm vi dự án (Scope)

| Module          | Mô tả                                              | MVP |
| --------------- | -------------------------------------------------- | :-: |
| 🏨 Khách sạn    | Tìm kiếm, xem phòng, đặt phòng theo ngày         | ✅  |
| 🗺️ Tour         | Xem lịch trình, đặt slot tour theo ngày/giờ       | ✅  |
| 🍽️ Nhà hàng     | Xem thực đơn, đặt bàn theo khung giờ             | ✅  |
| 💳 Thanh toán   | VNPay + MoMo, lưu lịch sử giao dịch              | ✅  |
| 👤 Tài khoản    | Đăng ký/đăng nhập, quản lý hồ sơ                 | ✅  |
| 🌐 Web App      | React SPA (Vite + React Router DOM, Responsive) | ✅  |
| 📊 Admin Panel  | Quản lý nội dung, duyệt đơn, thống kê            | ✅  |
| ⚙️ Staff Panel  | Xác nhận booking, cập nhật trạng thái            | ✅  |
| 📣 Notification | Email + Push notification                         | ⬜  |
| 🗺️ Map tích hợp | Google Maps cho vị trí khách sạn/nhà hàng         | ⬜  |

> ✅ MVP · ⬜ Phase 2

---

## 2. Tech Stack

> **Lý do chọn:** Toàn bộ stack hướng tới **1 developer solo** — ưu tiên năng suất, tài liệu phong phú, và cộng đồng lớn.

### 2.1. Frontend — Web

| Layer           | Công nghệ                   | Lý do chọn                                                                        |
| --------------- | --------------------------- | --------------------------------------------------------------------------------- |
| **Bundler / Dev Server** | Vite                     | Khởi tạo nhanh, HMR tốt, cấu hình nhẹ, phù hợp solo dev                         |
| **UI Library**  | React 18 + JSX             | Component model mạnh, ecosystem lớn, phù hợp SPA hiện đại                        |
| **Routing**     | React Router DOM           | Routing rõ ràng cho Public, User, Staff, Admin trong mô hình SPA                 |
| **Styling**     | Tailwind CSS v3             | Utility-first — tốc độ phát triển nhanh, không cần đặt tên class                 |
| **State Client**| Zustand                    | Nhẹ hơn Redux, boilerplate ít, phù hợp solo dev                                   |
| **Data Fetch**  | TanStack Query (React Query)| Cache tự động, loading/error states, tối ưu UX                                    |
| **Form**        | React Hook Form + Zod      | Hiệu năng cao, validation type-safe                                                |
| **i18n**        | react-i18next              | Linh hoạt cho React SPA, dễ tổ chức namespace và lazy-load translation            |

### 2.2. Backend

| Layer           | Công nghệ                   | Lý do chọn                                                                        |
| --------------- | --------------------------- | --------------------------------------------------------------------------------- |
| **Runtime**     | Java 21 (JDK 21)           | LTS version, Virtual Threads (Project Loom) — concurrency tốt cho booking        |
| **Framework**   | Spring Boot 3.x            | Production-ready, auto-configuration, tích hợp Spring Security dễ dàng          |
| **API Style**   | RESTful API                | Đơn giản, stateless, dễ consume từ web                                           |
| **ORM**         | Spring Data JPA (Hibernate)| Tự động generate query, giảm boilerplate SQL                                      |
| **Security**    | Spring Security + JWT      | Chuẩn industry, stateless auth phù hợp API                                      |
| **Validation**  | Bean Validation (Jakarta)  | Tích hợp sẵn Spring, annotation-based                                             |
| **Docs API**    | SpringDoc OpenAPI (Swagger)| Tự động generate API docs — hữu ích khi bảo vệ đồ án                            |

### 2.4. Database & Storage

| Layer           | Công nghệ                   | Lý do chọn                                                                        |
| --------------- | --------------------------- | --------------------------------------------------------------------------------- |
| **Database**    | MySQL 8.0+                 | ACID, phổ biến, dễ setup, hiệu năng tốt cho booking system                      |
| **ORM**         | Hibernate / JPA             | Native với Spring Boot                                                             |
| **Cache**       | Redis                       | Cache danh sách khách sạn/tour hot, session management, rate limiting             |
| **Media**       | Cloudinary                  | Upload & optimize ảnh sản phẩm, tự động resize, CDN global                      |
| **Migration**   | Flyway                      | Version control cho schema database                                                |

### 2.4. Payment & External Services

| Service         | Công nghệ                   | Mục đích                                                                          |
| --------------- | --------------------------- | --------------------------------------------------------------------------------- |
| **Payment 1**   | VNPay                      | Cổng thanh toán nội địa — ATM, QR, thẻ quốc tế                                  |
| **Payment 2**   | MoMo                       | Ví điện tử phổ biến tại VN                                                        |
| **Email**       | SendGrid / Resend           | Gửi email xác nhận đặt phòng, OTP                                                 |
| **Maps**        | Google Maps API             | Hiển thị vị trí khách sạn, nhà hàng (Phase 2)                                    |

### 2.5. DevOps & Tooling

| Layer           | Công nghệ                   | Mục đích                                                                          |
| --------------- | --------------------------- | --------------------------------------------------------------------------------- |
| **Container**   | Docker + Docker Compose     | Local dev nhất quán, dễ deploy                                                    |
| **CI/CD**       | GitHub Actions              | Tự động test & build khi push                                                     |
| **Deploy Web**  | Netlify / Cloudflare Pages  | Deploy SPA tĩnh đơn giản, nhanh, dễ cấu hình redirect fallback cho client routing |
| **Deploy API**  | Railway / Render            | Free tier tốt cho đồ án, Spring Boot Docker image                                |

### 2.6. Frontend Setup & Workflow

> Mục tiêu của frontend là **đơn giản để phát triển, rõ để mở rộng, nhanh để demo**.
> Kiến trúc ưu tiên business flow và UI/UX hơn là tối ưu hóa server-render phức tạp.

**Khởi tạo dự án**

```bash
npm create vite@latest travelnest-web -- --template react
cd travelnest-web
npm install
npm install react-router-dom zustand @tanstack/react-query react-hook-form zod @hookform/resolvers react-i18next i18next
npm install -D tailwindcss postcss autoprefixer
```

**Script khuyến nghị**

```json
{
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview",
    "lint": "eslint ."
  }
}
```

**Workflow solo dev**

- Dùng `Vite` để chạy local nhanh, HMR tốt khi build UI liên tục
- Tách route theo khu vực nghiệp vụ: public, account, staff, admin
- Dùng `TanStack Query` cho dữ liệu server
- Dùng `Zustand` cho client state ngắn hạn như auth/session UI, booking draft, filters
- Dùng `React Hook Form + Zod` cho form validation
- Dùng `react-i18next` cho đa ngôn ngữ theo namespace

---

## 3. Kiến trúc hệ thống

```
┌──────────────────────────────────────────────────────────┐
│                    CLIENT                                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │   Web App                                          │  │
│  │   React 18 SPA (Vite + React Router DOM)           │  │
│  │   Browser (Desktop)                                │  │
│  └──────────────────────┬───────────────────────────┘  │
└─────────────────────────┼──────────────────────────────┘
                          │   HTTPS / REST API
                          ▼
┌──────────────────────────────────────────────────────────┐
│               API GATEWAY / Load Balancer                 │
│                   (Nginx / Spring Gateway)                │
├──────────────────────────────────────────────────────────┤
│                 BACKEND (Spring Boot 3)                   │
│                                                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────┐   │
│  │  Auth    │ │  Hotel   │ │   Tour   │ │Restaurant │   │
│  │ Service  │ │ Service  │ │ Service  │ │  Service  │   │
│  └──────────┘ └──────────┘ └──────────┘ └───────────┘   │
│                                                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────────────────────┐  │
│  │ Booking  │ │ Payment  │ │    Notification Service  │  │
│  │ Service  │ │ Service  │ │  (Email / Push)          │  │
│  └──────────┘ └──────────┘ └──────────────────────────┘  │
├──────────────────────────────────────────────────────────┤
│                  DATA LAYER                               │
│                                                           │
│  ┌──────────────┐  ┌──────────┐  ┌───────────────────┐   │
│  │  MySQL 8.0+  │  │  Redis   │  │    Cloudinary     │   │
│  │  (Main DB)   │  │  (Cache) │  │    (Media CDN)    │   │
│  └──────────────┘  └──────────┘  └───────────────────┘   │
└──────────────────────────────────────────────────────────┘
              │                           │
    ┌─────────▼─────────┐    ┌────────────▼──────────────┐
    │    VNPay Gateway  │    │      MoMo Gateway          │
    └───────────────────┘    └───────────────────────────┘
```

### Luồng dữ liệu chính (Booking Flow)

```
User chọn dịch vụ
  → Kiểm tra availability (Redis cache → MySQL)
  → Tạo booking tạm (status: PENDING)
  → Redirect sang Payment Gateway (VNPay/MoMo)
  → Payment callback → xác nhận booking (status: CONFIRMED)
  → Gửi email xác nhận
  → Staff nhận notification → xác nhận thủ công (nếu cần)
```

### 3.1. Frontend SPA Architecture

#### Folder structure gợi ý

```text
src/
  app/
    providers/
    router/
  pages/
    public/
    auth/
    account/
    staff/
    admin/
  features/
    auth/
    hotels/
    tours/
    restaurants/
    search/
    booking/
    reviews/
    vouchers/
    admin/
    staff/
  components/
    ui/
    layout/
    feedback/
    forms/
  services/
    api/
    query/
  stores/
  i18n/
  utils/
  types/
```

#### Routing architecture với React Router DOM

```jsx
<BrowserRouter>
  <Routes>
    <Route path="/" element={<PublicLayout />}>
      <Route index element={<HomePage />} />
      <Route path="hotels" element={<HotelsPage />} />
      <Route path="tours" element={<ToursPage />} />
      <Route path="restaurants" element={<RestaurantsPage />} />
      <Route path="search" element={<SearchPage />} />
    </Route>

    <Route path="/login" element={<LoginPage />} />
    <Route path="/register" element={<RegisterPage />} />

    <Route element={<ProtectedRoute roles={['CUSTOMER']} />}>
      <Route path="/account/*" element={<AccountLayout />} />
    </Route>

    <Route element={<ProtectedRoute roles={['STAFF', 'ADMIN']} />}>
      <Route path="/staff/*" element={<StaffLayout />} />
    </Route>

    <Route element={<ProtectedRoute roles={['ADMIN']} />}>
      <Route path="/admin/*" element={<AdminLayout />} />
    </Route>
  </Routes>
</BrowserRouter>
```

#### Ghi chú kiến trúc UI

- `pages/` giữ mỏng, chỉ làm entry cho route
- `features/` chứa business UI, hooks, API adapter, schema form
- `components/ui/` chứa component dùng chung như button, input, modal, table
- `components/layout/` chứa header, footer, dashboard shell, sidebar
- `services/api/` chứa API client, endpoint map, interceptor logic
- `stores/` chỉ giữ state phía client, không thay thế data fetching từ server

---

## 4. Quy chuẩn UI/UX & Design System

### 4.1. Bảng màu (Color Palette)

> Lấy cảm hứng từ thương hiệu du lịch — xanh dương tin cậy, vàng ấm áp, nền trắng sạch.

| Token                | Mã màu    | Mô tả                                   |
| -------------------- | --------- | ---------------------------------------- |
| `--color-primary`    | `#0EA5E9` | **Sky Blue** — Màu chủ đạo, thể hiện bầu trời & du lịch |
| `--color-primary-dark` | `#0284C7` | Hover/Active state của primary          |
| `--color-secondary`  | `#F59E0B` | **Amber** — Accent ấm áp, CTA buttons   |
| `--color-bg`         | `#FFFFFF` | Nền trắng chính                          |
| `--color-bg-subtle`  | `#F8FAFC` | Nền card, sidebar — xám rất nhạt        |
| `--color-text`       | `#0F172A` | Text chính — Slate-900                  |
| `--color-text-sub`   | `#64748B` | Text phụ — Slate-500                    |
| `--color-border`     | `#E2E8F0` | Đường viền — Slate-200                  |
| `--color-danger`     | `#EF4444` | Lỗi / Hủy đặt phòng                     |
| `--color-success`    | `#22C55E` | Đặt thành công / Xác nhận               |
| `--color-warning`    | `#F59E0B` | Cảnh báo / Pending                       |

### 4.2. Typography

| Vai trò        | Font                  | Ghi chú                                      |
| -------------- | --------------------- | -------------------------------------------- |
| **Tiêu đề**    | `Be Vietnam Pro`      | Google Font — đẹp với tiếng Việt có dấu     |
| **Body text**  | `Inter`               | Readable, hiện đại                           |
| **Fallback**   | `system-ui, sans-serif` | Load nhanh khi font chưa ready             |

**Font Scale:**
- `12px` — Caption, label nhỏ
- `14px` — Body nhỏ, metadata
- `16px` — Body chính (base)
- `18-20px` — Subtitle, card title
- `24-28px` — Section heading
- `32-40px` — Page hero title

### 4.3. Phong cách thiết kế

- **Clean & Professional**: Phong cách booking platform — không rườm rà, focus vào content.
- **Rounded corners**: `8px` (button, input) · `12px` (card) · `16px` (modal).
- **Shadow tinh tế**: `shadow-sm` cho card thường, `shadow-md` cho modal/dropdown.
- **Micro-animations**: Transition `200ms ease` cho hover; skeleton loading cho data fetch.
- **Mobile-first Responsive**: Breakpoints `sm(640) · md(768) · lg(1024) · xl(1280) · 2xl(1536)`.
- **Spacing**: 4px grid system — `4, 8, 12, 16, 20, 24, 32, 48, 64, 80, 96`.
- **Ảnh**: Luôn dùng `aspect-ratio` cố định — `16:9` (banner), `4:3` (card), `1:1` (avatar).

---

## 5. Chiến lược Render (Web)

> [!IMPORTANT]
> Frontend của dự án dùng **CSR (Client-Side Rendering)** thống nhất theo mô hình React SPA.
> Mục tiêu là giảm độ phức tạp hạ tầng frontend, tăng tốc độ phát triển giao diện và tập trung vào business flow.

| Chiến lược  | Trang áp dụng                                      | Ghi chú                                                                        |
| ----------- | -------------------------------------------------- | ----------------------------------------------------------------------------- |
| **CSR**     | Trang chủ (Home)                                   | Dữ liệu tải từ API hoặc config client, tối ưu UX bằng skeleton và caching      |
| **CSR**     | Danh sách khách sạn / tour / nhà hàng              | Filter, sort, pagination và query params xử lý hoàn toàn ở client              |
| **CSR**     | Chi tiết khách sạn / tour / nhà hàng               | Dùng API fetch theo route param, ưu tiên trải nghiệm và tốc độ phát triển      |
| **CSR**     | Trang kết quả tìm kiếm (Search Results)            | Query params quản lý bằng React Router DOM và TanStack Query                   |
| **CSR**     | Giỏ hàng / Checkout                               | Flow thanh toán nhạy cảm, stateful, phù hợp client rendering                   |
| **CSR**     | User Dashboard (lịch sử đặt phòng, hồ sơ)        | Dữ liệu cá nhân, cần auth và client state                                      |
| **CSR**     | Staff Panel                                        | Internal tool, cần interactivity cao                                           |
| **CSR**     | Admin Panel                                        | CRUD phức tạp, dashboard và report client-side                                 |

### Ghi chú kỹ thuật

- **Code splitting theo route**: Dùng `React.lazy()` hoặc route-level lazy loading để giảm initial bundle.
- **Client caching**: Dùng TanStack Query để cache listing, detail, booking history và report data.
- **Loading UX**: Dùng skeleton, optimistic UI và retry states thay cho server render optimization.
- **Route fallback**: Cấu hình SPA fallback trên frontend hosting để mọi route đều trỏ về `index.html`.

---

## 6. Chiến lược i18n (Đa ngôn ngữ)

> **Mục tiêu**: Hỗ trợ đồng thời **Tiếng Việt (vi)** và **Tiếng Anh (en)**, mặc định theo browser locale.

### Web SPA (react-i18next)

```
/src
  /i18n
    /index.js
  /locales
    /vi
      common.json
      home.json
    /en
      common.json
      home.json
  /routes
    /public
    /account
    /staff
    /admin
```

- **URL structure**: Có thể dùng `travelnest.vn/vi/hotels` · `travelnest.vn/en/hotels` hoặc lưu locale trong state/app config
- **Translation files**: `/src/locales/vi/*.json` · `/src/locales/en/*.json`
- **Currency**: VND cho `vi` · USD cho `en` (hiển thị, không đổi tiền thật)
- **Date format**: `DD/MM/YYYY` cho vi · `MM/DD/YYYY` cho en

---

## 7. Phân tích Roles & Tính năng

### 7.1. USER (Khách đặt dịch vụ)

> Người dùng cuối — truy cập qua **Web** (responsive trên Desktop & Mobile).

#### Xác thực & Tài khoản

- Đăng ký / Đăng nhập bằng **Email + Password**
- **OTP xác minh email** khi đăng ký (gửi qua SendGrid)
- Đăng nhập **Google OAuth2** (Social Login)
- Quản lý hồ sơ cá nhân: Avatar, Họ tên, SĐT, Ngày sinh
- Quản lý **sổ địa chỉ** (Thêm / Sửa / Xóa / Đặt mặc định)
- Xem lịch sử đăng nhập
- Đổi mật khẩu / Quên mật khẩu (qua email reset link)

#### Tìm kiếm & Khám phá

- **Tìm kiếm đa dịch vụ**: Tìm kiếm khách sạn, tour, nhà hàng trên cùng search bar
- **Bộ lọc nâng cao**:
  - Khách sạn: Khoảng giá / ngày, số sao, loại phòng, tiện nghi (WiFi, hồ bơi, gym...)
  - Tour: Điểm đến, thời gian, số người, loại tour (trong ngày / nhiều ngày)
  - Nhà hàng: Loại ẩm thực, khu vực, khung giờ trống, số khách
- **Sắp xếp**: Giá thấp→cao, đánh giá cao nhất, phổ biến nhất, mới nhất
- **Xem theo danh mục**: Theo điểm đến, theo loại dịch vụ
- **Gợi ý thông minh**: Dựa trên lịch sử xem / đặt chỗ trước đó

#### Chi tiết Dịch vụ

**🏨 Khách sạn:**
- Gallery ảnh (Cloudinary CDN)
- Thông tin phòng: Loại phòng, diện tích, tiện nghi, sức chứa
- Lịch tình trạng phòng (Date picker — không cho chọn ngày đã full)
- Chính sách: Giờ nhận / trả phòng, hủy đặt phòng, thú cưng
- Vị trí bản đồ (Google Maps embed)
- Đánh giá từ khách hàng đã ở

**🗺️ Tour:**
- Mô tả lịch trình chi tiết theo ngày (Day 1 / Day 2 / ...)
- Điểm đón / điểm tham quan
- Danh sách bao gồm / không bao gồm
- Lịch khởi hành & số slot còn lại
- Yêu cầu (Độ tuổi, sức khỏe, cần mang gì)

**🍽️ Nhà hàng:**
- Menu / Thực đơn (hình ảnh + giá)
- Sơ đồ chỗ ngồi (Indoor / Outdoor / Tầng)
- Lịch khung giờ còn trống theo ngày
- Số lượng khách tối đa / bàn

#### Đặt dịch vụ & Thanh toán

- Chọn ngày, giờ, số lượng khách / phòng
- Điền thông tin liên hệ & ghi chú đặc biệt
- Xem **tóm tắt đơn đặt** trước khi thanh toán (giá, thuế, tổng cộng)
- Áp dụng **mã giảm giá / voucher**
- Thanh toán qua:
  - 💳 **VNPay**: ATM nội địa, Visa/Mastercard, VNPay QR
  - 📱 **MoMo**: Ví điện tử, quét QR
- Nhận **email xác nhận đặt chỗ** ngay sau khi thanh toán thành công
- Xem mã booking / QR code xác nhận

#### Quản lý Đặt chỗ

- Xem **toàn bộ lịch sử đặt dịch vụ** (Khách sạn · Tour · Nhà hàng)
- Filter theo: Trạng thái (Chờ xác nhận / Đã xác nhận / Hoàn thành / Đã hủy)
- **Hủy đặt chỗ** (theo chính sách hủy của từng dịch vụ)
- Yêu cầu **hoàn tiền** khi hủy trong thời hạn cho phép
- Tải **voucher / invoice** dạng PDF

#### Đánh giá & Nhận xét

- Đánh giá dịch vụ (1–5 sao) sau khi hoàn thành chuyến đi
- Viết nhận xét kèm hình ảnh (upload tối đa 5 ảnh)
- Phân loại đánh giá: Phòng ốc, Vệ sinh, Thái độ, Vị trí, Giá cả
- Xem đánh giá của người dùng khác (filter theo sao, từ khóa)
- **Helpful vote**: Đánh dấu review hữu ích

#### Wishlist / Yêu thích

- Lưu khách sạn / tour / nhà hàng vào **danh sách yêu thích**
- So sánh 2–3 dịch vụ cùng loại

---

### 7.2. STAFF (Nhân viên vận hành)

> Nhân viên nội bộ — truy cập qua **Web Admin Panel** (không có mobile riêng).
> STAFF không có quyền thay đổi nội dung dịch vụ — chỉ xử lý vận hành.

#### Xác thực

- Đăng nhập bằng tài khoản do ADMIN tạo (Email + Password)
- Không có tính năng đăng ký — ADMIN tạo account

#### Quản lý Booking

- Xem **danh sách booking mới** (real-time, cập nhật khi có đơn mới)
- Xem chi tiết từng booking: Thông tin khách, dịch vụ, ngày giờ, ghi chú
- **Xác nhận booking** (status: PENDING → CONFIRMED)
- **Từ chối booking** kèm lý do (ví dụ: hết chỗ đột xuất)
- Đánh dấu booking **đã hoàn thành** (khách đã check-in / tour đã kết thúc)
- Xử lý yêu cầu **hủy & hoàn tiền** của user (escalate lên ADMIN nếu cần)

#### Quản lý Lịch & Tình trạng

- Xem **lịch đặt chỗ theo ngày / tuần** (calendar view)
- Cập nhật tình trạng phòng / slot tour còn lại (nếu có thay đổi ngoài hệ thống)
- Đánh dấu dịch vụ tạm **không khả dụng** (maintenance, đặc biệt)

#### Giao tiếp với Khách

- Gửi **email / thông báo** tới khách về thay đổi booking
- Ghi **internal notes** vào từng booking (STAFF thấy, USER không thấy)

#### Dashboard STAFF

- Số booking mới cần xử lý hôm nay
- Booking sắp đến (24h / 48h tới)
- Thống kê công việc cá nhân (đã xử lý, pending)

---

### 7.3. ADMIN (Quản trị viên)

> Full quyền — quản lý toàn bộ hệ thống.

#### Quản lý Nội dung Dịch vụ

**🏨 Khách sạn:**
- Thêm / Sửa / Xóa (soft delete) khách sạn
- Quản lý loại phòng (Room Type): Số lượng, giá, tiện nghi, ảnh
- Thiết lập giá theo mùa / ngày đặc biệt (Dynamic pricing)
- Upload ảnh gallery qua Cloudinary

**🗺️ Tour:**
- Tạo / Sửa / Xóa tour
- Thiết lập lịch khởi hành & số lượng slot
- Quản lý lịch trình chi tiết (timeline editor)
- Phân loại: Tour trong ngày / Tour nhiều ngày / Tour theo chủ đề

**🍽️ Nhà hàng:**
- Thêm / Sửa / Xóa nhà hàng
- Quản lý menu (Thêm món, giá, ảnh, phân loại)
- Cấu hình bàn: Số bàn, sức chứa, khung giờ phục vụ

#### Quản lý Người dùng & Nhân viên

- Danh sách **toàn bộ Users** (Search, Filter theo status, Phân trang)
- Xem chi tiết lịch sử đặt chỗ của từng user
- **Ban / Unban** tài khoản user vi phạm
- **Tạo tài khoản STAFF** (email, mật khẩu, phân quyền)
- **Quản lý STAFF**: Sửa thông tin, vô hiệu hóa tài khoản, xem log hoạt động

#### Quản lý Booking & Tài chính

- Xem **toàn bộ booking** (filter theo loại dịch vụ, trạng thái, ngày, user)
- Override: Xác nhận / Hủy / Hoàn tiền bất kỳ booking nào
- Quản lý **yêu cầu hoàn tiền**: Duyệt / Từ chối, trigger refund qua VNPay / MoMo API
- Export báo cáo booking ra **CSV / Excel**

#### Quản lý Voucher & Khuyến mãi

- Tạo **Voucher** giảm giá (% hoặc số tiền cố định)
- Thiết lập điều kiện: Đơn tối thiểu, loại dịch vụ, thời hạn, số lần dùng tối đa
- Xem thống kê sử dụng voucher (đã dùng / còn lại)
- Tạo **Flash Deal**: Giảm giá theo khung giờ cho dịch vụ cụ thể

#### Cấu hình Hệ thống

- Quản lý **danh mục điểm đến** (Thêm / Sửa / Xóa / Sắp xếp)
- Cấu hình **phương thức thanh toán** (Bật / Tắt VNPay, MoMo)
- Quản lý **chính sách hủy** theo loại dịch vụ
- Quản lý **Banner trang chủ**: Upload, sắp xếp, lên lịch hiển thị
- Cài đặt chung: Tên thương hiệu, logo, thông tin liên hệ, social links
- Cấu hình **email template** (xác nhận booking, hủy, hoàn tiền)

#### Báo cáo & Thống kê

- **Dashboard tổng quan**:
  - Doanh thu hôm nay / tuần / tháng
  - Số booking mới / đã xác nhận / đã hủy
  - Users mới đăng ký
  - Tỷ lệ hủy (Cancellation rate)
- **Biểu đồ doanh thu** theo ngày / tuần / tháng / năm
- **Top dịch vụ** được đặt nhiều nhất (Khách sạn · Tour · Nhà hàng)
- **Top điểm đến** phổ biến
- Báo cáo **doanh thu theo loại thanh toán** (VNPay vs MoMo)
- Export tất cả báo cáo ra **PDF / CSV**

---

## Ghi chú phiên bản

| Phiên bản | Ngày cập nhật | Ghi chú                                           |
| --------- | ------------- | -------------------------------------------------- |
| v1.0      | 2026-05-12    | Khởi tạo tài liệu Source of Truth                 |

---

> **Tài liệu này là Source of Truth cho toàn bộ dự án.**
> Mọi quyết định thiết kế, kiến trúc và phát triển phải tuân thủ các quy chuẩn tại đây.
> Cập nhật tài liệu khi có thay đổi lớn về scope hoặc tech stack.
