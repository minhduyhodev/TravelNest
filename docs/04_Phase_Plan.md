# 04_Phase_Plan.md — SmartTravelHub / TravelNest: Kế hoạch triển khai theo Phase

> Chia dự án thành 5 giai đoạn từ Foundation đến Advanced.
> Mỗi phase tách rõ đầu việc Backend và Frontend theo dạng checklist để team có thể tick trực tiếp khi hoàn thành.

---

## Mục lục

- [1. Mục tiêu của kế hoạch này](#1-mục-tiêu-của-kế-hoạch-này)
- [2. Nguyên tắc chia phase](#2-nguyên-tắc-chia-phase)
- [3. Bảng tracking tổng](#3-bảng-tracking-tổng)
- [4. Tổng quan khối lượng công việc](#4-tổng-quan-khối-lượng-công-việc)
- [5. Phase 1 — Foundation & Auth](#5-phase-1--foundation--auth)
- [6. Phase 2 — Service Catalog & CMS Core](#6-phase-2--service-catalog--cms-core)
- [7. Phase 3 — Search, Booking Flow & Staff Operations](#7-phase-3--search-booking-flow--staff-operations)
- [8. Phase 4 — Payments, Voucher, Review & Notifications](#8-phase-4--payments-voucher-review--notifications)
- [9. Phase 5 — Analytics, Optimization & Production Readiness](#9-phase-5--analytics-optimization--production-readiness)
- [10. Gợi ý cách dùng checklist](#10-gợi-ý-cách-dùng-checklist)

---

## 1. Mục tiêu của kế hoạch này

Kế hoạch này bám theo 4 tài liệu hiện có trong thư mục `docs/`:

- `01_Project_Overview_and_Features.md`
- `02_Features_Detail.md`
- `03_Database_Schema.md`
- `04_Phase_Plan.md`

Phạm vi triển khai chính:

- Nền tảng booking đa dịch vụ: Hotel, Tour, Restaurant
- 3 nhóm vai trò: User, Staff, Admin
- Kiến trúc: React SPA (Vite + React Router DOM) + Spring Boot + MySQL + Redis + Cloudinary
- Luồng nghiệp vụ trọng tâm: Search → Detail → Booking → Payment → Staff xử lý → Review
- Các module hỗ trợ: Voucher, Banner, Destination, System Config, Dashboard, Report

---

## 2. Nguyên tắc chia phase

- Đi từ nền tảng kỹ thuật trước, nghiệp vụ cốt lõi sau, tối ưu và mở rộng ở cuối.
- Mỗi phase đều có đầu ra dùng được, không chờ đến cuối dự án mới ghép hệ thống.
- Backend và Frontend được tách checklist riêng để dễ giao việc, review và tracking.
- Những phần ảnh hưởng trực tiếp đến doanh thu hoặc rủi ro dữ liệu được ưu tiên sớm:
  Auth, catalog, availability, booking, payment, refund.
- Những phần nâng cao được dời về phase sau:
  analytics, tối ưu hiệu năng, monitoring, production hardening.

### Quy ước Backend cần thống nhất sớm

- `Entity` hoặc `Model`: class dùng cho persistence và mapping với database qua JPA
- `DTO`: vẫn là class thường, nhưng dùng cho request và response của API
- `Controller` không trả trực tiếp `Entity` ra client, nên trả `Response DTO`
- `Request validation` đặt ở DTO bằng Bean Validation như `@NotBlank`, `@Email`, `@Size`
- Nên có mapper hoặc service mapping rõ ràng giữa `Entity <-> DTO`

---

## 3. Bảng tracking tổng

### Tracking theo Phase

> Phần này dùng để nhìn nhanh toàn bộ roadmap theo phase.
> Mỗi dòng nên được cập nhật khi bắt đầu sprint hoặc khi một phase đổi trạng thái lớn.

| Phase | Tên phase | Mục tiêu chính | Backend | Frontend | Trạng thái |
|---|---|---|---|---|---|
| 1 | Foundation & Auth | Dựng nền tảng kỹ thuật, auth, RBAC, profile | [ ] | [ ] | `Todo` |
| 2 | Service Catalog & CMS Core | CRUD hotel, tour, restaurant, CMS, catalog public | [ ] | [ ] | `Todo` |
| 3 | Search, Booking Flow & Staff Operations | Search, availability, order, booking, staff xử lý đơn | [ ] | [ ] | `Todo` |
| 4 | Payments, Voucher, Review & Notifications | Payment, refund, voucher, review, email notifications | [ ] | [ ] | `Todo` |
| 5 | Analytics, Optimization & Production Readiness | Dashboard, report, cache, CI/CD, performance, production hardening | [ ] | [ ] | `Todo` |

### Quy ước cập nhật

- `Backend`: tick khi toàn bộ checklist backend của phase đó hoàn tất
- `Frontend`: tick khi toàn bộ checklist frontend của phase đó hoàn tất
- `Trạng thái`: dùng một trong 5 giá trị `Todo`, `Doing`, `Blocked`, `Review`, `Done`

### Cách hiểu nhanh

- Nếu `Backend` và `Frontend` đều được tick, phase đó có thể chuyển `Trạng thái` sang `Done`
- Nếu mới làm một phần, giữ `Backend` hoặc `Frontend` chưa tick và đổi `Trạng thái` sang `Doing`
- Nếu một phase bị phụ thuộc phase trước hoặc chờ quyết định nghiệp vụ, dùng `Blocked`

### Tracking theo môi trường

- [ ] Local dev ổn định
- [ ] Staging deploy được
- [ ] Production deploy được
- [ ] Có seed data để demo
- [ ] Có tài khoản demo cho User / Staff / Admin

---

## 4. Tổng quan khối lượng công việc

> Bảng này chỉ đếm **task triển khai chính** trong phần `Backend Checklist` và `Frontend Checklist`.
> Không tính các dòng tracking tổng, milestone hoàn thành phase, hay ghi chú hướng dẫn.

| Phase | Tên phase | Backend | Frontend | Tổng |
|---|---|:---:|:---:|:---:|
| 1 | Foundation & Auth | 15 | 16 | 31 |
| 2 | Service Catalog & CMS Core | 14 | 14 | 28 |
| 3 | Search, Booking Flow & Staff Operations | 16 | 13 | 29 |
| 4 | Payments, Voucher, Review & Notifications | 17 | 12 | 29 |
| 5 | Analytics, Optimization & Production Readiness | 13 | 12 | 25 |
|  | **Tổng cộng** | **75** | **67** | **142** |

### Gợi ý dùng bảng này

- `Phase 1-2` phù hợp để hoàn thiện nền tảng và catalog.
- `Phase 3-4` là lõi MVP vì bao trùm search, booking, payment, refund, review.
- `Phase 5` nên được xem là hardening + analytics, không nên kéo lên quá sớm.

### Mẫu theo dõi sprint / owner

| Task | Layer | Owner | Status | ETA | Note |
|---|---|---|---|---|---|
| API đăng nhập | BE |  | Todo |  |  |
| Trang login | FE |  | Todo |  |  |
| Booking summary panel | FE |  | Doing |  |  |
| Payment callback verify | BE |  | Blocked |  |  |

**Quy ước status**

- `Todo`: chưa bắt đầu
- `Doing`: đang triển khai
- `Blocked`: đang bị chặn
- `Review`: đang chờ review/test
- `Done`: hoàn thành

---

## 5. Phase 1 — Foundation & Auth

**Mục tiêu**

- Dựng nền tảng kỹ thuật chung cho toàn bộ hệ thống.
- Hoàn thiện xác thực, phân quyền, hồ sơ người dùng và khung UI nền.
- Tạo chuẩn coding, API, migration, error handling để các phase sau bám theo.

**Ưu tiên business**

- Có thể đăng ký, đăng nhập, xác minh email, phân quyền cơ bản.
- Có khung web public và dashboard bảo vệ bằng auth.

### Backend Checklist

- [ ] Khởi tạo project Spring Boot 3 theo module rõ ràng: `auth`, `user`, `common`, `config`
- [ ] Cấu hình MySQL, Flyway, Swagger/OpenAPI, Bean Validation
- [ ] Tạo migration cho nhóm bảng auth và user:
  `users`, `social_accounts`, `otp_tokens`, `user_addresses`, `roles`, `permissions`, `role_permissions`, `user_roles`
- [ ] Seed dữ liệu mặc định cho role và permission
- [ ] Hoàn thiện entity, repository, service, controller cho auth và user profile
- [ ] Xây JWT authentication + refresh strategy phù hợp cho web app
- [ ] Tích hợp Spring Security + RBAC theo vai trò `CUSTOMER`, `STAFF`, `ADMIN`
- [ ] API đăng ký, đăng nhập, đăng xuất, lấy profile, cập nhật profile, đổi mật khẩu
- [ ] Tích hợp Google OAuth2 login và mapping `social_accounts`
- [ ] API quên mật khẩu / reset mật khẩu bằng OTP hoặc email token
- [ ] API quản lý địa chỉ người dùng
- [ ] API tối thiểu để admin tạo hoặc vô hiệu hóa tài khoản `STAFF`
- [ ] Chuẩn hóa response wrapper, exception handler, validation error format
- [ ] Thiết lập logging cơ bản, cấu hình CORS, env profiles `local` và `staging`
- [ ] Viết unit test cho auth service và integration test cho auth APIs

### Frontend Checklist

- [ ] Khởi tạo Vite + React 18 + JSX, Tailwind, Zustand, TanStack Query, React Hook Form, Zod, `react-i18next`
- [ ] Thiết lập cấu trúc SPA cho public pages, user dashboard, admin area, staff area
- [ ] Tạo design tokens theo tài liệu màu sắc, typography, spacing
- [ ] Dựng layout chung: header, footer, container, breadcrumb, loading, empty state
- [ ] Cấu hình `react-i18next` cho `vi` và `en`, kèm namespace theo feature
- [ ] Tạo auth pages: đăng nhập, đăng ký, quên mật khẩu, reset mật khẩu
- [ ] Tạo UI xác minh email / OTP
- [ ] Tạo UI đăng nhập Google ở auth flow
- [ ] Tạo user profile page và form cập nhật thông tin cá nhân
- [ ] Tạo UI quản lý địa chỉ người dùng
- [ ] Tích hợp auth API, lưu session/token an toàn cho client flow
- [ ] Dựng protected route cho user dashboard, staff panel, admin panel
- [ ] Dựng màn hình tối thiểu để admin xem hoặc tạo tài khoản `STAFF`
- [ ] Tạo component nền: form field, modal, toast, table, status badge, confirm dialog
- [ ] Thiết lập skeleton loading, error state, not-found state
- [ ] Viết test cơ bản cho auth flow và form validation

### Mốc hoàn thành Phase 1

- [ ] User có thể đăng ký, đăng nhập và cập nhật hồ sơ
- [ ] Staff/Admin có thể đăng nhập đúng vùng quản trị
- [ ] Hệ thống phân quyền cơ bản hoạt động đúng
- [ ] FE đã có app shell, i18n, auth flow và dashboard shell

---

## 6. Phase 2 — Service Catalog & CMS Core

**Mục tiêu**

- Xây xong nền quản lý dữ liệu cho Hotel, Tour, Restaurant.
- Có public catalog để xem danh sách và trang chi tiết.
- Có admin CRUD cơ bản để nhập dữ liệu thật.

**Ưu tiên business**

- User xem được dịch vụ.
- Admin tạo và cập nhật được dịch vụ, media, destination, banner.

### Backend Checklist

- [ ] Tạo migration cho nhóm bảng dịch vụ:
  `hotels`, `hotel_images`, `hotel_amenities`, `hotel_amenity_map`, `room_types`, `room_type_images`
- [ ] Tạo migration cho nhóm tour:
  `tours`, `tour_itineraries`, `tour_images`, `tour_slots`
- [ ] Tạo migration cho nhóm nhà hàng:
  `restaurants`, `restaurant_images`, `menu_categories`, `menu_items`, `restaurant_tables`
- [ ] Tạo migration cho nhóm CMS/config cơ bản:
  `destinations`, `banners`, `system_configs`
- [ ] Hoàn thiện entity mapping và quan hệ JPA cho toàn bộ catalog
- [ ] API CRUD cho hotel, room type, amenity, image
- [ ] API CRUD cho tour, itinerary, slot, image
- [ ] API CRUD cho restaurant, menu, table, image
- [ ] API CRUD cho destination, banner, system config
- [ ] API public danh sách dịch vụ có filter cơ bản theo loại
- [ ] API public chi tiết dịch vụ theo `slug`
- [ ] Tích hợp Cloudinary hoặc lớp abstraction cho upload ảnh
- [ ] Thiết lập soft delete, publish status, audit fields cho admin flows
- [ ] Viết test cho CRUD chính và validate input của catalog

### Frontend Checklist

- [ ] Dựng homepage với banner, destination, khu vực featured services
- [ ] Tạo route danh sách hotel bằng React Router DOM + CSR
- [ ] Tạo route danh sách tour bằng React Router DOM + CSR
- [ ] Tạo route danh sách restaurant bằng React Router DOM + CSR
- [ ] Tạo route chi tiết hotel bằng React Router DOM + CSR
- [ ] Tạo route chi tiết tour bằng React Router DOM + CSR
- [ ] Tạo route chi tiết restaurant bằng React Router DOM + CSR
- [ ] Dựng gallery ảnh, section tiện ích, menu, itinerary, policy block
- [ ] Dựng admin CRUD cho hotels, tours, restaurants
- [ ] Dựng admin CRUD cho destinations, banners, system configs cơ bản
- [ ] Tạo form upload ảnh, input song ngữ `vi/en`, status switch, slug preview
- [ ] Tạo reusable data table cho admin với filter, pagination, status badge
- [ ] Gắn dữ liệu thật từ API cho toàn bộ catalog
- [ ] Tối ưu responsive cho danh sách và chi tiết dịch vụ

### Mốc hoàn thành Phase 2

- [ ] Admin tạo được dữ liệu dịch vụ hoàn chỉnh
- [ ] Public web xem được danh sách và chi tiết 3 loại dịch vụ
- [ ] Hệ thống media, destination, banner hoạt động ổn
- [ ] Có dữ liệu seed/demo đủ cho giai đoạn booking

---

## 7. Phase 3 — Search, Booking Flow & Staff Operations

**Mục tiêu**

- Hoàn thiện luồng nghiệp vụ cốt lõi: tìm kiếm, kiểm tra availability, tạo order, tạo booking.
- Mở staff panel để xử lý đơn vận hành.
- Cho user theo dõi lịch sử đặt chỗ.

**Ưu tiên business**

- Đây là phase quan trọng nhất của MVP vì biến catalog thành hệ thống booking thực sự.

### Backend Checklist

- [ ] Tạo migration cho `orders`, `order_items`
- [ ] Tạo migration cho `bookings`, `hotel_bookings`, `tour_bookings`, `restaurant_bookings`
- [ ] Thiết kế service layer cho hybrid flow:
  order snapshot, booking polymorphic, contact snapshot
- [ ] API search đa dịch vụ theo keyword, địa điểm, ngày, số khách
- [ ] API check availability cho hotel theo ngày ở
- [ ] API check availability cho tour slot theo số chỗ còn lại
- [ ] API check availability cho restaurant theo ngày/giờ/số khách
- [ ] API tạo order nháp từ lựa chọn dịch vụ
- [ ] API tạo booking từ order item và thông tin liên hệ
- [ ] Triển khai logic chống overbooking:
  pessimistic lock hoặc transaction strategy phù hợp
- [ ] Triển khai status machine cho booking:
  `PENDING_CONFIRMATION`, `CONFIRMED`, `COMPLETED`, `CANCELLED`
- [ ] API lịch sử booking cho user
- [ ] API danh sách booking cho staff/admin có filter theo status, loại dịch vụ, ngày
- [ ] API xác nhận / từ chối / hoàn tất booking cho staff
- [ ] API ghi `staff_note`, gán staff phụ trách booking
- [ ] Viết integration test cho booking flow và race condition quan trọng

### Frontend Checklist

- [ ] Dựng global search bar hỗ trợ hotel, tour, restaurant
- [ ] Tạo search results page bằng CSR với query params trên React Router DOM
- [ ] Xây filter UI theo từng loại dịch vụ:
  giá, sao, tiện ích, lịch khởi hành, khung giờ, số khách
- [ ] Tạo date picker, guest selector, room selector, slot selector, table selector
- [ ] Dựng booking summary panel và form thông tin liên hệ
- [ ] Tạo cart / checkout shell cho flow đặt dịch vụ
- [ ] Tạo user booking history page và booking detail page
- [ ] Hiển thị timeline trạng thái booking cho user
- [ ] Dựng staff dashboard: booking mới, booking sắp đến, trạng thái xử lý
- [ ] Dựng staff booking table, detail drawer, action confirm/cancel/complete
- [ ] Tạo calendar/list view cho staff theo ngày hoặc tuần
- [ ] Bổ sung validation và UX chống double submit ở bước booking
- [ ] Tối ưu loading state cho luồng search và checkout

### Mốc hoàn thành Phase 3

- [ ] User tìm kiếm được dịch vụ theo nhu cầu thực tế
- [ ] User tạo được booking nháp hợp lệ
- [ ] Staff xử lý được booking trên dashboard
- [ ] Hệ thống chống overbooking ở các luồng chính

---

## 8. Phase 4 — Payments, Voucher, Review & Notifications

**Mục tiêu**

- Chuyển booking nháp thành giao dịch thật qua payment gateway.
- Bổ sung voucher, refund, review và thông báo email.
- Hoàn thiện trust flow sau mua hàng.

**Ưu tiên business**

- Sau phase này hệ thống đã gần đạt mức MVP hoàn chỉnh để demo end-to-end.

### Backend Checklist

- [ ] Tạo migration cho `payments`, `refunds`
- [ ] Tạo migration cho `vouchers`, `voucher_usages`
- [ ] Tạo migration cho `reviews`, `review_images`, `review_helpful_votes`
- [ ] Tích hợp VNPay payment flow
- [ ] Tích hợp MoMo payment flow
- [ ] Xây payment callback handler và verify chữ ký gateway
- [ ] Bảo vệ idempotency cho payment và callback retry
- [ ] Đồng bộ trạng thái order, payment, booking sau thanh toán thành công/thất bại
- [ ] API áp dụng voucher vào order
- [ ] API tính discount và validate điều kiện voucher
- [ ] API tạo refund request và xử lý refund workflow cho admin/staff
- [ ] API tạo review sau khi booking hoàn tất
- [ ] API list reviews theo service và filter theo số sao
- [ ] API helpful vote cho review
- [ ] Tích hợp email service cho:
  verify account, xác nhận booking, thanh toán thành công, hủy, refund
- [ ] Chuẩn bị abstraction cho notification nội bộ để mở rộng push ở phase sau
- [ ] Viết test cho payment callback, voucher validation, refund flow

### Frontend Checklist

- [ ] Hoàn thiện trang checkout với lựa chọn cổng thanh toán VNPay / MoMo
- [ ] Hiển thị tổng tiền, discount, voucher, trạng thái thanh toán
- [ ] Tạo payment result page: success / failed / pending
- [ ] Tạo UI nhập và áp dụng voucher ở checkout
- [ ] Tạo user action hủy booking và xem điều kiện hoàn tiền
- [ ] Hiển thị refund status trong booking detail
- [ ] Tạo review form sau chuyến đi với rating, nội dung, upload ảnh
- [ ] Tạo review list và filter trên trang chi tiết dịch vụ
- [ ] Hiển thị helpful vote cho review
- [ ] Cập nhật email-driven states và message UX rõ ràng sau thanh toán
- [ ] Tạo admin/staff UI xử lý refund request và theo dõi payment status
- [ ] Bổ sung chống double click, retry state và đồng bộ trạng thái payment

### Mốc hoàn thành Phase 4

- [ ] User thanh toán được booking bằng VNPay hoặc MoMo
- [ ] Voucher áp dụng đúng điều kiện
- [ ] Refund flow và payment status theo dõi được
- [ ] User có thể đánh giá dịch vụ sau khi hoàn tất booking
- [ ] Email thông báo chạy được ở các mốc quan trọng

---

## 9. Phase 5 — Analytics, Optimization & Production Readiness

**Mục tiêu**

- Hoàn thiện năng lực vận hành thật: dashboard, export, monitoring, performance, security.
- Tối ưu hiệu năng frontend, trải nghiệm, logging và quy trình release.
- Đưa hệ thống về mức sẵn sàng staging/production.

**Ưu tiên business**

- Phase này biến MVP thành sản phẩm có thể demo ổn định, review tốt và dễ bảo trì.

### Backend Checklist

- [ ] Hoàn thiện dashboard metrics cho admin:
  doanh thu, booking mới, tỉ lệ hủy, user mới, top dịch vụ
- [ ] API báo cáo theo ngày, tuần, tháng, năm
- [ ] API export CSV/PDF cho booking, revenue, users
- [ ] Tối ưu index theo tài liệu schema và query thực tế
- [ ] Tích hợp Redis cho cache danh sách hot, availability read cache, config cache
- [ ] Tối ưu query nặng bằng pagination, projection, fetch strategy phù hợp
- [ ] Tăng cường security:
  rate limit, audit log, input sanitization, secure headers
- [ ] Thiết lập observability:
  health check, request log, error log, metrics
- [ ] Chuẩn bị seed data và script bootstrap cho demo/staging
- [ ] Hoàn thiện API quản lý user/staff:
  search, filter status, ban/unban, activity overview
- [ ] Hoàn thiện CI/CD: build, test, migration, deploy pipeline
- [ ] Hoàn thiện test coverage cho các flow quan trọng nhất
- [ ] Tài liệu hóa env vars, setup guide, release checklist

### Frontend Checklist

- [ ] Hoàn thiện admin dashboard với chart, KPI cards, top services, top destinations
- [ ] Tạo report filters và export actions cho admin
- [ ] Tối ưu bundle, route-level lazy loading và Core Web Vitals cho homepage, listing, detail
- [ ] Rà lại đúng chiến lược render SPA:
  CSR thống nhất cho public pages, checkout, dashboard, admin và staff
- [ ] Tối ưu caching, image loading, skeleton, error boundary
- [ ] Bổ sung Google Maps cho hotel/restaurant nếu scope cho phép ở giai đoạn cuối
- [ ] Tối ưu accessibility cơ bản:
  keyboard navigation, aria labels, color contrast
- [ ] Hoàn thiện responsive cho admin/staff screen ở breakpoint chính
- [ ] Hoàn thiện admin user management UI:
  search user, ban/unban, xem lịch sử booking, quản lý staff
- [ ] Viết test cho critical flows:
  auth, search, booking, payment result
- [ ] Chuẩn hóa metadata cơ bản, Open Graph và social preview cho SPA
- [ ] Chuẩn bị demo script và dữ liệu hiển thị đẹp cho hội đồng/khách hàng

### Mốc hoàn thành Phase 5

- [ ] Admin xem được dashboard và báo cáo
- [ ] Hệ thống có cache, logging, monitoring, security baseline
- [ ] FE đạt mức hiển thị ổn định trên mobile và desktop
- [ ] Dự án deploy được lên staging/production với checklist rõ ràng

---

## 10. Gợi ý cách dùng checklist

- Khi bắt đầu task:
  thêm tên người phụ trách ở cuối dòng, ví dụ `- [ ] API tạo booking nháp (BE - An)`
- Khi đang làm nhưng bị block:
  ghi chú thêm ở cuối dòng, ví dụ `(blocked: chờ schema voucher)`
- Khi hoàn thành:
  đổi sang `- [x]`
- Khi một phase hoàn tất:
  tick cả 3 mức
  `Backend`, `Frontend`, `Phase`

### Gợi ý nhịp triển khai

- Phase 1: 1-2 sprint
- Phase 2: 1-2 sprint
- Phase 3: 2 sprint
- Phase 4: 1-2 sprint
- Phase 5: 1 sprint

### Gợi ý ưu tiên nếu nguồn lực ít

- Ưu tiên hoàn thành Phase 1 → Phase 4 để có MVP chạy end-to-end
- Phase 5 có thể cắt bớt phần nâng cao nhưng không nên bỏ:
  logging, deploy checklist, dashboard tối thiểu, smoke test

---

> **Kết luận**
>
> Thứ tự triển khai tốt nhất cho SmartTravelHub là:
> **Foundation → Catalog → Booking Engine → Payment & Trust → Production Readiness**
>
> Nếu team cần quản lý công việc chi tiết hơn, có thể tách tiếp mỗi checkbox lớn thành issue hoặc task riêng trên Jira/Trello/GitHub Projects mà vẫn giữ file này làm roadmap gốc.
