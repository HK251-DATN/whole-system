# Identity Service

Ứng dụng Identity Service cung cấp quản lý người dùng, nhóm và quyền cùng với bảo mật JWT. Dự án viết bằng Spring Boot (Java) và phù hợp cho việc tích hợp xác thực và phân quyền trong hệ thống phân tán.

## Tính năng chính

- Quản lý User (CRUD)
- Quản lý Group (CRUD)
- Quản lý Permission (CRUD)
- Gán quyền cho nhóm, gán người dùng vào nhóm
- Xác thực và ủy quyền bằng JWT (có hỗ trợ khóa RSA trong `src/main/resources/keys`)

## Cấu trúc thư mục chính

- `src/main/java/.../controller` — Controllers/REST endpoints
- `src/main/java/.../service` — Business logic
- `src/main/java/.../repository` — JPA repositories
- `src/main/java/.../security` — JWT, bộ lọc và cấu hình bảo mật
- `src/main/resources` — cấu hình `application.yaml`, khóa và tài nguyên

## Yêu cầu

- Java 11+ (khuyến nghị Java 17)
- Maven hoặc Gradle
- OpenSSL (tùy chọn, để tạo khóa RSA nếu cần)

## Cài đặt & chạy

1. Kiểm tra cấu hình trong `src/main/resources/application.yaml`.
2. Nếu dùng khóa RSA, đặt `private.pem` và `public.pem` vào `src/main/resources/keys` hoặc cấu hình đường dẫn tương ứng.
   - Tạo khóa bằng OpenSSL (ví dụ):
     - `openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048`
     - `openssl rsa -pubout -in private.pem -out public.pem`
3. Chạy bằng Maven:
   - `mvn clean package`
   - `java -jar target/<artifact>.jar`
   - hoặc `mvn spring-boot:run`
   - Với Gradle: `./gradlew bootRun`
4. Chạy test:
   - `mvn test` hoặc `./gradlew test`

## Endpoints (ví dụ)

- `/api/user` — quản lý người dùng
- `/api/group` — quản lý nhóm
- `/api/permission` — quản lý quyền
  (Chi tiết các endpoint và payload tham khảo trong controller tương ứng: `UserController`, `GroupController`, `PermissionController`.)

## Cấu hình JWT

- Kiểm tra các lớp `JwtTokenGenerator`, `JwtTokenValidator`, `JwtAuthenticationFilter`, và `SecurityConfig`.
- Đảm bảo khóa công khai/riêng tư được cấu hình đúng trong `application.yaml` hoặc đọc từ `resources/keys`.

## Góp ý & Phát triển

- Viết unit test cho service/repository khi thêm logic mới.
- Cân nhắc thêm endpoints quản trị, audit logging, và rate limiting nếu cần.

## Hướng dẫn cài JWT validator (portable) cho các service khác

Để tái sử dụng cơ chế kiểm tra JWT từ Identity Service trong các service khác, làm theo các bước sau:

### Thêm các file mã nguồn (copy từ dự án này) vào package tương ứng trong service nhận

- main/java/edu/hcmut/datn/identity_service/security/portable/AuthenticatedUser.java
- main/java/edu/hcmut/datn/identity_service/security/portable/JwtAuthenticationFilter.java
- main/java/edu/hcmut/datn/identity_service/security/portable/JwtTokenValidator.java
- main/java/edu/hcmut/datn/identity_service/security/portable/SecurityConfig.java

Lưu ý: Điều chỉnh package và cấu hình nếu namespace của service khác.

### Thêm file public.pem (khóa công khai) vào

- `src/main/resources/keys/public.pem`

### Thêm các dependency cần thiết vào pom.xml của service nhận (ví dụ chung, phiên bản tùy thuộc BOM của Spring Boot)

```xml
<!-- pom.xml - dependencies -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Nếu JwtTokenValidator dùng io.jsonwebtoken (jjwt) -->
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <scope>runtime</scope>
</dependency>
```

### Dùng @PreAuthorize (SpEL) trước các mapping để kiểm tra quyền và so sánh id requester

```java
@GetMapping("/{id}")
@PreAuthorize("hasAuthority('USER_VIEW') or #id == principal.id")
public ResponseEntity<?> getById(@PathVariable Long id) { ... }
```

### Có thể inject principal trực tiếp bằng @AuthenticationPrincipal để lấy thông tin requester (id, authorities, ...)

```java
@GetMapping("/me")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<?> me(@AuthenticationPrincipal AuthenticatedUser principal) {
    Long requesterId = principal.getId();
    // truy xuất permissions: principal.getAuthorities()
    // ...existing code...
}
```

### Ghi chú

- Biểu thức SpEL `principal` dùng để truy cập đối tượng principal (ở đây class AuthenticatedUser trong project).
- @AuthenticationPrincipal tiện khi cần dữ liệu requester trong body method thay vì dùng biểu thức SpEL.

## Tài liệu tham khảo

- Method security (EnableMethodSecurity, @PreAuthorize): <https://docs.spring.io/spring-security/reference/servlet/authorization/method-security/>
- Expression-based access control / principal: <https://docs.spring.io/spring-security/reference/servlet/authorization/expression-based.html>
- @AuthenticationPrincipal: <https://docs.spring.io/spring-security/reference/servlet/authentication/principal.html>
