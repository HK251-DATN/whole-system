# Kiến Trúc Tổng Quan Hệ Thống (High-Level Architecture)

## Mô Tả Kiến Trúc

Hình 1 trình bày kiến trúc tổng quan của hệ thống thương mại điện tử được xây dựng theo mô hình kiến trúc vi dịch vụ (microservices architecture). Hệ thống được phân tầng rõ ràng thành bốn tầng chính: tầng giao diện người dùng (UI Layer), tầng dịch vụ (Services Layer), tầng dữ liệu (Data Layer) và tầng dịch vụ ngoài (External Services Layer).

---

## Tầng Giao Diện Người Dùng (UI Layer)

Tầng giao diện bao gồm ba ứng dụng web độc lập, phục vụ ba nhóm người dùng khác nhau:

- **Ecommerce UI**: Giao diện dành cho khách hàng cuối (end-users), cho phép người dùng duyệt sản phẩm, quản lý giỏ hàng và thực hiện giao dịch mua sắm trực tuyến.
- **Provider UI**: Cổng thông tin dành cho nhà cung cấp và nông dân/nhà sản xuất, hỗ trợ quản lý sản phẩm, theo dõi đơn hàng và nắm bắt nhu cầu thị trường.
- **Back Office UI**: Giao diện quản trị nội bộ, phục vụ đội ngũ vận hành trong việc quản lý danh mục sản phẩm, tài khoản người dùng và các cấu hình hệ thống.

Toàn bộ ba giao diện này được xây dựng bằng React và Vite, tương tác với hệ thống backend thông qua các REST API được cung cấp bởi tầng dịch vụ phía dưới.

---

## Tầng Dịch Vụ (Services Layer)

Tầng dịch vụ là trung tâm xử lý nghiệp vụ của hệ thống, bao gồm các vi dịch vụ độc lập sau:

- **Ecommerce Service** (cổng 9300): Chịu trách nhiệm xử lý các luồng nghiệp vụ thương mại điện tử như quản lý đơn hàng, giỏ hàng, sự kiện khuyến mãi và giao dịch thanh toán. Dịch vụ này được thiết kế theo mô hình Clean Architecture nhằm tách biệt logic nghiệp vụ khỏi các phụ thuộc kỹ thuật.
- **Back-office Service** (cổng 9100): Quản lý danh mục sản phẩm theo cấu trúc phân cấp ba tầng (danh mục → danh mục con → danh mục chi tiết) và thông tin tổng quát của sản phẩm.
- **Product-storage Service** (cổng 9200): Quản lý kho hàng, công cụ lưu trữ và quy trình xử lý lô hàng (batch processing), bao gồm chuyển đổi đơn vị đo lường từ hàng hóa theo lô sang đơn vị bán lẻ.
- **Identity Service** (cổng 9000): Cung cấp chức năng xác thực và phân quyền người dùng dựa trên chuẩn OAuth2 và JWT.
- **Search & Chat Service** (cổng 5000): Vi dịch vụ dựa trên Python (Flask), cung cấp chức năng tìm kiếm sản phẩm nâng cao và giao diện chatbot hỗ trợ ngôn ngữ tự nhiên. Dịch vụ này hoạt động độc lập với các vi dịch vụ Java còn lại và đóng vai trò là lớp trí tuệ nhân tạo (AI layer) của hệ thống.

### Communication Stack (Tầng Giao Tiếp Nội Bộ)

Các vi dịch vụ giao tiếp với nhau thông qua hai cơ chế bổ sung cho nhau:

- **Apache Kafka**: Hệ thống nhắn tin phân tán hỗ trợ giao tiếp bất đồng bộ (asynchronous messaging) thông qua mô hình publish/subscribe. Kafka được sử dụng để truyền tải các sự kiện miền (domain events) như tạo sản phẩm, cập nhật lô hàng và đặt đơn hàng, đảm bảo tính nhất quán cuối cùng (eventual consistency) giữa các dịch vụ.
- **REST API**: Giao thức giao tiếp đồng bộ (synchronous communication) được sử dụng trong các trường hợp yêu cầu phản hồi tức thời, chẳng hạn như truy vấn thông tin người dùng hoặc xác thực dữ liệu liên dịch vụ.

Sự kết hợp giữa Kafka và REST API phản ánh chiến lược thiết kế hướng sự kiện (event-driven architecture), vừa đảm bảo hiệu năng, vừa tăng khả năng mở rộng và chịu lỗi của hệ thống.

---

## Tầng Dữ Liệu (Data Layer)

Mỗi vi dịch vụ sở hữu một cơ sở dữ liệu độc lập theo nguyên tắc Database-per-Service, đảm bảo tính tự trị và tránh sự phụ thuộc dữ liệu trực tiếp giữa các dịch vụ:

- **Ecommerce DB**: Lưu trữ dữ liệu liên quan đến đơn hàng, giỏ hàng, người mua và các sự kiện bán hàng.
- **Back-office DB**: Lưu trữ danh mục sản phẩm và thông tin sản phẩm tổng quát.
- **Product-storage DB**: Lưu trữ thông tin kho hàng, lô hàng và chi tiết sản phẩm theo đơn vị bán lẻ.
- **Identity DB**: Lưu trữ tài khoản người dùng, thông tin phiên đăng nhập và phân quyền.

Tất cả các cơ sở dữ liệu đều sử dụng PostgreSQL 17 và được quản lý schema thông qua cơ chế Hibernate DDL auto-update.

Ngoài các cơ sở dữ liệu quan hệ, tầng dữ liệu còn tích hợp hai dịch vụ lưu trữ bổ sung:

- **Cloudflare R2**: Dịch vụ lưu trữ đối tượng phân tán (object storage) tương thích S3, được sử dụng để lưu trữ ảnh sản phẩm và ảnh đại diện người dùng.
- **Elasticsearch**: Công cụ tìm kiếm và phân tích toàn văn (full-text search), hỗ trợ chức năng tìm kiếm sản phẩm với hiệu suất cao. Dữ liệu sản phẩm được đánh chỉ mục vào Elasticsearch thông qua tiến trình nạp dữ liệu (data ingestion pipeline) của Search & Chat Service, với chỉ mục `products_index` lưu trữ cả trường văn bản (text fields) lẫn véc-tơ nhúng 384 chiều (384-dimensional dense vectors) phục vụ tìm kiếm ngữ nghĩa.

---

## Search & Chat Service — Kiến Trúc Chi Tiết

Search & Chat Service là thành phần AI chuyên biệt của hệ thống, được triển khai bằng Python với Flask làm framework REST API. Dịch vụ này cung cấp hai nhóm chức năng chính:

### 1. Tìm Kiếm Sản Phẩm Nâng Cao

Dịch vụ hỗ trợ ba chế độ tìm kiếm được xây dựng trên Elasticsearch:

- **BM25 Keyword Search** (`GET /api/search/keyword`): Tìm kiếm toàn văn truyền thống dựa trên thuật toán xếp hạng BM25 (Best Match 25). Trường `product_name` được tăng trọng số (boost 3×) và `tags` (boost 2×) để ưu tiên kết quả phù hợp về tên và nhãn sản phẩm.
- **k-NN Vector Search** (`GET /api/search/vector`): Tìm kiếm ngữ nghĩa (semantic search) dựa trên độ tương đồng cosin (cosine similarity) giữa véc-tơ nhúng của truy vấn và véc-tơ nhúng của sản phẩm trong chỉ mục. Mô hình `all-MiniLM-L6-v2` của SentenceTransformers được sử dụng để tạo ra véc-tơ nhúng 384 chiều, cho phép hệ thống hiểu được ngữ nghĩa tiềm ẩn của truy vấn dù không khớp từ khóa chính xác.
- **Hybrid Search** (`GET /api/search/hybrid`): Kết hợp song song BM25 và k-NN trong một truy vấn Elasticsearch duy nhất với trọng số cân bằng (0.5/0.5), tận dụng ưu điểm của cả hai phương pháp để nâng cao độ chính xác và độ phủ của kết quả tìm kiếm.

### 2. Chatbot Tư Vấn Sản Phẩm

Endpoint `/api/chat` (POST) triển khai luồng xử lý hai bước tích hợp mô hình ngôn ngữ lớn (LLM):

1. **Trích xuất từ khóa** — Tin nhắn người dùng được gửi đến Google Gemini API (`gemini-1.5-flash`) với prompt cấu hình sẵn để trích xuất các từ khóa tìm kiếm sản phẩm có liên quan.
2. **Tìm kiếm & sinh phản hồi** — Từ khóa được dùng để thực hiện hybrid search trên Elasticsearch (top-5 kết quả); kết quả sản phẩm và câu hỏi gốc sau đó được đưa vào Gemini để tạo ra câu trả lời tự nhiên, mang tính tư vấn, bằng tiếng Việt.

### 3. Pipeline Nạp Dữ Liệu (Data Ingestion Pipeline)

Dữ liệu sản phẩm được đồng bộ hóa từ cơ sở dữ liệu PostgreSQL của Ecommerce Service sang Elasticsearch theo quy trình hai bước:

1. `data_cronjob.py` truy vấn PostgreSQL và xuất dữ liệu sản phẩm ra file `product.csv`.
2. `ingest_data.py` đọc file CSV, tạo véc-tơ nhúng cho từng sản phẩm và nạp hàng loạt (bulk index) vào chỉ mục `products_index` trên Elasticsearch.

---

## Tầng Dịch Vụ Ngoài (External Services Layer)

Hệ thống tích hợp với các dịch vụ bên ngoài nhằm mở rộng tính năng:

- **Google APIs**: Cung cấp các dịch vụ xác thực và tiện ích từ hệ sinh thái Google, bao gồm đăng nhập qua tài khoản Google (OAuth2 Social Login).
- **Google Gemini API**: Mô hình ngôn ngữ lớn (LLM) được sử dụng bởi Search & Chat Service để trích xuất từ khóa từ ngôn ngữ tự nhiên và tạo ra các phản hồi tư vấn sản phẩm. Mô hình `gemini-1.5-flash` được lựa chọn vì ưu điểm về tốc độ phản hồi và chi phí vận hành phù hợp với các tác vụ truy vấn thời gian thực.
- **Goong API**: Dịch vụ bản đồ và định vị địa lý dành riêng cho thị trường Việt Nam, được sử dụng để xác thực địa chỉ giao hàng và hỗ trợ tính toán tuyến đường vận chuyển.

---

## Nhận Xét Tổng Quan

Kiến trúc này thể hiện các nguyên tắc thiết kế hiện đại trong phát triển phần mềm phân tán: mỗi vi dịch vụ được triển khai độc lập với cơ sở dữ liệu riêng, giao tiếp thông qua các giao thức chuẩn hóa, và toàn bộ hệ thống được container hóa bằng Docker Compose. Mô hình này mang lại khả năng mở rộng linh hoạt theo chiều ngang (horizontal scaling), tăng tính chịu lỗi và dễ dàng triển khai, bảo trì từng thành phần một cách độc lập mà không ảnh hưởng đến toàn hệ thống.

Sự bổ sung của Search & Chat Service phản ánh xu hướng tích hợp trí tuệ nhân tạo (AI-augmented microservices) vào kiến trúc thương mại điện tử hiện đại. Bằng cách tách biệt logic tìm kiếm ngữ nghĩa và chatbot thành một vi dịch vụ riêng biệt, hệ thống có thể nâng cấp hoặc thay thế mô hình AI độc lập mà không ảnh hưởng đến các dịch vụ nghiệp vụ cốt lõi.
