# Tài Liệu Kỹ Thuật: Back-Office Service

**Phiên bản tài liệu:** 1.0  
**Ngày cập nhật:** 2026-05-06  
**Tác giả:** Được tổng hợp từ mã nguồn thực tế của hệ thống

---

## Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Cấu Trúc Package](#2-cấu-trúc-package)
3. [Mô Hình Dữ Liệu](#3-mô-hình-dữ-liệu)
4. [Tầng Nghiệp Vụ (Service Layer)](#4-tầng-nghiệp-vụ-service-layer)
5. [Tầng Trình Bày — REST API](#5-tầng-trình-bày--rest-api)
6. [Bảo Mật và Xác Thực](#6-bảo-mật-và-xác-thực)
7. [Giao Tiếp Hướng Sự Kiện (Kafka)](#7-giao-tiếp-hướng-sự-kiện-kafka)
8. [Lưu Trữ Tệp](#8-lưu-trữ-tệp)
9. [Giao Tiếp Liên Dịch Vụ (REST Calls)](#9-giao-tiếp-liên-dịch-vụ-rest-calls)
10. [Khởi Tạo Dữ Liệu (DataSeeder)](#10-khởi-tạo-dữ-liệu-dataseeder)
11. [Cấu Hình Ứng Dụng](#11-cấu-hình-ứng-dụng)
12. [Phụ Thuộc Kỹ Thuật Chính](#12-phụ-thuộc-kỹ-thuật-chính)
13. [Nhận Xét Kiến Trúc](#13-nhận-xét-kiến-trúc)

---

## 1. Tổng Quan

### 1.1. Mục Đích

Back-Office Service là vi dịch vụ trung tâm phụ trách các nghiệp vụ quản trị nội bộ của nền tảng thương mại điện tử nông sản. Dịch vụ đảm nhận các trách nhiệm chính sau:

- **Quản lý danh mục sản phẩm**: Duy trì cấu trúc phân cấp ba tầng gồm danh mục chính, danh mục phụ và danh mục con cùng với thông tin chung của sản phẩm.
- **Quản lý nhà cung cấp (Provider)**: Xử lý quá trình đăng ký, xác minh tư cách nhà cung cấp thông qua chứng chỉ chất lượng hoặc video thực địa.
- **Quản lý đơn hàng nội bộ**: Theo dõi và điều phối luồng xử lý đơn hàng từ khi tiếp nhận đến khi giao hàng hoàn tất, bao gồm phân công nhân viên đóng gói và vận chuyển.
- **Quản lý người dùng**: Lưu trữ thông tin hồ sơ người dùng (khách hàng, nhân viên, nhà cung cấp) được đồng bộ từ Identity Service qua Kafka.
- **Quản lý sự kiện và chính sách**: Tạo và quản lý các sự kiện khuyến mãi, chính sách đặt hàng trước, mã giảm giá.

### 1.2. Ngăn Xếp Công Nghệ

| Thành phần | Giá trị |
|---|---|
| Framework | Spring Boot 4.0.2 |
| Ngôn ngữ | Java 25 |
| Cổng dịch vụ (Port) | 9100 |
| Cơ sở dữ liệu | PostgreSQL 17 — `back_office_db` |
| Giao tiếp bất đồng bộ | Apache Kafka 4.2.0 |
| Lưu trữ đối tượng | Cloudflare R2 (tương thích S3) |
| Bảo mật | Spring Security 6 + JWT (RS256) |
| ORM | Spring Data JPA / Hibernate |
| Build tool | Maven (Maven Wrapper) |

### 1.3. Kiểu Kiến Trúc

Back-Office Service áp dụng kiến trúc **phân tầng truyền thống (Layered Architecture)** gồm bốn tầng xếp chồng theo chiều dọc:

```
┌────────────────────────────────────┐
│      Tầng trình bày (Controller)   │  ← REST API, xử lý HTTP request/response
├────────────────────────────────────┤
│      Tầng nghiệp vụ (Service)      │  ← Logic nghiệp vụ, quy tắc xử lý
├────────────────────────────────────┤
│      Tầng truy cập dữ liệu (Repo)  │  ← Spring Data JPA, truy vấn SQL
├────────────────────────────────────┤
│      Tầng dữ liệu (DAO/Entity)     │  ← JPA Entity, ánh xạ cơ sở dữ liệu
└────────────────────────────────────┘
```

Ngoài ra, dịch vụ tích hợp thêm hai thành phần nằm ngang:
- **Tầng nhắn tin (Messaging)**: Kafka Producers/Consumers hoạt động song song với luồng HTTP.
- **Tầng lưu trữ tệp (Storage)**: Tích hợp Cloudflare R2 thông qua AWS SDK S3.

---

## 2. Cấu Trúc Package

```
services/back-office-service/
├── Dockerfile                         # Ảnh Docker cho môi trường production
├── Dockerfile.local                   # Ảnh Docker cho môi trường phát triển cục bộ
├── pom.xml                            # Cấu hình Maven và phụ thuộc
├── .env.example                       # Mẫu biến môi trường
├── db_scheme/                         # Tài liệu và bản sao lưu lược đồ cơ sở dữ liệu
└── src/
    ├── main/
    │   ├── java/edu/hcmut/datn/back_office_service/
    │   │   ├── BackOfficeServiceApplication.java       # Điểm khởi động ứng dụng Spring Boot
    │   │   │
    │   │   ├── common/
    │   │   │   └── enums/                              # Tập hợp 17 kiểu liệt kê nghiệp vụ
    │   │   │       ├── AccountStatus.java              # Trạng thái tài khoản người dùng
    │   │   │       ├── Bank.java                       # Danh sách ngân hàng Việt Nam được hỗ trợ
    │   │   │       ├── CertificateType.java            # Loại chứng chỉ chất lượng nông sản
    │   │   │       ├── DemandResponseStatus.java       # Trạng thái phản hồi yêu cầu cung ứng
    │   │   │       ├── DiscountType.java               # Kiểu giảm giá (phần trăm / cố định)
    │   │   │       ├── EmployeeStatus.java             # Trạng thái nhân sự
    │   │   │       ├── EventType.java                  # Loại sự kiện hệ thống
    │   │   │       ├── Gender.java                     # Giới tính
    │   │   │       ├── MembershipLevel.java            # Cấp độ thành viên khách hàng
    │   │   │       ├── OrderStatus.java                # Trạng thái đơn hàng (14 giá trị)
    │   │   │       ├── PaymentProvider.java            # Nhà cung cấp thanh toán
    │   │   │       ├── PaymentType.java                # Hình thức thanh toán
    │   │   │       ├── ReviewStatus.java               # Trạng thái xét duyệt
    │   │   │       ├── Unit.java                       # Đơn vị đo lường sản phẩm
    │   │   │       ├── VerificationMethod.java         # Phương thức xác minh nhà cung cấp
    │   │   │       ├── VerificationStatus.java         # Trạng thái xác minh nhà cung cấp
    │   │   │       └── VideoType.java                  # Loại video xác minh thực địa
    │   │   │
    │   │   ├── config/
    │   │   │   ├── DataSeeder.java                     # Khởi tạo dữ liệu mẫu khi cơ sở dữ liệu trống
    │   │   │   ├── R2Config.java                       # Cấu hình AWS S3 Client cho Cloudflare R2
    │   │   │   └── WebConfig.java                      # Cấu hình CORS cho các nguồn gốc frontend
    │   │   │
    │   │   ├── controller/                             # 13 REST Controller xử lý HTTP request
    │   │   │   ├── BuyerController.java                # CRUD khách hàng
    │   │   │   ├── CategoryController.java             # Quản lý danh mục 3 tầng
    │   │   │   ├── CouponPolicyController.java         # Quản lý chính sách mã giảm giá
    │   │   │   ├── DemandResponseController.java       # Quản lý phản hồi yêu cầu cung ứng
    │   │   │   ├── EmployeeController.java             # CRUD nhân viên
    │   │   │   ├── EnterpriseStoreController.java      # Quản lý cửa hàng doanh nghiệp
    │   │   │   ├── EventController.java                # Quản lý sự kiện hệ thống
    │   │   │   ├── OrderController.java                # Xử lý đơn hàng và tác vụ nhân viên
    │   │   │   ├── PaymentMethodController.java        # Quản lý phương thức thanh toán
    │   │   │   ├── PreorderPolicyController.java       # Quản lý chính sách đặt trước
    │   │   │   ├── ProductGeneralController.java       # CRUD thông tin chung sản phẩm
    │   │   │   ├── ProviderController.java             # Quản lý và xác minh nhà cung cấp
    │   │   │   ├── ProviderCertificateController.java  # Tải lên và xét duyệt chứng chỉ
    │   │   │   ├── ProviderVerificationVideoController.java  # Tải lên và xét duyệt video
    │   │   │   └── UserController.java                 # CRUD hồ sơ người dùng
    │   │   │
    │   │   ├── dao/                                    # 17 lớp JPA Entity ánh xạ bảng cơ sở dữ liệu
    │   │   │   ├── Buyer.java                          # Bảng buyers — hồ sơ khách hàng
    │   │   │   ├── Category.java                       # Bảng categories — danh mục 3 tầng
    │   │   │   ├── CouponPolicy.java                   # Bảng coupon_policies
    │   │   │   ├── DemandResponse.java                 # Bảng demand_responses
    │   │   │   ├── Employee.java                       # Bảng employees
    │   │   │   ├── EnterpriseStore.java                # Bảng enterprise_stores
    │   │   │   ├── Event.java                          # Bảng events — sự kiện hệ thống
    │   │   │   ├── Order.java                          # Bảng orders
    │   │   │   ├── OrderItem.java                      # Placeholder (chưa đầy đủ)
    │   │   │   ├── PaymentMethod.java                  # Bảng payment_methods
    │   │   │   ├── PreorderPolicy.java                 # Bảng preorder_policies
    │   │   │   ├── ProductGeneral.java                 # Bảng product_generals
    │   │   │   ├── ProductRequest.java                 # Bảng product_requests
    │   │   │   ├── Provider.java                       # Bảng providers — nhà cung cấp
    │   │   │   ├── ProviderCertificate.java            # Bảng provider_certificates
    │   │   │   ├── ProviderVerificationVideo.java      # Bảng provider_verification_videos
    │   │   │   ├── SaleEvent.java                      # Bảng sale_events
    │   │   │   ├── SubSubcategory.java                 # Bảng sub_subcategories
    │   │   │   └── User.java                           # Bảng users — hồ sơ người dùng chung
    │   │   │
    │   │   ├── dto/
    │   │   │   ├── request/                            # 30+ DTO nhận dữ liệu từ HTTP request
    │   │   │   └── response/                           # 4 DTO trả kết quả về cho client
    │   │   │       ├── ApiResponse.java                # Wrapper phản hồi chuẩn hóa
    │   │   │       ├── BuyerUserDTO.java               # Ghép thông tin Buyer và User
    │   │   │       ├── ProviderDetailResponse.java     # Ghép thông tin Provider và User
    │   │   │       └── ProviderVerificationStatusResponse.java  # Trạng thái xác minh nhà cung cấp
    │   │   │
    │   │   ├── exception/                              # 25+ lớp ngoại lệ tùy chỉnh theo nghiệp vụ
    │   │   │
    │   │   ├── messaging/
    │   │   │   ├── config/
    │   │   │   │   ├── KafkaProducerConfig.java        # Cấu hình Kafka Producer (idempotent)
    │   │   │   │   └── KafkaConsumerConfig.java        # Cấu hình Kafka Consumer (group: backoffice-group)
    │   │   │   ├── consumer/                           # Các lớp lắng nghe sự kiện Kafka
    │   │   │   ├── event/                              # Các lớp POJO đại diện cho sự kiện Kafka
    │   │   │   └── producer/                           # Các lớp phát sự kiện Kafka
    │   │   │
    │   │   ├── repository/                             # 14 JPA Repository giao tiếp cơ sở dữ liệu
    │   │   │
    │   │   ├── security/
    │   │   │   └── portable/
    │   │   │       ├── AuthenticatedUser.java          # Principal tùy chỉnh chứa userId và email
    │   │   │       ├── CustomAccessDeniedHandler.java  # Xử lý lỗi 403
    │   │   │       ├── CustomAuthenticationEntryPoint.java  # Xử lý lỗi 401
    │   │   │       ├── JwtAuthenticationFilter.java    # Filter xác thực JWT trước mỗi request
    │   │   │       ├── JwtTokenValidator.java          # Xác thực và trích xuất thông tin từ JWT
    │   │   │       └── SecurityConfig.java             # Cấu hình Spring Security
    │   │   │
    │   │   ├── service/
    │   │   │   ├── BuyerService.java                   # Interface nghiệp vụ khách hàng
    │   │   │   ├── CategoryService.java                # Interface nghiệp vụ danh mục
    │   │   │   ├── CouponPolicyService.java            # Interface nghiệp vụ chính sách mã giảm giá
    │   │   │   ├── DemandResponseService.java          # Interface nghiệp vụ phản hồi cung ứng
    │   │   │   ├── EmployeeService.java                # Interface nghiệp vụ nhân viên
    │   │   │   ├── EnterpriseStoreService.java         # Interface nghiệp vụ cửa hàng
    │   │   │   ├── EventService.java                   # Interface nghiệp vụ sự kiện
    │   │   │   ├── OrderService.java                   # Interface nghiệp vụ đơn hàng
    │   │   │   ├── PaymentMethodService.java           # Interface nghiệp vụ phương thức thanh toán
    │   │   │   ├── PreorderPolicyService.java          # Interface nghiệp vụ chính sách đặt trước
    │   │   │   ├── ProductGeneralService.java          # Interface nghiệp vụ sản phẩm tổng quát
    │   │   │   ├── ProductRequestService.java          # Interface nghiệp vụ yêu cầu sản phẩm
    │   │   │   ├── ProviderCertificateService.java     # Interface nghiệp vụ chứng chỉ nhà cung cấp
    │   │   │   ├── ProviderService.java                # Interface nghiệp vụ nhà cung cấp
    │   │   │   ├── ProviderVerificationVideoService.java  # Interface nghiệp vụ video xác minh
    │   │   │   ├── R2UploadService.java                # Interface tải tệp lên Cloudflare R2
    │   │   │   ├── SaleEventService.java               # Interface nghiệp vụ sự kiện khuyến mãi
    │   │   │   ├── UserService.java                    # Interface nghiệp vụ người dùng
    │   │   │   └── impl/                               # 15 lớp triển khai Service Interface
    │   │   │
    │   │   └── util/                                   # Các lớp tiện ích dùng chung
    │   │
    │   └── resources/
    │       ├── application.yaml                        # Cấu hình ứng dụng chính
    │       └── keys/
    │           └── public.pem                          # Khóa công khai RSA để xác thực JWT
    │
    └── test/                                           # Lớp kiểm thử tự động
```

---

## 3. Mô Hình Dữ Liệu

### 3.1. Các Thực Thể JPA

#### 3.1.1. User — `users`

Lưu trữ thông tin hồ sơ cơ bản của tất cả người dùng trong hệ thống (khách hàng, nhân viên, nhà cung cấp). Dữ liệu được đồng bộ từ Identity Service thông qua sự kiện Kafka `user-events`.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `userId` | `Long` | BIGINT | PK, không tự tăng | ID được gán từ Identity Service |
| `email` | `String` | VARCHAR | | Địa chỉ thư điện tử |
| `fName` | `String` | VARCHAR | | Tên |
| `lName` | `String` | VARCHAR | | Họ |
| `avtUrl` | `String` | VARCHAR | | URL ảnh đại diện trên R2 |
| `dob` | `LocalDate` | DATE | | Ngày sinh |
| `pNum` | `String` | VARCHAR | | Số điện thoại |
| `gender` | `Gender` | VARCHAR | | Giới tính (MALE/FEMALE/OTHER/UNSPECIFIED) |
| `accStatus` | `AccountStatus` | VARCHAR | | Trạng thái tài khoản |
| `createdAt` | `LocalDateTime` | TIMESTAMP | NOT NULL | Tự đặt qua `@PrePersist` |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | NOT NULL | Tự cập nhật qua `@PreUpdate` |

#### 3.1.2. Buyer — `buyers`

Mở rộng thông tin khách hàng từ `User`, lưu điểm tích lũy, lịch sử mua hàng và cấp độ thành viên.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `buyerId` | `Long` | BIGINT | PK | Bằng `userId` của `User` |
| `userId` | `Long` | BIGINT | | Tham chiếu tới `users.userId` |
| `loyaltyPoint` | `Long` | BIGINT | | Mặc định: 50 |
| `totalOrders` | `Long` | BIGINT | | Mặc định: 0 |
| `totalSpentAmount` | `Long` | BIGINT | | Mặc định: 0 (VNĐ) |
| `membershipLevel` | `MembershipLevel` | VARCHAR | | NEW / MEM / VIP; mặc định: NEW |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | Tự đặt qua `@PrePersist` |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | Tự cập nhật qua `@PreUpdate` |

#### 3.1.3. Employee — `employees`

Lưu thông tin nhân sự của nhân viên vận hành hệ thống back-office.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `empId` | `Long` | BIGINT | PK, không tự tăng | |
| `userId` | `Long` | BIGINT | | Tham chiếu tới `users.userId` |
| `hireDate` | `LocalDate` | DATE | | Ngày vào làm |
| `empStatus` | `EmployeeStatus` | VARCHAR | | ACTIVE/INACTIVE/PROBATION/SUSPENDED/RESIGNED/TERMINATED |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.4. Provider — `providers`

Lưu thông tin nhà cung cấp (nông dân, hợp tác xã) bao gồm trạng thái xác minh và thông tin ngân hàng.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `providerId` | `Long` | BIGINT | PK, IDENTITY | Tự tăng |
| `userId` | `Long` | BIGINT | | Tham chiếu tới `users.userId` |
| `reputationPoint` | `Long` | BIGINT | | Điểm uy tín; mặc định: 100 |
| `verificationStatus` | `VerificationStatus` | VARCHAR | | UNVERIFIED/PENDING/APPROVED/REJECTED/SUSPENDED; mặc định: UNVERIFIED |
| `verificationMethod` | `VerificationMethod` | VARCHAR | | CERTIFICATE / VIDEO |
| `bankId` | `Bank` | VARCHAR | | Ngân hàng đã đăng ký |
| `bankNum` | `String` | VARCHAR | | Số tài khoản ngân hàng |
| `logoUrl` | `String` | VARCHAR | | URL logo trên R2 |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.5. Category — `categories`

Thực thể tự tham chiếu để biểu diễn cấu trúc phân cấp hai tầng (danh mục chính và danh mục phụ).

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `categoryId` | `Long` | BIGINT | PK, IDENTITY | Tự tăng |
| `name` | `String` | VARCHAR | NOT NULL | Tên danh mục |
| `description` | `String` | TEXT | | Mô tả |
| `displayOrder` | `Integer` | INT | | Thứ tự hiển thị |
| `iconUrl` | `String` | VARCHAR | | URL biểu tượng |
| `isSubCategory` | `String` | VARCHAR | | `"N"` = danh mục chính; `"Y"` = danh mục phụ |
| `belongToCategory` | `Long` | BIGINT | | ID danh mục cha (khi `isSubCategory = "Y"`) |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.6. SubSubcategory — `sub_subcategories`

Tầng thứ ba của cấu trúc danh mục, là đơn vị phân loại nhỏ nhất gắn trực tiếp với sản phẩm.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `subSubcategoryId` | `Long` | BIGINT | PK, IDENTITY | Tự tăng |
| `name` | `String` | VARCHAR | NOT NULL | Tên danh mục con |
| `description` | `String` | TEXT | | |
| `iconUrl` | `String` | VARCHAR | | |
| `subcategoryId` | `Long` | BIGINT | NOT NULL | Tham chiếu `categories.categoryId` (isSubCategory="Y") |
| `avgShelfDays` | `Integer` | INT | | Số ngày bảo quản trung bình của loại nông sản này |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.7. ProductGeneral — `product_generals`

Thông tin chung của sản phẩm, độc lập với lô hàng cụ thể. Sản phẩm được đồng bộ sang Product Storage Service thông qua Kafka.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `prodGenId` | `Long` | BIGINT | PK, IDENTITY | Tự tăng |
| `prodName` | `String` | VARCHAR | | Tên sản phẩm |
| `imgUrl` | `String` | VARCHAR | | URL ảnh sản phẩm trên R2 |
| `tags` | `String[]` | TEXT[] | | Mảng nhãn tìm kiếm (PostgreSQL native array) |
| `description` | `String` | TEXT | | Mô tả sản phẩm |
| `unit` | `Unit` | VARCHAR | | Đơn vị đo lường |
| `unitQuantity` | `Long` | BIGINT | | Số lượng mỗi đơn vị đóng gói |
| `preorderPolicyId` | `Long` | BIGINT | | Liên kết chính sách đặt trước |
| `enterpriseStoreId` | `Long` | BIGINT | | Liên kết cửa hàng doanh nghiệp |
| `subSubcategoryId` | `Long` | BIGINT | | Liên kết danh mục con cấp 3 |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.8. Order — `orders`

Lưu thông tin đơn hàng và tiến trình xử lý nội bộ của đội vận hành.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `orderId` | `Long` | BIGINT | PK, không tự tăng | ID đồng bộ từ Ecommerce Service |
| `status` | `OrderStatus` | VARCHAR | | 14 trạng thái (xem mục 3.2) |
| `ownedBy` | `Long` | BIGINT | | ID khách hàng sở hữu đơn |
| `confirmedBy` | `Long` | BIGINT | | ID nhân viên xác nhận đơn |
| `packagedBy` | `Long` | BIGINT | | ID nhân viên đóng gói |
| `shippedBy` | `Long` | BIGINT | | ID nhân viên giao hàng |
| `totalPrice` | `Long` | BIGINT | | Tổng giá trị đơn hàng (VNĐ) |
| `packagingProgress` | `Integer` | INT | | Tiến độ đóng gói (0–100%) |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.9. ProviderCertificate — `provider_certificates`

Lưu tài liệu chứng chỉ chất lượng do nhà cung cấp tải lên để xác minh tư cách.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `certificateId` | `Long` | BIGINT | PK, IDENTITY | |
| `providerId` | `Long` | BIGINT | NOT NULL | |
| `certificateType` | `CertificateType` | VARCHAR | NOT NULL | VietGAP, GlobalGAP, HACCP, ISO 22000, v.v. |
| `certificateNumber` | `String` | VARCHAR | | Số hiệu chứng chỉ |
| `issuingAuthority` | `String` | VARCHAR | | Cơ quan cấp phép |
| `issuedDate` | `LocalDate` | DATE | | Ngày cấp |
| `expiryDate` | `LocalDate` | DATE | nullable | Ngày hết hạn (có thể bỏ trống) |
| `documentUrl` | `String` | VARCHAR | | URL tài liệu trên R2 |
| `status` | `ReviewStatus` | VARCHAR | | PENDING/APPROVED/REJECTED; mặc định: PENDING |
| `reviewedBy` | `Long` | BIGINT | | ID nhân viên xét duyệt |
| `reviewNote` | `String` | TEXT | | Ghi chú xét duyệt |
| `reviewedAt` | `LocalDateTime` | TIMESTAMP | | Thời điểm xét duyệt |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.10. ProviderVerificationVideo — `provider_verification_videos`

Lưu video thực địa do nhà cung cấp tải lên làm bằng chứng xác minh môi trường sản xuất.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `videoId` | `Long` | BIGINT | PK, IDENTITY | |
| `providerId` | `Long` | BIGINT | NOT NULL | |
| `videoType` | `VideoType` | VARCHAR | NOT NULL | WORKING_ENVIRONMENT, GARDEN_FARM, MEAT_PROCESSING, v.v. |
| `videoUrl` | `String` | VARCHAR | nullable | URL tệp trên R2; null cho đến khi tải lên |
| `description` | `String` | TEXT | | Mô tả nội dung video |
| `status` | `ReviewStatus` | VARCHAR | | Mặc định: PENDING |
| `reviewedBy` | `Long` | BIGINT | | |
| `reviewNote` | `String` | TEXT | | |
| `reviewedAt` | `LocalDateTime` | TIMESTAMP | | |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.11. EnterpriseStore — `enterprise_stores`

Lưu thông tin cửa hàng/gian hàng của nhà cung cấp doanh nghiệp.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `storeId` | `Long` | BIGINT | PK, IDENTITY | |
| `providerId` | `Long` | BIGINT | | |
| `storeName` | `String` | VARCHAR | | |
| `storeDes` | `String` | VARCHAR | | Mô tả cửa hàng |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.12. CouponPolicy — `coupon_policies`

Định nghĩa quy tắc áp dụng mã giảm giá.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `couponPolicyId` | `Long` | BIGINT | PK, IDENTITY | |
| `applicableCateIds` | `List<Long>` | BIGINT[] | | Danh mục được áp dụng |
| `discountType` | `DiscountType` | VARCHAR | | PERCENTAGE / FIXED_AMOUNT |
| `discountVal` | `Long` | BIGINT | | Giá trị giảm |
| `maxDiscountAmount` | `Long` | BIGINT | | Mức giảm tối đa (VNĐ) |
| `minOrderValue` | `Long` | BIGINT | | Giá trị đơn tối thiểu để áp dụng |
| `maxUsesPerAcc` | `Long` | BIGINT | | Số lần dùng tối đa mỗi tài khoản |
| `curTotalUses` | `Long` | BIGINT | | Tổng số lần đã dùng |
| `createdBy` | `Long` | BIGINT | | ID nhân viên tạo |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.13. PreorderPolicy — `preorder_policies`

Định nghĩa quy tắc đặt hàng trước cho sản phẩm chưa có sẵn.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `preorderPolicyId` | `Long` | BIGINT | PK, IDENTITY | |
| `isActive` | `Boolean` | BOOLEAN | | Trạng thái kích hoạt |
| `requirePayment` | `Boolean` | BOOLEAN | | Có yêu cầu đặt cọc không |
| `depositPercentage` | `Long` | BIGINT | | Tỷ lệ đặt cọc (%) |
| `minPreorderDay` | `Long` | BIGINT | | Số ngày tối thiểu trước ngày giao |
| `allowCancel` | `Boolean` | BOOLEAN | | Có cho phép hủy không |
| `cancelDeadline` | `Long` | BIGINT | | Số ngày được hủy trước ngày giao |
| `notes` | `String` | VARCHAR | | Ghi chú bổ sung |
| `createdBy` | `Long` | BIGINT | | |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.14. PaymentMethod — `payment_methods`

Lưu phương thức thanh toán đã đăng ký của khách hàng.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `paymentMethodId` | `Long` | BIGINT | PK, IDENTITY | |
| `paymentType` | `PaymentType` | VARCHAR | | E_WALLET / BANK_TRANSFER / COD |
| `paymentProvider` | `PaymentProvider` | VARCHAR | | MOMO / ZALOPAY / COD |
| `accountNum` | `String` | VARCHAR | | Số tài khoản ví/ngân hàng |
| `isActive` | `Boolean` | BOOLEAN | | Trạng thái kích hoạt |
| `isDefault` | `Boolean` | BOOLEAN | | Phương thức mặc định |
| `buyerId` | `Long` | BIGINT | | |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.15. DemandResponse — `demand_responses`

Ghi nhận phản hồi của nhà cung cấp đối với yêu cầu cung ứng từ hệ thống.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `demandRespId` | `Long` | BIGINT | PK, IDENTITY | |
| `status` | `DemandResponseStatus` | VARCHAR | | PENDING/ACCEPTED/EXPIRED/CANCELLED |
| `quantity` | `Long` | BIGINT | | Số lượng cam kết cung ứng |
| `unit` | `Unit` | VARCHAR | | Đơn vị |
| `prodRqstId` | `Long` | BIGINT | | Tham chiếu `product_requests.prodRequestId` |
| `providerId` | `Long` | BIGINT | | |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.16. Event — `events`

Sự kiện hệ thống được lên lịch (khuyến mãi, đặt trước, phiếu giảm giá).

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `eventId` | `Long` | BIGINT | PK, IDENTITY | |
| `eventType` | `EventType` | VARCHAR | | SALE_EVENT / PLACE_PREORDER / CREATE_COUPON |
| `cronExp` | `String` | VARCHAR | | Biểu thức cron lên lịch |
| `beginTime` | `LocalDateTime` | TIMESTAMP | | Thời điểm bắt đầu |
| `endTime` | `LocalDateTime` | TIMESTAMP | | Thời điểm kết thúc |
| `isActive` | `Boolean` | BOOLEAN | | Trạng thái kích hoạt |
| `lastTrigger` | `LocalDateTime` | TIMESTAMP | | Lần kích hoạt gần nhất |
| `nextTrigger` | `LocalDateTime` | TIMESTAMP | | Lần kích hoạt tiếp theo |
| `createdBy` | `Long` | BIGINT | | |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.17. SaleEvent — `sale_events`

Thông tin hiển thị cho sự kiện khuyến mãi (banner, thời gian áp dụng).

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `saleEventId` | `String` | VARCHAR | PK | Khóa chính kiểu chuỗi |
| `name` | `String` | VARCHAR | | Tên sự kiện |
| `description` | `String` | VARCHAR | | |
| `img` | `String` | VARCHAR | | URL ảnh banner |
| `displayPriority` | `Long` | BIGINT | | Độ ưu tiên hiển thị |
| `isActive` | `Boolean` | BOOLEAN | | |
| `beginDate` | `LocalDate` | DATE | | Ngày bắt đầu |
| `endDate` | `LocalDate` | DATE | | Ngày kết thúc |
| `beginTime` | `LocalTime` | TIME | | Giờ bắt đầu hàng ngày |
| `endTime` | `LocalTime` | TIME | | Giờ kết thúc hàng ngày |
| `eventId` | `Long` | BIGINT | | Liên kết `events.eventId` |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

#### 3.1.18. ProductRequest — `product_requests`

Yêu cầu thu mua sản phẩm, liên kết với sự kiện hệ thống.

| Trường | Kiểu Java | Kiểu SQL | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `prodRequestId` | `Long` | BIGINT | PK, IDENTITY | |
| `unit` | `Unit` | VARCHAR | | |
| `quantity` | `Long` | BIGINT | | Số lượng cần thu mua |
| `requiredAfterDays` | `Long` | BIGINT | | Số ngày cho đến khi cần hàng |
| `prodGenId` | `Long` | BIGINT | | Tham chiếu `product_generals.prodGenId` |
| `eventId` | `Long` | BIGINT | | Tham chiếu `events.eventId` |
| `createdAt` | `LocalDateTime` | TIMESTAMP | | |
| `updatedAt` | `LocalDateTime` | TIMESTAMP | | |

### 3.2. Sơ Đồ Quan Hệ Thực Thể (ERD)

```
┌─────────────┐          ┌─────────────┐         ┌──────────────────────┐
│   users     │1        1│   buyers    │         │      employees       │
│─────────────│──────────│─────────────│         │──────────────────────│
│ userId (PK) │          │ buyerId(PK) │         │ empId (PK)           │
│ email       │          │ userId      │         │ userId               │
│ fName/lName │          │ loyaltyPt   │         │ hireDate             │
│ gender      │          │ memberLevel │         │ empStatus            │
│ accStatus   │          └─────────────┘         └──────────────────────┘
└──────┬──────┘
       │1
       │
       │1
┌──────┴──────┐          ┌──────────────────────────────┐
│  providers  │1        *│   provider_certificates      │
│─────────────│──────────│──────────────────────────────│
│providerId(PK)│         │ certificateId (PK)           │
│ userId      │          │ providerId                   │
│ reputPt     │          │ certificateType              │
│ verifyStatus│          │ status (PENDING/APPROVED/...)│
│ verifyMethod│    1    *│──────────────────────────────│
│ bankId/Num  │──────────│ provider_verification_videos │
│ logoUrl     │          │ videoId (PK)                 │
└──────┬──────┘          │ providerId                   │
       │1                │ videoType                    │
       │                 │ status                       │
       │1                └──────────────────────────────┘
┌──────┴──────────┐
│enterprise_stores│
│─────────────────│
│ storeId (PK)    │
│ providerId      │
│ storeName       │
└─────────────────┘

┌─────────────────┐    ┌───────────────────────┐    ┌─────────────────────┐
│   categories    │    │  sub_subcategories    │    │  product_generals   │
│─────────────────│    │───────────────────────│    │─────────────────────│
│ categoryId (PK) │1  *│ subSubcategoryId (PK) │1  *│ prodGenId (PK)      │
│ name            │────│ subcategoryId (FK)    │────│ subSubcategoryId    │
│ isSubCategory   │    │ name                  │    │ prodName            │
│ belongToCategory│◄──┐│ avgShelfDays          │    │ unit/unitQuantity   │
└─────────────────┘   ││───────────────────────┘    │ preorderPolicyId    │
  (self-reference)    │└──────────────── ──────────  │ enterpriseStoreId   │
                      │                              └──────────┬──────────┘
categories.belongTo ──┘                                        │*
references categories.categoryId                               │
(isSubCategory="Y")                              product_requests (via eventId)
                                                               │
                                                    ┌──────────┴──────────┐
                                                    │   product_requests  │
                                                    │─────────────────────│
                                                    │ prodRequestId (PK)  │
                                                    │ prodGenId           │
                                                    │ eventId             │
                                                    │ quantity/unit       │
                                                    └──────────┬──────────┘
                                                               │*
                                                               │1
                                                    ┌──────────┴──────────┐
                                                    │      events         │
                                                    │─────────────────────│
                                                    │ eventId (PK)        │
                                                    │ eventType           │
                                                    │ cronExp             │
                                                    └──────────┬──────────┘
                                                               │1
                                                               │1
                                                    ┌──────────┴──────────┐
                                                    │    sale_events      │
                                                    │─────────────────────│
                                                    │ saleEventId (PK)    │
                                                    │ eventId             │
                                                    │ beginDate/endDate   │
                                                    └─────────────────────┘

┌─────────────────────┐       ┌──────────────────────┐
│      orders         │       │  demand_responses    │
│─────────────────────│       │──────────────────────│
│ orderId (PK)        │       │ demandRespId (PK)    │
│ status              │       │ prodRqstId (FK)      │
│ ownedBy (→ buyers)  │       │ providerId (→ prov.) │
│ confirmedBy (→ emp) │       │ status               │
│ packagedBy (→ emp)  │       │ quantity/unit        │
│ shippedBy (→ emp)   │       └──────────────────────┘
│ packagingProgress   │
└─────────────────────┘

┌─────────────────────┐       ┌──────────────────────┐
│  payment_methods    │       │   coupon_policies    │
│─────────────────────│       │──────────────────────│
│ paymentMethodId(PK) │       │ couponPolicyId (PK)  │
│ buyerId             │       │ discountType/Val     │
│ paymentType         │       │ applicableCateIds    │
│ paymentProvider     │       │ maxUsesPerAcc        │
└─────────────────────┘       └──────────────────────┘

┌─────────────────────┐
│  preorder_policies  │
│─────────────────────│
│ preorderPolicyId(PK)│
│ depositPercentage   │
│ minPreorderDay      │
│ allowCancel         │
└─────────────────────┘
```

### 3.3. Danh Sách Giá Trị Liệt Kê (Enum)

| Enum | Các Giá Trị |
|---|---|
| `AccountStatus` | ACTIVE, INACTIVE, SUSPENDED, LOCKED, DELETED |
| `OrderStatus` | CREATED, CONFIRMED, CANCELLED, WAITING_FOR_SUPPLY, SUPPLY_CONFIRM, OUT_OF_STOCK, PACKING, READY_FOR_PICKUP, SHIPPING, DELIVERED, COMPLETED, RETURN_REQUESTED, RETURNED, REFUNDED |
| `VerificationStatus` | UNVERIFIED, PENDING, APPROVED, REJECTED, SUSPENDED |
| `VerificationMethod` | CERTIFICATE, VIDEO |
| `ReviewStatus` | PENDING, APPROVED, REJECTED |
| `MembershipLevel` | NEW, MEM, VIP |
| `EmployeeStatus` | ACTIVE, INACTIVE, PROBATION, SUSPENDED, RESIGNED, TERMINATED |
| `DemandResponseStatus` | PENDING, ACCEPTED, EXPIRED, CANCELLED |
| `DiscountType` | PERCENTAGE, FIXED_AMOUNT |
| `EventType` | SALE_EVENT, PLACE_PREORDER, CREATE_COUPON |
| `CertificateType` | VIETGAP, VIETGAP_LIVESTOCK, GLOBALGAP, HACCP, ISO_22000, OCOP, ATTP_MOH, ATTP_MARD |
| `VideoType` | WORKING_ENVIRONMENT, GARDEN_FARM, MEAT_PROCESSING, VEGETABLE_HARVEST, STORAGE_FACILITY, OTHER |
| `Unit` | KILOGRAM, GRAM, PIECE, DOZEN, LITER, MILLILITER, PACK, BOX, BOTTLE |
| `PaymentType` | E_WALLET, BANK_TRANSFER, COD |
| `PaymentProvider` | MOMO, ZALOPAY, COD |
| `Bank` | VIETCOMBANK, VIETINBANK, BIDV, AGRIBANK, TECHCOMBANK, ACB, MBBANK, SACOMBANK, VPBANK, TPBANK, SHB, OCB, HDBANK, EXIMBANK |
| `Gender` | MALE, FEMALE, OTHER, UNSPECIFIED |

---

## 4. Tầng Nghiệp Vụ (Service Layer)

### 4.1. Mô Hình Tổ Chức

Tầng nghiệp vụ được tổ chức theo mô hình **Interface + Implementation**: mỗi đơn vị nghiệp vụ được định nghĩa qua một interface và triển khai bởi lớp `*ServiceImpl` tương ứng. Tất cả lớp triển khai sử dụng `@RequiredArgsConstructor` (Lombok) để tiêm phụ thuộc qua constructor.

### 4.2. UserService / UserServiceImpl

| Phương thức | Mô tả |
|---|---|
| `create(User user)` | Lưu mới một hồ sơ người dùng vào cơ sở dữ liệu |
| `read(Long userId)` | Tìm người dùng theo ID; ném `UserNotFoundException` nếu không tồn tại |
| `readAll(Integer pageNum, Integer pageSize)` | Trả danh sách phân trang người dùng |
| `update(Long userId, User user)` | Cập nhật từng phần: chỉ ghi đè các trường khác null |
| `delete(Long userId)` | Xóa hồ sơ người dùng theo ID |

### 4.3. BuyerService / BuyerServiceImpl

| Phương thức | Mô tả |
|---|---|
| `create(Buyer buyer)` | Khởi tạo hồ sơ khách hàng (loyaltyPoint=50, membershipLevel=NEW) |
| `read(Long buyerId)` | Tìm khách hàng theo ID; ném `BuyerNotFoundException` nếu không tồn tại |
| `readAll(Integer pageNum, Integer pageSize)` | Trả danh sách phân trang khách hàng |
| `update(Long buyerId, Buyer buyer)` | Cập nhật từng phần hồ sơ khách hàng |
| `delete(Long buyerId)` | Xóa khách hàng theo ID |

### 4.4. EmployeeService / EmployeeServiceImpl

| Phương thức | Mô tả |
|---|---|
| `create(Employee employee)` | Tạo hồ sơ nhân viên; ném `EmployeeAlreadyExistsException` nếu userId đã tồn tại |
| `read(Long empId)` | Tìm nhân viên theo ID; ném `EmployeeNotFoundException` nếu không tồn tại |
| `readAll(Integer pageNum, Integer pageSize)` | Trả danh sách phân trang nhân viên |
| `update(Long empId, Employee employee)` | Cập nhật từng phần hồ sơ nhân viên |
| `delete(Long empId)` | Xóa nhân viên theo ID |

### 4.5. ProviderService / ProviderServiceImpl

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(Provider provider)` | Tạo hồ sơ nhà cung cấp | Ném `ProviderAlreadyExistsException` nếu `userId` đã tồn tại |
| `read(Long providerId)` | Tìm nhà cung cấp theo ID | Ném `ProviderNotFoundException` nếu không tồn tại |
| `readByUserId(Long userId)` | Tìm nhà cung cấp theo `userId` | Dùng cho tra cứu theo danh tính người dùng đang đăng nhập |
| `readAll(Integer pageNum, Integer pageSize, VerificationStatus status)` | Danh sách có lọc theo trạng thái xác minh | Nếu `status=null`, trả tất cả |
| `update(Long providerId, Provider provider)` | Cập nhật từng phần | Nếu `verificationStatus` hoặc `verificationMethod` thay đổi, tự động phát sự kiện `provider-verification-events` |
| `delete(Long providerId)` | Xóa nhà cung cấp | |
| `uploadLogo(Long userId, MultipartFile file)` | Tải lên logo nhà cung cấp | Tải tệp lên bucket `provider-logos` rồi cập nhật `logoUrl` |

**Logic phát sự kiện xác minh** (`publishVerificationUpdate`):
Khi trạng thái hoặc phương thức xác minh thay đổi, dịch vụ truy vấn chứng chỉ đã phê duyệt để lấy loại chứng chỉ đầu tiên, sau đó phát `ProviderVerificationUpdatedEvent` chứa đầy đủ thông tin xác minh.

### 4.6. ProviderCertificateService / ProviderCertificateServiceImpl

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `upload(Long userId, CertificateType, ...)` | Tải tệp lên R2 và tạo bản ghi chứng chỉ | Sau khi tải lên, đặt `verificationStatus` của nhà cung cấp thành PENDING |
| `read(Long certificateId)` | Tìm chứng chỉ theo ID | |
| `readAllByProvider(Long userId)` | Lấy danh sách chứng chỉ của nhà cung cấp đang đăng nhập | Tra cứu qua `userId` → `providerId` |
| `readAllByProviderId(Long providerId)` | Lấy danh sách chứng chỉ theo ID nhà cung cấp | Dành cho quản trị viên |
| `review(Long certificateId, ReviewStatus, String reviewNote, Long reviewedBy)` | Xét duyệt chứng chỉ | Nếu APPROVED: cập nhật `verificationStatus=APPROVED` và `verificationMethod=CERTIFICATE` của nhà cung cấp |
| `delete(Long certificateId)` | Xóa bản ghi và tệp trên R2 | Trích xuất key từ URL để xóa tệp; ghi nhật ký nếu xóa tệp thất bại nhưng vẫn xóa bản ghi |

### 4.7. ProviderVerificationVideoService / ProviderVerificationVideoServiceImpl

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(Long userId, VideoType, String description)` | Tạo bản ghi video (chưa có tệp) | `videoUrl=null` cho đến khi gọi `uploadFile` |
| `uploadFile(Long videoId, MultipartFile file)` | Tải tệp video lên R2 và cập nhật URL | |
| `read(Long videoId)` | Tìm video theo ID | |
| `readAllByProvider(Long userId)` | Danh sách video của nhà cung cấp đăng nhập | |
| `readAllByProviderId(Long providerId)` | Danh sách video theo ID (cho quản trị viên) | |
| `review(Long videoId, ReviewStatus, String, Long)` | Xét duyệt video xác minh | |
| `delete(Long videoId)` | Xóa bản ghi và tệp trên R2 | |

### 4.8. CategoryService / CategoryServiceImpl

| Phương thức | Mô tả |
|---|---|
| `create(Category category)` | Tạo danh mục hoặc danh mục phụ (phân biệt bởi `isSubCategory`) |
| `read(Long categoryId)` | Tìm danh mục theo ID |
| `readAll()` | Trả danh sách tất cả danh mục |
| `readSubcategories(Long parentId)` | Lấy danh sách danh mục phụ thuộc về một danh mục cha |
| `readAllSubSubcategories(Long subcategoryId)` | Lấy danh sách `SubSubcategory` của một danh mục phụ |
| `readAllSubSubcategories_v2()` | Lấy tất cả `SubSubcategory` toàn hệ thống |
| `readSubSubcategory(Long id)` | Tìm một `SubSubcategory` theo ID |
| `update(Long categoryId, Category)` | Cập nhật danh mục |
| `delete(Long categoryId)` | Xóa danh mục |
| `createSubSubcategory(SubSubcategory)` | Tạo danh mục con cấp 3; phát sự kiện `subsubcategory-events` |
| `updateSubSubcategory(Long id, SubSubcategory)` | Cập nhật danh mục con cấp 3 |
| `deleteSubSubcategory(Long id)` | Xóa danh mục con cấp 3 |

**Quy tắc nghiệp vụ:** Khi tạo hoặc cập nhật `SubSubcategory`, dịch vụ phát sự kiện `SubSubcategoryCreatedEvent` qua topic `subsubcategory-events` để đồng bộ sang Product Storage Service.

### 4.9. ProductGeneralService / ProductGeneralServiceImpl

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(ProductGeneral product)` | Tạo thông tin chung sản phẩm | Sau khi lưu, phát sự kiện `ProductGeneralCreatedEvent` qua topic `product-general-events` |
| `read(Long prodGenId)` | Tìm sản phẩm theo ID | |
| `readAll(Integer pageNum, Integer pageSize)` | Danh sách phân trang sản phẩm | |
| `update(Long prodGenId, ProductGeneral)` | Cập nhật từng phần | |
| `delete(Long prodGenId)` | Xóa sản phẩm | |

### 4.10. OrderService / OrderServiceImpl

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(Order order)` | Tạo bản ghi đơn hàng | Được gọi bởi Kafka consumer khi nhận sự kiện `order-events` |
| `read(Long orderId)` | Tìm đơn hàng theo ID | |
| `readAll(Integer pageNum, Integer pageSize)` | Danh sách đơn hàng | |
| `adminReadAll(String status, Long packagingEmpId, Long deliveringEmpId, Long orderId)` | Truy vấn đơn hàng nâng cao với lọc nhiều tiêu chí | Sử dụng native SQL query với projection `OrderInformation` |
| `update(Long orderId, Order)` | Cập nhật từng phần đơn hàng | |
| `delete(Long orderId)` | Xóa đơn hàng | |
| `empConfirmOrder(Long orderId, Long empId)` | Nhân viên xác nhận đơn hàng | Đặt `status=CONFIRMED`, `confirmedBy=empId`; phát sự kiện `order-confirmed-events` |
| `empPackageOrder(Long orderId, Long empId)` | Nhân viên bắt đầu đóng gói | Đặt `status=PACKING`, `packagedBy=empId` |
| `empShipOrder(Long orderId, Long empId)` | Nhân viên giao hàng | Đặt `status=SHIPPING`, `shippedBy=empId`; phát sự kiện `order-delivering-events` |
| `empDeliverOrder(Long orderId, Long empId)` | Hoàn thành giao hàng | Đặt `status=DELIVERED`; phát sự kiện `order-delivered-events` |
| `updatePackagingProgress(Long orderId, Integer progress)` | Cập nhật tiến độ đóng gói | Nhận giá trị 0–100% |

### 4.11. R2UploadService / R2UploadServiceImpl

| Phương thức | Mô tả |
|---|---|
| `upload(MultipartFile file, String bucket)` | Tải tệp lên bucket R2; tạo tên tệp theo định dạng `UUID_originalFilename`; trả về URL công khai |
| `delete(String key, String bucket)` | Xóa đối tượng khỏi bucket; xác minh đối tượng tồn tại trước và sau khi xóa |

**Ánh xạ bucket → URL công khai:**

| Bucket | URL Công Khai |
|---|---|
| `back-office-user-avts` | `https://pub-954e99f131cf4cc896de1ad360338682.r2.dev` |
| `product-general-img` | `https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev` |
| `provider-certificates` | `https://pub-b72d8c021b3848f8b4d8805e93e18af6.r2.dev` |
| `provider-verification-videos` | `https://pub-1b4c6325308a40f68dc9dca3d1771fbd.r2.dev` |
| `provider-logos` | `https://pub-b5792a8ef25c47838481acc16ac8daec.r2.dev` |

### 4.12. Các Service Nghiệp Vụ Khác

| Service | Mô tả |
|---|---|
| `CouponPolicyService` | CRUD chính sách mã giảm giá |
| `DemandResponseService` | CRUD phản hồi yêu cầu cung ứng |
| `EnterpriseStoreService` | CRUD cửa hàng doanh nghiệp của nhà cung cấp |
| `EventService` | CRUD sự kiện hệ thống |
| `PaymentMethodService` | CRUD phương thức thanh toán của khách hàng |
| `PreorderPolicyService` | CRUD chính sách đặt hàng trước |
| `ProductRequestService` | CRUD yêu cầu sản phẩm liên kết sự kiện |
| `SaleEventService` | CRUD sự kiện khuyến mãi (khóa chính kiểu `String`) |

---

## 5. Tầng Trình Bày — REST API

Tất cả endpoint đều yêu cầu xác thực JWT (không có endpoint công khai). Các controller trả về `ResponseEntity<ApiResponse<T>>` với wrapper chuẩn hóa.

**Cấu trúc phản hồi chuẩn:**

```json
{
  "type": "GOOD",
  "code": "200",
  "message": "Success",
  "detail": { ... },
  "timestamp": "2026-05-06T10:00:00"
}
```

### 5.1. UserController — `/api/user`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/user` | Có | `UserDTO` | `User` |
| GET | `/api/user/{userId}` | Có | — | `User` |
| GET | `/api/user?pageNum=&pageSize=` | Có | — | `List<User>` |
| PUT | `/api/user/{userId}` | Có | `UserDTO` | `User` |
| DELETE | `/api/user/{userId}` | Có | — | — |

### 5.2. BuyerController — `/api/buyer`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/buyer` | Có | `BuyerCreateRequest` | `Buyer` |
| GET | `/api/buyer/{buyerId}` | Có | — | `Buyer` |
| GET | `/api/buyer` | Có | — | `List<Buyer>` |
| PUT | `/api/buyer/{buyerId}` | Có | `BuyerUpdateRequest` | `Buyer` |
| DELETE | `/api/buyer/{buyerId}` | Có | — | — |

### 5.3. EmployeeController — `/api/employee`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/employee` | Có | `EmployeeCreateRequest` | `Employee` |
| GET | `/api/employee/{empId}` | Có | — | `Employee` |
| GET | `/api/employee` | Có | — | `List<Employee>` |
| PUT | `/api/employee/{empId}` | Có | `EmployeeUpdateRequest` | `Employee` |
| DELETE | `/api/employee/{empId}` | Có | — | — |

### 5.4. CategoryController — `/api/categories`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/categories` | Có | `CategoryCreateRequest` | `Category` |
| GET | `/api/categories/{categoryId}` | Có | — | `Category` |
| GET | `/api/categories` | Có | — | `List<Category>` |
| GET | `/api/categories/{parentId}/subcategories` | Có | — | `List<Category>` |
| PUT | `/api/categories/{categoryId}` | Có | `CategoryCreateRequest` | `Category` |
| DELETE | `/api/categories/{categoryId}` | Có | — | — |
| POST | `/api/categories/sub-subcategories` | Có | `SubSubcategoryCreateRequest` | `SubSubcategory` |
| GET | `/api/categories/sub-subcategories` | Có | — | `List<SubSubcategory>` |
| GET | `/api/categories/sub-subcategories/{id}` | Có | — | `SubSubcategory` |
| GET | `/api/categories/{subcategoryId}/sub-subcategories` | Có | — | `List<SubSubcategory>` |
| PUT | `/api/categories/sub-subcategories/{id}` | Có | `SubSubcategoryCreateRequest` | `SubSubcategory` |
| DELETE | `/api/categories/sub-subcategories/{id}` | Có | — | — |

### 5.5. ProductGeneralController — `/api/product-general`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/product-general` | Có | `ProductGeneralCreateRequest` | `ProductGeneral` |
| GET | `/api/product-general/{prodGenId}` | Có | — | `ProductGeneral` |
| GET | `/api/product-general?pageNum=&pageSize=` | Có | — | `List<ProductGeneral>` |
| PUT | `/api/product-general/{prodGenId}` | Có | `ProductGeneralUpdateRequest` | `ProductGeneral` |
| DELETE | `/api/product-general/{prodGenId}` | Có | — | — |

### 5.6. ProviderController — `/api/provider`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body / Param | Kiểu phản hồi |
|---|---|---|---|---|
| GET | `/api/provider/me` | Có | `@AuthenticationPrincipal` | `ProviderDetailResponse` |
| GET | `/api/provider/my-status` | Có | `@AuthenticationPrincipal` | `ProviderVerificationStatusResponse` |
| POST | `/api/provider` | Có | `ProviderCreateRequest` | `Provider` |
| GET | `/api/provider/{providerId}` | Có | — | `ProviderDetailResponse` |
| GET | `/api/provider?pageNum=&pageSize=&status=` | Có | — | `List<Provider>` |
| GET | `/api/provider/{providerId}/certificates` | Có | — | `List<ProviderCertificate>` |
| GET | `/api/provider/{providerId}/videos` | Có | — | `List<ProviderVerificationVideo>` |
| PUT | `/api/provider/{providerId}` | Có | `ProviderUpdateRequest` | `Provider` |
| DELETE | `/api/provider/{providerId}` | Có | — | — |
| POST | `/api/provider/upload-logo` | Có | `MultipartFile` (form-data) | `Provider` |

### 5.7. ProviderCertificateController — `/api/provider/certificates`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body / Param | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/provider/certificates` | Có | Form-data: `certificateType`, `certificateNumber`, `issuingAuthority`, `issuedDate`, `expiryDate` (tùy chọn), `file` | `ProviderCertificate` |
| GET | `/api/provider/certificates` | Có | `@AuthenticationPrincipal` | `List<ProviderCertificate>` |
| GET | `/api/provider/certificates/{certificateId}` | Có | — | `ProviderCertificate` |
| PUT | `/api/provider/certificates/{certificateId}/review` | Có | `ProviderCertificateReviewRequest` | `ProviderCertificate` |
| DELETE | `/api/provider/certificates/{certificateId}` | Có | — | — |

### 5.8. ProviderVerificationVideoController — `/api/provider/videos`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body / Param | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/provider/videos` | Có | `ProviderVerificationVideoCreateRequest` | `ProviderVerificationVideo` |
| POST | `/api/provider/videos/{videoId}/upload` | Có | `MultipartFile` (form-data) | `ProviderVerificationVideo` |
| GET | `/api/provider/videos` | Có | `@AuthenticationPrincipal` | `List<ProviderVerificationVideo>` |
| GET | `/api/provider/videos/{videoId}` | Có | — | `ProviderVerificationVideo` |
| PUT | `/api/provider/videos/{videoId}/review` | Có | `ProviderVerificationVideoReviewRequest` | `ProviderVerificationVideo` |
| DELETE | `/api/provider/videos/{videoId}` | Có | — | — |

### 5.9. OrderController — `/api/order`

| Phương thức HTTP | Đường dẫn | Yêu cầu xác thực | Body / Param | Kiểu phản hồi |
|---|---|---|---|---|
| POST | `/api/order` | Có | `OrderCreateRequest` | `Order` |
| GET | `/api/order/{orderId}` | Có | — | `Order` |
| GET | `/api/order/admin?status=` | Có | Query params | `List<OrderInformation>` |
| GET | `/api/order/admin/{orderId}` | Có | — | `List<OrderInformation>` |
| PUT | `/api/order/{orderId}` | Có | `OrderUpdateRequest` | `Order` |
| DELETE | `/api/order/{orderId}` | Có | — | — |
| PUT | `/api/order/{orderId}/confirm` | Có | `@AuthenticationPrincipal` | `Long` (empId) |
| PUT | `/api/order/{orderId}/package` | Có | `@AuthenticationPrincipal` | — |
| PUT | `/api/order/{orderId}/ship` | Có | `@AuthenticationPrincipal` | — |
| PUT | `/api/order/{orderId}/progress` | Có | `PackagingProgressUpdateRequest` | — |
| PUT | `/api/order/{orderId}/deliver` | Có | `@AuthenticationPrincipal` | — |
| GET | `/api/order/emp/packaging-tasks` | Có | `@AuthenticationPrincipal` | `List<OrderInformation>` |
| GET | `/api/order/emp/delivering-tasks` | Có | `@AuthenticationPrincipal` | `List<OrderInformation>` |

### 5.10. Các Controller Khác

| Controller | Đường dẫn gốc | Các endpoint |
|---|---|---|
| `CouponPolicyController` | `/api/coupon-policy` | CRUD đầy đủ |
| `DemandResponseController` | `/api/demand-response` | CRUD đầy đủ |
| `EnterpriseStoreController` | `/api/enterprise-store` | CRUD đầy đủ |
| `EventController` | `/api/event` | CRUD đầy đủ |
| `PaymentMethodController` | `/api/payment-method` | CRUD đầy đủ |
| `PreorderPolicyController` | `/api/preorder-policy` | CRUD đầy đủ |

### 5.11. Cấu Trúc Phản Hồi ApiResponse

```java
public enum ApiResponseType { ERROR, WARN, GOOD, SKIP_AS_GOOD }

// Phương thức factory tĩnh:
ApiResponse.SUCCESS(code, message, detail)   // type = GOOD
ApiResponse.ERROR(code, message, detail)     // type = ERROR
ApiResponse.WARN(code, message, detail)      // type = WARN
ApiResponse.SKIP_AS_GOOD(code, message, detail) // type = SKIP_AS_GOOD
```

---

## 6. Bảo Mật và Xác Thực

### 6.1. Cơ Chế Xác Thực

Back-Office Service sử dụng mô hình xác thực JWT không phiên làm việc (stateless). Token JWT được phát hành bởi **Identity Service** và được xác thực tại đây bằng **khóa công khai RSA** (lưu tại `src/main/resources/keys/public.pem`).

**Luồng xử lý request:**

```
HTTP Request
    │
    ▼
JwtAuthenticationFilter (OncePerRequestFilter)
    │
    ├── Trích xuất header Authorization: Bearer <token>
    │
    ├── JwtTokenValidator.validate(token)
    │       ├── Xác thực chữ ký với public.pem
    │       ├── Kiểm tra hạn sử dụng
    │       └── Trích xuất claims: userId, email
    │
    ├── Tạo AuthenticatedUser(id, email)
    │
    ├── Đặt SecurityContextHolder
    │
    ▼
Controller (@AuthenticationPrincipal AuthenticatedUser)
```

### 6.2. Cấu Hình Spring Security

```yaml
Chính sách phiên làm việc: STATELESS
CSRF: Tắt
HTTP Basic: Tắt
Form Login: Tắt
Tất cả request: yêu cầu xác thực (trừ OPTIONS)
Phương thức bảo mật: @EnableMethodSecurity
Mã hóa mật khẩu: BCryptPasswordEncoder
```

### 6.3. Xử Lý Lỗi Xác Thực

| Tình huống | Lớp xử lý | Mã HTTP |
|---|---|---|
| Token không hợp lệ hoặc thiếu | `CustomAuthenticationEntryPoint` | 401 Unauthorized |
| Không đủ quyền truy cập | `CustomAccessDeniedHandler` | 403 Forbidden |

Cả hai handler đều trả về phản hồi dạng `ApiResponse` với `type=ERROR`.

### 6.4. Cấu Hình CORS

Dịch vụ cho phép các nguồn gốc sau truy cập API:

| Nguồn gốc | Mục đích |
|---|---|
| `http://localhost:5173` | Back-Office UI phát triển cục bộ |
| `http://localhost:3000` | Ecommerce UI phát triển cục bộ |
| `http://localhost:5273` | Provider UI phát triển cục bộ |
| `http://10.205.183.122:5173` | Mạng nội bộ |
| `http://10.185.89.85:5173` | Mạng nội bộ |
| `http://192.168.1.75:3000` | Mạng LAN |
| `http://10.194.144.9:3000` | Mạng nội bộ |

Cấu hình cho phép tất cả header, tất cả phương thức HTTP, thông tin xác thực (credentials), thời gian cache preflight 3600 giây, áp dụng cho đường dẫn `/api/**`.

---

## 7. Giao Tiếp Hướng Sự Kiện (Kafka)

### 7.1. Cấu Hình Producer

```
Bootstrap Servers:  ${app.kafka-url}  (= ${KAFKA_HOST}:${KAFKA_PORT})
Key Serializer:     StringSerializer
Value Serializer:   JacksonJsonSerializer
Acks:               all  (xác nhận từ tất cả replica)
Retries:            3
Linger (ms):        5
Idempotence:        true  (đảm bảo đúng một lần gửi)
```

### 7.2. Cấu Hình Consumer

```
Bootstrap Servers:  ${app.kafka-url}
Consumer Group:     backoffice-group
Auto Offset Reset:  earliest
Auto Create Topics: false
Key Deserializer:   StringDeserializer
Value Deserializer: StringDeserializer
Converter:          StringJacksonJsonMessageConverter
```

### 7.3. Danh Sách Topic và Sự Kiện

#### 7.3.1. Các Topic Producer (Dịch vụ này phát)

| Topic | Lớp sự kiện | Điều kiện kích hoạt | Producer |
|---|---|---|---|
| `category-events` | `CategoryCreatedEvent` | Tạo danh mục mới | `CategoryProducer` |
| `subsubcategory-events` | `SubSubcategoryCreatedEvent` | Tạo/cập nhật danh mục con cấp 3 | `CategoryProducer` |
| `product-general-events` | `ProductGeneralCreatedEvent` | Tạo sản phẩm mới | `ProductGeneralProducer` |
| `order-confirmed-events` | `OrderConfirmedEvent` | Nhân viên xác nhận đơn hàng | `OrderEventProducer` |
| `order-delivering-events` | `OrderDeliveringEvent` | Nhân viên bắt đầu giao hàng | `OrderEventProducer` |
| `order-delivered-events` | `OrderDeliveredEvent` | Nhân viên hoàn thành giao hàng | `OrderEventProducer` |
| `provider-verification-events` | `ProviderVerificationUpdatedEvent` | Trạng thái xác minh nhà cung cấp thay đổi | `ProviderVerificationProducer` |

#### 7.3.2. Các Topic Consumer (Dịch vụ này lắng nghe)

| Topic | Lớp sự kiện | Hành động xử lý | Consumer |
|---|---|---|---|
| `user-events` | `UserCreatedEvent` | Tạo bản ghi `User` (accStatus=ACTIVE) và `Buyer` (loyaltyPoint=50) | `UserCreatedConsumer` |
| `provider-create-events` | `ProviderCreatedEvent` | Nếu `isFreshAccount=true`: tạo `User`; luôn tạo `Provider` | `ProviderCreatedConsumer` |
| `order-events` | `OrderCreatedEvent` | Tạo bản ghi `Order` với status=CREATED | `OrderCreatedConsumer` |
| `order-confirmed-events` | `OrderConfirmedEvent` | Cập nhật `Order.status=CONFIRMED`, `confirmedBy` | `OrderConfirmedConsumer` |
| `order-packaging-progress-update-events` | `OrderUpdatePackagingProgressEvent` | Cập nhật `Order.packagingProgress` (0–100%) | `OrderUpdatePackagingProgressEventConsumer` |

### 7.4. Cấu Trúc Lớp Sự Kiện

#### UserCreatedEvent

```json
{
  "userId": 123,
  "email": "user@example.com",
  "fName": "Nguyen",
  "lName": "Van A",
  "avtUrl": "https://...",
  "dob": "1990-01-15",
  "pNum": "0901234567",
  "gender": "MALE"
}
```

#### ProviderCreatedEvent

```json
{
  "userId": 456,
  "email": "provider@example.com",
  "fName": "Tran",
  "lName": "Thi B",
  "avtUrl": "https://...",
  "dob": "1985-06-20",
  "pNum": "0912345678",
  "gender": "FEMALE",
  "bankId": "VIETCOMBANK",
  "bankNum": "1234567890",
  "isFreshAccount": true
}
```

#### OrderCreatedEvent

```json
{
  "orderId": 789,
  "buyerId": "123",
  "totalPrice": 250000
}
```

#### ProductGeneralCreatedEvent

```json
{
  "prodGenId": 101,
  "prodName": "Rau muống hữu cơ",
  "subSubcategoryId": 15,
  "unit": "KILOGRAM",
  "unitQuantity": 1
}
```

#### ProviderVerificationUpdatedEvent

```json
{
  "providerId": 456,
  "verificationStatus": "APPROVED",
  "verificationMethod": "CERTIFICATE",
  "certificateType": "VIETGAP",
  "logoUrl": "https://..."
}
```

### 7.5. Xử Lý Lỗi Kafka

Dịch vụ không triển khai cơ chế thử lại (retry) hoặc hàng đợi thư chết (dead-letter queue). Khi xử lý sự kiện thất bại, lớp consumer ghi nhật ký lỗi (log error/warn) và tiếp tục xử lý sự kiện tiếp theo. Đây là điểm kỹ thuật nợ (technical debt) cần cải thiện ở phiên bản sau.

---

## 8. Lưu Trữ Tệp

### 8.1. Tổng Quan

Back-Office Service tích hợp **Cloudflare R2** — dịch vụ lưu trữ đối tượng tương thích với giao thức AWS S3. Tích hợp được thực hiện thông qua thư viện **AWS SDK for Java v2** (`software.amazon.awssdk:s3:2.42.4`).

### 8.2. Cấu Hình Kết Nối (R2Config)

```java
S3Client.builder()
  .endpointOverride(URI.create("https://{R2_ACCOUNT_ID}.r2.cloudflarestorage.com"))
  .region(Region.of("auto"))
  .credentialsProvider(StaticCredentialsProvider.create(
      AwsBasicCredentials.create(accessKey, secretKey)))
  .serviceConfiguration(
      S3Configuration.builder().pathStyleAccessEnabled(true).build())
  .build()
```

Lưu ý: `pathStyleAccessEnabled(true)` là bắt buộc vì Cloudflare R2 không hỗ trợ virtual-hosted style bucket URL.

### 8.3. Danh Sách Bucket và Mục Đích

| Bucket | Mục đích | URL công khai |
|---|---|---|
| `back-office-user-avts` | Ảnh đại diện người dùng | `pub-954e99f131cf4cc896de1ad360338682.r2.dev` |
| `product-general-img` | Ảnh sản phẩm | `pub-0365edd1781141cdb68675969c7cdb87.r2.dev` |
| `provider-certificates` | Tài liệu chứng chỉ nhà cung cấp | `pub-b72d8c021b3848f8b4d8805e93e18af6.r2.dev` |
| `provider-verification-videos` | Video xác minh thực địa | `pub-1b4c6325308a40f68dc9dca3d1771fbd.r2.dev` |
| `provider-logos` | Logo nhà cung cấp | `pub-b5792a8ef25c47838481acc16ac8daec.r2.dev` |

### 8.4. Luồng Tải Tệp Lên

```
Client gửi multipart/form-data
    │
    ▼
Controller (ProviderCertificateController / ProviderVerificationVideoController / ProviderController)
    │   nhận MultipartFile
    ▼
R2UploadServiceImpl.upload(file, bucketName)
    │
    ├── Tạo tên tệp: UUID.randomUUID() + "_" + originalFilename
    ├── Xây dựng PutObjectRequest (bucket, key, contentType)
    ├── s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()))
    └── Trả về: publicBucketUrl + "/" + fileName
    │
    ▼
Service cập nhật trường URL trong Entity
    │
    ▼
Repository lưu vào PostgreSQL
```

### 8.5. Luồng Xóa Tệp

```
R2UploadServiceImpl.delete(key, bucket)
    │
    ├── Kiểm tra key và bucket không trống
    ├── objectExists(key, bucket) → s3Client.headObject()
    │       nếu không tồn tại → ném ObjectNotFoundException
    ├── s3Client.deleteObject(DeleteObjectRequest)
    └── Xác nhận lại objectExists() → nếu vẫn tồn tại → ném DeletionFailedException
```

### 8.6. Giới Hạn Tải Tệp

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB
```

---

## 9. Giao Tiếp Liên Dịch Vụ (REST Calls)

Cấu hình `application.yaml` khai báo URL của các dịch vụ khác:

```yaml
services:
  identity:
    url: http://${IDENTITY_HOST:localhost}:${IDENTITY_PORT:9000}
  product-storage:
    url: http://${PRODUCT_STORAGE_HOST:localhost}:${PRODUCT_STORAGE_PORT:9200}
  ecommerce:
    url: http://${ECOMMERCE_HOST:localhost}:${ECOMMERCE_PORT:9300}
```

Tuy nhiên, qua quá trình kiểm tra mã nguồn thực tế, **Back-Office Service không thực hiện bất kỳ cuộc gọi REST đồng bộ nào** đến các dịch vụ khác trong luồng xử lý request thông thường. Tất cả giao tiếp liên dịch vụ đều diễn ra qua Kafka (bất đồng bộ). Các URL dịch vụ khác được khai báo sẵn nhưng chưa được sử dụng, có thể phục vụ cho các tính năng đang phát triển.

---

## 10. Khởi Tạo Dữ Liệu (DataSeeder)

### 10.1. Điều Kiện Kích Hoạt

Lớp `DataSeeder` được đánh dấu `@Configuration` và chứa phương thức `@Bean` trả về `CommandLineRunner`. Dữ liệu mẫu chỉ được tạo khi bảng `categories` trống, đảm bảo seeder chỉ chạy một lần duy nhất khi khởi động hệ thống lần đầu.

```java
if (categoryRepository.count() == 0) {
    // Thực hiện seed dữ liệu
}
```

### 10.2. Dữ Liệu Được Tạo

#### Danh Mục Sản Phẩm (3 tầng)

| Tầng | Ví dụ |
|---|---|
| Danh mục chính | Thịt & Hải Sản, Rau Củ Quả, Sữa & Trứng |
| Danh mục phụ | Thịt lợn, Thịt bò, Hải sản, Rau xanh, Trái cây, v.v. |
| Danh mục con cấp 3 (SubSubcategory) | Ba chỉ lợn, Thăn bò, Tôm sú, Rau muống, Xoài cát, Sữa tươi, v.v. (60+ loại) |

Mỗi `SubSubcategory` được gán `avgShelfDays` phù hợp với đặc tính bảo quản của từng loại nông sản.

#### Phương Thức Thanh Toán

| Nhà cung cấp | Loại |
|---|---|
| MOMO | E_WALLET |
| ZALOPAY | E_WALLET |
| COD | COD |

#### Sản Phẩm Tổng Quát

Hơn 150 sản phẩm nông sản tươi Việt Nam thuộc các nhóm: thịt, gia cầm, thủy hải sản, rau củ, trái cây, sữa và trứng. Mỗi sản phẩm được gán nhãn tìm kiếm (tags), đơn vị đo lường và số lượng đóng gói tiêu chuẩn.

#### Người Dùng và Khách Hàng Mẫu

Một số bản ghi `User` và `Buyer` mẫu phục vụ kiểm thử hệ thống.

### 10.3. Thông Tin Đăng Nhập Mặc Định

DataSeeder không tạo thông tin đăng nhập quản trị viên. Xác thực được quản lý hoàn toàn bởi Identity Service.

---

## 11. Cấu Hình Ứng Dụng

### 11.1. Tệp Cấu Hình Chính (`application.yaml`)

```yaml
spring:
  application:
    name: back_office_service
  config:
    import: optional:file:.env[.properties]   # Nạp biến môi trường từ .env

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/back_office_db?stringtype=unspecified
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    # Tham số stringtype=unspecified: cho phép Hibernate gửi kiểu text[] của PostgreSQL

  jpa:
    hibernate:
      ddl-auto: update       # Tự động cập nhật lược đồ khi khởi động
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      max-file-size: 500MB
      max-request-size: 500MB

server:
  port: 9100

services:
  identity:
    url: http://${IDENTITY_HOST:localhost}:${IDENTITY_PORT:9000}
  product-storage:
    url: http://${PRODUCT_STORAGE_HOST:localhost}:${PRODUCT_STORAGE_PORT:9200}
  ecommerce:
    url: http://${ECOMMERCE_HOST:localhost}:${ECOMMERCE_PORT:9300}

cloudflare:
  r2:
    account-id: ${R2_ACCOUNT_ID}
    endpoint: https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
    access-key: ${R2_ACCESS_KEY}
    secret-key: ${R2_SECRET_KEY}
    region: auto

app:
  kafka-url: ${KAFKA_HOST}:${KAFKA_PORT}
  user-avatar-bucket: back-office-user-avts
  product-general-img-bucket: product-general-img
  provider-cert-bucket: provider-certificates
  provider-video-bucket: provider-verification-videos
  provider-logo-bucket: provider-logos
  # URL công khai mặc định:
  user-avatar-public-bucket-url: https://pub-954e99f131cf4cc896de1ad360338682.r2.dev
  product-general-image-public-bucket-url: https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev
  provider-cert-public-bucket-url: https://pub-b72d8c021b3848f8b4d8805e93e18af6.r2.dev
  provider-video-public-bucket-url: https://pub-1b4c6325308a40f68dc9dca3d1771fbd.r2.dev
  provider-logo-bucket-public-url: https://pub-b5792a8ef25c47838481acc16ac8daec.r2.dev
  # URL ảnh mặc định:
  user-avatar-default-url: https://pub-954e99f131cf4cc896de1ad360338682.r2.dev/128c271e-c0a6-433e-bcd1-f3bbc4243401-default-user-avt.png
  product-general-img-default-url: https://pub-0365edd1781141cdb68675969c7cdb87.r2.dev/5fe95e6c-c064-49c4-8712-1c8f54472502.jpg

logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

### 11.2. Biến Môi Trường Bắt Buộc

| Biến | Mô tả | Ví dụ |
|---|---|---|
| `DB_HOST` | Địa chỉ máy chủ PostgreSQL | `localhost` hoặc `postgres` (Docker) |
| `DB_PORT` | Cổng PostgreSQL | `5432` |
| `DB_USERNAME` | Tên người dùng PostgreSQL | `khoidev` |
| `DB_PASSWORD` | Mật khẩu PostgreSQL | |
| `KAFKA_HOST` | Địa chỉ Kafka broker | `localhost` hoặc `kafka` (Docker) |
| `KAFKA_PORT` | Cổng Kafka | `9092` |
| `R2_ACCOUNT_ID` | ID tài khoản Cloudflare | |
| `R2_ACCESS_KEY` | Khóa truy cập R2 | |
| `R2_SECRET_KEY` | Khóa bí mật R2 | |

### 11.3. Biến Môi Trường Liên Dịch Vụ (Tùy Chọn)

| Biến | Mặc định | Mô tả |
|---|---|---|
| `IDENTITY_HOST` | `localhost` | Địa chỉ Identity Service |
| `IDENTITY_PORT` | `9000` | Cổng Identity Service |
| `PRODUCT_STORAGE_HOST` | `localhost` | Địa chỉ Product Storage Service |
| `PRODUCT_STORAGE_PORT` | `9200` | Cổng Product Storage Service |
| `ECOMMERCE_HOST` | `localhost` | Địa chỉ Ecommerce Service |
| `ECOMMERCE_PORT` | `9300` | Cổng Ecommerce Service |

---

## 12. Phụ Thuộc Kỹ Thuật Chính

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| `spring-boot-starter-parent` | 4.0.2 | Framework nền tảng |
| `spring-boot-starter-data-jpa` | (kế thừa) | ORM, Spring Data JPA, Hibernate |
| `spring-boot-starter-security` | (kế thừa) | Spring Security 6 |
| `spring-boot-starter-webmvc` | (kế thừa) | Spring MVC, REST controller |
| `spring-boot-devtools` | (kế thừa) | Tự động khởi động lại khi thay đổi mã nguồn |
| `postgresql` | (kế thừa) | Driver JDBC PostgreSQL (scope: runtime) |
| `lombok` | (kế thừa) | Sinh mã tự động (@Getter, @Setter, @Builder, @AllArgsConstructor) |
| `spring-kafka` | (kế thừa) | Tích hợp Apache Kafka |
| `jjwt-api` | 0.11.5 | API xác thực và phân tích JWT |
| `jjwt-impl` | 0.11.5 | Triển khai nội bộ JJWT |
| `jjwt-jackson` | 0.11.5 | Hỗ trợ JSON cho JJWT |
| `software.amazon.awssdk:s3` | 2.42.4 | AWS SDK S3 Client (dùng cho Cloudflare R2) |
| `spring-boot-starter-test` | (kế thừa) | JUnit 5, Mockito, Spring Test |

---

## 13. Nhận Xét Kiến Trúc

### 13.1. Điểm Mạnh Kiến Trúc

**Phân tách trách nhiệm rõ ràng:**
Kiến trúc phân tầng được tuân thủ nhất quán. Mỗi tầng chỉ giao tiếp với tầng liền kề, tránh phụ thuộc chéo giữa Controller và Repository.

**Thiết kế Interface-based:**
Tất cả service đều được định nghĩa qua interface, tạo điều kiện thuận lợi cho việc kiểm thử đơn vị (unit testing) thông qua mock và dễ dàng thay thế triển khai khi cần.

**Nhất quán trong xử lý phản hồi:**
Lớp `ApiResponse<T>` chuẩn hóa toàn bộ phản hồi HTTP, bao gồm cả trường hợp lỗi, giúp frontend dễ dàng xử lý và hiển thị.

**Cập nhật từng phần (Partial Update):**
Tất cả phương thức `update()` kiểm tra giá trị null trước khi ghi đè, tránh vô tình xóa dữ liệu hiện có.

**Idempotent Kafka Producer:**
Cấu hình `ENABLE_IDEMPOTENCE=true` và `ACKS=all` đảm bảo mỗi sự kiện chỉ được ghi một lần duy nhất, tránh trùng lặp khi mạng không ổn định.

**Xác minh nhà cung cấp hai bước:**
Quy trình xác minh tách biệt rõ ràng giữa bước nộp hồ sơ (tải lên chứng chỉ/video) và bước xét duyệt (nhân viên phê duyệt), phản ánh đúng quy trình kiểm soát chất lượng thực tế.

### 13.2. Quyết Định Thiết Kế Đáng Chú Ý

**Cấu trúc danh mục tự tham chiếu:**
Thay vì dùng hai bảng riêng cho danh mục chính và danh mục phụ, thiết kế dùng một bảng `categories` với trường `isSubCategory` và `belongToCategory` tự tham chiếu. Điều này đơn giản hóa truy vấn nhưng làm mờ ràng buộc toàn vẹn dữ liệu ở tầng cơ sở dữ liệu.

**ID đơn hàng không tự tăng:**
`Order.orderId` không sử dụng `@GeneratedValue`, phản ánh thiết kế trong đó ID đơn hàng được tạo và quản lý bởi Ecommerce Service, sau đó đồng bộ sang Back-Office Service qua Kafka.

**Khóa chính chuỗi cho SaleEvent:**
`SaleEvent.saleEventId` là `String` thay vì `Long`, cho phép sử dụng ID có ý nghĩa ngữ nghĩa (ví dụ: `"FLASH_SALE_2026_05`). Tuy nhiên, điều này làm mất tính nhất quán với các thực thể khác.

**Tách biệt Provider và User:**
Nhà cung cấp được mô hình hóa là hai thực thể riêng biệt (`Provider` + `User`) thay vì kế thừa hoặc nhúng vào nhau. Điều này cho phép mỗi thực thể phát triển độc lập và phản ánh đúng sự phân tách giữa thông tin nhận dạng và thông tin nghiệp vụ.

**Luồng video hai giai đoạn:**
Việc tạo video (`POST /videos`) tách biệt với tải tệp lên (`POST /videos/{id}/upload`) cho phép nhà cung cấp tạo mô tả trước rồi tải tệp sau, hỗ trợ kịch bản tải tệp lớn hoặc mạng không ổn định.

### 13.3. Kỹ Thuật Nợ (Technical Debt) Đã Nhận Diện

**Thiếu xử lý lỗi Kafka:**
Hiện tại, khi consumer xử lý sự kiện thất bại (ví dụ: vi phạm ràng buộc cơ sở dữ liệu, ngoại lệ từ service), hệ thống chỉ ghi nhật ký và bỏ qua. Không có cơ chế thử lại (retry) hay hàng đợi thư chết (DLQ). Điều này có thể dẫn đến mất dữ liệu khi có lỗi nhất thời.

**`OrderItem` là lớp rỗng:**
Lớp `OrderItem.java` không có bất kỳ trường nào, cho thấy tính năng quản lý chi tiết đơn hàng chưa được triển khai tại Back-Office Service. Việc hiển thị danh sách sản phẩm trong đơn hàng hiện chưa khả dụng.

**Thiếu xử lý ngoại lệ tập trung (Global Exception Handler):**
Không tìm thấy lớp `@ControllerAdvice` hoặc `@RestControllerAdvice` trong mã nguồn. Điều này có nghĩa là các ngoại lệ chưa được bắt sẽ trả về phản hồi lỗi mặc định của Spring thay vì định dạng `ApiResponse` chuẩn hóa.

**URL R2 công khai được cấu hình cứng (hardcoded):**
Các URL bucket công khai được ghi trực tiếp vào `application.yaml`. Nếu Cloudflare R2 thay đổi URL hoặc cần đổi bucket, cần sửa cấu hình và triển khai lại.

**Cấu hình CORS chứa địa chỉ IP nội bộ:**
Danh sách `allowedOrigins` trong `WebConfig.java` bao gồm các địa chỉ IP mạng nội bộ cố định (10.x.x.x, 192.168.x.x). Cách tiếp cận này không linh hoạt và cần được thay thế bằng cấu hình dạng biến môi trường trong môi trường production.

**Phân quyền chưa được triển khai:**
Mặc dù SecurityConfig bật `@EnableMethodSecurity`, không tìm thấy annotation `@PreAuthorize` hoặc `@Secured` nào trong các controller. Tất cả endpoint chỉ yêu cầu xác thực (authenticated) mà không phân biệt vai trò (role), có nghĩa là khách hàng thông thường về lý thuyết có thể gọi các endpoint quản trị nếu có token hợp lệ.

**Mức độ ghi nhật ký DEBUG trong production:**
Cấu hình `logging.level` đặt ở mức `DEBUG` cho cả `org.springframework.web` và `org.springframework.security`. Điều này phù hợp cho môi trường phát triển nhưng có thể gây ra vấn đề hiệu năng và rò rỉ thông tin nhạy cảm trong môi trường production.
