# Tài Liệu Kỹ Thuật: Product Storage Service

**Phiên bản**: 4.0.3  
**Ngày cập nhật**: 06/05/2026  
**Tác giả**: Hệ thống thương mại điện tử nông sản — Đồ án tốt nghiệp

---

## Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Cấu Trúc Package](#2-cấu-trúc-package)
3. [Mô Hình Dữ Liệu](#3-mô-hình-dữ-liệu)
4. [Tầng Nghiệp Vụ (Service Layer)](#4-tầng-nghiệp-vụ-service-layer)
5. [Tầng Trình Bày — REST API](#5-tầng-trình-bày--rest-api)
6. [Bảo Mật và Phân Quyền](#6-bảo-mật-và-phân-quyền)
7. [Giao Tiếp Hướng Sự Kiện (Kafka)](#7-giao-tiếp-hướng-sự-kiện-kafka)
8. [Lưu Trữ Tệp](#8-lưu-trữ-tệp)
9. [Giao Tiếp Liên Dịch Vụ (REST Calls)](#9-giao-tiếp-liên-dịch-vụ-rest-calls)
10. [Khởi Tạo Dữ Liệu (DataSeeder)](#10-khởi-tạo-dữ-liệu-dataseeder)
11. [Cấu Hình Ứng Dụng](#11-cấu-hình-ứng-dụng)
12. [Phụ Thuộc Kỹ Thuật Chính](#12-phụ-thuộc-kỹ-thuật-chính)
13. [Nhận Xét Kiến Trúc](#13-nhận-xét-kiến-trúc)

---

## 1. Tổng Quan

### 1.1 Mục Đích

Product Storage Service là dịch vụ vi mô (microservice) chịu trách nhiệm quản lý toàn bộ chuỗi lưu trữ hàng hoá trong hệ thống thương mại điện tử nông sản. Phạm vi chức năng bao gồm:

- **Quản lý cơ sở hạ tầng kho bãi**: Kho hàng (Warehouse), công cụ lưu trữ (kệ hàng Rack, tủ lạnh Fridge), và các tầng kệ (RackLevel).
- **Quản lý danh mục sản phẩm nội bộ**: Tiếp nhận thông tin sản phẩm tổng quát (ProductGeneral) và danh mục phân loại chi tiết (SubSubcategory) từ dịch vụ back-office thông qua Kafka.
- **Quản lý lô hàng và kiểm định nhà cung cấp**: Hỗ trợ hai luồng kiểm định nhà cung cấp riêng biệt — kiểm định bằng chứng chỉ (CERTIFICATE) và kiểm định qua video (VIDEO), phản ánh đặc thù của nông sản tươi sống.
- **Xử lý lô hàng thành sản phẩm bán lẻ**: Thực hiện phép chia lô (batch splitting) kèm chuyển đổi đơn vị đo lường (kg → g, L → mL) để tạo ra các đơn vị sản phẩm riêng lẻ (ProductDetail) sẵn sàng đưa vào kho.
- **Quản lý đơn hàng và danh sách nhặt hàng**: Nhận sự kiện yêu cầu xuất hàng từ ecommerce-service qua Kafka và điều phối quá trình nhặt hàng (pick list) trong kho.
- **Quản lý cầu nguyên liệu thô (RawProductDemand)**: Theo dõi nhu cầu thu mua nông sản từ nhà cung cấp.

### 1.2 Ngăn Xếp Công Nghệ

| Thành phần | Giá trị |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Ngôn ngữ | Java 25 |
| Cổng dịch vụ | 9200 |
| Cơ sở dữ liệu | PostgreSQL 17 (`product_storage_db`) |
| Hàng đợi sự kiện | Apache Kafka 4.2.0 |
| Lưu trữ tệp | Cloudflare R2 (tương thích AWS S3) |
| Xác thực | JWT (RSA public key) |
| ORM | Spring Data JPA / Hibernate |
| Build tool | Maven Wrapper (`./mvnw`) |

### 1.3 Kiểu Kiến Trúc

Dịch vụ áp dụng kiến trúc **phân lớp truyền thống (Layered Architecture)** kết hợp với **hướng sự kiện (Event-Driven Architecture)**:

```
Presentation Layer  →  Controller (REST endpoints)
Service Layer       →  Business logic, Kafka producers
Persistence Layer   →  JPA Repositories, DAO entities
Infrastructure      →  Kafka consumers, R2 storage, JWT security
```

Mô hình bảo mật là **stateless** (không duy trì session phía server), sử dụng JWT được ký bằng khóa RSA để xác thực mọi yêu cầu đến.

---

## 2. Cấu Trúc Package

```
services/product_storage_service/
├── src/main/java/edu/hcmut/datn/productstorage/
│   ├── common/                        # Hằng số và enum dùng chung
│   │   └── enums/
│   │       ├── Unit.java              # Đơn vị đo lường (KILOGRAM, GRAM, LITER, ...)
│   │       ├── UnitCategory.java      # Nhóm đơn vị (WEIGHT, VOLUME, COUNT)
│   │       ├── ProductStatus.java     # Trạng thái sản phẩm đơn lẻ
│   │       ├── ProductBatchProcessStatus.java  # Trạng thái lô hàng
│   │       ├── ProviderVerificationType.java   # Loại kiểm định nhà cung cấp
│   │       ├── StorageType.java       # Loại công cụ lưu trữ (RACK, FRIDGE)
│   │       ├── StorageToolStatus.java # Trạng thái công cụ lưu trữ
│   │       ├── VerificationStatus.java # Trạng thái xác minh nhà cung cấp
│   │       ├── CertificateType.java   # Loại chứng chỉ (VIETGAP, GLOBALGAP, ...)
│   │       └── RawProductDemandStatus.java  # Trạng thái cầu nguyên liệu
│   │
│   ├── config/                        # Cấu hình ứng dụng và khởi tạo dữ liệu
│   │   ├── R2Config.java              # Cấu hình S3Client kết nối Cloudflare R2
│   │   ├── WebConfig.java             # Cấu hình CORS
│   │   └── DataSeeder.java            # Khởi tạo dữ liệu mẫu khi database trống
│   │
│   ├── controller/                    # Tầng trình bày — REST endpoints
│   │   ├── WarehouseController.java   # CRUD kho hàng
│   │   ├── StorageToolController.java # CRUD công cụ lưu trữ
│   │   ├── RackController.java        # CRUD kệ hàng
│   │   ├── RackLevelController.java   # CRUD tầng kệ
│   │   ├── FridgeController.java      # CRUD tủ lạnh
│   │   ├── ProductGeneralController.java   # CRUD thông tin sản phẩm chung
│   │   ├── ProductBatchController.java     # CRUD lô hàng + upload chứng minh
│   │   ├── ProductSubBatchController.java  # CRUD lô hàng con + video chứng minh
│   │   ├── ProductDetailController.java    # CRUD sản phẩm đơn lẻ + xử lý lô
│   │   ├── PickListController.java         # Quản lý danh sách nhặt hàng
│   │   ├── OrderItemController.java        # CRUD đơn vị đặt hàng
│   │   └── RawProductDemandController.java # CRUD cầu nguyên liệu
│   │
│   ├── dao/                           # JPA Entity classes (ánh xạ bảng database)
│   │   ├── Warehouse.java             # Kho hàng
│   │   ├── StorageTool.java           # Công cụ lưu trữ (lớp cha)
│   │   ├── Rack.java                  # Kệ hàng
│   │   ├── RackLevel.java             # Tầng kệ
│   │   ├── Fridge.java                # Tủ lạnh
│   │   ├── SubSubcategory.java        # Danh mục phân loại chi tiết nhất
│   │   ├── ProductGeneral.java        # Thông tin sản phẩm chung
│   │   ├── ProductBatch.java          # Lô hàng từ nhà cung cấp
│   │   ├── ProductSubBatch.java       # Lô hàng con (cho luồng VIDEO)
│   │   ├── ProductDetail.java         # Sản phẩm đơn lẻ có thể bán
│   │   ├── Provider.java              # Thông tin nhà cung cấp
│   │   ├── OrderItem.java             # Đơn vị trong đơn hàng
│   │   └── RawProductDemand.java      # Cầu nguyên liệu thô
│   │
│   ├── dto/                           # Data Transfer Objects
│   │   ├── request/                   # DTO yêu cầu từ client
│   │   │   ├── *CreateRequest.java    # Các DTO tạo mới (một file per entity)
│   │   │   ├── *UpdateRequest.java    # Các DTO cập nhật (một file per entity)
│   │   │   ├── ProcessBatchRequest.java        # Yêu cầu xử lý lô hàng v2
│   │   │   ├── ProcessProductBatchRequest.java # Yêu cầu xử lý lô hàng v1 (legacy)
│   │   │   └── DeliveryAcceptanceRequest.java  # Yêu cầu xác nhận giao hàng
│   │   └── response/                  # DTO phản hồi
│   │       ├── ApiResponse.java       # Wrapper phản hồi chuẩn hóa
│   │       ├── ProcessBatchResponse.java       # Kết quả xử lý lô hàng
│   │       └── SubBatchBreakdown.java # Phân tích chi tiết lô hàng con
│   │
│   ├── exception/                     # Ngoại lệ nghiệp vụ tùy chỉnh
│   │   ├── ProductBatchNotFoundException.java
│   │   ├── ProductBatchAlreadyProcessedException.java
│   │   ├── ProductBatchExpiredException.java
│   │   ├── SubSubcategoryMismatchException.java
│   │   └── ... (và các exception khác tương ứng với entity)
│   │
│   ├── messaging/                     # Tích hợp Kafka
│   │   ├── KafkaProducerConfig.java   # Cấu hình producer (serializer, ack, retry)
│   │   ├── KafkaConsumerConfig.java   # Cấu hình consumer (group-id, offset-reset)
│   │   ├── consumer/                  # Các Kafka Listener
│   │   │   ├── ProductGeneralCreatedConsumer.java   # Nhận sự kiện tạo sản phẩm
│   │   │   ├── SubSubcategoryCreatedConsumer.java   # Nhận sự kiện tạo danh mục
│   │   │   ├── ProviderCreatedConsumer.java         # Nhận sự kiện tạo nhà cung cấp
│   │   │   ├── ProviderVerificationUpdatedConsumer.java  # Cập nhật trạng thái xác minh
│   │   │   └── OrderPickRequestedConsumer.java      # Nhận yêu cầu xuất hàng
│   │   └── producer/                  # Các Kafka Producer
│   │       ├── BatchDetailProducer.java             # Phát sự kiện batch-detail-events
│   │       └── OrderPackagingProgressUpdateEventProducer.java  # Cập nhật tiến độ đóng gói
│   │
│   ├── projector/                     # JPA Projection interfaces
│   │   ├── PickListItem.java          # Projection danh sách nhặt hàng
│   │   └── ProductDetailForPickItem.java  # Projection chi tiết vị trí sản phẩm
│   │
│   ├── repository/                    # Spring Data JPA Repositories
│   │   ├── WarehouseRepository.java
│   │   ├── StorageToolRepository.java
│   │   ├── RackRepository.java
│   │   ├── RackLevelRepository.java
│   │   ├── FridgeRepository.java
│   │   ├── SubSubcategoryRepository.java
│   │   ├── ProductGeneralRepository.java
│   │   ├── ProductBatchRepository.java
│   │   ├── ProductSubBatchRepository.java
│   │   ├── ProductDetailRepository.java
│   │   ├── ProviderRepository.java
│   │   ├── OrderItemRepository.java
│   │   └── RawProductDemandRepository.java
│   │
│   ├── security/portable/             # Cấu hình bảo mật JWT
│   │   ├── SecurityConfig.java        # Filter chain, authorization rules
│   │   ├── JwtAuthenticationFilter.java  # Xác thực JWT từ header
│   │   ├── CustomAuthenticationEntryPoint.java  # Xử lý lỗi 401
│   │   ├── CustomAccessDeniedHandler.java       # Xử lý lỗi 403
│   │   └── AuthenticatedUser.java     # Model người dùng đã xác thực
│   │
│   ├── service/                       # Giao diện tầng nghiệp vụ
│   │   ├── impl/                      # Các lớp triển khai nghiệp vụ
│   │   ├── R2UploadService.java       # Giao diện upload file lên R2
│   │   ├── BackOfficeServiceClient.java  # HTTP client gọi back-office
│   │   └── PickListService.java       # Nghiệp vụ danh sách nhặt hàng
│   │
│   └── util/
│       └── UnitConverter.java         # Tiện ích chuyển đổi đơn vị đo lường
│
├── src/main/resources/
│   ├── application.yaml               # Cấu hình ứng dụng chính
│   └── keys/
│       └── public.pem                 # RSA public key cho xác thực JWT
│
└── pom.xml                            # Khai báo phụ thuộc Maven
```

---

## 3. Mô Hình Dữ Liệu

### 3.1 Các Entity JPA

#### 3.1.1 Warehouse (Kho hàng)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `warehouseId` | Long | BIGINT | PK, AUTO_INCREMENT | Khóa chính tự tăng |
| `address` | String | VARCHAR | NOT NULL | Địa chỉ kho hàng |
| `usagePercentage` | Long | BIGINT | | Tỷ lệ sử dụng (%) |
| `numOfFridge` | Long | BIGINT | | Số lượng tủ lạnh |
| `numOfRack` | Long | BIGINT | | Số lượng kệ hàng |
| `createdAt` | LocalDateTime | TIMESTAMP | | Tự động gán bằng `@PrePersist` |
| `updatedAt` | LocalDateTime | TIMESTAMP | | Tự động cập nhật bằng `@PreUpdate` |

#### 3.1.2 StorageTool (Công cụ lưu trữ — lớp cha)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `storageToolId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `lastMaintainanceDate` | LocalDate | DATE | | Ngày bảo trì gần nhất |
| `status` | StorageToolStatus | VARCHAR | NOT NULL | ACTIVE, INACTIVE, FULL, IN_MAINTAINANCE |
| `usagePercentage` | Long | BIGINT | | Tỷ lệ sử dụng (%) |
| `warehouseId` | Long | BIGINT | FK → Warehouse | Kho chứa công cụ này |
| `toolType` | StorageType | VARCHAR | NOT NULL | RACK hoặc FRIDGE |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.3 Rack (Kệ hàng)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `rackId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `numOfLevel` | Long | BIGINT | | Số tầng của kệ |
| `storageToolId` | Long | BIGINT | FK → StorageTool | Liên kết với bản ghi cha |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.4 RackLevel (Tầng kệ)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `rackLevelId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `usagePercentage` | Long | BIGINT | | Tỷ lệ lấp đầy của tầng kệ (%) |
| `rackId` | Long | BIGINT | FK → Rack | Kệ chứa tầng này |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.5 Fridge (Tủ lạnh)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `fridgeId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `curTemp` | Long | BIGINT | | Nhiệt độ hiện tại (°C) |
| `minTemp` | Long | BIGINT | | Nhiệt độ tối thiểu an toàn (°C) |
| `maxTemp` | Long | BIGINT | | Nhiệt độ tối đa an toàn (°C) |
| `storageToolId` | Long | BIGINT | FK → StorageTool | Liên kết với bản ghi cha |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.6 SubSubcategory (Danh mục chi tiết)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `subSubcategoryId` | Long | BIGINT | PK, **thủ công** | ID được gán từ sự kiện Kafka (đồng bộ với back-office) |
| `name` | String | VARCHAR | NOT NULL | Tên danh mục |
| `description` | String | TEXT | | Mô tả danh mục |
| `iconUrl` | String | VARCHAR | | URL biểu tượng |
| `subcategoryId` | Long | BIGINT | | ID danh mục cha (tham chiếu logic, không FK) |
| `avgShelfDays` | Integer | INTEGER | | Số ngày hạn sử dụng trung bình — dùng để tính `expiredAt` cho lô hàng |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.7 ProductGeneral (Thông tin sản phẩm chung)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `prodGenId` | Long | BIGINT | PK, **thủ công** | ID đồng bộ từ back-office-service qua Kafka |
| `name` | String | VARCHAR | NOT NULL | Tên sản phẩm |
| `imgUrl` | String | VARCHAR | | URL ảnh (tên cột DB: `img`) |
| `description` | String | TEXT | | Mô tả |
| `subSubcategoryId` | Long | BIGINT | FK → SubSubcategory | Danh mục chi tiết |
| `unit` | Unit | VARCHAR | NOT NULL | Đơn vị tính của sản phẩm |
| `unitQuantity` | Long | BIGINT | | Kích thước gói tiêu chuẩn (ví dụ: 500 → 500g/gói) |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.8 ProductBatch (Lô hàng từ nhà cung cấp)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `batchId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `quantity` | Long | BIGINT | | Số lượng (theo đơn vị `unit`) |
| `unit` | Unit | VARCHAR | NOT NULL | Đơn vị đo lường |
| `note` | String | TEXT | | Ghi chú lô hàng |
| `receivedAt` | LocalDateTime | TIMESTAMP | | Thời điểm nhận hàng |
| `expiredAt` | LocalDateTime | TIMESTAMP | | Hạn sử dụng (tính từ `avgShelfDays`) |
| `processStatus` | ProductBatchProcessStatus | VARCHAR | NOT NULL | WAIT_FOR_DELIVERY, PENDING, PROCESSED, EXPIRED, REJECTED |
| `providerId` | Long | BIGINT | | Nhà cung cấp (chỉ có với CERTIFICATE; NULL với VIDEO) |
| `subSubcategoryId` | Long | BIGINT | FK → SubSubcategory | Phải khớp với `ProductGeneral.subSubcategoryId` khi xử lý |
| `verificationType` | ProviderVerificationType | VARCHAR | NOT NULL | CERTIFICATE hoặc VIDEO |
| `rawProductDemandId` | Long | BIGINT | FK → RawProductDemand | Cầu nguyên liệu tương ứng |
| `proofImageUrls` | List\<String\> | product_batch_proof_images | @ElementCollection | Danh sách URL ảnh/chứng từ |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

**Ghi chú nghiệp vụ**: Constructor mặc định khởi tạo `processStatus = PENDING`.

#### 3.1.9 ProductSubBatch (Lô hàng con — chỉ dùng với luồng VIDEO)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `subBatchId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `quantity` | Long | BIGINT | | |
| `unit` | Unit | VARCHAR | NOT NULL | |
| `note` | String | TEXT | | |
| `receivedAt` | LocalDateTime | TIMESTAMP | | |
| `expiredAt` | LocalDateTime | TIMESTAMP | | |
| `processStatus` | ProductBatchProcessStatus | VARCHAR | NOT NULL | |
| `providerId` | Long | BIGINT | NOT NULL | Nhà cung cấp của lô con cụ thể này |
| `subSubcategoryId` | Long | BIGINT | FK → SubSubcategory | |
| `productBatchId` | Long | BIGINT | FK → ProductBatch | Lô hàng gộp cha |
| `rawProductDemandId` | Long | BIGINT | FK → RawProductDemand | |
| `proofImageUrls` | List\<String\> | product_sub_batch_proof_images | @ElementCollection | URL video chứng minh |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.10 ProductDetail (Sản phẩm đơn lẻ có thể bán)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `prodDetailId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `status` | ProductStatus | VARCHAR | NOT NULL | STORED, EXPIRED, PICKED, IN_TRANSIT, DELIVERED, RETURNED, DISPOSED |
| `price` | Long | BIGINT | | Giá bán (VND) |
| `numOfStar` | Long | BIGINT | | Điểm đánh giá |
| `storageToolId` | Long | BIGINT | FK → StorageTool | Vị trí lưu trữ vật lý |
| `batchId` | Long | BIGINT | FK → ProductBatch | Lô hàng nguồn gốc |
| `prodGenId` | Long | BIGINT | FK → ProductGeneral | Loại sản phẩm |
| `subBatchId` | Long | BIGINT | FK → ProductSubBatch | NULL (CERTIFICATE) hoặc ID sub-batch (VIDEO) |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

**Ghi chú nghiệp vụ**: Phương thức `copy()` tạo bản sao entity (xóa ID) phục vụ xử lý lô hàng.

#### 3.1.11 Provider (Nhà cung cấp)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `providerId` | Long | BIGINT | PK, **thủ công** | ID đồng bộ từ identity-service qua Kafka |
| `verificationMethod` | ProviderVerificationType | VARCHAR | NOT NULL | CERTIFICATE hoặc VIDEO |
| `verificationStatus` | VerificationStatus | VARCHAR | NOT NULL | UNVERIFIED, PENDING, VERIFIED, REJECTED |
| `certificateType` | CertificateType | VARCHAR | | VIETGAP, GLOBALGAP, ... |
| `logoUrl` | String | VARCHAR | | URL logo nhà cung cấp |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.12 OrderItem (Đơn vị trong đơn hàng)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `orderItemId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `orderId` | Long | BIGINT | NOT NULL | ID đơn hàng từ ecommerce-service |
| `batchDetailId` | Long | BIGINT | NOT NULL | ID batch detail từ ecommerce-service |
| `buyerId` | Long | BIGINT | NOT NULL | ID người mua |
| `productDetailId` | Long | BIGINT | FK → ProductDetail | Liên kết với sản phẩm vật lý cụ thể (do nhân viên kho gán) |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

#### 3.1.13 RawProductDemand (Cầu nguyên liệu thô)

| Trường | Kiểu Java | Kiểu DB | Ràng buộc | Ghi chú |
|---|---|---|---|---|
| `demandId` | Long | BIGINT | PK, AUTO_INCREMENT | |
| `subSubcategoryId` | Long | BIGINT | FK → SubSubcategory | Loại nông sản cần thu mua |
| `unit` | Unit | VARCHAR | NOT NULL | Đơn vị |
| `unitQuantity` | Long | BIGINT | | Kích thước gói |
| `unitPrice` | Long | BIGINT | | Giá thu mua dự kiến (VND) |
| `currentProgress` | Long | BIGINT | | Lượng đã giao (khởi tạo = 0) |
| `dateNeed` | LocalDate | DATE | | Ngày cần hàng |
| `status` | RawProductDemandStatus | VARCHAR | NOT NULL | PENDING, PARTIALLY_FULFILLED, FULFILLED, CANCELLED |
| `note` | String | TEXT | | |
| `createdAt` | LocalDateTime | TIMESTAMP | | |
| `updatedAt` | LocalDateTime | TIMESTAMP | | |

### 3.2 Sơ Đồ Quan Hệ Entity (ERD)

```
Warehouse (1) ─────────────── (N) StorageTool
                                      │
                          ┌───────────┴───────────┐
                          │                       │
                        Rack (1) ── (N) RackLevel  Fridge
                          │
                          └──── storageToolId (FK)
                                        │
                                        │ (dùng bởi)
                                        ▼
SubSubcategory (1) ─── (N) ProductGeneral (1) ─────────────── (N) ProductDetail
       │                                                               │
       │  (avgShelfDays → expiredAt)                         batchId / subBatchId / prodGenId / storageToolId
       │
       └─── (N) ProductBatch
                   │  verificationType = CERTIFICATE → providerId != NULL
                   │  verificationType = VIDEO       → providerId == NULL
                   │
                   └─── (N) ProductSubBatch (only VIDEO)
                                 │
                                 └─── providerId → Provider

RawProductDemand (1) ─── (N) ProductBatch
RawProductDemand (1) ─── (N) ProductSubBatch

OrderItem.productDetailId ──── (1) ProductDetail
OrderItem.orderId ─── (logic) ─── đơn hàng trong ecommerce-service
```

---

## 4. Tầng Nghiệp Vụ (Service Layer)

Tất cả các service tuân theo giao diện CRUD chuẩn hóa sau đây, với injection constructor bằng annotation `@AllArgsConstructor` của Lombok:

```java
T create(T entity);
T read(Long id);                            // Ném *NotFoundException nếu không tìm thấy
Page<T> readAll(Integer pageNum, Integer pageSize);  // Chỉ số 1-based từ API, chuyển thành 0-based nội bộ
T update(Long id, T entity);               // Cập nhật một phần: bỏ qua trường null
void delete(Long id);
```

### 4.1 Các Service CRUD Tiêu Chuẩn

| Service Interface | Implementation | Mô tả tóm tắt |
|---|---|---|
| `WarehouseService` | `WarehouseServiceImpl` | CRUD kho hàng |
| `StorageToolService` | `StorageToolServiceImpl` | CRUD công cụ lưu trữ (bảng cha cho Rack/Fridge) |
| `RackService` | `RackServiceImpl` | CRUD kệ hàng |
| `RackLevelService` | `RackLevelServiceImpl` | CRUD tầng kệ |
| `FridgeService` | `FridgeServiceImpl` | CRUD tủ lạnh |
| `SubSubcategoryService` | `SubSubcategoryServiceImpl` | CRUD danh mục phân loại chi tiết |
| `OrderItemService` | `OrderItemServiceImpl` | CRUD đơn vị đặt hàng |
| `RawProductDemandService` | `RawProductDemandServiceImpl` | CRUD cầu nguyên liệu thô |

### 4.2 ProductGeneralService

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(ProductGeneral)` | Tạo mới bản ghi sản phẩm chung | ID được gán thủ công, đồng bộ từ back-office qua Kafka |
| `read(Long id)` | Lấy thông tin theo ID | Ném `ProductGeneralNotFoundException` nếu không tồn tại |
| `readAll(int, int)` | Danh sách phân trang | |
| `update(Long, ProductGeneral)` | Cập nhật một phần | Bỏ qua các trường null |
| `delete(Long)` | Xóa bản ghi | |
| `getSuitableForBatch(Long batchId)` | Lấy danh sách sản phẩm phù hợp với lô hàng | Lọc theo `subSubcategoryId` trùng khớp với lô hàng |

### 4.3 ProductBatchService

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(ProductBatch)` | Tạo lô hàng mới | `processStatus` khởi tạo = `PENDING` |
| `read(Long id)` | Lấy thông tin lô hàng | Ném `ProductBatchNotFoundException` |
| `readAll(int, int)` | Danh sách phân trang | |
| `update(Long, ProductBatch)` | Cập nhật một phần | |
| `delete(Long)` | Xóa lô hàng | |
| `uploadProofImages(Long batchId, List<MultipartFile>)` | Upload ảnh chứng minh lên R2 | Trả về danh sách URL; Lưu URL vào `proofImageUrls` |
| `getProofImages(Long batchId)` | Lấy danh sách URL ảnh chứng minh | |
| `findByProcessStatus(ProductBatchProcessStatus)` | Lọc lô hàng theo trạng thái | |
| `acceptDelivery(Long batchId, Long actualQty, String note)` | Xác nhận giao hàng thực tế | Cập nhật `quantity` theo `actualQty`, giữ `processStatus = PENDING` |
| `rejectDelivery(Long batchId, String note)` | Từ chối lô hàng | Chuyển `processStatus → REJECTED` |

### 4.4 ProductSubBatchService

| Phương thức | Mô tả | Quy tắc nghiệp vụ |
|---|---|---|
| `create(ProductSubBatch)` | Tạo lô hàng con | Chỉ dùng với lô hàng gộp kiểu VIDEO |
| `read(Long id)` | Lấy thông tin | Ném `ProductSubBatchNotFoundException` |
| `readAll(int, int)` | Danh sách phân trang | |
| `update(Long, ProductSubBatch)` | Cập nhật | |
| `delete(Long)` | Xóa | |
| `uploadProofImages(Long subBatchId, List<MultipartFile>)` | Upload video chứng minh lên R2 | URL lưu vào `proofImageUrls` của sub-batch |
| `getProofImages(Long subBatchId)` | Lấy danh sách URL video | |
| `findByProductBatchId(Long batchId)` | Lấy tất cả sub-batch của lô gộp | Dùng khi xử lý lô VIDEO |
| `acceptDelivery(Long subBatchId, Long actualQty, String note)` | Xác nhận giao hàng | Cập nhật `quantity`, `processStatus = PENDING` |
| `rejectDelivery(Long subBatchId, String note)` | Từ chối | `processStatus → REJECTED` |

### 4.5 ProductDetailService

Đây là service trung tâm, chứa nghiệp vụ phức tạp nhất của dịch vụ.

| Phương thức | Mô tả | Quy tắc nghiệp vụ quan trọng |
|---|---|---|
| `create(ProductDetail)` | Tạo đơn lẻ một sản phẩm | |
| `read(Long id)` | Lấy theo ID | Ném `ProductDetailNotFoundException` |
| `readAll(int, int)` | Danh sách phân trang | |
| `update(Long, ProductDetail)` | Cập nhật | |
| `delete(Long)` | Xóa | |
| `processProductBatch(ProductDetail)` | Xử lý lô hàng (phiên bản cũ) | Deprecated; thay bằng v2 |
| `processProductBatchV2(ProcessBatchRequest)` | **Xử lý lô hàng phiên bản 2 (khuyến nghị)** | Xem chi tiết bên dưới |

**Luồng xử lý `processProductBatchV2`**:

1. Kiểm tra `ProductBatch` tồn tại và có `processStatus = PENDING`.
2. Kiểm tra `expiredAt` chưa qua (trước `LocalDateTime.now()`).
3. Kiểm tra `ProductGeneral.subSubcategoryId == ProductBatch.subSubcategoryId` (ném `SubSubcategoryMismatchException` nếu không khớp).
4. Phân nhánh theo `verificationType`:
   - **CERTIFICATE**: Gọi `processCertificateBatch()` — tạo `ProductDetail` với `batchId` đã gán, `subBatchId = null`.
   - **VIDEO**: Gọi `processVideoBatch()` — lấy tất cả `ProductSubBatch` thuộc lô, với mỗi sub-batch hợp lệ (PENDING, chưa hết hạn), tạo `ProductDetail` gán cả `batchId` lẫn `subBatchId`.
5. Gọi `UnitConverter.splitBatch()` để tính số lượng `ProductDetail` = `batch.quantity` (theo đơn vị batch) ÷ `productGeneral.unitQuantity` (theo đơn vị gói).
6. Tạo các bản ghi `ProductDetail` với `status = STORED`.
7. Cập nhật `processStatus → PROCESSED` cho batch (hoặc sub-batch).
8. Phát sự kiện `BatchDetailCreateEvent` lên Kafka topic `batch-detail-events`.
9. Trả về `ProcessBatchResponse` kèm danh sách `SubBatchBreakdown`.

### 4.6 PickListService

| Phương thức | Mô tả |
|---|---|
| `createPickList(OrderPickRequestedEvent)` | Tạo danh sách `OrderItem` từ sự kiện Kafka |
| `getPickList(Long orderId)` | Trả về `List<PickListItem>` (projection) cho đơn hàng |
| `linkOrderItem(Long orderItemId, Long productDetailId)` | Gán sản phẩm vật lý cụ thể vào mục đặt hàng |
| `getProductDetailCurrentQuantity(Long batchId)` | Đếm số `ProductDetail` có `status = STORED` của một batch |
| `getProductDetailListForPickItem(Long batchId)` | Trả về `List<ProductDetailForPickItem>` — vị trí lưu kho kèm kệ/tủ |

### 4.7 R2UploadService

| Phương thức | Mô tả |
|---|---|
| `upload(MultipartFile file, String bucket)` | Upload tệp lên bucket R2 được chỉ định, trả về URL công khai |

### 4.8 UnitConverter (Tiện ích)

| Phương thức | Mô tả |
|---|---|
| `splitBatch(quantity, batchUnit, unitQuantity, packageUnit)` | Chuyển đổi đơn vị và chia lô: ví dụ 10 kg ÷ 500 g/gói = 20 gói |

---

## 5. Tầng Trình Bày — REST API

### 5.1 Quy Ước Chung

- **Base path**: `/api/{resource}`
- **Phân trang**: tham số `pageNum` (bắt đầu từ 1) và `pageSize` (mặc định 20)
- **Phản hồi**: bọc trong `ApiResponse<T>` chuẩn hóa:

```json
{
  "type": "GOOD",
  "code": 200,
  "message": "Mô tả thao tác",
  "detail": { ... },
  "timestamp": "2026-05-06T10:00:00"
}
```

- **Xác thực**: Tất cả endpoint (trừ OPTIONS) đều yêu cầu JWT hợp lệ trong header `Authorization: Bearer <token>`.

### 5.2 WarehouseController — `/api/warehouse`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| POST | `/api/warehouse` | Có | `WarehouseCreateRequest` | `ApiResponse<Warehouse>` | Tạo kho hàng mới |
| GET | `/api/warehouse/{id}` | Có | — | `ApiResponse<Warehouse>` | Lấy thông tin kho theo ID |
| GET | `/api/warehouse?pageNum=1&pageSize=20` | Có | — | `ApiResponse<Page<Warehouse>>` | Danh sách kho phân trang |
| PUT | `/api/warehouse/{id}` | Có | `WarehouseUpdateRequest` | `ApiResponse<Warehouse>` | Cập nhật kho |
| DELETE | `/api/warehouse/{id}` | Có | — | `ApiResponse<Void>` | Xóa kho |

### 5.3 StorageToolController — `/api/storage-tool`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| POST | `/api/storage-tool` | Có | `StorageToolCreateRequest` | `ApiResponse<StorageTool>` | Tạo công cụ lưu trữ |
| GET | `/api/storage-tool/{id}` | Có | — | `ApiResponse<StorageTool>` | Lấy theo ID |
| GET | `/api/storage-tool` | Có | — | `ApiResponse<Page<StorageTool>>` | Danh sách phân trang |
| PUT | `/api/storage-tool/{id}` | Có | `StorageToolUpdateRequest` | `ApiResponse<StorageTool>` | Cập nhật |
| DELETE | `/api/storage-tool/{id}` | Có | — | `ApiResponse<Void>` | Xóa |

### 5.4 RackController — `/api/rack`

Hỗ trợ CRUD tiêu chuẩn tương tự StorageToolController với body `RackCreateRequest` / `RackUpdateRequest` và kiểu trả về `Rack`.

### 5.5 RackLevelController — `/api/rack-level`

Hỗ trợ CRUD tiêu chuẩn với body `RackLevelCreateRequest` / `RackLevelUpdateRequest` và kiểu trả về `RackLevel`.

### 5.6 FridgeController — `/api/fridge`

Hỗ trợ CRUD tiêu chuẩn với body `FridgeCreateRequest` / `FridgeUpdateRequest` và kiểu trả về `Fridge`.

### 5.7 ProductGeneralController — `/api/product-general`

Hỗ trợ CRUD tiêu chuẩn với body `ProductGeneralCreateRequest` / `ProductGeneralUpdateRequest` và kiểu trả về `ProductGeneral`.

### 5.8 ProductBatchController — `/api/product-batch`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| POST | `/api/product-batch` | Có | `ProductBatchCreateRequest` | `ApiResponse<ProductBatch>` | Tạo lô hàng |
| GET | `/api/product-batch/{id}` | Có | — | `ApiResponse<ProductBatch>` | Lấy theo ID |
| GET | `/api/product-batch` | Có | — | `ApiResponse<Page<ProductBatch>>` | Danh sách phân trang |
| PUT | `/api/product-batch/{id}` | Có | `ProductBatchUpdateRequest` | `ApiResponse<ProductBatch>` | Cập nhật |
| DELETE | `/api/product-batch/{id}` | Có | — | `ApiResponse<Void>` | Xóa |
| GET | `/api/product-batch/{id}/proof-images` | Có | — | `ApiResponse<List<String>>` | Lấy URL ảnh chứng minh |
| POST | `/api/product-batch/{id}/proof-images` | Có | `multipart/form-data` (images) | `ApiResponse<List<String>>` | Upload ảnh chứng minh |
| GET | `/api/product-batch/status/{status}` | Có | — | `ApiResponse<List<ProductBatch>>` | Lọc theo trạng thái |
| POST | `/api/product-batch/{id}/accept-delivery` | Có | `DeliveryAcceptanceRequest` | `ApiResponse<ProductBatch>` | Xác nhận nhận hàng thực tế |
| POST | `/api/product-batch/{id}/reject-delivery` | Có | `{note: String}` | `ApiResponse<ProductBatch>` | Từ chối lô hàng |

### 5.9 ProductSubBatchController — `/api/product-sub-batch`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| POST | `/api/product-sub-batch` | Có | `ProductSubBatchCreateRequest` | `ApiResponse<ProductSubBatch>` | Tạo lô hàng con |
| GET | `/api/product-sub-batch/{subBatchId}` | Có | — | `ApiResponse<ProductSubBatch>` | Lấy theo ID |
| GET | `/api/product-sub-batch` | Có | — | `ApiResponse<Page<ProductSubBatch>>` | Danh sách phân trang |
| PUT | `/api/product-sub-batch/{subBatchId}` | Có | `ProductSubBatchUpdateRequest` | `ApiResponse<ProductSubBatch>` | Cập nhật |
| DELETE | `/api/product-sub-batch/{subBatchId}` | Có | — | `ApiResponse<Void>` | Xóa |
| GET | `/api/product-sub-batch/{subBatchId}/proof-images` | Có | — | `ApiResponse<List<String>>` | Lấy URL video chứng minh |
| POST | `/api/product-sub-batch/{subBatchId}/proof-images` | Có | `multipart/form-data` | `ApiResponse<List<String>>` | Upload video chứng minh |
| GET | `/api/product-sub-batch/by-batch/{batchId}` | Có | — | `ApiResponse<List<ProductSubBatch>>` | Lấy tất cả sub-batch của lô gộp |
| POST | `/api/product-sub-batch/{subBatchId}/accept-delivery` | Có | `DeliveryAcceptanceRequest` | `ApiResponse<ProductSubBatch>` | Xác nhận nhận hàng |
| POST | `/api/product-sub-batch/{subBatchId}/reject-delivery` | Có | `{note: String}` | `ApiResponse<ProductSubBatch>` | Từ chối lô con |

### 5.10 ProductDetailController — `/api/product-detail`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| POST | `/api/product-detail` | Có | `ProductDetailCreateRequest` | `ApiResponse<ProductDetail>` | Tạo sản phẩm đơn lẻ |
| GET | `/api/product-detail/{id}` | Có | — | `ApiResponse<ProductDetail>` | Lấy theo ID |
| GET | `/api/product-detail` | Có | — | `ApiResponse<Page<ProductDetail>>` | Danh sách phân trang |
| PUT | `/api/product-detail/{id}` | Có | `ProductDetailUpdateRequest` | `ApiResponse<ProductDetail>` | Cập nhật |
| DELETE | `/api/product-detail/{id}` | Có | — | `ApiResponse<Void>` | Xóa |
| POST | `/api/product-detail/process-batch` | Có | `ProcessProductBatchRequest` | `ApiResponse<?>` | Xử lý lô hàng (phiên bản cũ — deprecated) |
| POST | `/api/product-detail/process-batch-v2` | Có | `ProcessBatchRequest` | `ApiResponse<ProcessBatchResponse>` | Xử lý lô hàng (phiên bản 2 — khuyến nghị) |
| GET | `/api/product-detail/quantity/{batchId}` | Có | — | `ApiResponse<Long>` | Đếm số sản phẩm còn tồn kho (STORED) |

**Cấu trúc `ProcessBatchRequest`**:

```json
{
  "batchId": 1,
  "productGeneralId": 5,
  "price": 35000,
  "storageToolId": 2,
  "numOfStar": 4
}
```

**Cấu trúc `ProcessBatchResponse`**:

```json
{
  "verificationType": "VIDEO",
  "totalProductDetailsCreated": 30,
  "batchId": 1,
  "productGeneralId": 5,
  "providerId": null,
  "subBatchBreakdowns": [
    {
      "subBatchId": 10,
      "providerId": 201,
      "quantityProcessed": 20,
      "productDetailsCreated": 20
    },
    {
      "subBatchId": 11,
      "providerId": 202,
      "quantityProcessed": 10,
      "productDetailsCreated": 10
    }
  ]
}
```

### 5.11 PickListController — `/api/pick-list`

| Phương thức HTTP | Đường dẫn | Xác thực | Body yêu cầu | Kiểu phản hồi | Mô tả |
|---|---|---|---|---|---|
| GET | `/api/pick-list/{orderId}` | Có | — | `ApiResponse<List<PickListItem>>` | Lấy danh sách nhặt hàng theo đơn hàng |
| PUT | `/api/pick-list/{orderItemId}/link/{productDetailId}` | Có | — | `ApiResponse<OrderItem>` | Gán sản phẩm vật lý vào mục đặt hàng |
| GET | `/api/pick-list/product-detail-list/{orderItemId}` | Có | — | `ApiResponse<List<ProductDetailForPickItem>>` | Lấy danh sách sản phẩm khả dụng cho mục nhặt hàng |

### 5.12 OrderItemController — `/api/order-items`

Hỗ trợ CRUD tiêu chuẩn với kiểu trả về `OrderItem`.

### 5.13 RawProductDemandController — `/api/raw-product-demand`

Hỗ trợ CRUD tiêu chuẩn với body `RawProductDemandCreateRequest` / `RawProductDemandUpdateRequest` và kiểu trả về `RawProductDemand`.

---

## 6. Bảo Mật và Phân Quyền

### 6.1 Cơ Chế Xác Thực

Dịch vụ sử dụng xác thực dựa trên **JWT (JSON Web Token)** với chữ ký RSA. Khóa công khai (`public.pem`) được nhúng vào tài nguyên ứng dụng.

**Quy trình xác thực**:

1. Client gửi request kèm header `Authorization: Bearer <jwt_token>`.
2. `JwtAuthenticationFilter` trích xuất và xác minh chữ ký JWT bằng khóa RSA công khai.
3. Nếu hợp lệ, thông tin người dùng được tạo thành `AuthenticatedUser` và lưu vào `SecurityContextHolder`.
4. Nếu không hợp lệ hoặc thiếu token, `CustomAuthenticationEntryPoint` trả về HTTP 401.
5. Nếu không đủ quyền, `CustomAccessDeniedHandler` trả về HTTP 403.

### 6.2 Cấu Hình Filter Chain

```
OPTIONS requests  →  Permit All (no auth required)
All other paths   →  Authenticated (JWT required)
```

**Đặc điểm**:
- CSRF: Tắt (stateless API)
- HTTP Basic: Tắt
- Form Login: Tắt
- Session: `STATELESS` — không duy trì session phía server

### 6.3 Endpoints Công Khai và Bảo Vệ

| Loại | Điều kiện | Mô tả |
|---|---|---|
| Công khai | Phương thức OPTIONS | Preflight CORS |
| Bảo vệ | Tất cả còn lại | Yêu cầu JWT hợp lệ |

**Ghi chú**: Dịch vụ hiện tại **không phân quyền theo vai trò** (role-based) tại tầng HTTP — mọi người dùng đã xác thực đều có quyền truy cập tất cả endpoint. Phân quyền logic nghiệp vụ (nếu có) được thực hiện tại tầng service.

---

## 7. Giao Tiếp Hướng Sự Kiện (Kafka)

### 7.1 Cấu Hình Producer

| Tham số | Giá trị |
|---|---|
| Bootstrap servers | `${KAFKA_HOST}:${KAFKA_PORT}` |
| Key serializer | `StringSerializer` |
| Value serializer | `JacksonJsonSerializer` |
| `acks` | `all` (chờ xác nhận từ tất cả replica) |
| `retries` | 3 |
| `linger.ms` | 5 |
| `enable.idempotence` | `true` |

### 7.2 Cấu Hình Consumer

| Tham số | Giá trị |
|---|---|
| Bootstrap servers | `${KAFKA_HOST}:${KAFKA_PORT}` |
| Group ID | `productstorage-group` |
| Auto offset reset | `earliest` |
| Auto create topics | `false` |
| Message converter | `StringJacksonJsonMessageConverter` |
| Container factory | `ConcurrentKafkaListenerContainerFactory` |

### 7.3 Các Topic Tiêu Thụ (Consumer)

#### 7.3.1 `product-general-events`

| Thuộc tính | Giá trị |
|---|---|
| Consumer class | `ProductGeneralCreatedConsumer` |
| Group ID | `product-storage-group` |
| Kích hoạt khi | Back-office-service tạo sản phẩm mới |

**Cấu trúc sự kiện `ProductGeneralCreatedEvent`**:

```json
{
  "prodGenId": 123,
  "name": "Thịt Heo Vai",
  "imgUrl": "https://...",
  "description": "Thịt heo vai tươi",
  "subSubcategoryId": 5,
  "unit": "KILOGRAM",
  "unitQuantity": 1
}
```

**Hành động**: Tạo bản ghi `ProductGeneral` trong database cục bộ với ID thủ công từ event.

#### 7.3.2 `subsubcategory-events`

| Thuộc tính | Giá trị |
|---|---|
| Consumer class | `SubSubcategoryCreatedConsumer` |
| Kích hoạt khi | Back-office-service tạo danh mục chi tiết mới |

**Cấu trúc sự kiện `SubSubcategoryCreatedEvent`**:

```json
{
  "subSubcategoryId": 10,
  "name": "Thịt Heo",
  "description": "Các loại thịt heo tươi",
  "iconUrl": "https://...",
  "subcategoryId": 3,
  "avgShelfDays": 3
}
```

**Hành động**: Tạo bản ghi `SubSubcategory` với `avgShelfDays` dùng để tính hạn sử dụng lô hàng.

#### 7.3.3 `provider-create-events`

| Thuộc tính | Giá trị |
|---|---|
| Consumer class | `ProviderCreatedConsumer` |
| Annotation bổ sung | `@Transactional` |
| Kích hoạt khi | Identity-service tạo tài khoản nhà cung cấp |

**Cấu trúc sự kiện `ProviderCreatedEvent`**:

```json
{
  "userId": 201,
  "verificationMethod": "CERTIFICATE"
}
```

**Hành động**: Tạo `Provider` với `providerId = userId`, `verificationStatus = UNVERIFIED`.

#### 7.3.4 `provider-verification-update-events`

| Thuộc tính | Giá trị |
|---|---|
| Consumer class | `ProviderVerificationUpdatedConsumer` |
| Kích hoạt khi | Admin phê duyệt xác minh nhà cung cấp |

**Hành động**: Cập nhật `verificationStatus` và `certificateType` của `Provider`.

#### 7.3.5 `order-pick-requested-events`

| Thuộc tính | Giá trị |
|---|---|
| Consumer class | `OrderPickRequestedConsumer` |
| Group ID | `product-storage-group` |
| Kích hoạt khi | Ecommerce-service yêu cầu xuất hàng cho đơn hàng |

**Cấu trúc sự kiện `OrderPickRequestedEvent`**:

```json
{
  "orderId": 456,
  "buyerId": 789,
  "orderItems": [
    {
      "batchDetailId": 101,
      "quantity": 3
    },
    {
      "batchDetailId": 102,
      "quantity": 1
    }
  ]
}
```

**Hành động**: Gọi `PickListService.createPickList()` để tạo các bản ghi `OrderItem` trong database.

### 7.4 Các Topic Phát Hành (Producer)

#### 7.4.1 `batch-detail-events`

| Thuộc tính | Giá trị |
|---|---|
| Producer class | `BatchDetailProducer` |
| Phương thức | `publishBatchDetailCreated(BatchDetailCreateEvent)` |
| Message key | `batchDetailId.toString()` |
| Consumer dự kiến | ecommerce-service |
| Kích hoạt khi | Hoàn thành xử lý lô hàng qua `processProductBatchV2()` |

**Cấu trúc sự kiện `BatchDetailCreateEvent`**:

```json
{
  "batchDetailId": 789,
  "productGeneralId": 123,
  "quantity": 20,
  "price": 35000,
  "avgRate": 4,
  "numRate": 0,
  "detailContent": "Mô tả sản phẩm",
  "subBatchId": null,
  "verificationType": "CERTIFICATE",
  "certificateType": "VIETGAP",
  "providerId": 201,
  "logoUrl": "https://..."
}
```

**Biến thể cho luồng VIDEO**:

```json
{
  "batchDetailId": 790,
  "productGeneralId": 123,
  "quantity": 20,
  "price": 35000,
  "avgRate": 4,
  "numRate": 0,
  "detailContent": "Mô tả sản phẩm",
  "subBatchId": 10,
  "verificationType": "VIDEO",
  "certificateType": null,
  "providerId": null,
  "logoUrl": null
}
```

**Ghi chú**: Với luồng VIDEO, sự kiện được phát một lần cho mỗi sub-batch và không bao gồm thông tin nhà cung cấp (bảo vệ tính ẩn danh của nguồn gốc).

#### 7.4.2 `order-packaging-progress-update-events`

| Thuộc tính | Giá trị |
|---|---|
| Producer class | `OrderPackagingProgressUpdateEventProducer` |
| Kích hoạt khi | Nhân viên kho gán `ProductDetail` vào `OrderItem` (qua `/api/pick-list/{orderItemId}/link/{productDetailId}`) |
| Consumer dự kiến | ecommerce-service |

**Mục đích**: Thông báo tiến độ đóng gói để ecommerce-service cập nhật trạng thái đơn hàng.

### 7.5 Xử Lý Lỗi Kafka

- **Consumer**: Lỗi được ghi log; không có cơ chế retry tự động được cấu hình sẵn. Consumer phải tự xử lý trường hợp ngoại lệ.
- **Producer**: Cấu hình `retries=3` và `enable.idempotence=true` giúp giảm thiểu mất dữ liệu. Lỗi publish được ghi log kèm chi tiết sự kiện.

---

## 8. Lưu Trữ Tệp

### 8.1 Nhà Cung Cấp

Dịch vụ sử dụng **Cloudflare R2** — dịch vụ lưu trữ đối tượng tương thích với giao thức AWS S3.

### 8.2 Cấu Hình Kết Nối

| Tham số | Giá trị |
|---|---|
| Endpoint | `https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com` |
| Access Key | `${R2_ACCESS_KEY}` |
| Secret Key | `${R2_SECRET_KEY}` |
| Region | `auto` (tiêu chuẩn Cloudflare) |
| Path style access | `true` |
| SDK | AWS SDK for Java v2 (`S3Client`) |

### 8.3 Bucket và Mục Đích Sử Dụng

| Bucket | Mục đích | Đọc công khai |
|---|---|---|
| `product-batch-proof-img` | Ảnh chứng minh lô hàng (ProductBatch) | Có (qua public URL) |
| `product-batch-proof-img` | Video chứng minh lô hàng con (ProductSubBatch) | Có |

**URL công khai**: `https://pub-f718e3cf29184b27a8a55e13032dd01e.r2.dev`

### 8.4 Luồng Upload

1. Client gửi request `POST /api/product-batch/{id}/proof-images` hoặc `POST /api/product-sub-batch/{subBatchId}/proof-images` với `multipart/form-data`.
2. Controller chuyển `List<MultipartFile>` đến `ProductBatchService.uploadProofImages()`.
3. Service gọi `R2UploadService.upload(file, bucketName)` cho từng tệp.
4. `R2UploadService` sử dụng `S3Client.putObject()` để upload.
5. URL công khai được tạo và lưu vào `proofImageUrls` (ElementCollection) của entity.
6. Danh sách URL được trả về trong response.

**Giới hạn kích thước tệp**:
- Kích thước tệp tối đa: 10 MB
- Kích thước request tối đa: 50 MB

---

## 9. Giao Tiếp Liên Dịch Vụ (REST Calls)

### 9.1 BackOfficeServiceClient

| Thuộc tính | Giá trị |
|---|---|
| Class | `BackOfficeServiceClient` |
| Base URL | `http://${BACK_OFFICE_HOST:localhost}:${BACK_OFFICE_PORT:9100}` |
| Mục đích | Gọi đồng bộ đến back-office-service để lấy thông tin danh mục/sản phẩm |

**Ghi chú**: Trong kiến trúc hiện tại, luồng chính đồng bộ dữ liệu danh mục và sản phẩm qua **Kafka** (bất đồng bộ). `BackOfficeServiceClient` là thành phần dự phòng hoặc dùng cho các trường hợp cần truy vấn trực tiếp.

### 9.2 URL Các Dịch Vụ Được Cấu Hình

```yaml
services:
  identity:
    url: http://${IDENTITY_HOST:localhost}:${IDENTITY_PORT:9000}
  back-office:
    url: http://${BACK_OFFICE_HOST:localhost}:${BACK_OFFICE_PORT:9100}
  ecommerce:
    url: http://${ECOMMERCE_HOST:localhost}:${ECOMMERCE_PORT:9300}
```

---

## 10. Khởi Tạo Dữ Liệu (DataSeeder)

### 10.1 Điều Kiện Kích Hoạt

Lớp `DataSeeder` triển khai `CommandLineRunner` và chạy tự động khi ứng dụng khởi động. Điều kiện kích hoạt:

```
warehouseRepository.count() == 0
```

Nếu database đã có dữ liệu, seeder bỏ qua toàn bộ quá trình khởi tạo.

### 10.2 Dữ Liệu Được Tạo

#### 10.2.1 Kho hàng (3 Warehouse)

| Warehouse | Địa chỉ |
|---|---|
| Kho 1 | Quận 7, TP.HCM |
| Kho 2 | Thủ Đức, TP.HCM |
| Kho 3 | Gò Vấp, TP.HCM |

#### 10.2.2 Công cụ lưu trữ (8 StorageTool)

| Số lượng | Loại | Mô tả |
|---|---|---|
| 4 | Rack | Kệ hàng thông thường cho hàng khô |
| 4 | Fridge | Tủ lạnh với các dải nhiệt độ khác nhau |

**Cấu hình nhiệt độ tủ lạnh**:

| Tủ lạnh | minTemp | maxTemp | Dùng cho |
|---|---|---|---|
| Fridge 1 | 0°C | 8°C | Rau củ quả |
| Fridge 2 | -2°C | 5°C | Thịt, hải sản |
| Fridge 3 | 1°C | 6°C | Sữa, trứng |
| Fridge 4 | 0°C | 8°C | Đồ uống |

#### 10.2.3 Tầng kệ (20 RackLevel)

20 tầng kệ phân bổ đều cho 4 Rack (4–6 tầng mỗi kệ).

#### 10.2.4 SubSubcategory (55 danh mục)

Phân loại nông sản tươi sống của Việt Nam:

| Nhóm | Ví dụ | avgShelfDays |
|---|---|---|
| Gia cầm | Thịt Gà, Vịt, Ngan | 2–3 ngày |
| Thịt đỏ | Bò, Heo, Dê | 3–4 ngày |
| Hải sản | Tôm, Cá, Mực | 1–3 ngày |
| Rau lá | Rau muống, Cải | 1–3 ngày |
| Củ quả | Cà rốt, Khoai tây | 7–30 ngày |
| Trái cây | Xoài, Dưa hấu | 5–7 ngày |
| Gia vị | Ớt, Tỏi, Gừng | 7 ngày |
| Sữa | Sữa tươi, Phô mai | 7–180 ngày |
| Trứng | Trứng gà, vịt | 7–21 ngày |
| Thay thế sữa | Sữa đậu nành | 5–14 ngày |

#### 10.2.5 ProductGeneral (105 sản phẩm)

3 biến thể cho mỗi SubSubcategory (ví dụ: Thịt Heo Vai, Thịt Heo Ba Chỉ, Thịt Heo Sườn).

#### 10.2.6 ProductBatch (không seed)

Không seed sẵn `ProductBatch` nào — một lô hàng luôn cần một nhà cung cấp thật
đứng sau nó (`providerId` cho lô CERTIFICATE, hoặc các `ProductSubBatch` thật
cho lô VIDEO), mà nhà cung cấp chỉ tồn tại cục bộ sau khi đã trải qua quy trình
xác minh thật (xem kịch bản `provider-certificate`/`provider-video` và
`batch-to-detail` trong `tools/scenario-seeder`).

### 10.3 Thông Tin Đăng Nhập Mặc Định

DataSeeder không tạo tài khoản người dùng trong dịch vụ này. Quản lý người dùng thuộc về identity-service.

---

## 11. Cấu Hình Ứng Dụng

### 11.1 Tệp Cấu Hình Chính (`application.yaml`)

```yaml
spring:
  application:
    name: productstorage

  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/product_storage_db?stringtype=unspecified
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true

  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 50MB

server:
  port: 9200

app:
  kafka-url: ${KAFKA_HOST}:${KAFKA_PORT}
  product-batch-proof-img-bucket: product-batch-proof-img
  product-batch-proof-img-public-bucket-url: https://pub-f718e3cf29184b27a8a55e13032dd01e.r2.dev

cloudflare:
  r2:
    account-id: ${R2_ACCOUNT_ID}
    endpoint: https://${R2_ACCOUNT_ID}.r2.cloudflarestorage.com
    access-key: ${R2_ACCESS_KEY}
    secret-key: ${R2_SECRET_KEY}
    region: auto

services:
  identity:
    url: http://${IDENTITY_HOST:localhost}:${IDENTITY_PORT:9000}
  back-office:
    url: http://${BACK_OFFICE_HOST:localhost}:${BACK_OFFICE_PORT:9100}
  ecommerce:
    url: http://${ECOMMERCE_HOST:localhost}:${ECOMMERCE_PORT:9300}
```

### 11.2 Biến Môi Trường Bắt Buộc

| Biến | Mô tả | Ví dụ |
|---|---|---|
| `DB_HOST` | Địa chỉ máy chủ PostgreSQL | `localhost` hoặc `postgres` (Docker) |
| `DB_PORT` | Cổng PostgreSQL | `5432` |
| `DB_USERNAME` | Tên đăng nhập database | `khoidev` |
| `DB_PASSWORD` | Mật khẩu database | |
| `KAFKA_HOST` | Địa chỉ Kafka broker | `localhost` hoặc `kafka` (Docker) |
| `KAFKA_PORT` | Cổng Kafka | `9092` |
| `R2_ACCOUNT_ID` | ID tài khoản Cloudflare | |
| `R2_ACCESS_KEY` | Access key Cloudflare R2 | |
| `R2_SECRET_KEY` | Secret key Cloudflare R2 | |
| `BACK_OFFICE_HOST` | Địa chỉ back-office-service | `localhost` hoặc `back-office-service` |
| `BACK_OFFICE_PORT` | Cổng back-office-service | `9100` |
| `IDENTITY_HOST` | Địa chỉ identity-service | `localhost` hoặc `identity-service` |
| `IDENTITY_PORT` | Cổng identity-service | `9000` |
| `ECOMMERCE_HOST` | Địa chỉ ecommerce-service | `localhost` hoặc `ecommerce-service` |
| `ECOMMERCE_PORT` | Cổng ecommerce-service | `9300` |

### 11.3 Cấu Hình CORS

| Tham số | Giá trị |
|---|---|
| Allowed Origins | `http://10.205.183.122:5173`, `http://10.185.89.85:5173`, `http://localhost:5173`, `http://10.194.144.9:3000`, `http://localhost:5273` |
| Allow Credentials | `true` |
| Allowed Headers | `*` (tất cả) |
| Allowed Methods | `*` (tất cả) |
| Max Age | 3600 giây |
| Áp dụng cho | `/api/**` |

---

## 12. Phụ Thuộc Kỹ Thuật Chính

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| `spring-boot-starter-parent` | 4.0.3 | Framework chính, auto-configuration |
| `spring-boot-starter-data-jpa` | (quản lý bởi BOM) | ORM với Hibernate, Spring Data repositories |
| `spring-boot-starter-web` | (quản lý bởi BOM) | REST API, nhúng Tomcat |
| `spring-boot-starter-security` | (quản lý bởi BOM) | Bảo mật, filter chain, CSRF, session |
| `spring-kafka` | (quản lý bởi BOM) | Kafka producer và consumer |
| `postgresql` | (quản lý bởi BOM) | JDBC driver cho PostgreSQL (runtime) |
| `jjwt-api` | 0.11.5 | Xác minh JWT |
| `jjwt-impl` | 0.11.5 | Triển khai JWT |
| `jjwt-jackson` | 0.11.5 | Deserialize JWT payload bằng Jackson |
| `aws-sdk-s3` | 2.42.4 | Giao tiếp Cloudflare R2 (S3-compatible) |
| `lombok` | (quản lý bởi BOM) | Giảm boilerplate: `@Getter`, `@Setter`, `@AllArgsConstructor` |
| `jackson-databind` | (quản lý bởi BOM) | Serialization/deserialization JSON |

---

## 13. Nhận Xét Kiến Trúc

### 13.1 Điểm Mạnh Thiết Kế

**Mô hình kiểm định nhà cung cấp hai luồng (CERTIFICATE / VIDEO)**  
Đây là điểm sáng tạo nhất trong thiết kế. Hệ thống phân biệt rõ ràng hai phương thức kiểm định phù hợp với thực tiễn nông nghiệp Việt Nam: nhà cung cấp lớn có chứng chỉ (VietGAP, GlobalGAP) và nhà cung cấp nhỏ lẻ kiểm định qua video. Mô hình này cho phép ecommerce-service hiển thị thông tin nguồn gốc minh bạch (CERTIFICATE) hoặc ẩn danh nguồn gốc (VIDEO) một cách tự nhiên từ cấu trúc dữ liệu.

**Chuỗi xử lý lô hàng rõ ràng**  
Luồng `ProductBatch → processProductBatchV2 → ProductDetail → Kafka event` có ranh giới trách nhiệm rõ ràng. Mỗi bước có trạng thái kiểm soát (PENDING → PROCESSED) và validation nghiệp vụ tường minh.

**Phân trang 1-based API, 0-based nội bộ**  
Thiết kế API nhất quán sử dụng chỉ số trang bắt đầu từ 1 (thân thiện với người dùng) trong khi nội bộ sử dụng `PageRequest.of(pageNum - 1, pageSize)` của Spring Data JPA.

**Khởi tạo dữ liệu thực tế**  
DataSeeder tạo dữ liệu phong phú với phân loại nông sản thực tế của Việt Nam và cấu hình nhiệt độ tủ lạnh phù hợp từng loại thực phẩm — hỗ trợ tốt cho việc phát triển và kiểm thử.

**Projection pattern cho pick list**  
Sử dụng JPA Projection interface (`PickListItem`, `ProductDetailForPickItem`) thay vì tải toàn bộ entity giúp giảm tải dữ liệu khi truy vấn danh sách nhặt hàng vốn cần nhiều thông tin từ nhiều bảng.

### 13.2 Quyết Định Thiết Kế Đáng Chú Ý

**Khóa chính thủ công cho các entity đồng bộ**  
`SubSubcategory`, `ProductGeneral`, và `Provider` sử dụng khóa chính thủ công được gán từ dịch vụ nguồn (back-office, identity) qua sự kiện Kafka. Quyết định này đảm bảo tính nhất quán ID xuyên suốt các dịch vụ mà không cần tra cứu ánh xạ — một sự đánh đổi hợp lý giữa sự phức tạp ánh xạ và rủi ro xung đột ID.

**ElementCollection cho danh sách URL**  
`proofImageUrls` của `ProductBatch` và `ProductSubBatch` dùng `@ElementCollection` thay vì entity riêng. Quyết định này đơn giản hóa truy cập nhưng hạn chế khả năng query (không thể dùng JPQL để lọc theo URL cụ thể).

**Hai phiên bản API xử lý lô hàng**  
Sự tồn tại song song của `process-batch` (v1) và `process-batch-v2` cho thấy hệ thống đang trong quá trình migration API. Phiên bản cũ chưa được xóa có thể gây nhầm lẫn cho người tích hợp.

### 13.3 Nợ Kỹ Thuật Được Xác Định

**Không có cơ chế retry cho Kafka consumer**  
Hiện tại, khi consumer gặp lỗi (ví dụ: database tạm thời không khả dụng), message bị bỏ qua và chỉ được ghi log. Không có Dead Letter Topic (DLT) hoặc cơ chế retry backoff được cấu hình. Trong môi trường production, điều này có thể dẫn đến mất đồng bộ giữa các dịch vụ nếu xảy ra lỗi tạm thời.

**Không có phân quyền theo vai trò tại tầng HTTP**  
Tất cả endpoint đều chỉ yêu cầu xác thực, không phân biệt vai trò (nhân viên kho, quản lý, admin). Nếu hệ thống mở rộng phạm vi truy cập, cần bổ sung kiểm soát phân quyền.

**CORS với địa chỉ IP cố định**  
Danh sách `allowedOrigins` trong `WebConfig` chứa các địa chỉ IP cụ thể (`10.205.183.122`, `10.185.89.85`, `10.194.144.9`). Cách cấu hình này gây khó khăn khi triển khai vào môi trường khác và nên được đưa vào biến môi trường.

**Thiếu API version prefix**  
Các endpoint hiện tại dùng `/api/{resource}` mà không có version (`/api/v1/`). Khi cần thay đổi hợp đồng API không tương thích ngược, sẽ khó quản lý.

**DDL auto-update trong production**  
Cấu hình `hibernate.ddl-auto: update` phù hợp cho phát triển nhưng tiềm ẩn rủi ro trong production: Hibernate có thể tự thay đổi schema theo cách không mong muốn. Khuyến nghị chuyển sang quản lý migration bằng Flyway hoặc Liquibase.

**Legacy endpoint `process-batch` (v1) chưa bị xóa**  
Endpoint `POST /api/product-detail/process-batch` đã lỗi thời nhưng vẫn còn tồn tại. Cần lên kế hoạch deprecation và loại bỏ để tránh nhầm lẫn khi tích hợp.
