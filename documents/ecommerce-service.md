# Tài Liệu Kỹ Thuật: Ecommerce Service

**Phiên bản tài liệu**: 1.0  
**Ngày tạo**: 06/05/2026  
**Phạm vi**: Toàn bộ module `services/ecommerce-service`

---

## Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Cấu Trúc Package](#2-cấu-trúc-package)
3. [Mô Hình Dữ Liệu](#3-mô-hình-dữ-liệu)
4. [Tầng Nghiệp Vụ (Service Layer)](#4-tầng-nghiệp-vụ-service-layer)
5. [Tầng Trình Bày — REST API](#5-tầng-trình-bày--rest-api)
6. [Bảo Mật và Xác Thực](#6-bảo-mật-và-xác-thực)
7. [Giao Tiếp Hướng Sự Kiện (Kafka)](#7-giao-tiếp-hướng-sự-kiện-kafka)
8. [Lưu Trữ Tệp (Object Storage)](#8-lưu-trữ-tệp-object-storage)
9. [Giao Tiếp Liên Dịch Vụ](#9-giao-tiếp-liên-dịch-vụ)
10. [Khởi Tạo Dữ Liệu (DataSeeder)](#10-khởi-tạo-dữ-liệu-dataseeder)
11. [Cấu Hình Ứng Dụng](#11-cấu-hình-ứng-dụng)
12. [Phụ Thuộc Kỹ Thuật Chính](#12-phụ-thuộc-kỹ-thuật-chính)
13. [Nhận Xét Kiến Trúc](#13-nhận-xét-kiến-trúc)

---

## 1. Tổng Quan

### 1.1 Mục Đích

Ecommerce Service là dịch vụ vi mô chịu trách nhiệm xử lý toàn bộ luồng nghiệp vụ thương mại điện tử trong hệ thống, bao gồm: quản lý giỏ hàng, đặt hàng, thanh toán, khuyến mãi, và đánh giá sản phẩm. Dịch vụ đóng vai trò là điểm cuối tương tác trực tiếp với người mua hàng (buyer) và cung cấp các API nội bộ phục vụ tác nghiệp quản trị.

### 1.2 Ngăn Xếp Công Nghệ

| Thành phần | Công nghệ |
|---|---|
| Framework | Spring Boot 3.5.6 |
| Ngôn ngữ | Java 21 |
| Cơ sở dữ liệu | PostgreSQL (database: `ecommerce_db`) |
| Message broker | Apache Kafka 4.2.0 |
| Bảo mật | Spring Security + JWT (RSA, JJWT 0.11.5) |
| Lưu trữ tệp | Cloudflare R2 (tương thích AWS S3 SDK) |
| Thanh toán | Sepay QR Code + Google Sheets API (polling) |
| Geocoding | Goong Maps API (tính phí vận chuyển) |
| Cổng dịch vụ | `9300` |

### 1.3 Phong Cách Kiến Trúc

Dịch vụ áp dụng **Clean Architecture** (Kiến trúc sạch, còn gọi là Hexagonal Architecture), phân tách rõ ràng thành bốn tầng:

```
presentation  →  domain (use_case + entity)  ←  persistence
                         ↑
                   infrastructure
```

- **Domain Layer** (tầng miền): Chứa các entity thuần (JPA), interface định nghĩa use case, và domain service. Tầng này không phụ thuộc vào bất kỳ framework nào.
- **Persistence Layer** (tầng lưu trữ): Triển khai các repository JPA và DTO truy vấn.
- **Infrastructure Layer** (tầng cơ sở hạ tầng): Cấu hình Spring, security, Kafka, storage, và payment polling.
- **Presentation Layer** (tầng trình bày): REST Controller, request/response DTO, và mapper.

Mô hình này cô lập logic nghiệp vụ hoàn toàn khỏi các phụ thuộc kỹ thuật, giúp code dễ kiểm thử và dễ thay thế thành phần công nghệ khi cần.

---

## 2. Cấu Trúc Package

```
services/ecommerce-service/
├── src/main/java/microservice/base_source/
│   ├── BaseSourceApplication.java              # Điểm khởi động Spring Boot
│   │
│   ├── domain/                                 # Tầng miền (không phụ thuộc framework)
│   │   ├── entity/                             # Các JPA entity (mô hình dữ liệu)
│   │   │   ├── Order.java                      # Entity đơn hàng
│   │   │   ├── OrderItem.java                  # Entity mục đơn hàng
│   │   │   ├── Cart.java                       # Entity giỏ hàng
│   │   │   ├── CartItem.java                   # Entity mục giỏ hàng
│   │   │   ├── Buyer.java                      # Entity người mua
│   │   │   ├── Category.java                   # Entity danh mục sản phẩm
│   │   │   ├── ProductGeneral.java             # Entity thông tin chung sản phẩm
│   │   │   ├── BatchDetail.java                # Entity chi tiết lô hàng (tồn kho)
│   │   │   ├── Address.java                    # Entity địa chỉ giao hàng
│   │   │   ├── SaleEvent.java                  # Entity sự kiện khuyến mãi
│   │   │   ├── SaleProduct.java                # Entity liên kết sản phẩm-khuyến mãi
│   │   │   ├── SaleProductId.java              # Composite key cho SaleProduct
│   │   │   ├── Coupon.java                     # Entity phiếu giảm giá
│   │   │   ├── FeedBack.java                   # Entity đánh giá sản phẩm
│   │   │   ├── ProcessedTransaction.java       # Entity giao dịch đã xử lý (idempotency)
│   │   │   ├── Coordinate.java                 # Value object tọa độ địa lý
│   │   │   ├── BuyerSaleProduct.java           # Entity theo dõi mua hàng khuyến mãi
│   │   │   └── BuyerSaleProductId.java         # Composite key cho BuyerSaleProduct
│   │   ├── use_case/                           # Interface định nghĩa các ca sử dụng
│   │   │   ├── OrderUseCase.java               # Hợp đồng nghiệp vụ đơn hàng
│   │   │   ├── CartUseCase.java                # Hợp đồng nghiệp vụ giỏ hàng
│   │   │   ├── CartItemUseCase.java            # Hợp đồng nghiệp vụ mục giỏ hàng
│   │   │   ├── OrderItemUseCase.java           # Hợp đồng nghiệp vụ mục đơn hàng
│   │   │   ├── AddressUseCase.java             # Hợp đồng nghiệp vụ địa chỉ
│   │   │   ├── BatchDetailUseCase.java         # Hợp đồng nghiệp vụ lô hàng
│   │   │   ├── CategoryUseCase.java            # Hợp đồng nghiệp vụ danh mục
│   │   │   ├── ProductGeneralUseCase.java      # Hợp đồng nghiệp vụ sản phẩm
│   │   │   ├── SaleEventUseCase.java           # Hợp đồng nghiệp vụ sự kiện khuyến mãi
│   │   │   ├── SaleProductUseCase.java         # Hợp đồng nghiệp vụ sản phẩm khuyến mãi
│   │   │   ├── BuyerUseCase.java               # Hợp đồng nghiệp vụ người mua
│   │   │   ├── FeedBackUseCase.java            # Hợp đồng nghiệp vụ đánh giá
│   │   │   ├── SearchUseCase.java              # Hợp đồng nghiệp vụ tìm kiếm
│   │   │   └── PaymentPollingUseCase.java      # Hợp đồng polling thanh toán
│   │   └── service/                            # Triển khai các use case interface
│   │       ├── OrderService.java               # Triển khai OrderUseCase
│   │       ├── CartService.java                # Triển khai CartUseCase
│   │       ├── CartItemService.java            # Triển khai CartItemUseCase
│   │       ├── OrderItemService.java           # Triển khai OrderItemUseCase
│   │       ├── AddressService.java             # Triển khai AddressUseCase
│   │       ├── BatchDetailService.java         # Triển khai BatchDetailUseCase
│   │       ├── CategoryService.java            # Triển khai CategoryUseCase
│   │       ├── ProductGeneralService.java      # Triển khai ProductGeneralUseCase
│   │       ├── SaleEventService.java           # Triển khai SaleEventUseCase
│   │       ├── SaleProductService.java         # Triển khai SaleProductUseCase
│   │       ├── BuyerService.java               # Triển khai BuyerUseCase
│   │       ├── FeedBackService.java            # Triển khai FeedBackUseCase
│   │       ├── SearchService.java              # Triển khai SearchUseCase
│   │       └── PaymentPollingService.java      # Triển khai PaymentPollingUseCase
│   │
│   ├── persistence/                            # Tầng lưu trữ (truy cập cơ sở dữ liệu)
│   │   ├── repository/                         # Spring Data JPA Repositories
│   │   │   ├── OrderRepository.java
│   │   │   ├── OrderItemRepository.java
│   │   │   ├── CartRepository.java
│   │   │   ├── CartItemRepository.java
│   │   │   ├── BuyerRepository.java
│   │   │   ├── CategoryRepository.java
│   │   │   ├── ProductGeneralRepository.java
│   │   │   ├── BatchDetailRepository.java
│   │   │   ├── AddressRepository.java
│   │   │   ├── SaleEventRepository.java
│   │   │   ├── SaleProductRepository.java
│   │   │   ├── CouponRepository.java
│   │   │   ├── FeedBackRepository.java
│   │   │   └── ProcessedTransactionRepository.java
│   │   └── dto/                                # DTO phục vụ truy vấn phức hợp
│   │       ├── OrderSummaryDTO.java            # Thống kê đơn hàng theo trạng thái
│   │       ├── OrderDeliveryDTO.java           # Thông tin giao hàng
│   │       ├── DetailGeneralDTO.java           # Thông tin chi tiết-tổng hợp sản phẩm
│   │       ├── CartItemWithBatchDetailDTO.java # Mục giỏ hàng kèm chi tiết lô
│   │       ├── FeedBackDTO.java                # DTO đánh giá có thông tin người dùng
│   │       └── CategoryDTO.java                # DTO danh mục phân cấp
│   │
│   ├── infrastructure/                         # Tầng cơ sở hạ tầng
│   │   ├── configuration/                      # Cấu hình Spring Bean
│   │   │   ├── DataSeeder.java                 # Khởi tạo dữ liệu mẫu ban đầu
│   │   │   ├── SecurityConfig.java             # Cấu hình Spring Security
│   │   │   ├── WebConfig.java                  # Cấu hình CORS
│   │   │   ├── GlobalConfig.java               # Bean cấu hình toàn cục
│   │   │   ├── R2Config.java                   # Cấu hình Cloudflare R2 (S3 client)
│   │   │   ├── GoogleSheetsConfig.java         # Cấu hình Google Sheets API
│   │   │   └── GoongConfig.java                # Cấu hình Goong Maps API
│   │   ├── security/                           # Cơ chế xác thực và phân quyền
│   │   │   ├── JwtAuthenticationFilter.java    # Bộ lọc xác thực JWT
│   │   │   ├── JwtTokenValidator.java          # Kiểm tra và giải mã JWT
│   │   │   ├── AuthenticatedUser.java          # Đối tượng người dùng đã xác thực
│   │   │   ├── CustomAuthenticationEntryPoint.java  # Xử lý lỗi 401
│   │   │   └── CustomAccessDeniedHandler.java  # Xử lý lỗi 403
│   │   ├── messaging/                          # Tích hợp Apache Kafka
│   │   │   ├── KafkaConsumerConfig.java        # Cấu hình consumer
│   │   │   ├── KafkaProducerConfig.java        # Cấu hình producer
│   │   │   ├── batchdetail/                    # Consumer: batch-detail-events
│   │   │   ├── buyer/                          # Consumer: user-events
│   │   │   ├── category/                       # Consumer: category-events
│   │   │   ├── productgeneral/                 # Consumer: product-general-events
│   │   │   └── order/                          # Producer + Consumer: order events
│   │   └── storage/
│   │       └── R2UploadService.java            # Dịch vụ tải tệp lên Cloudflare R2
│   │
│   └── presentation/                           # Tầng trình bày (REST API)
│       ├── rest/                               # REST Controllers
│       │   ├── OrderController.java
│       │   ├── CartController.java
│       │   ├── CartItemController.java
│       │   ├── ProductSearchController.java
│       │   ├── AddressController.java
│       │   ├── CouponController.java
│       │   ├── FeedBackController.java
│       │   ├── CategoryController.java
│       │   ├── BatchDetailController.java
│       │   ├── ProductController.java
│       │   ├── ProductGeneralController.java
│       │   ├── SaleEventController.java
│       │   └── SaleProductController.java
│       ├── request/                            # DTO nhận dữ liệu từ client
│       ├── response/                           # DTO trả dữ liệu về client
│       └── mapping/
│           └── ProductMapMapper.java           # Mapper chuyển đổi request/response
│
├── src/main/resources/
│   ├── application.yaml                        # Cấu hình ứng dụng
│   ├── keys/
│   │   └── public.pem                          # Khóa công khai RSA để xác thực JWT
│   └── google-credentials.json                 # Service account Google API
│
├── pom.xml                                     # Quản lý phụ thuộc Maven
├── docker-compose.yml                          # Cấu hình Docker
└── .env.example                                # Mẫu biến môi trường
```

---

## 3. Mô Hình Dữ Liệu

### 3.1 Danh Sách Entity và Cấu Trúc Bảng

#### Order — Bảng `ORDERS`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `order_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `buyer_id` | VARCHAR | NOT NULL, FK → BUYER | ID người mua (đồng bộ từ identity-service) |
| `address_id` | BIGINT | NOT NULL, FK → address | Địa chỉ giao hàng |
| `shipping_fee` | BIGINT | | Phí vận chuyển (VND) |
| `status` | VARCHAR (Enum) | NOT NULL | Trạng thái: PENDING, PAID, CONFIRMED, DELIVERING, DELIVERED, RECEIVED, CANCELLED |
| `note` | TEXT | | Ghi chú của người mua |
| `total_price` | BIGINT | | Tổng tiền cuối (đã giảm + phí ship) |
| `coupon_id` | BIGINT | FK → COUPON | Phiếu giảm giá áp dụng |
| `price_before_discount` | BIGINT | | Tổng tiền trước khi giảm |
| `discount_amount` | BIGINT | | Số tiền được giảm |
| `price_after_discount` | BIGINT | | Tổng tiền sau khi giảm (chưa cộng ship) |
| `transaction_id` | VARCHAR | UNIQUE | ID giao dịch Sepay |
| `transaction_qr_url` | TEXT | | URL ảnh QR thanh toán |
| `payment_method` | VARCHAR (Enum) | | COD hoặc VNPAY |
| `created_at` | TIMESTAMP | Auto @PrePersist | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto @PreUpdate | Thời điểm cập nhật |

#### OrderItem — Bảng `ORDER_ITEM`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `order_item_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `order_id` | BIGINT | NOT NULL, FK → ORDERS | Đơn hàng chứa mục này |
| `batch_detail_id` | VARCHAR | NOT NULL, FK → BATCH_DETAIL | Lô hàng được mua |
| `sale_event_id` | BIGINT | FK → SALE_EVENT | Sự kiện khuyến mãi áp dụng (nếu có) |
| `quantity` | BIGINT | NOT NULL | Số lượng mua |
| `original_price` | DECIMAL | | Giá gốc tại thời điểm đặt hàng |
| `unit_price_at_purchase` | DECIMAL | | Giá thực trả sau khuyến mãi |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### Cart — Bảng `CART`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `card_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính (lưu ý: tên cột là `card_id`) |
| `buyer_id` | VARCHAR | NOT NULL, FK → BUYER | Người sở hữu giỏ hàng |
| `address_id` | BIGINT | FK → address | Địa chỉ giao hàng đang chọn |
| `shipping_fee` | BIGINT | | Phí vận chuyển hiện tại |
| `total_price` | BIGINT | | Tổng tiền cuối |
| `coupon_id` | BIGINT | FK → COUPON | Phiếu giảm giá đang áp dụng |
| `price_before_discount` | BIGINT | | Tổng trước giảm |
| `discount_amount` | BIGINT | | Số tiền giảm |
| `price_after_discount` | BIGINT | | Tổng sau giảm |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### CartItem — Bảng `CART_ITEM`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `cart_item_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `cart_id` | BIGINT | NOT NULL, FK → CART | Giỏ hàng chứa mục |
| `batch_detail_id` | VARCHAR | NOT NULL, FK → BATCH_DETAIL | Lô hàng thêm vào giỏ |
| `sale_event_id` | BIGINT | FK → SALE_EVENT | Sự kiện khuyến mãi (nếu có) |
| `quantity` | BIGINT | NOT NULL | Số lượng |
| `is_selected` | BOOLEAN | | Có được chọn để thanh toán không |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### Buyer — Bảng `BUYER`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `buyer_id` | VARCHAR | PK | ID người dùng (đồng bộ từ identity-service qua Kafka) |
| `f_name` | VARCHAR | | Họ |
| `l_name` | VARCHAR | | Tên |
| `email` | VARCHAR | | Email |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### Address — Bảng `address`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `address_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `buyer_id` | VARCHAR | NOT NULL, FK → BUYER | Người sở hữu địa chỉ |
| `receiver_name` | VARCHAR | NOT NULL | Tên người nhận |
| `receiver_p_num` | VARCHAR | | Số điện thoại người nhận |
| `province` | VARCHAR | | Tỉnh/Thành phố |
| `district` | VARCHAR | | Quận/Huyện |
| `commune` | VARCHAR | | Phường/Xã |
| `detail` | VARCHAR | | Địa chỉ chi tiết (số nhà, đường) |
| `lat` | DOUBLE | | Vĩ độ (dùng tính phí ship) |
| `lng` | DOUBLE | | Kinh độ (dùng tính phí ship) |
| `is_default` | BOOLEAN | | Có phải địa chỉ mặc định không |

#### Category — Bảng `CATEGORY`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `category_id` | BIGINT | PK | ID (gán thủ công từ back-office-service) |
| `category_name` | VARCHAR | NOT NULL | Tên danh mục |
| `display_order` | INTEGER | | Thứ tự hiển thị |
| `description` | TEXT | | Mô tả danh mục |
| `icon_url` | VARCHAR | | URL biểu tượng |
| `is_sub_category` | VARCHAR | | "Y" nếu là danh mục con |
| `belong_to_category` | BIGINT | FK (tự tham chiếu) | ID danh mục cha |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### ProductGeneral — Bảng `PRODUCT_GENERAL`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `product_general_id` | BIGINT | PK | ID (đồng bộ từ back-office-service) |
| `category_id` | BIGINT | FK → CATEGORY | Danh mục thuộc về |
| `provider_id` | VARCHAR | | ID nhà cung cấp/nông dân |
| `name` | VARCHAR | NOT NULL | Tên sản phẩm |
| `description` | TEXT | | Mô tả chi tiết sản phẩm |
| `status` | VARCHAR | | Trạng thái (ACTIVE/INACTIVE) |
| `unit` | VARCHAR (Enum) | | Đơn vị: KILOGRAM, GRAM, PIECE, DOZEN, LITER, MILLILITER, PACK, BOX, BOTTLE |
| `unit_quantity` | BIGINT | | Số lượng mặc định mỗi đơn vị |
| `tags` | VARCHAR[] | | Mảng từ khóa tìm kiếm |
| `img` | TEXT | | URL hình ảnh sản phẩm |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |
| `deleted_at` | TIMESTAMP | | Xóa mềm (soft delete) |

#### BatchDetail — Bảng `BATCH_DETAIL`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `batch_detail_id` | VARCHAR | PK | ID lô hàng (đồng bộ từ product-storage-service) |
| `product_general_id` | BIGINT | FK → PRODUCT_GENERAL | Sản phẩm thuộc về |
| `quantity` | INT | | Số lượng tồn kho |
| `price` | DECIMAL | | Giá bán mỗi đơn vị (VND) |
| `avg_rate` | DECIMAL(3,2) | | Điểm đánh giá trung bình |
| `num_rate` | INT | | Tổng số đánh giá |
| `detail_content` | TEXT | | Mô tả chi tiết lô (kích cỡ, màu sắc, v.v.) |
| `sub_batch_id` | BIGINT | | ID lô phụ (nếu có) |
| `verification_type` | VARCHAR | | Loại chứng nhận kiểm tra chất lượng |
| `certificate_type` | VARCHAR | | Loại giấy chứng nhận |
| `provider_id` | BIGINT | | ID nhà cung cấp |
| `logo_url` | VARCHAR | | URL logo nhà cung cấp |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |
| `deleted_at` | TIMESTAMP | | Xóa mềm |

#### SaleEvent — Bảng `SALE_EVENT`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `sale_event_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `name` | VARCHAR | | Tên chiến dịch khuyến mãi |
| `description` | VARCHAR | | Mô tả chiến dịch |
| `img` | TEXT | | URL banner khuyến mãi |
| `display_priority` | BIGINT | | Độ ưu tiên hiển thị |
| `active_yn` | VARCHAR | | "Y"/"N" — đang kích hoạt |
| `enabled_yn` | VARCHAR | | "Y"/"N" — cho phép sử dụng |
| `begin_time` | TIME | | Giờ bắt đầu trong ngày |
| `end_time` | TIME | | Giờ kết thúc trong ngày |
| `begin_date` | TIMESTAMP | | Ngày bắt đầu chiến dịch |
| `end_date` | TIMESTAMP | | Ngày kết thúc chiến dịch |
| `detail` | JSONB | | Cấu hình mở rộng (JSON) |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |
| `deleted_at` | TIMESTAMP | | Xóa mềm |

#### SaleProduct — Bảng `SALE_PRODUCT`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `sale_event_id` | BIGINT | PK (composite), FK → SALE_EVENT | Khóa composite |
| `batch_id` | VARCHAR | PK (composite), FK → BATCH_DETAIL | Khóa composite |
| `dis_val` | INTEGER | | Phần trăm giảm giá (%) |
| `sale_price` | INTEGER | | Giá khuyến mãi (VND) |
| `max_qty` | BIGINT | | Số lượng tối đa trong đợt sale |
| `cur_qty` | BIGINT | | Số lượng còn lại trong đợt sale |
| `max_buy` | BIGINT | | Số lượng tối đa mỗi người mua |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |
| `deleted_at` | TIMESTAMP | | Xóa mềm |

#### Coupon — Bảng `COUPON`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `coupon_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `coupon_code` | VARCHAR | UNIQUE, NOT NULL | Mã phiếu giảm giá |
| `total_quantity` | BIGINT | | Tổng số lần sử dụng được phép |
| `current_quantity` | BIGINT | | Số lần sử dụng còn lại |
| `discount_type` | VARCHAR (Enum) | | PERCENTAGE (%) hoặc FIXED_AMOUNT (VND cố định) |
| `discount_value` | BIGINT | | Giá trị giảm (% hoặc VND tùy loại) |
| `max_discount_amount` | BIGINT | | Giới hạn tối đa tiền được giảm |
| `min_order_value` | BIGINT | | Giá trị đơn hàng tối thiểu để áp dụng |
| `public_yn` | VARCHAR | | "Y"/"N" — hiển thị công khai |
| `expired_at` | TIMESTAMP | | Thời điểm hết hạn |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |

#### FeedBack — Bảng `FEED_BACK`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `feedback_id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `reply_id` | BIGINT | FK (tự tham chiếu, nullable) | ID đánh giá cha (nếu là phản hồi) |
| `buyer_id` | VARCHAR | NOT NULL, FK → BUYER | Người đánh giá |
| `batch_detail_id` | VARCHAR | FK → BATCH_DETAIL | Lô hàng được đánh giá |
| `rating` | DECIMAL(3,2) | | Điểm đánh giá (thang 5.0) |
| `content` | TEXT | | Nội dung nhận xét |
| `img` | TEXT | | URL hình ảnh kèm theo |
| `detail` | JSONB | | Nội dung mở rộng (HTML, CSS, video, tệp) |
| `created_at` | TIMESTAMP | Auto | Thời điểm tạo |
| `updated_at` | TIMESTAMP | Auto | Thời điểm cập nhật |
| `deleted_at` | TIMESTAMP | | Xóa mềm |

#### ProcessedTransaction — Bảng `processed_transactions`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Khóa chính |
| `transaction_id` | VARCHAR | UNIQUE, NOT NULL | ID giao dịch Sepay (idempotency key) |
| `processed_at` | TIMESTAMP | Auto @PrePersist | Thời điểm xử lý |

#### BuyerSaleProduct — Bảng `BUYER_SALE_PRODUCT`

| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
|---|---|---|---|
| `buyer_id` | VARCHAR | PK (composite), FK → BUYER | Người mua |
| `sale_event_id` | BIGINT | PK (composite), FK → SALE_EVENT | Sự kiện sale |
| `batch_id` | VARCHAR | PK (composite), FK → BATCH_DETAIL | Lô hàng |
| `quantity_bought` | BIGINT | | Số lượng đã mua trong đợt sale |

### 3.2 Sơ Đồ Quan Hệ Thực Thể (ERD)

```
BUYER (buyer_id PK)
  |-- 1:N --> CART (buyer_id FK)
  |             |-- 1:N --> CART_ITEM (cart_id FK)
  |                           |-- N:1 --> BATCH_DETAIL
  |                           |-- N:1 --> SALE_EVENT
  |-- 1:N --> ORDER (buyer_id FK)
  |             |-- 1:N --> ORDER_ITEM (order_id FK)
  |                           |-- N:1 --> BATCH_DETAIL
  |                           |-- N:1 --> SALE_EVENT
  |             |-- N:1 --> address
  |             |-- N:1 --> COUPON
  |-- 1:N --> address (buyer_id FK)
  |-- 1:N --> FEED_BACK (buyer_id FK)
  |-- M:N --> BUYER_SALE_PRODUCT

PRODUCT_GENERAL (product_general_id PK)
  |-- N:1 --> CATEGORY
  |-- 1:N --> BATCH_DETAIL (product_general_id FK)
                |-- 1:N --> CART_ITEM
                |-- 1:N --> ORDER_ITEM
                |-- 1:N --> FEED_BACK
                |-- M:N --> SALE_EVENT via SALE_PRODUCT

CATEGORY (category_id PK)
  |-- 0:1 --> CATEGORY (self-reference: belong_to_category)
  |-- 1:N --> PRODUCT_GENERAL

SALE_EVENT (sale_event_id PK)
  |-- M:N --> BATCH_DETAIL via SALE_PRODUCT
  |-- M:N --> BUYER via BUYER_SALE_PRODUCT

COUPON (coupon_id PK)
  |-- 1:N --> CART
  |-- 1:N --> ORDER

PROCESSED_TRANSACTION (transaction_id UNIQUE)
  — Bảng kiểm tra trùng lặp giao dịch Sepay
```

---

## 4. Tầng Nghiệp Vụ (Service Layer)

Mỗi use case interface được định nghĩa trong `domain/use_case/` và được triển khai tương ứng trong `domain/service/`. Các service triển khai được tiêm phụ thuộc qua constructor (`@AllArgsConstructor` của Lombok).

### 4.1 OrderUseCase / OrderService

| Phương thức | Mô tả |
|---|---|
| `search(buyerId, searchString, status, minPrice, maxPrice, minTime, maxTime, sortOptions, page, size)` | Tìm kiếm và lọc đơn hàng theo nhiều tiêu chí, hỗ trợ sắp xếp và phân trang |
| `create(Order, List<OrderItem>)` | Tạo đơn hàng mới từ danh sách mục được cung cấp trực tiếp |
| `createFromCart(buyerId, paymentMethod, note)` | Chuyển toàn bộ mục được chọn trong giỏ hàng thành đơn hàng; tính toán giá, giảm giá, phí ship; tạo QR nếu thanh toán VNPAY; phát sự kiện `OrderCreatedEvent` |
| `getAll(page, size)` | Lấy toàn bộ đơn hàng (dành cho admin) |
| `getByBuyerId(buyerId, page, size)` | Lấy danh sách đơn hàng của một người mua cụ thể |
| `getOrderDetail(orderId)` | Lấy chi tiết đơn hàng bao gồm tất cả mục, địa chỉ, và thông tin người mua |
| `get(id)` | Lấy một đơn hàng theo ID; ném ngoại lệ nếu không tồn tại |
| `delete(id)` | Xóa đơn hàng theo ID |
| `confirmOrder(orderId)` | Admin xác nhận đơn hàng (PAID → CONFIRMED); phát sự kiện `OrderConfirmedEvent` và `OrderPickRequestedEvent` cho kho |
| `updateOrderStatus(orderId, status)` | Cập nhật trạng thái đơn hàng trực tiếp (dùng nội bộ khi nhận sự kiện Kafka) |
| `getPaymentStatus(orderId)` | Trả về trạng thái thanh toán nhẹ (orderId, status, paymentMethod, transactionId, qrUrl) cho client polling |
| `receiveOrder(orderId)` | Người mua xác nhận đã nhận hàng (DELIVERED → RECEIVED) |
| `cancelOrder(orderId)` | Người mua hủy đơn hàng (chỉ khi còn ở trạng thái PENDING) |
| `getOrderSummary()` | Thống kê đơn hàng theo từng trạng thái (dành cho dashboard admin) |
| `getDeliveryOrders()` | Lấy danh sách đơn đang giao với thông tin địa chỉ đầy đủ |

**Quy trình trạng thái đơn hàng:**

```
PENDING → PAID → CONFIRMED → DELIVERING → DELIVERED → RECEIVED
    |
    └──> CANCELLED (chỉ từ PENDING)
```

### 4.2 CartUseCase / CartService

| Phương thức | Mô tả |
|---|---|
| `create(Cart)` | Tạo giỏ hàng mới cho người mua |
| `getByBuyerId(buyerId)` | Lấy giỏ hàng theo ID người mua |
| `getCartWithItems(buyerId)` | Lấy giỏ hàng kèm danh sách đầy đủ các mục, giá, và trạng thái khuyến mãi |
| `update(cartId, Cart)` | Cập nhật thông tin giỏ hàng |
| `delete(cartId)` | Xóa giỏ hàng |
| `applyCoupon(buyerId, couponCode)` | Áp dụng mã giảm giá: kiểm tra hạn, số lần còn lại, giá trị tối thiểu, và tính lại tổng tiền |
| `removeCoupon(buyerId)` | Xóa mã giảm giá đang áp dụng và tính lại tổng tiền |
| `updateAddress(buyerId, addressId)` | Cập nhật địa chỉ giao hàng cho giỏ; kích hoạt tính toán lại phí vận chuyển |
| `getAllCoupon(cartId, page, size)` | Liệt kê tất cả phiếu giảm giá hợp lệ có thể áp dụng cho giỏ hàng hiện tại |

### 4.3 CartItemUseCase / CartItemService

| Phương thức | Mô tả |
|---|---|
| `addItem(buyerId, batchDetailId, quantity, saleEventId)` | Thêm sản phẩm vào giỏ; nếu sản phẩm đã tồn tại thì tăng số lượng |
| `updateQuantity(cartItemId, quantity)` | Cập nhật số lượng mục trong giỏ |
| `removeItem(cartItemId)` | Xóa một mục khỏi giỏ và tính lại tổng |
| `selectItem(cartItemId, isSelected)` | Đánh dấu mục được chọn/bỏ chọn để thanh toán |
| `getByCartId(cartId)` | Lấy tất cả mục trong giỏ hàng |

### 4.4 AddressUseCase / AddressService

| Phương thức | Mô tả |
|---|---|
| `create(Address)` | Tạo địa chỉ mới; nếu là địa chỉ đầu tiên thì tự động đặt làm mặc định |
| `read(addressId)` | Lấy địa chỉ theo ID |
| `readBuyerAddresses(buyerId)` | Lấy tất cả địa chỉ của người mua |
| `update(addressId, Address)` | Cập nhật thông tin địa chỉ; xử lý thay đổi địa chỉ mặc định |
| `delete(addressId)` | Xóa địa chỉ |
| `calculateShipmentFee(addressId)` | Tính phí vận chuyển từ kho đến địa chỉ chỉ định bằng Goong Maps API |

### 4.5 PaymentPollingUseCase / PaymentPollingService

| Phương thức | Mô tả |
|---|---|
| `pollPayments()` | Được gọi định kỳ (10 giây/lần); đọc Google Sheets để phát hiện giao dịch mới |
| `processRow(row)` | Xử lý một hàng dữ liệu từ Google Sheets; trích xuất mã đơn hàng và cập nhật trạng thái |
| `extractOrderCode(description)` | Dùng regex `DH\d{8}` để trích xuất mã đơn hàng từ mô tả giao dịch |

**Quy tắc nghiệp vụ quan trọng**: Trước khi xử lý, kiểm tra `ProcessedTransaction` để tránh xử lý giao dịch trùng lặp (idempotency).

### 4.6 SearchUseCase / SearchService

| Phương thức | Mô tả |
|---|---|
| `searchProducts(categoryId, productGeneralId, searchString, minPrice, maxPrice, minRating, maxRating, minNumRate, maxNumRate, searchTags, createdSortOption, ratingSortOption, numRateSortOption, page, size)` | Tìm kiếm sản phẩm toàn diện với lọc đa tiêu chí (danh mục, giá, đánh giá, từ khóa, thẻ) và nhiều tùy chọn sắp xếp |

### 4.7 SaleEventUseCase / SaleEventService

| Phương thức | Mô tả |
|---|---|
| `create(SaleEvent)` | Tạo chiến dịch khuyến mãi mới |
| `get(id)` | Lấy chiến dịch theo ID |
| `getAll(page, size)` | Lấy tất cả chiến dịch |
| `update(id, SaleEvent)` | Cập nhật thông tin chiến dịch |
| `delete(id)` | Xóa mềm chiến dịch |
| `getActiveSaleEvents()` | Lấy các chiến dịch đang trong thời gian hiệu lực |
| `uploadBanner(saleEventId, file)` | Tải lên ảnh banner cho chiến dịch lên Cloudflare R2 |

### 4.8 FeedBackUseCase / FeedBackService

| Phương thức | Mô tả |
|---|---|
| `create(FeedBack)` | Tạo đánh giá mới; cập nhật `avg_rate` và `num_rate` trong BatchDetail |
| `read(id)` | Lấy đánh giá theo ID |
| `readAll(page, size)` | Lấy tất cả đánh giá |
| `readByBatchId(batchId, page, size)` | Lấy đánh giá theo lô hàng |
| `readByBuyerId(buyerId, page, size)` | Lấy đánh giá của người mua |
| `readByProductGeneralId(productGeneralId, page, size)` | Lấy đánh giá theo sản phẩm chung |
| `update(id, FeedBack)` | Cập nhật nội dung đánh giá; chỉ cho phép chủ sở hữu |
| `delete(id)` | Xóa mềm đánh giá; cập nhật lại điểm trung bình |

---

## 5. Tầng Trình Bày — REST API

Tất cả endpoint đều trả về cấu trúc `ApiResponse<T>` nhất quán. Địa chỉ gốc: `http://localhost:9300`.

### 5.1 OrderController — `/api/orders`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Body yêu cầu | Kiểu phản hồi |
|---|---|---|---|---|---|
| `POST` | `/api/orders` | Bắt buộc | BUYER | `CreateOrderFromCartRequest` | `ApiResponse<Order>` |
| `GET` | `/api/orders` | Bắt buộc | BUYER | — | `ApiResponse<List<Order>>` |
| `GET` | `/api/orders/{id}` | Bắt buộc | BUYER | — | `ApiResponse<OrderDetailResponse>` |
| `GET` | `/api/orders/{id}/payment-status` | Bắt buộc | BUYER | — | `ApiResponse<OrderPaymentStatusResponse>` |
| `GET` | `/api/orders/search` | Bắt buộc | BUYER | Query params | `ApiResponse<List<Order>>` |
| `PUT` | `/api/orders/{id}/confirm` | Bắt buộc | ADMIN | — | `ApiResponse<Order>` |
| `PUT` | `/api/orders/{id}/receive` | Bắt buộc | BUYER | — | `ApiResponse<Order>` |
| `DELETE` | `/api/orders/{id}` | Bắt buộc | BUYER/ADMIN | — | `ApiResponse<Void>` |
| `GET` | `/api/orders/admin/order-summary` | Bắt buộc | ADMIN | — | `ApiResponse<List<OrderSummaryDTO>>` |
| `GET` | `/api/orders/admin/delivery` | Bắt buộc | ADMIN | — | `ApiResponse<List<OrderDeliveryDTO>>` |

**`CreateOrderFromCartRequest`:**
```json
{
  "paymentMethod": "COD | VNPAY",
  "note": "Giao trước 18h"
}
```

**`OrderPaymentStatusResponse`:**
```json
{
  "orderId": 12345,
  "status": "PENDING",
  "paymentMethod": "VNPAY",
  "transactionId": "TXN123",
  "transactionQrUrl": "https://qr.sepay.vn/img?..."
}
```

**Tham số tìm kiếm (`/api/orders/search`):**

| Tham số | Kiểu | Mô tả |
|---|---|---|
| `buyerId` | String | Lọc theo ID người mua |
| `searchString` | String | Từ khóa tìm kiếm |
| `status` | String | Trạng thái đơn hàng |
| `minPrice` / `maxPrice` | Long | Khoảng giá |
| `minTime` / `maxTime` | String | Khoảng thời gian |
| `sortByStatus` | Boolean | Sắp xếp theo trạng thái |
| `sortByPrice` | Boolean | Sắp xếp theo giá |
| `sortByTime` | Boolean | Sắp xếp theo thời gian |
| `page` / `size` | Integer | Phân trang |

### 5.2 CartController — `/api/cart`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Body / Query | Kiểu phản hồi |
|---|---|---|---|---|---|
| `POST` | `/api/cart` | Bắt buộc | BUYER | `CartRequest` | `ApiResponse<Cart>` |
| `GET` | `/api/cart` | Bắt buộc | BUYER | — | `ApiResponse<CartDetailResponse>` |
| `GET` | `/api/cart/coupons` | Bắt buộc | BUYER | `?page&size` | `ApiResponse<List<CartCouponResponse>>` |
| `POST` | `/api/cart/apply-coupon` | Bắt buộc | BUYER | `?couponCode` | `ApiResponse<Cart>` |
| `POST` | `/api/cart/remove-coupon` | Bắt buộc | BUYER | — | `ApiResponse<Cart>` |
| `POST` | `/api/cart/update-address` | Bắt buộc | BUYER | `?addressId` | `ApiResponse<Cart>` |
| `GET` | `/api/cart/admin/{userId}` | Bắt buộc | ADMIN | — | `ApiResponse<Cart>` |

### 5.3 CartItemController — `/api/cart-items`

| Phương thức HTTP | Đường dẫn | Xác thực | Body / Query | Kiểu phản hồi |
|---|---|---|---|---|
| `POST` | `/api/cart-items` | Bắt buộc | `CartItemRequest` | `ApiResponse<CartItem>` |
| `PUT` | `/api/cart-items/{id}` | Bắt buộc | `CartItemUpdateRequest` | `ApiResponse<CartItem>` |
| `DELETE` | `/api/cart-items/{id}` | Bắt buộc | — | `ApiResponse<Void>` |
| `PUT` | `/api/cart-items/{id}/select` | Bắt buộc | `?isSelected` | `ApiResponse<CartItem>` |
| `GET` | `/api/cart-items/cart/{cartId}` | Bắt buộc | — | `ApiResponse<List<CartItem>>` |

### 5.4 ProductSearchController — `/api/product-search`

| Phương thức HTTP | Đường dẫn | Xác thực | Tham số query | Kiểu phản hồi |
|---|---|---|---|---|
| `GET` | `/api/product-search` | Không | `categoryId`, `productGeneralId`, `searchString`, `minPrice`, `maxPrice`, `minRating`, `maxRating`, `minNumRate`, `maxNumRate`, `searchTags`, `createdSortOption`, `ratingSortOption`, `numRateSortOption`, `page`, `size` | `ApiResponse<List<ProductSearchResponse>>` |

### 5.5 AddressController — `/api/address`

| Phương thức HTTP | Đường dẫn | Xác thực | Body / Query | Kiểu phản hồi |
|---|---|---|---|---|
| `POST` | `/api/address` | Bắt buộc | `AddressRequest` | `ApiResponse<Address>` |
| `GET` | `/api/address` | Bắt buộc | — | `ApiResponse<List<Address>>` |
| `GET` | `/api/address/{addressId}` | Bắt buộc | — | `ApiResponse<Address>` |
| `PUT` | `/api/address/{addressId}` | Bắt buộc | `AddressRequest` | `ApiResponse<Address>` |
| `DELETE` | `/api/address/{addressId}` | Bắt buộc | — | `ApiResponse<Void>` |
| `GET` | `/api/address/shipment-fee` | Không | `?addressId` | `ApiResponse<ShipmentFeeResponse>` |

### 5.6 CouponController — `/api/coupon`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Body / Query | Kiểu phản hồi |
|---|---|---|---|---|---|
| `POST` | `/api/coupon` | Bắt buộc | ADMIN | `CouponRequest` | `ApiResponse<Coupon>` |
| `GET` | `/api/coupon` | Không | — | `?page&size` | `ApiResponse<List<Coupon>>` |
| `GET` | `/api/coupon/{id}` | Không | — | — | `ApiResponse<Coupon>` |
| `PUT` | `/api/coupon/{id}` | Bắt buộc | ADMIN | `CouponRequest` | `ApiResponse<Coupon>` |
| `DELETE` | `/api/coupon/{id}` | Bắt buộc | ADMIN | — | `ApiResponse<Void>` |

### 5.7 FeedBackController — `/api/feed-backs`

| Phương thức HTTP | Đường dẫn | Xác thực | Body / Query | Kiểu phản hồi |
|---|---|---|---|---|
| `POST` | `/api/feed-backs` | Bắt buộc | `FeedBackRequest` | `ApiResponse<FeedBack>` |
| `GET` | `/api/feed-backs` | Không | `?page&size` | `ApiResponse<List<FeedBack>>` |
| `GET` | `/api/feed-backs/{id}` | Không | — | `ApiResponse<FeedBack>` |
| `GET` | `/api/feed-backs/batch/{batchId}` | Không | `?page&size` | `ApiResponse<List<FeedBackDTO>>` |
| `GET` | `/api/feed-backs/buyer/{buyerId}` | Không | `?page&size` | `ApiResponse<List<FeedBackDTO>>` |
| `GET` | `/api/feed-backs/product/{productGeneralId}` | Không | `?page&size` | `ApiResponse<List<FeedBackDTO>>` |
| `PUT` | `/api/feed-backs/{id}` | Bắt buộc (chủ sở hữu) | `FeedBackRequest` | `ApiResponse<FeedBack>` |
| `DELETE` | `/api/feed-backs/{id}` | Bắt buộc (chủ sở hữu) | — | `ApiResponse<Void>` |

### 5.8 CategoryController — `/api/categories`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Kiểu phản hồi |
|---|---|---|---|---|
| `GET` | `/api/categories` | Không | — | `ApiResponse<List<CategoryDTO>>` |
| `GET` | `/api/categories/{id}` | Không | — | `ApiResponse<Category>` |
| `POST` | `/api/categories` | Bắt buộc | ADMIN | `ApiResponse<Category>` |
| `PUT` | `/api/categories/{id}` | Bắt buộc | ADMIN | `ApiResponse<Category>` |
| `DELETE` | `/api/categories/{id}` | Bắt buộc | ADMIN | `ApiResponse<Void>` |

### 5.9 SaleEventController — `/api/sale-events`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Body | Kiểu phản hồi |
|---|---|---|---|---|---|
| `POST` | `/api/sale-events` | Bắt buộc | ADMIN | `SaleEventRequest` | `ApiResponse<SaleEvent>` |
| `GET` | `/api/sale-events` | Không | — | — | `ApiResponse<List<SaleEvent>>` |
| `GET` | `/api/sale-events/active` | Không | — | — | `ApiResponse<List<SaleEvent>>` |
| `GET` | `/api/sale-events/{id}` | Không | — | — | `ApiResponse<SaleEvent>` |
| `PUT` | `/api/sale-events/{id}` | Bắt buộc | ADMIN | `SaleEventRequest` | `ApiResponse<SaleEvent>` |
| `DELETE` | `/api/sale-events/{id}` | Bắt buộc | ADMIN | — | `ApiResponse<Void>` |
| `POST` | `/api/sale-events/{id}/banner` | Bắt buộc | ADMIN | `MultipartFile` | `ApiResponse<String>` |

### 5.10 BatchDetailController — `/api/batch-details`

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền | Kiểu phản hồi |
|---|---|---|---|---|
| `GET` | `/api/batch-details` | Không | — | `ApiResponse<List<BatchDetail>>` |
| `GET` | `/api/batch-details/{id}` | Không | — | `ApiResponse<BatchDetail>` |
| `GET` | `/api/batch-details/product/{productId}` | Không | — | `ApiResponse<List<DetailGeneralDTO>>` |
| `PUT` | `/api/batch-details/{id}` | Bắt buộc | ADMIN | `ApiResponse<BatchDetail>` |
| `DELETE` | `/api/batch-details/{id}` | Bắt buộc | ADMIN | `ApiResponse<Void>` |

---

## 6. Bảo Mật và Xác Thực

### 6.1 Cơ Chế Xác Thực

Dịch vụ sử dụng **JWT (JSON Web Token) với thuật toán RS256** (RSA 2048-bit). Thay vì tự cấp phát token, dịch vụ chỉ **xác minh** token được cấp bởi `identity-service`. Khóa công khai RSA được đặt tại `src/main/resources/keys/public.pem`.

**Luồng xác thực:**

```
Client Request
  |
  ├── JwtAuthenticationFilter
  │     |
  │     ├── Trích xuất header: Authorization: Bearer <token>
  │     |
  │     ├── JwtTokenValidator.validate(token)
  │     │     |
  │     │     ├── Kiểm tra chữ ký RSA (public key)
  │     │     ├── Kiểm tra issuer: "identity-service"
  │     │     ├── Kiểm tra thời hạn (exp claim)
  │     │     └── Trích xuất: userId, userEmail, permissions
  │     |
  │     └── Tạo AuthenticatedUser, đặt vào SecurityContext
  |
  └── Controller / Service
        └── Lấy AuthenticatedUser từ SecurityContextHolder
```

**Cấu trúc claims trong JWT:**

| Claim | Kiểu | Mô tả |
|---|---|---|
| `userId` | Long | ID người dùng trong hệ thống |
| `userEmail` | String | Email người dùng |
| `permissions` | List\<String\> | Danh sách quyền (ví dụ: "ADMIN", "BUYER") |
| `iss` | String | Phải là "identity-service" |
| `exp` | Long | Thời điểm hết hạn |

### 6.2 Cấu Hình Security Chain

```
SecurityFilterChain:
├── CSRF: Vô hiệu hóa (stateless REST API)
├── HTTP Basic: Vô hiệu hóa
├── Form Login: Vô hiệu hóa
├── Session: STATELESS
├── Exception Handling:
│     ├── authenticationEntryPoint → CustomAuthenticationEntryPoint (HTTP 401 + JSON)
│     └── accessDeniedHandler → CustomAccessDeniedHandler (HTTP 403 + JSON)
└── Thứ tự filter: JwtAuthenticationFilter BEFORE UsernamePasswordAuthenticationFilter
```

### 6.3 Quy Tắc Phân Quyền Endpoint

| Loại endpoint | Quy tắc truy cập |
|---|---|
| `OPTIONS *` | Cho phép tất cả (CORS preflight) |
| `/api/orders/**` | Yêu cầu xác thực |
| `/api/address/**` | Yêu cầu xác thực |
| `/api/cart/**` | Yêu cầu xác thực |
| `/api/cart-items/**` | Yêu cầu xác thực |
| `/api/feed-backs` (POST, PUT, DELETE) | Yêu cầu xác thực + chủ sở hữu |
| `/api/orders/admin/**` | Yêu cầu xác thực + quyền ADMIN |
| Tất cả endpoint còn lại | Công khai (không cần token) |

---

## 7. Giao Tiếp Hướng Sự Kiện (Kafka)

### 7.1 Cấu Hình Kafka

**Consumer Configuration (`KafkaConsumerConfig`):**

| Tham số | Giá trị |
|---|---|
| Bootstrap servers | `${app.kafka-url}` |
| Group ID | `ecommerce-group` |
| Auto offset reset | `earliest` |
| Auto topic creation | Vô hiệu hóa |
| Deserializer | `StringDeserializer` + `StringJsonMessageConverter` |

**Producer Configuration (`KafkaProducerConfig`):**

| Tham số | Giá trị |
|---|---|
| Bootstrap servers | `${app.kafka-url}` |
| Key serializer | `StringSerializer` |
| Value serializer | `JsonSerializer` |
| Acknowledgment | `all` (chờ tất cả replica xác nhận) |
| Retries | 3 |
| Linger MS | 5 |
| Idempotence | Bật (`enable.idempotence=true`) |

### 7.2 Kafka Consumers (Nhận sự kiện)

#### Consumer: BatchDetailCreatedConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `batch-detail-events` |
| Group | `ecommerce-group` |
| Event class | `BatchDetailCreateEvent` |
| Kích hoạt khi | Product Storage Service tạo/cập nhật chi tiết lô hàng mới |
| Hành động | Tạo hoặc cập nhật `BatchDetail` trong cơ sở dữ liệu cục bộ |

**Cấu trúc `BatchDetailCreateEvent`:**
```json
{
  "eventType": "BATCH_DETAIL_CREATED",
  "batchDetailId": "BD-2025-001",
  "productGeneralId": 123,
  "quantity": 50,
  "price": 25000,
  "detailContent": "Gói 500g, chứng nhận VietGAP",
  "providerId": 5,
  "logoUrl": "https://...",
  "verificationCertificateType": "VIETGAP"
}
```

#### Consumer: CategoryCreatedConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `category-events` |
| Event class | `CategoryCreatedEvent` |
| Kích hoạt khi | Back-Office Service tạo/cập nhật danh mục |
| Hành động | Đồng bộ cây danh mục sản phẩm vào cơ sở dữ liệu cục bộ |

**Cấu trúc `CategoryCreatedEvent`:**
```json
{
  "eventType": "CATEGORY_CREATED",
  "categoryId": 10,
  "categoryName": "Rau Củ Quả",
  "isSubCategory": "Y",
  "belongToCategory": 1,
  "displayOrder": 2
}
```

#### Consumer: ProductGeneralCreatedConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `product-general-events` |
| Event class | `ProductGeneralCreatedEvent` |
| Kích hoạt khi | Back-Office Service tạo/cập nhật thông tin sản phẩm |
| Hành động | Đồng bộ catalog sản phẩm vào cơ sở dữ liệu cục bộ |

**Cấu trúc `ProductGeneralCreatedEvent`:**
```json
{
  "eventType": "PRODUCT_CREATED",
  "productGeneralId": 123,
  "name": "Cà Chua Cherry",
  "categoryId": 15,
  "providerId": "P001",
  "unit": "KILOGRAM",
  "unitQuantity": 500,
  "tags": ["cà chua", "rau sạch", "hữu cơ"],
  "img": "https://..."
}
```

#### Consumer: BuyerCreateConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `user-events` |
| Event class | `BuyerCreateEvent` |
| Kích hoạt khi | Identity Service tạo tài khoản người dùng mới |
| Hành động | Tạo bản ghi `Buyer` và tự động tạo `Cart` rỗng cho người dùng |

**Cấu trúc `BuyerCreateEvent`:**
```json
{
  "eventType": "USER_CREATED",
  "userId": "user-uuid-123",
  "firstName": "Nguyễn",
  "lastName": "Văn An",
  "email": "vanan@example.com"
}
```

#### Consumer: OrderDeliveringConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `order-delivering-events` |
| Event class | `OrderDeliveringEvent` |
| Kích hoạt khi | Hệ thống kho/vận chuyển xác nhận bắt đầu giao hàng |
| Hành động | Cập nhật trạng thái đơn hàng từ CONFIRMED → DELIVERING |

#### Consumer: OrderDeliveredConsumer

| Thuộc tính | Giá trị |
|---|---|
| Topic | `order-delivered-events` |
| Event class | `OrderDeliveredEvent` |
| Kích hoạt khi | Hệ thống vận chuyển xác nhận giao hàng thành công |
| Hành động | Cập nhật trạng thái đơn hàng từ DELIVERING → DELIVERED |

### 7.3 Kafka Producers (Phát sự kiện)

**OrderProducer** phát sự kiện đến ba topic khác nhau:

#### Topic: `order-events`

| Thuộc tính | Giá trị |
|---|---|
| Phát bởi | `OrderProducer.publishOrderCreated()` |
| Kích hoạt khi | Người mua đặt hàng thành công |
| Serialization | JSON |

**Cấu trúc `OrderCreatedEvent`:**
```json
{
  "eventType": "ORDER_CREATED",
  "orderId": 456,
  "buyerId": "user-uuid-123",
  "status": "PENDING",
  "totalPrice": 350000,
  "paymentMethod": "VNPAY",
  "items": [
    {
      "batchDetailId": "BD-2025-001",
      "quantity": 2,
      "unitPriceAtPurchase": 50000
    }
  ],
  "createdAt": "2026-05-06T10:30:00Z"
}
```

#### Topic: `order-confirmed-events`

| Thuộc tính | Giá trị |
|---|---|
| Phát bởi | `OrderProducer.publishOrderConfirmed()` |
| Kích hoạt khi | Admin xác nhận đơn hàng |

**Cấu trúc `OrderConfirmedEvent`:**
```json
{
  "eventType": "ORDER_CONFIRMED",
  "orderId": 456,
  "confirmationTime": "2026-05-06T11:00:00Z"
}
```

#### Topic: `order-pick-requested-events`

| Thuộc tính | Giá trị |
|---|---|
| Phát bởi | `OrderProducer.publishOrderPickRequested()` |
| Kích hoạt khi | Ngay sau khi xác nhận đơn, yêu cầu kho chuẩn bị hàng |

**Cấu trúc `OrderPickRequestedEvent`:**
```json
{
  "eventType": "ORDER_PICK_REQUESTED",
  "orderId": 456,
  "orderItems": [
    {
      "batchDetailId": "BD-2025-001",
      "quantity": 2
    }
  ],
  "pickingInstructions": "Ưu tiên lấy từ kho HCM"
}
```

### 7.4 Xử Lý Lỗi Kafka

- Lỗi trong consumer được ghi log qua SLF4J nhưng **không có cơ chế retry tự động**.
- Consumer tự chịu trách nhiệm xử lý ngoại lệ và quyết định bỏ qua hay thử lại.
- Producer cấu hình `retries=3` và `acks=all` để đảm bảo độ tin cậy phát sự kiện.

---

## 8. Lưu Trữ Tệp (Object Storage)

### 8.1 Cloudflare R2

Dịch vụ tích hợp Cloudflare R2 (tương thích S3) thông qua AWS SDK v2 để lưu trữ tệp media.

**Cấu hình `R2Config`:**

```yaml
cloudflare:
  r2:
    account-id: ${R2_ACCOUNT_ID}
    endpoint: https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
    access-key: ${R2_ACCESS_KEY}
    secret-key: ${R2_SECRET_KEY}
    region: auto
```

`S3Client` được tạo với `PathStyleAccessEnabled = true` (bắt buộc với R2).

### 8.2 Danh Sách Bucket

| Bucket | Mục đích | URL công khai |
|---|---|---|
| `back-office-user-avts` | Ảnh đại diện người dùng | Không công khai trực tiếp |
| `product-general-img` | Hình ảnh sản phẩm | Không công khai trực tiếp |
| `sale-event-banners` | Ảnh banner sự kiện khuyến mãi | `https://pub-4df933a93a6c4f2c8c9ce81cc96336ac.r2.dev/{filename}` |

### 8.3 Luồng Tải Tệp Lên

```
Client (multipart/form-data)
  |
  └── SaleEventController.uploadBanner(saleEventId, file)
        |
        └── R2UploadService.uploadSaleEventBanner(file)
              |
              ├── Tạo tên tệp: UUID.randomUUID() + originalFilename
              ├── PutObjectRequest:
              │     bucket: sale-event-banners
              │     key: generatedFileName
              │     contentType: file.getContentType()
              │     contentLength: file.getSize()
              ├── s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()))
              └── Trả về URL công khai: ${publicUrl}/${generatedFileName}
```

---

## 9. Giao Tiếp Liên Dịch Vụ

### 9.1 Giao Tiếp Bất Đồng Bộ (Kafka — Chủ yếu)

Toàn bộ giao tiếp nhận dữ liệu từ các dịch vụ khác đều thực hiện qua Kafka (xem mục 7). Đây là phương thức tích hợp chính giữa các dịch vụ:

| Dịch vụ nguồn | Kafka Topic | Dữ liệu nhận |
|---|---|---|
| identity-service | `user-events` | Thông tin người mua mới |
| back-office-service | `category-events` | Cây danh mục sản phẩm |
| back-office-service | `product-general-events` | Catalog sản phẩm |
| product-storage-service | `batch-detail-events` | Tồn kho và chi tiết lô hàng |

### 9.2 Giao Tiếp Đồng Bộ (HTTP — Bên ngoài)

#### Goong Maps API (Tính phí vận chuyển)

| Thuộc tính | Giá trị |
|---|---|
| Dịch vụ đích | Goong Maps API (bên ngoài) |
| Endpoint | `https://rsapi.goong.io/Distance` |
| Phương thức | `GET` |
| Khi nào gọi | Khi người dùng yêu cầu tính phí vận chuyển hoặc cập nhật địa chỉ giỏ hàng |
| Tham số | `origin` (tọa độ kho), `destination` (tọa độ địa chỉ người nhận), `api_key` |
| Kết quả sử dụng | Khoảng cách (km) → công thức tính phí vận chuyển |

#### Google Sheets API (Polling giao dịch thanh toán)

| Thuộc tính | Giá trị |
|---|---|
| Dịch vụ đích | Google Sheets API v4 (bên ngoài) |
| Spreadsheet ID | `1OgOhMyB660z8ITyLA8QZt39IWpz24qfT5GjjlcjT1m0` |
| Sheet name | `sepay` |
| Phương thức | Đọc (SPREADSHEETS_READONLY scope) |
| Khi nào gọi | Mỗi 10 giây (scheduler `@Scheduled`) |
| Mục đích | Đọc danh sách giao dịch chuyển khoản từ Sepay để xác nhận thanh toán |
| Xác thực | Google Service Account (file `google-credentials.json`) |

**Cấu trúc dữ liệu cột Google Sheets:**

| Cột | Nội dung |
|---|---|
| [0] | Thời điểm giao dịch |
| [1] | Số tiền |
| [2]–[4] | Thông tin phụ |
| [5] | Mô tả giao dịch (chứa mã đơn hàng dạng `DH00012345`) |
| [8] | Transaction ID (unique) |

### 9.3 Biến Môi Trường Kết Nối Dịch Vụ Khác

```yaml
# Cấu hình trong application.yaml (dự phòng cho future HTTP calls)
services:
  identity:
    url: http://${IDENTITY_HOST:localhost}:${IDENTITY_PORT:9000}
  back-office:
    url: http://${BACK_OFFICE_HOST:localhost}:${BACK_OFFICE_PORT:9100}
  product-storage:
    url: http://${PRODUCT_STORAGE_HOST:localhost}:${PRODUCT_STORAGE_PORT:9200}
```

---

## 10. Khởi Tạo Dữ Liệu (DataSeeder)

### 10.1 Điều Kiện Kích Hoạt

`DataSeeder` là một bean `CommandLineRunner` được thực thi một lần khi ứng dụng khởi động. Việc seed chỉ diễn ra nếu `BuyerRepository.count() == 0`, đảm bảo không tạo dữ liệu trùng lặp trên các lần khởi động tiếp theo.

### 10.2 Dữ Liệu Được Tạo

#### Nhóm 1: Người Mua (5 bản ghi)

| buyer_id | Họ tên | Email |
|---|---|---|
| "2" | Nguyễn Văn An | vanan@example.com |
| "3" | Trần Thị Bích | thibich@example.com |
| "4" | Lê Minh Cường | minhcuong@example.com |
| "5" | Phạm Thị Dung | thidung@example.com |
| "6" | Hoàng Văn Em | vanem@example.com |

#### Nhóm 2: Địa Chỉ Giao Hàng (6 bản ghi)

Địa chỉ tập trung tại Thành phố Hồ Chí Minh (Quận 1, Quận 5, Quận 7, Thủ Đức, Gò Vấp) với tọa độ GPS đầy đủ để tính phí vận chuyển.

#### Nhóm 3: Cây Danh Mục Sản Phẩm (14 danh mục)

```
Cấp 1:
├── Thịt & Hải Sản
│   ├── Gia Cầm
│   ├── Thịt Đỏ
│   └── Hải Sản
├── Rau Củ Quả
│   ├── Rau Lá Xanh
│   ├── Củ Quả
│   └── Trái Cây Tươi
└── Sữa & Trứng
    ├── Sữa & Sản Phẩm Từ Sữa
    └── Trứng
```

#### Nhóm 4: Sản Phẩm (105 sản phẩm thực phẩm tươi sống)

| Nhóm | Số lượng | Ví dụ |
|---|---|---|
| Thịt gia cầm và đỏ | 30 | Gà ta, vịt xiêm, thịt bò, thịt heo |
| Hải sản | 27 | Tôm sú, cá lóc, mực ống, cua biển |
| Rau củ quả | 35 | Rau muống, bí đao, cà chua, xoài |
| Sữa và trứng | 13 | Sữa tươi, trứng gà, phô mai |

Mỗi sản phẩm có đầy đủ: tên, mô tả tiếng Việt, đơn vị tính, số lượng, từ khóa tìm kiếm (`tags`).

#### Nhóm 5: Phiếu Giảm Giá (6 mã coupon)

| Mã coupon | Loại | Giá trị | Giới hạn tối đa | Đơn tối thiểu | Số lượng | Hết hạn |
|---|---|---|---|---|---|---|
| `GIAM10% MAX 500K` | PERCENTAGE | 10% | 500.000 VND | 100.000 VND | 100 | +1 tháng |
| `GIAM10% MAX 50K` | PERCENTAGE | 10% | 50.000 VND | 100.000 VND | 100 | +1 tháng |
| `VIP100K` | FIXED_AMOUNT | 100.000 VND | 100.000 VND | 1.000.000 VND | 50 | +2 tuần |
| `BANMOI` | PERCENTAGE | 50% | 20.000 VND | 50.000 VND | 500 | +7 ngày |
| `LE3004` | PERCENTAGE | 15% | 100.000 VND | 500.000 VND | 200 | +2 tháng |
| `FLASHSALE` | FIXED_AMOUNT | 200.000 VND | 200.000 VND | 2.000.000 VND | 10 | +5 giờ |

#### Nhóm 6: Giỏ Hàng

Tự động tạo 5 giỏ hàng rỗng tương ứng với 5 người mua vừa được tạo.

### 10.3 Thông Tin Đăng Nhập Mặc Định

DataSeeder không tạo tài khoản đăng nhập. Tài khoản người dùng được quản lý bởi `identity-service`. ID người mua (buyer_id = "2"–"6") phải khớp với ID người dùng trong `identity-service` để tích hợp đúng.

---

## 11. Cấu Hình Ứng Dụng

### 11.1 Tham Số Cơ Bản

```yaml
server:
  port: 9300

spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/ecommerce_db?stringtype=unspecified
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### 11.2 Cấu Hình Kafka

```yaml
app:
  kafka-url: ${KAFKA_HOST}:${KAFKA_PORT}
```

### 11.3 Cấu Hình CORS

Các origin được phép truy cập API:

```
http://10.205.183.122:5173
http://10.185.89.85:5173
http://localhost:5173
http://localhost:3000
http://192.168.96.110:3000
http://192.168.1.75:3000
http://10.194.144.9:3000
```

- Allowed Methods: `*`
- Allowed Headers: `*`
- Max Age: `3600` giây

### 11.4 Biến Môi Trường Bắt Buộc

| Biến môi trường | Mô tả | Ví dụ |
|---|---|---|
| `DB_HOST` | Hostname cơ sở dữ liệu | `localhost` hoặc `postgres` |
| `DB_PORT` | Cổng PostgreSQL | `5432` |
| `DB_USERNAME` | Tên đăng nhập PostgreSQL | `khoidev` |
| `DB_PASSWORD` | Mật khẩu PostgreSQL | `password` |
| `KAFKA_HOST` | Hostname Kafka | `localhost` hoặc `kafka` |
| `KAFKA_PORT` | Cổng Kafka | `9092` |
| `R2_ACCOUNT_ID` | ID tài khoản Cloudflare | `abc123` |
| `R2_ACCESS_KEY` | Khóa truy cập R2 | `...` |
| `R2_SECRET_KEY` | Khóa bí mật R2 | `...` |
| `GOONG_API_KEY` | Khóa API Goong Maps | `...` |
| `IDENTITY_HOST` | Hostname identity-service | `localhost` |
| `IDENTITY_PORT` | Cổng identity-service | `9000` |

### 11.5 Cấu Hình Thanh Toán

```yaml
payment:
  polling:
    interval: 10000   # Polling mỗi 10 giây

sepay:
  account: SEPTAK21191    # Tài khoản thụ hưởng Sepay
  bank: OCB               # Ngân hàng

google:
  sheets:
    credentials-path: google-credentials.json
    spreadsheet-id: 1OgOhMyB660z8ITyLA8QZt39IWpz24qfT5GjjlcjT1m0
```

---

## 12. Phụ Thuộc Kỹ Thuật Chính

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| `spring-boot-starter-parent` | 3.5.6 | Framework ứng dụng chính, quản lý phiên bản phụ thuộc |
| `spring-boot-starter-web` | (managed) | REST API, Jackson JSON, Tomcat |
| `spring-boot-starter-data-jpa` | (managed) | ORM, Hibernate, Spring Data repositories |
| `spring-boot-starter-security` | (managed) | Security filter chain, authentication |
| `spring-boot-starter-validation` | (managed) | Bean Validation (JSR-380) cho request DTO |
| `spring-kafka` | (managed) | Apache Kafka producer/consumer integration |
| `postgresql` | (managed, runtime) | JDBC driver PostgreSQL |
| `jjwt-api` | 0.11.5 | API xây dựng và xác thực JWT |
| `jjwt-impl` | 0.11.5 | Triển khai JWT (runtime) |
| `jjwt-jackson` | 0.11.5 | Serialization JWT với Jackson (runtime) |
| `software.amazon.awssdk:s3` | 2.42.4 | AWS S3 SDK tương thích Cloudflare R2 |
| `google-api-services-sheets` | v4-rev612-1.25.0 | Google Sheets API (đọc giao dịch thanh toán) |
| `google-auth-library-oauth2-http` | 1.46.0 | Xác thực OAuth2 với Google Service Account |
| `google-http-client-jackson2` | 2.1.0 | HTTP client Google với Jackson serialization |
| `lombok` | (managed, optional) | Code generation: `@Getter`, `@Setter`, `@AllArgsConstructor` |
| `grpc-services` | 1.74.0 | gRPC framework (chuẩn bị cho tương lai) |
| `spring-grpc-server-web-spring-boot-starter` | 0.11.0 | gRPC over HTTP (chuẩn bị cho tương lai) |

---

## 13. Nhận Xét Kiến Trúc

### 13.1 Điểm Mạnh Kiến Trúc

**Áp dụng Clean Architecture một cách nhất quán.** Việc tách biệt rõ ràng giữa `domain/`, `persistence/`, `infrastructure/`, và `presentation/` mang lại các lợi ích:
- Logic nghiệp vụ cốt lõi trong `domain/service/` không phụ thuộc vào Spring Boot hay JPA, dễ kiểm thử đơn vị (unit test) thuần.
- Thay thế cơ sở dữ liệu hoặc message broker chỉ ảnh hưởng tầng `infrastructure/` và `persistence/`, không ảnh hưởng tầng `domain/`.
- Interface use case trong `domain/use_case/` đóng vai trò hợp đồng (contract) rõ ràng giữa tầng nghiệp vụ và tầng trình bày.

**Mô hình đồng bộ dữ liệu phi đồng bộ qua Kafka.** Thay vì gọi REST đồng bộ sang các dịch vụ khác, ecommerce-service nhận dữ liệu qua event streaming. Điều này:
- Đảm bảo tính sẵn sàng cao (availability) ngay cả khi back-office-service hoặc product-storage-service tạm ngừng.
- Giảm độ trễ xử lý yêu cầu từ người dùng (không phải chờ gọi đồng bộ).

**Cơ chế idempotency trong xử lý thanh toán.** Bảng `ProcessedTransaction` với ràng buộc `UNIQUE` trên `transaction_id` ngăn chặn xử lý giao dịch trùng lặp do polling lặp lại, đây là yêu cầu quan trọng trong hệ thống thanh toán.

**Mô hình giỏ hàng toàn vẹn.** Việc lưu `price_before_discount`, `discount_amount`, `price_after_discount`, và `shipping_fee` riêng biệt giúp minh bạch trong tính toán và dễ debug khi có sai lệch giá.

### 13.2 Quyết Định Thiết Kế Đáng Chú Ý

**Lựa chọn phương thức thanh toán Sepay + Google Sheets.** Thay vì tích hợp trực tiếp với cổng thanh toán, hệ thống sử dụng Sepay để tạo QR mã VietQR và đọc xác nhận từ Google Sheets do Sepay ghi vào. Đây là giải pháp thực dụng cho thị trường Việt Nam, tuy nhiên tạo ra phụ thuộc vào tính ổn định của Google Sheets API và độ trễ polling (tối đa 10 giây).

**Dùng `VARCHAR` thay `BIGINT` cho `buyer_id`.** ID người mua được giữ nguyên dạng chuỗi từ identity-service (có thể là UUID), tránh rủi ro mất dữ liệu khi chuyển đổi kiểu và đảm bảo tính nhất quán với hệ thống định danh.

**Lưu snapshot giá tại thời điểm mua.** Trường `unit_price_at_purchase` trong `OrderItem` lưu giá thực tế tại thời điểm mua, tách biệt với giá hiện tại của `BatchDetail`. Điều này đảm bảo tính chính xác lịch sử đơn hàng kể cả khi giá thay đổi sau đó.

**Cây danh mục tự tham chiếu.** Bảng `CATEGORY` dùng `belong_to_category` tự tham chiếu để biểu diễn cây phân cấp không giới hạn độ sâu, thay vì dùng bảng riêng cho từng cấp. Giải pháp này linh hoạt nhưng cần truy vấn đệ quy hoặc nhiều lần JOIN để lấy cây đầy đủ.

### 13.3 Nợ Kỹ Thuật Đã Xác Định

**Lỗi đặt tên cột trong `Cart` entity.** Cột khóa chính được đặt tên `card_id` thay vì `cart_id`. Mặc dù không ảnh hưởng chức năng, điều này gây nhầm lẫn khi đọc schema và có thể dẫn đến lỗi khi viết query thủ công. Cần migration để đổi tên cột.

**Không có cơ chế Dead Letter Queue (DLQ) cho Kafka consumer.** Khi consumer gặp lỗi xử lý sự kiện (ví dụ: dữ liệu không hợp lệ), sự kiện bị bỏ qua sau khi ghi log mà không có cơ chế retry hoặc lưu vào DLQ. Trong môi trường sản xuất, điều này có thể dẫn đến mất đồng bộ dữ liệu giữa các dịch vụ.

**Dependency vào Google Sheets cho xác nhận thanh toán.** Cơ chế polling Google Sheets mỗi 10 giây tạo ra độ trễ tối đa 10 giây trong xác nhận thanh toán và phụ thuộc vào tính ổn định của dịch vụ bên ngoài. Về lâu dài, nên thay thế bằng webhook từ Sepay hoặc hệ thống thanh toán chính thức (VNPay, MOMO).

**Phụ thuộc gRPC chưa được sử dụng.** Các thư viện `grpc-services` và `spring-grpc-server-web-spring-boot-starter` được khai báo trong `pom.xml` nhưng chưa có triển khai tương ứng trong mã nguồn, làm tăng kích thước artifact không cần thiết.

**CORS cấu hình cứng địa chỉ IP.** Danh sách allowed origins chứa các địa chỉ IP cụ thể (`10.205.183.122`, `192.168.1.75`, v.v.), thay vì sử dụng wildcard hoặc cấu hình qua biến môi trường. Điều này làm phức tạp quá trình triển khai sang môi trường mới.

**Không có versioning cho REST API.** Các endpoint hiện tại không có tiền tố phiên bản (ví dụ: `/api/v1/`), gây khó khăn cho việc duy trì tương thích ngược khi thay đổi contract API trong tương lai.

**Thiếu phân trang cho một số endpoint danh sách.** Một số endpoint như `readBuyerAddresses()` trả về toàn bộ danh sách không giới hạn, có thể gây vấn đề hiệu năng khi số lượng bản ghi tăng lớn.
