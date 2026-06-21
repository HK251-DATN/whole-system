# Báo cáo Kiến trúc Hệ thống E-Commerce Microservices

**Dự án:** Hệ thống quản lý thương mại điện tử thực phẩm sạch
**Học phần:** Đồ án tốt nghiệp / Công nghệ phần mềm nâng cao
**Ngày lập:** 06/05/2026
**Phiên bản:** 1.0

---

## 1. Tóm tắt (Executive Summary)

Tài liệu này trình bày chi tiết kiến trúc của một nền tảng thương mại điện tử hiện đại, được xây dựng dựa trên mô hình **Microservices**, giao tiếp hướng sự kiện (**Event-Driven Communication**) và các nguyên lý thiết kế sạch (**Clean Architecture**). Hệ thống được thiết kế đặc thù cho việc kinh doanh thực phẩm sạch, kết nối trực tiếp nhà cung cấp (nông dân) với người tiêu dùng.

**Các đặc điểm cốt lõi:**
- Hệ thống bao gồm 4 microservices độc lập: Identity, Back-Office, Product Storage, và Ecommerce.
- Giao tiếp bất đồng bộ thông qua **Apache Kafka**.
- Mỗi dịch vụ sở hữu cơ sở dữ liệu riêng biệt (**Database-per-Service**) sử dụng PostgreSQL.
- Tích hợp các dịch vụ bên ngoài như Cloudflare R2 (lưu trữ đối tượng) và Goong Maps API (định vị tại Việt Nam).
- Triển khai container hóa hoàn toàn bằng **Docker** và **Docker Compose**.

---

## 2. Tổng quan hệ thống (System Overview)

### 2.1 Mục tiêu hệ thống
Hệ thống cung cấp một giải pháp toàn diện cho thương mại điện tử, bao gồm:
- **Khách hàng:** Tìm kiếm, xem sản phẩm theo danh mục và thực hiện mua hàng.
- **Quản trị viên:** Quản lý danh mục sản phẩm 3 cấp, quản lý kho hàng và người dùng.
- **Nhà cung cấp (Nông dân):** Theo dõi nhu cầu thị trường, quản lý lô hàng và xem lịch sử giao dịch.

### 2.2 Các thành phần chính
Hệ thống được chia thành 3 lớp chính:
1.  **Lớp giao diện (Client Layer):** Gồm 3 ứng dụng React (Ecommerce UI, Back-Office UI, Provider UI).
2.  **Lớp dịch vụ (Microservices Layer):** Các dịch vụ backend xử lý logic nghiệp vụ.
3.  **Lớp hạ tầng (Infrastructure Layer):** Kafka, PostgreSQL, Docker, Cloudflare R2.

---

## 3. Các mô hình kiến trúc (Architecture Patterns)

### 3.1 Kiến trúc Microservices
Hệ thống được chia nhỏ thành các dịch vụ độc lập dựa trên nghiệp vụ (Bounded Context):
- **Tính độc lập:** Mỗi dịch vụ có thể được phát triển, triển khai và mở rộng quy mô riêng biệt.
- **Tính đa dạng công nghệ:** Sử dụng các phiên bản Spring Boot (3.5.6 và 4.0.x) và Java (21 và 25) khác nhau tùy thuộc vào yêu cầu cụ thể của từng dịch vụ.

### 3.2 Kiến trúc hướng sự kiện (Event-Driven Architecture)
Sử dụng **Apache Kafka** làm trục xương sống cho việc trao đổi dữ liệu:
- **Giảm sự phụ thuộc (Loose Coupling):** Các dịch vụ không cần biết trực tiếp về nhau. Ví dụ: Khi Back-Office cập nhật sản phẩm, nó chỉ cần gửi một sự kiện lên Kafka, dịch vụ Product Storage sẽ tự động tiêu thụ sự kiện đó để cập nhật kho.
- **Tính nhất quán cuối cùng (Eventual Consistency):** Dữ liệu được đồng bộ hóa dần dần giữa các dịch vụ thông qua các sự kiện tên miền (Domain Events).

### 3.3 Clean Architecture (Kiến trúc sạch)
Riêng dịch vụ **Ecommerce** được triển khai theo mô hình Clean Architecture (Hexagonal):
- Tách biệt hoàn toàn logic nghiệp vụ cốt lõi (Domain) khỏi các khung phần mềm (Spring) và hạ tầng (Database, Kafka).
- Giúp hệ thống dễ dàng kiểm thử và bảo trì khi quy mô nghiệp vụ trở nên phức tạp.

---

## 4. Chi tiết các dịch vụ (Service Details)

### 4.1 Identity Service (Cổng 9000)
- **Chức năng:** Quản lý xác thực và phân quyền người dùng.
- **Công nghệ:** Spring Boot 4.0.1, Java 25, Spring Security, OAuth2.
- **Dữ liệu:** Lưu trữ thông tin tài khoản, phiên làm việc và quyền hạn.

### 4.2 Back-Office Service (Cổng 9100)
- **Chức năng:** Quản lý danh mục sản phẩm theo cấu trúc 3 cấp (Ngành hàng -> Nhóm hàng -> Loại hàng). Quản lý thông tin chung của sản phẩm và hình ảnh.
- **Giao tiếp:** Phát hành các sự kiện `category-events`, `product-general-events`.

### 4.3 Product Storage Service (Cổng 9200)
- **Chức năng:** Quản lý kho hàng, dụng cụ lưu trữ (kệ, tủ lạnh) và xử lý lô hàng (Batch Processing).
- **Logic đặc trưng:** Chuyển đổi từ lô hàng lớn (ví dụ: 10kg rau) sang các đơn vị bán lẻ (ví dụ: 20 gói 500g) dựa trên quy cách đóng gói.
- **Giao tiếp:** Tiêu thụ sự kiện từ Back-Office và phát hành sự kiện `batch-detail-events` cho dịch vụ Ecommerce.

### 4.4 Ecommerce Service (Cổng 9300)
- **Chức năng:** Xử lý đơn hàng, giỏ hàng, chương trình khuyến mãi và đánh giá từ khách hàng.
- **Công nghệ:** Spring Boot 3.5.6, Java 21, Clean Architecture.
- **Tích hợp:** Sử dụng Goong Maps API để chuẩn hóa địa chỉ và tính toán lộ trình giao hàng tại Việt Nam.

---

## 5. Kiến trúc dữ liệu (Data Architecture)

Hệ thống áp dụng mô hình **Database-per-Service** để đảm bảo tính cô lập:

| Dịch vụ | Cơ sở dữ liệu | Nội dung lưu trữ |
| :--- | :--- | :--- |
| Identity | `identity_db` | Tài khoản, thông tin định danh |
| Back-Office | `back_office_db` | Danh mục, thông tin sản phẩm chung |
| Product Storage | `product_storage_db` | Kho hàng, lô hàng, tồn kho thực tế |
| Ecommerce | `ecommerce_db` | Đơn hàng, giỏ hàng, sự kiện giảm giá |

**Công nghệ sử dụng:**
- **Hệ quản trị:** PostgreSQL 17.
- **ORM:** Spring Data JPA / Hibernate giúp tự động quản lý schema và ánh xạ đối tượng.

---

## 6. Luồng sự kiện chính (Event Flows)

Quy trình đồng bộ dữ liệu sản phẩm:
1.  **Quản trị viên** tạo sản phẩm mới tại **Back-Office Service**.
2.  Sự kiện `product-general-events` được đẩy lên **Kafka**.
3.  **Product Storage Service** nhận sự kiện, tạo thực thể sản phẩm tương ứng trong kho.
4.  Khi có lô hàng mới về, **Product Storage** xử lý và phát hành sự kiện `batch-detail-events`.
5.  **Ecommerce Service** nhận sự kiện và cập nhật sản phẩm lên gian hàng trực tuyến để khách hàng có thể mua.
6.  Khi có đơn hàng, **Ecommerce** phát sự kiện `order-item-events` để **Product Storage** trừ tồn kho.

---

## 7. Giao diện người dùng (Frontend)

Hệ thống cung cấp 3 trải nghiệm riêng biệt được xây dựng bằng **React 19** và **Vite**:
- **Ecommerce UI:** Giao diện mua sắm hiện đại cho khách hàng.
- **Back-Office UI:** Công cụ quản lý mạnh mẽ cho Admin với Material-UI.
- **Provider UI:** Giao diện tối giản, sử dụng tiếng Việt và phông chữ lớn, tối ưu cho người nông dân không chuyên về kỹ thuật.

---

## 8. Hạ tầng và Bảo mật (Infrastructure & Security)

### 8.1 Triển khai (Deployment)
Toàn bộ hệ thống được container hóa bằng Docker. File `docker-compose.yml` định nghĩa cách các dịch vụ kết nối với nhau thông qua mạng nội bộ `ecommerce-network`. Cơ chế Health Check đảm bảo các dịch vụ hạ tầng (Database, Kafka) sẵn sàng trước khi khởi động các microservices.

### 8.2 Lưu trữ (Storage)
Sử dụng **Cloudflare R2** (tương thích S3) để lưu trữ hình ảnh sản phẩm và ảnh đại diện người dùng. Điều này giúp giảm tải cho server ứng dụng và tăng tốc độ tải trang thông qua cơ chế CDN.

### 8.3 Bảo mật (Security)
- **Xác thực:** Kết hợp OAuth2 (trong Identity Service) và JWT tùy chỉnh (trong Ecommerce Service).
- **Phân quyền:** Sử dụng Role-Based Access Control (RBAC) để giới hạn quyền truy cập vào các API nhạy cảm.
- **Môi trường:** Các thông tin nhạy cảm (API Key, mật khẩu DB) được quản lý qua file `.env`.

---

## 9. Kết luận và Hướng phát triển

### 9.1 Ưu điểm
- Kiến trúc Microservices giúp hệ thống linh hoạt và dễ mở rộng.
- Việc sử dụng Kafka giúp hệ thống hoạt động mượt mà, không bị nghẽn khi có lượng dữ liệu lớn.
- Áp dụng Clean Architecture giúp đảm bảo chất lượng mã nguồn lâu dài.

### 9.2 Hướng phát triển tương lai
- Triển khai **API Gateway** để quản lý tập trung các luồng yêu cầu từ Frontend.
- Bổ sung hệ thống giám sát tập trung (Prometheus & Grafana) và quản lý log (ELK Stack).
- Chuyển đổi sang triển khai trên **Kubernetes** để tự động hóa việc mở rộng (Auto-scaling) theo tải trọng thực tế.

---
**Người lập báo cáo:** Gemini CLI Agent
**Ngày hoàn tất:** 06/05/2026
