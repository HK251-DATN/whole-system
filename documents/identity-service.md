# Identity Service — Tài Liệu Kỹ Thuật

## 1. Tổng Quan

Identity Service là vi dịch vụ trung tâm chịu trách nhiệm quản lý danh tính, xác thực và phân quyền người dùng trong hệ thống thương mại điện tử. Dịch vụ được xây dựng trên nền tảng Spring Boot 4.0.1 với Java 25, lắng nghe trên cổng **9000**, và áp dụng mô hình kiến trúc phân tầng truyền thống (Controller → Service → Repository → DAO).

Dịch vụ triển khai cơ chế kiểm soát truy cập dựa trên vai trò (Role-Based Access Control — RBAC) theo mô hình ba tầng: **Người dùng (User) → Nhóm (Group) → Quyền hạn (Permission)**. Xác thực stateless được thực hiện qua cặp khóa RSA-256 asymmetric (private key ký token, public key xác minh), đảm bảo các dịch vụ khác trong hệ thống có thể xác minh token mà không cần gọi lại Identity Service.

---

## 2. Cấu Trúc Package

```
edu.hcmut.datn.identity_service/
├── config/           # Cấu hình hệ thống (CORS, R2, DataSeeder)
├── controller/       # Tầng trình bày — REST API endpoints
├── dao/              # Thực thể JPA (ánh xạ cơ sở dữ liệu)
├── dto/
│   ├── request/      # DTO đầu vào từ client
│   ├── response/     # DTO đầu ra trả về client
│   └── misc/         # Projection interface (truy vấn tối giản)
├── messaging/        # Kafka producer và cấu hình
├── repository/       # Spring Data JPA repositories
├── security/
│   ├── jwt/          # Sinh token JWT
│   └── portable/     # Xác minh token, filter, security config
├── service/          # Interface và implementation nghiệp vụ
└── common/enums/     # Enum dùng chung (Gender, Bank)
```

**Tài nguyên tĩnh:**
- `src/main/resources/keys/private.pem` — RSA private key (chỉ Identity Service sở hữu)
- `src/main/resources/keys/public.pem` — RSA public key (có thể phân phối sang các dịch vụ khác)

---

## 3. Mô Hình Dữ Liệu

### 3.1 Các Thực Thể Chính

#### User (`users`)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `userId` | Long (PK) | Định danh tự tăng |
| `userEmail` | String | Email đăng nhập (duy nhất) |
| `hashedPwd` | String | Mật khẩu băm bằng BCrypt |
| `createdAt` | LocalDateTime | Thời điểm tạo (tự động) |
| `updatedAt` | LocalDateTime | Thời điểm cập nhật cuối (tự động) |

#### Group (`groups`)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `groupId` | Long (PK) | Định danh tự tăng |
| `groupName` | String | Tên nhóm/vai trò (ví dụ: ADMIN, BUYER) |
| `description` | String | Mô tả nhóm |
| `isActive` | boolean | Trạng thái kích hoạt |
| `createdAt` | LocalDateTime | Thời điểm tạo (tự động) |
| `updatedAt` | LocalDateTime | Thời điểm cập nhật cuối (tự động) |

#### Permission (`permissions`)

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `perId` | Long (PK) | Định danh tự tăng |
| `perCode` | String | Mã quyền hạn (ví dụ: USER_VIEW, GROUP_MANAGE) |
| `perName` | String | Tên hiển thị |
| `perDescription` | String | Mô tả chức năng |
| `isActive` | boolean | Trạng thái kích hoạt |
| `createdAt` | LocalDateTime | Thời điểm tạo (tự động) |
| `updatedAt` | LocalDateTime | Thời điểm cập nhật cuối (tự động) |

#### UserGroup (`user_group`) — Bảng liên kết User–Group

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `userGroupId` | Long (PK) | Định danh tự tăng |
| `userId` | Long (FK) | Tham chiếu đến User |
| `groupId` | Long (FK) | Tham chiếu đến Group |
| `isActive` | boolean | Trạng thái kích hoạt |
| `addedAt` | LocalDateTime | Thời điểm gán nhóm (tự động) |
| `updatedAt` | LocalDateTime | Thời điểm cập nhật cuối (tự động) |

#### GroupPermission (`group_permission`) — Bảng liên kết Group–Permission

| Trường | Kiểu | Mô tả |
|--------|------|-------|
| `groupPerId` | Long (PK) | Định danh tự tăng |
| `groupId` | Long (FK) | Tham chiếu đến Group |
| `perId` | Long (FK) | Tham chiếu đến Permission |
| `isActive` | boolean | Trạng thái kích hoạt |
| `validUntil` | LocalDateTime | Thời hạn hiệu lực của quyền |
| `createdAt` | LocalDateTime | Thời điểm tạo (tự động) |
| `updatedAt` | LocalDateTime | Thời điểm cập nhật cuối (tự động) |

### 3.2 Quan Hệ Thực Thể

```
User ──(UserGroup)──▶ Group ──(GroupPermission)──▶ Permission
     many-to-many              many-to-many
```

Đây là mô hình RBAC chuẩn: một người dùng có thể thuộc nhiều nhóm; một nhóm có thể được cấp nhiều quyền hạn; một quyền hạn có thể được gán cho nhiều nhóm. Mỗi liên kết đều mang cờ `isActive` cho phép vô hiệu hóa mà không cần xóa dữ liệu. Liên kết `GroupPermission` còn có trường `validUntil` cho phép cấp quyền tạm thời có thời hạn.

---

## 4. Tầng Nghiệp Vụ (Service Layer)

### 4.1 UserService

Interface `UserService` và implementation `UserServiceImpl` hiện thực hóa toàn bộ logic quản lý người dùng:

| Phương thức | Mô tả |
|-------------|-------|
| `create(User)` | Tạo người dùng thô, kiểm tra trùng email trước khi lưu |
| `create(UserRegistrationRequest)` | Tạo tài khoản người mua (buyer), công bố `UserCreatedEvent` lên Kafka |
| `create(EmployeeRegistrationRequest)` | Tạo tài khoản nhân viên, mặc định mật khẩu `"12345678"`, công bố `EmpCreatedEvent` |
| `createProvider(ProviderRegistrationRequest)` | Tạo tài khoản nhà cung cấp mới, công bố `ProviderCreatedEvent` |
| `linkProvider(Long, ProviderLinkRequest)` | Liên kết tài khoản hiện có thành nhà cung cấp (thêm thông tin ngân hàng), công bố `ProviderCreatedEvent` với cờ `isFreshAccount=false` |
| `authenticate(String, String)` | Xác minh email và mật khẩu bằng BCrypt, trả về `true/false` |
| `changePassword(Long, String, String)` | Đổi mật khẩu sau khi xác minh mật khẩu cũ |
| `getUserPermissions(Long)` | Lấy danh sách quyền hạn hiệu lực của người dùng qua JPQL join |
| `getUserGroups(Long)` | Lấy danh sách nhóm của người dùng |
| `getUserPermissionsList(Long)` | Lấy danh sách mã quyền (String) dùng để nhúng vào JWT |
| `getAll(Integer, Integer)` | Lấy danh sách phân trang (0-based index) |

**Lưu ý nghiệp vụ:**
- Mật khẩu được băm bằng `BCryptPasswordEncoder` trước khi lưu.
- URL avatar mặc định trỏ đến tài nguyên tĩnh công khai trên Cloudflare R2.
- Các phương thức trả về `null` khi thất bại thay vì ném ngoại lệ (ngoại trừ `changePassword` và `linkProvider` ném `RuntimeException` với thông điệp rõ ràng).

### 4.2 GroupService

`GroupService`/`GroupServiceImpl` quản lý vòng đời nhóm vai trò và hai thao tác liên kết:

- **Quản lý nhóm:** tạo, đọc, cập nhật, xóa nhóm; kiểm tra trùng tên nhóm.
- **Quản lý thành viên:** `addUser`, `removeUser`, `getUser` — thao tác qua bảng `UserGroup`.
- **Quản lý quyền hạn nhóm:** `grantPermission`, `revokePermission`, `getPermission` — thao tác qua bảng `GroupPermission`; kiểm tra trùng liên kết trước khi cấp quyền.

### 4.3 PermissionService

`PermissionService`/`PermissionServiceImpl` quản lý từ điển quyền hạn của hệ thống (CRUD cơ bản), kiểm tra tính duy nhất của `perCode` trước khi tạo.

### 4.4 R2UploadService

`R2UploadServiceImpl` đóng gói việc tải ảnh lên Cloudflare R2:
1. Nhận `MultipartFile` từ controller.
2. Tạo tên file duy nhất theo định dạng `UUID_tên-gốc`.
3. Gọi `S3Client.putObject()` với content-type tương ứng.
4. Trả về tên file đã sinh để caller lưu vào cơ sở dữ liệu.

---

## 5. Tầng Bảo Mật (Security Layer)

### 5.1 Cơ Chế Xác Thực

Dịch vụ sử dụng **JSON Web Token (JWT)** ký bằng thuật toán **RSA-256 (RS256)** thay vì HMAC đối xứng. Lợi thế của phương pháp này là các dịch vụ khác trong hệ thống chỉ cần public key để xác minh token mà không cần chia sẻ secret key, từ đó giảm bề mặt tấn công trong kiến trúc phân tán.

**Cấu trúc JWT Claims:**

```json
{
  "sub": "user@email.com",
  "iss": "identity-service",
  "iat": <unix_timestamp>,
  "exp": <unix_timestamp + 86400>,
  "userId": 123,
  "userEmail": "user@email.com",
  "permissions": ["USER_VIEW", "GROUP_MANAGE", "..."]
}
```

**Thời hạn token:** 24 giờ.

### 5.2 Luồng Xác Thực

1. Client gửi `POST /api/user/login` với email và mật khẩu.
2. `UserService.authenticate()` xác minh mật khẩu qua BCrypt.
3. Nếu hợp lệ, `JwtTokenGenerator` truy xuất danh sách quyền của người dùng, tạo JWT có ký và trả về cùng `accessToken`, `permissions`, `roles`.
4. Với các yêu cầu tiếp theo, client đính kèm `Authorization: Bearer <token>` trong header.
5. `JwtAuthenticationFilter` (chạy trước `UsernamePasswordAuthenticationFilter`) trích xuất và xác minh token, tạo đối tượng `AuthenticatedUser` với danh sách `SimpleGrantedAuthority` từ claim `permissions`, rồi đặt vào `SecurityContextHolder`.
6. Spring Security kiểm tra `@PreAuthorize("hasAuthority('X')")` tại controller để cấp hoặc từ chối truy cập.

### 5.3 Các Endpoint Công Khai (Không Yêu Cầu Xác Thực)

```
POST  /api/user/login
POST  /api/user/buyer-register
POST  /api/user/provider-register
POST  /api/user/upload-avt-img
POST  /api/user
OPTIONS  /**  (CORS preflight)
```

### 5.4 Mô Hình Phân Quyền

Phân quyền được kiểm tra ở cấp phương thức (method-level) thông qua annotation `@PreAuthorize`. Danh sách quyền mặc định được khởi tạo bởi `DataSeeder`:

| Mã quyền | Chức năng |
|----------|-----------|
| `USER_VIEW` | Xem thông tin người dùng |
| `USER_UPDATE` | Cập nhật thông tin người dùng |
| `USER_DELETE` | Xóa người dùng |
| `GROUP_MANAGE` | Quản lý nhóm và thành viên |
| `PERMISSION_MANAGE` | Cấp/thu hồi quyền hạn cho nhóm |
| `PERMISSION_VIEW` | Xem danh sách quyền hạn |

**Nhóm khởi tạo mặc định:**
- `ADMIN`: Có đủ 6 quyền trên.
- `BUYER`: Chỉ có `USER_VIEW`.

### 5.5 Xử Lý Lỗi Bảo Mật

| Tình huống | HTTP Status | Phản hồi |
|------------|-------------|---------|
| Token không hợp lệ hoặc hết hạn | 401 | `{"message": "Unauthorized: token invalid or expired"}` |
| Thiếu quyền hạn | 403 | Xử lý bởi `CustomAccessDeniedHandler` |
| Không có token | 401 | Xử lý bởi `CustomAuthenticationEntryPoint` |

### 5.6 Tính Portable của Security Module

Toàn bộ package `security/portable/` được thiết kế để có thể sao chép sang các vi dịch vụ khác nhằm xác minh JWT mà không phụ thuộc vào Identity Service. Điều này tuân theo nguyên tắc self-contained token trong kiến trúc microservices.

---

## 6. Tầng Trình Bày — REST API Endpoints

### 6.1 UserController (`/api/user`)

| Phương thức HTTP | Đường dẫn | Xác thực | Quyền hạn | Mô tả |
|-----------------|-----------|----------|-----------|-------|
| POST | `/api/user` | Không | — | Tạo người dùng thô |
| POST | `/api/user/login` | Không | — | Đăng nhập, trả về JWT |
| POST | `/api/user/buyer-register` | Không | — | Đăng ký tài khoản người mua |
| POST | `/api/user/emp-register` | Không | — | Đăng ký tài khoản nhân viên |
| POST | `/api/user/provider-register` | Không | — | Đăng ký tài khoản nhà cung cấp |
| POST | `/api/user/upload-avt-img` | Không | — | Tải ảnh đại diện lên R2 |
| POST | `/api/user/provider-link` | Có | — | Liên kết tài khoản hiện có thành nhà cung cấp |
| POST | `/api/user/change-password` | Có | — | Đổi mật khẩu |
| GET | `/api/user/{id}` | Có | `USER_VIEW` hoặc chính chủ | Lấy thông tin người dùng |
| GET | `/api/user` | Có | `USER_VIEW` | Lấy danh sách người dùng (phân trang) |
| PUT | `/api/user/{id}` | Có | `USER_UPDATE` hoặc chính chủ | Cập nhật thông tin người dùng |
| DELETE | `/api/user/{id}` | Có | `USER_DELETE` hoặc chính chủ | Xóa người dùng |
| GET | `/api/user/{userId}/group` | Có | `GROUP_MANAGE` hoặc chính chủ | Lấy danh sách nhóm của người dùng |
| GET | `/api/user/{userId}/permission` | Có | `USER_VIEW` + `PERMISSION_VIEW` | Lấy danh sách quyền hạn của người dùng |

### 6.2 GroupController (`/api/group`)

Toàn bộ endpoint yêu cầu quyền `GROUP_MANAGE`.

| Phương thức HTTP | Đường dẫn | Quyền bổ sung | Mô tả |
|-----------------|-----------|--------------|-------|
| POST | `/api/group` | — | Tạo nhóm |
| GET | `/api/group` | — | Lấy danh sách nhóm (phân trang) |
| GET | `/api/group/{groupId}` | — | Lấy thông tin nhóm |
| PUT | `/api/group/{groupId}` | — | Cập nhật nhóm |
| DELETE | `/api/group/{groupId}` | — | Xóa nhóm |
| GET | `/api/group/{groupId}/user` | — | Lấy danh sách thành viên |
| POST | `/api/group/{groupId}/user` | — | Thêm người dùng vào nhóm |
| DELETE | `/api/group/{groupId}/user` | — | Xóa người dùng khỏi nhóm |
| GET | `/api/group/{groupId}/permission` | — | Lấy danh sách quyền của nhóm |
| POST | `/api/group/{groupId}/permission` | `PERMISSION_MANAGE` | Cấp quyền cho nhóm |
| DELETE | `/api/group/{groupId}/permission` | `PERMISSION_MANAGE` | Thu hồi quyền khỏi nhóm |

### 6.3 PermissionController (`/api/permission`)

Toàn bộ endpoint yêu cầu quyền `PERMISSION_MANAGE`.

| Phương thức HTTP | Đường dẫn | Mô tả |
|-----------------|-----------|-------|
| POST | `/api/permission` | Tạo quyền hạn |
| GET | `/api/permission/{perId}` | Lấy thông tin quyền hạn |
| GET | `/api/permission` | Lấy danh sách quyền hạn (phân trang) |
| PUT | `/api/permission/{perId}` | Cập nhật quyền hạn |
| DELETE | `/api/permission/{perId}` | Xóa quyền hạn |

### 6.4 Cấu Trúc Phản Hồi Chuẩn

Mọi API đều trả về wrapper `ApiResponse<T>`:

```json
{
  "type": "GOOD | ERROR | WARN | SKIP_AS_GOOD",
  "code": "200 | 400 | 404 | ...",
  "message": "Mô tả kết quả",
  "detail": { ... },
  "timestamp": "2026-05-06T10:00:00"
}
```

---

## 7. Giao Tiếp Hướng Sự Kiện (Kafka)

Identity Service đóng vai trò **producer thuần túy** — chỉ công bố sự kiện, không tiêu thụ sự kiện từ dịch vụ nào khác.

### 7.1 Cấu Hình Producer

| Tham số | Giá trị | Ý nghĩa |
|---------|---------|---------|
| `acks` | `all` | Chờ xác nhận từ tất cả replica |
| `retries` | `3` | Tối đa 3 lần thử lại khi thất bại |
| `linger.ms` | `5` | Trễ 5ms để gom nhóm message (batching) |
| `enable.idempotence` | `true` | Đảm bảo mỗi message chỉ được ghi đúng một lần |

Serialization: Key dùng `StringSerializer`, Value dùng `JacksonJsonSerializer`.

### 7.2 Các Sự Kiện Công Bố

| Lớp sự kiện | Topic | Kích hoạt khi |
|-------------|-------|--------------|
| `UserCreatedEvent` | `user-events` | Đăng ký tài khoản người mua |
| `EmpCreatedEvent` | `emp-create-events` | Đăng ký tài khoản nhân viên |
| `ProviderCreatedEvent` | `provider-create-events` | Đăng ký hoặc liên kết tài khoản nhà cung cấp |

**Cấu trúc `ProviderCreatedEvent`** bổ sung hai trường so với `UserCreatedEvent`:
- `bankId` (enum `Bank`): Mã ngân hàng Việt Nam (14 ngân hàng được hỗ trợ).
- `bankNum` (String): Số tài khoản ngân hàng.
- `isFreshAccount` (boolean): Phân biệt tài khoản tạo mới (`true`) với tài khoản hiện có được liên kết (`false`).

Hành vi công bố là **bất đồng bộ, không chặn** — lỗi Kafka được ghi log nhưng không làm thất bại yêu cầu HTTP của client.

---

## 8. Lưu Trữ Tệp — Cloudflare R2

Dịch vụ tích hợp Cloudflare R2 (tương thích AWS S3 API) qua AWS SDK v2 để lưu trữ ảnh đại diện người dùng.

| Tham số | Giá trị |
|---------|---------|
| Endpoint | `https://499bb5ccc0de718e6ef985931d71a03a.r2.cloudflarestorage.com` |
| Bucket | `back-office-user-avts` |
| Region | `auto` |
| Access mode | Path-style |
| Credentials | `R2_ACCESS_KEY` + `R2_SECRET_KEY` (biến môi trường) |

Quy trình tải lên: client gọi `POST /api/user/upload-avt-img` với `MultipartFile`; dịch vụ tạo tên file duy nhất (`UUID_tênGốc`), tải lên bucket, và trả về tên file. URL công khai được xây dựng từ tên file này và lưu vào trường dữ liệu người dùng.

---

## 9. Khởi Tạo Dữ Liệu Mặc Định (DataSeeder)

Khi khởi động, `DataSeeder` (hiện thực `CommandLineRunner`) kiểm tra bảng `users`. Nếu bảng trống, dịch vụ tự động tạo dữ liệu nền:

**Quyền hạn (6):** `USER_VIEW`, `USER_UPDATE`, `USER_DELETE`, `GROUP_MANAGE`, `PERMISSION_MANAGE`, `PERMISSION_VIEW`.

**Nhóm và người dùng mẫu:**

| Nhóm | Quyền hạn | Tài khoản mẫu |
|------|-----------|--------------|
| ADMIN | Tất cả 6 quyền | `admin@gmail.com` / `admin` |
| BUYER | `USER_VIEW` | `buyer@gmail.com` / `buyer` và 4 tài khoản người mua mẫu |

Liên kết `GroupPermission` được tạo với `validUntil` 10 năm kể từ thời điểm seeding.

---

## 10. Cấu Hình Ứng Dụng

| Tham số | Giá trị |
|---------|---------|
| Cổng dịch vụ | `9000` |
| Tên ứng dụng | `identity_service` |
| Cơ sở dữ liệu | PostgreSQL 17 — `identity_db` |
| Quản lý schema | `hibernate.ddl-auto: update` |
| Kafka bootstrap | `${KAFKA_HOST}:${KAFKA_PORT}` |

**CORS** được cấu hình cho các origin:
- `http://localhost:3000`, `http://localhost:5173`, `http://localhost:5273`
- Một số IP nội bộ của nhóm phát triển

---

## 11. Phụ Thuộc Kỹ Thuật Chính

| Thư viện | Phiên bản | Mục đích |
|----------|-----------|---------|
| Spring Boot | 4.0.1 | Framework ứng dụng |
| Spring Security | (theo Boot) | Xác thực, phân quyền |
| Spring Data JPA / Hibernate | (theo Boot) | ORM, quản lý schema |
| Spring Kafka | (theo Boot) | Kafka producer |
| `io.jsonwebtoken:jjwt-*` | 0.11.5 | Ký và xác minh JWT (RS256) |
| `software.amazon.awssdk:s3` | 2.42.4 | Cloudflare R2 (S3-compatible) |
| PostgreSQL JDBC Driver | (theo Boot) | Kết nối cơ sở dữ liệu |
| Lombok | (theo Boot) | Giảm boilerplate code |
| Java | 25 | Ngôn ngữ lập trình |

---

## 12. Nhận Xét Kiến Trúc

Identity Service đóng vai trò là **security anchor** của toàn hệ thống. Thiết kế sử dụng RSA asymmetric key cho JWT cho phép xác minh token **phi tập trung** — mỗi vi dịch vụ có thể xác minh tính hợp lệ của token bằng public key mà không cần gọi lại Identity Service, từ đó loại bỏ bottleneck xác thực và tăng khả năng chịu lỗi.

Mô hình RBAC ba tầng (User–Group–Permission) với cờ `isActive` và `validUntil` trên các bảng liên kết cho phép quản lý quyền hạn linh hoạt theo thời gian (time-bound permissions) mà không cần thay đổi cấu trúc schema. Đây là điểm mạnh về khả năng mở rộng so với mô hình RBAC tĩnh truyền thống.

Hạn chế hiện tại cần lưu ý: package `exception/` còn trống (chưa triển khai exception handler tập trung), và các service method trả về `null` thay vì ném ngoại lệ có ngữ nghĩa — đây là vùng kỹ thuật nợ (technical debt) cần cải thiện trong các phiên bản tiếp theo.
