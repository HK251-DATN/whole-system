# KIẾN THỨC NỀN TẢNG

## 1. Kiến trúc phần mềm

### 1.1 Kiến trúc Microservices

Kiến trúc Microservices là một phong cách thiết kế phần mềm trong đó một ứng dụng được xây dựng như một tập hợp các dịch vụ nhỏ, độc lập, mỗi dịch vụ thực thi một tập hợp nghiệp vụ cụ thể và giao tiếp với nhau thông qua các giao thức mạng được định nghĩa rõ ràng (thường là HTTP/REST hoặc message queue). Khái niệm này được Martin Fowler và James Lewis hệ thống hóa vào năm 2014 và ngày càng trở nên phổ biến trong các hệ thống quy mô lớn.

Trái với kiến trúc nguyên khối (monolithic), nơi toàn bộ ứng dụng được triển khai như một đơn vị duy nhất, kiến trúc Microservices cho phép mỗi dịch vụ được phát triển, triển khai và mở rộng một cách độc lập. Điều này mang lại nhiều lợi ích thực tiễn trong môi trường phát triển hiện đại:

- **Khả năng mở rộng độc lập (Independent Scalability):** Mỗi dịch vụ có thể được mở rộng theo chiều ngang (horizontal scaling) dựa trên nhu cầu riêng của nó mà không ảnh hưởng đến toàn bộ hệ thống.
- **Cô lập lỗi (Fault Isolation):** Sự cố xảy ra trong một dịch vụ không nhất thiết làm sập toàn bộ hệ thống, tăng tính sẵn sàng (availability) tổng thể.
- **Tự do công nghệ (Technology Heterogeneity):** Các dịch vụ khác nhau có thể sử dụng ngôn ngữ lập trình, framework và cơ sở dữ liệu phù hợp nhất với bài toán của chúng.
- **Triển khai độc lập (Independent Deployment):** Nhóm phát triển có thể phát hành cập nhật cho một dịch vụ mà không cần triển khai lại toàn bộ hệ thống.

<!-- [Hình minh họa] So sánh kiến trúc Monolithic và Microservices: bên trái là khối đơn lớn; bên phải là các hình chữ nhật nhỏ rời nhau, nối bằng mũi tên qua API Gateway -->

Hệ thống trong đề tài này được tổ chức thành bốn microservice độc lập: **identity-service** (cổng 9000) xử lý xác thực và phân quyền người dùng, **back-office-service** (cổng 9100) quản lý danh mục và thông tin sản phẩm, **product-storage-service** (cổng 9200) quản lý kho hàng và tồn kho, và **ecommerce-service** (cổng 9300) xử lý đơn hàng và giao dịch mua bán. Ngoài ra còn có **recommend-service** (cổng 5000) cung cấp chức năng tìm kiếm thông minh và chatbot, được triển khai bằng Python/Flask tách biệt hoàn toàn với các dịch vụ Java.

### 1.2 Kiến trúc hướng sự kiện (Event-Driven Architecture)

Kiến trúc hướng sự kiện (Event-Driven Architecture - EDA) là một mô hình thiết kế hệ thống phân tán trong đó các thành phần giao tiếp với nhau thông qua việc phát và nhận các sự kiện (events). Một sự kiện là một bản ghi về một điều đã xảy ra trong hệ thống, chẳng hạn như "đơn hàng được tạo" hay "sản phẩm được cập nhật". Thay vì các dịch vụ gọi trực tiếp vào nhau (synchronous call), chúng gửi sự kiện đến một hàng đợi thông điệp trung gian và tiêu thụ sự kiện từ đó theo cách bất đồng bộ (asynchronous).

<!-- [Hình minh họa] Luồng sự kiện trong hệ thống: Back-Office Service phát sự kiện "product-general-events" → Apache Kafka (broker) → Product Storage Service tiêu thụ → phát "batch-detail-events" → Ecommerce Service tiêu thụ -->

Mô hình này đem lại khả năng tách rời (decoupling) cao giữa các dịch vụ: dịch vụ phát sự kiện không cần biết ai sẽ tiêu thụ, và dịch vụ tiêu thụ không cần biết ai đã phát. Điều này làm cho hệ thống dễ mở rộng và bảo trì hơn đáng kể trong bối cảnh kiến trúc Microservices.

### 1.3 Kiến trúc sạch (Clean Architecture)

Kiến trúc sạch (Clean Architecture), được đề xuất bởi Robert C. Martin (Uncle Bob) vào năm 2012, là một triết lý thiết kế phần mềm nhằm tách biệt rõ ràng các mối quan tâm (separation of concerns) trong một hệ thống phần mềm. Nguyên tắc cốt lõi là quy tắc phụ thuộc (Dependency Rule): các thành phần ở tầng trong không được phép phụ thuộc vào bất kỳ thành phần nào ở tầng ngoài.

<!-- [Hình minh họa] Sơ đồ đồng tâm của Clean Architecture: vòng trong cùng là Entities, tiếp theo là Use Cases, tiếp theo là Interface Adapters, ngoài cùng là Frameworks & Drivers -->

Kiến trúc này tổ chức mã nguồn thành các tầng đồng tâm:

- **Domain (Entities & Use Cases):** Chứa toàn bộ logic nghiệp vụ thuần túy, không phụ thuộc vào bất kỳ framework hay công nghệ cụ thể nào. Đây là phần ổn định nhất của hệ thống.
- **Persistence:** Tầng truy cập dữ liệu, chứa các repository JPA và DTO dùng để ánh xạ giữa domain entity và bảng cơ sở dữ liệu.
- **Infrastructure:** Tầng tích hợp với các framework bên ngoài như Spring, Kafka, cấu hình bảo mật.
- **Presentation:** Tầng giao tiếp với bên ngoài, gồm các REST controller, request/response model và mapping logic.

Trong hệ thống này, **ecommerce-service** được triển khai theo Clean Architecture, trong khi ba dịch vụ còn lại sử dụng kiến trúc phân tầng truyền thống (Layered Architecture) với các tầng controller, service, repository và DAO.

---

## 2. Công nghệ Front-end

### 2.1 React

React là một thư viện JavaScript mã nguồn mở được Facebook (nay là Meta) phát triển lần đầu vào năm 2013, được sử dụng để xây dựng giao diện người dùng (User Interface) dưới dạng các thành phần tái sử dụng (reusable components). React hoạt động dựa trên mô hình khai báo (declarative paradigm): thay vì mô tả từng bước thao tác DOM, lập trình viên mô tả trạng thái mong muốn của giao diện và React sẽ tự động cập nhật DOM hiệu quả khi trạng thái thay đổi.

Cơ chế quan trọng nhất của React là **Virtual DOM**: React duy trì một bản sao DOM ảo trong bộ nhớ, so sánh (diffing) nó với trạng thái trước đó mỗi khi có thay đổi, và chỉ cập nhật những phần thực sự thay đổi lên DOM thật (reconciliation). Điều này giúp tối ưu hóa hiệu năng đáng kể so với việc thao tác DOM trực tiếp.

<!-- [Hình minh họa] Quy trình Virtual DOM: State thay đổi → React tạo Virtual DOM mới → So sánh (diff) với Virtual DOM cũ → Chỉ cập nhật phần thay đổi lên Real DOM -->

React giới thiệu khái niệm **React Hooks** từ phiên bản 16.8, cho phép quản lý state và lifecycle trong các function component mà không cần class component:

- `useState`: Quản lý trạng thái cục bộ của component.
- `useEffect`: Xử lý side effects như gọi API, đăng ký sự kiện.
- `useContext`: Truy cập dữ liệu dùng chung mà không cần prop drilling.
- `useMemo`, `useCallback`: Tối ưu hóa hiệu năng thông qua memoization.

Hệ thống sử dụng React 19 cho cả ba ứng dụng frontend: **ecommerce-ui** (giao diện khách hàng), **back-office-ui** (giao diện quản trị nội bộ) và **provider-ui** (cổng thông tin nhà cung cấp).

### 2.2 Vite

Vite là một công cụ xây dựng (build tool) thế hệ mới cho các ứng dụng web hiện đại, được Evan You (tác giả Vue.js) phát triển vào năm 2020. Điểm khác biệt cốt lõi của Vite so với các công cụ truyền thống như Webpack là kiến trúc hai giai đoạn: trong môi trường phát triển, Vite sử dụng trực tiếp ES Modules gốc của trình duyệt thay vì đóng gói (bundle) toàn bộ mã nguồn, từ đó đạt được thời gian khởi động server gần như tức thì; trong môi trường sản xuất, Vite sử dụng Rollup để tạo ra các gói (bundle) được tối ưu hóa.

<!-- [Hình minh họa] So sánh luồng phát triển: Webpack (bundle toàn bộ → Dev Server) so với Vite (Dev Server trước → chỉ biên dịch module khi được yêu cầu) -->

Vite còn cung cấp tính năng **Hot Module Replacement (HMR)** cực nhanh: khi lập trình viên sửa một file, chỉ module đó được cập nhật mà không cần tải lại toàn bộ trang, giúp phản hồi ngay lập tức trong quá trình phát triển.

### 2.3 TypeScript

TypeScript là ngôn ngữ lập trình mã nguồn mở do Microsoft phát triển, là tập cha (superset) của JavaScript với hệ thống kiểu tĩnh (static type system). TypeScript được biên dịch (transpile) sang JavaScript trước khi chạy, do đó tương thích hoàn toàn với mọi môi trường chạy JavaScript.

Hệ thống kiểu tĩnh của TypeScript cho phép phát hiện lỗi ngay tại thời điểm viết mã (compile-time) thay vì chỉ phát hiện khi chạy (runtime). Điều này đặc biệt có giá trị trong các dự án quy mô lớn với nhiều thành viên tham gia, nơi khả năng tái cấu trúc mã (refactoring) an toàn và tính tự tài liệu hóa (self-documentation) của mã là yêu cầu thiết yếu.

### 2.4 Thư viện quản lý trạng thái và giao diện

#### 2.4.1 Redux Toolkit và TanStack Query

**Redux Toolkit** là bộ công cụ chính thức được khuyến nghị để sử dụng Redux, giải quyết ba vấn đề phổ biến khi sử dụng Redux thuần: cấu hình phức tạp, quá nhiều boilerplate code, và phải cài đặt thêm nhiều thư viện. Redux Toolkit cung cấp các tiện ích như `createSlice` (tạo reducer và action cùng lúc) và `createAsyncThunk` (xử lý bất đồng bộ).

**TanStack Query** (trước đây là React Query) là thư viện quản lý trạng thái phía máy chủ (server state), xử lý toàn bộ vòng đời của dữ liệu từ API: fetching, caching, synchronization, và background updates. TanStack Query phân biệt rõ ràng giữa "client state" (trạng thái UI như modal mở/đóng) và "server state" (dữ liệu từ API), từ đó tránh được sự phức tạp không cần thiết khi cố gắng quản lý cả hai bằng một công cụ duy nhất.

#### 2.4.2 Zustand

Zustand là một thư viện quản lý trạng thái toàn cục (global state management) cho React, được thiết kế theo triết lý tối giản: API đơn giản, không yêu cầu provider wrapper hay boilerplate phức tạp. Zustand sử dụng hook để truy cập và cập nhật store, đồng thời hỗ trợ tốt cho TypeScript. Thư viện này được sử dụng trong **provider-ui** để quản lý trạng thái ứng dụng của cổng thông tin nhà cung cấp.

#### 2.4.3 Material-UI và Ant Design

**Material-UI (MUI)** là thư viện component React triển khai hệ thống thiết kế Material Design của Google, cung cấp hàng trăm component sẵn có như button, table, dialog, form. **Ant Design** là thư viện component React được Alibaba phát triển, được sử dụng rộng rãi trong các ứng dụng doanh nghiệp với bộ component phong phú hướng đến nghiệp vụ. Hệ thống sử dụng kết hợp cả hai thư viện trong **back-office-ui** và **provider-ui** để xây dựng giao diện quản trị.

---

## 3. Công nghệ Back-end

### 3.1 Spring Framework và Spring Boot

Spring Framework là một framework Java toàn diện được phát triển bởi Rod Johnson vào năm 2003, cung cấp cơ sở hạ tầng cho việc phát triển các ứng dụng Java doanh nghiệp. Nền tảng của Spring xoay quanh hai nguyên lý cốt lõi:

**Inversion of Control (IoC):** Thay vì các đối tượng tự tạo ra và quản lý các phụ thuộc của mình, quyền kiểm soát được chuyển giao cho container Spring. Container này chịu trách nhiệm khởi tạo, cấu hình và lắp ráp các đối tượng (được gọi là "bean") theo cấu hình được khai báo.

**Dependency Injection (DI):** Là cơ chế cụ thể mà Spring sử dụng để thực hiện IoC. Các phụ thuộc được "tiêm" vào đối tượng thông qua constructor, setter, hoặc annotation `@Autowired`, thay vì đối tượng tự tạo ra chúng bằng từ khóa `new`.

<!-- [Hình minh họa] Sơ đồ Spring IoC Container: các Bean definitions (XML/Annotations) → IoC Container → Fully configured application -->

**Spring Boot** là module mở rộng của Spring Framework, được thiết kế để tối thiểu hóa cấu hình và khởi động ứng dụng nhanh chóng. Spring Boot đạt được điều này thông qua cơ chế **auto-configuration**: dựa trên các thư viện có trong classpath, Spring Boot tự động cấu hình các bean cần thiết. Ví dụ, nếu classpath có thư viện PostgreSQL JDBC, Spring Boot sẽ tự động cấu hình DataSource.

Hệ thống trong đề tài sử dụng Spring Boot 4.x (Java 25) cho ba dịch vụ identity, back-office và product-storage, và Spring Boot 3.5.6 (Java 21) cho ecommerce-service.

### 3.2 RESTful API

REST (Representational State Transfer) là một phong cách kiến trúc cho các hệ thống phân tán được Roy Fielding đề xuất trong luận án tiến sĩ năm 2000. Một API được gọi là RESTful khi tuân thủ các ràng buộc kiến trúc của REST, trong đó quan trọng nhất là:

- **Giao diện đồng nhất (Uniform Interface):** Tài nguyên được xác định thông qua URI; tương tác với tài nguyên thông qua các phương thức HTTP chuẩn (GET, POST, PUT, DELETE).
- **Phi trạng thái (Stateless):** Mỗi yêu cầu từ client đến server phải chứa đầy đủ thông tin để server hiểu và xử lý; server không lưu trạng thái phiên của client giữa các yêu cầu.
- **Có thể cache (Cacheable):** Phản hồi của server phải khai báo rõ có thể cache hay không, giúp cải thiện hiệu năng.
- **Kiến trúc phân tầng (Layered System):** Client không cần biết nó đang giao tiếp trực tiếp với server hay qua một trung gian (proxy, load balancer).

<!-- [Hình minh họa] Sơ đồ luồng REST API: Client gửi HTTP Request (Method + URI + Headers + Body) → Server xử lý → Server trả HTTP Response (Status Code + Body JSON) -->

Tất cả bốn microservice trong hệ thống đều cung cấp RESTful API với định dạng JSON, tuân thủ các quy ước thống nhất: phân trang 0-indexed qua tham số `pageNum` và `pageSize`, mã trạng thái HTTP chuẩn, và cấu trúc endpoint theo dạng `/api/{resource}/{id}`.

### 3.3 Bảo mật: OAuth2, JWT và Spring Security

#### 3.3.1 OAuth2

OAuth 2.0 là một framework ủy quyền (authorization framework) tiêu chuẩn công nghiệp được định nghĩa trong RFC 6749, cho phép ứng dụng bên thứ ba (third-party) truy cập tài nguyên được bảo vệ thay mặt người dùng mà không cần chia sẻ thông tin xác thực (credentials). OAuth 2.0 định nghĩa bốn luồng ủy quyền (grant types) cho các tình huống khác nhau; phổ biến nhất trong ứng dụng web là Authorization Code Flow.

<!-- [Hình minh họa] Luồng OAuth2 Authorization Code: User → Client App → Authorization Server (đăng nhập, đồng ý) → Authorization Code → Client App → Access Token → Resource Server → Dữ liệu được bảo vệ -->

Trong hệ thống, **identity-service** triển khai vai trò Authorization Server sử dụng Spring Security OAuth2, hỗ trợ đăng nhập qua Google (Social Login) cũng như đăng nhập bằng email/mật khẩu nội bộ.

#### 3.3.2 JSON Web Token (JWT)

JSON Web Token (JWT) là một tiêu chuẩn mở (RFC 7519) định nghĩa một cách gọn nhẹ và tự chứa (self-contained) để truyền thông tin an toàn giữa các bên dưới dạng JSON. Một JWT bao gồm ba phần được mã hóa Base64URL và ngăn cách nhau bởi dấu chấm:

- **Header:** Chứa loại token (JWT) và thuật toán ký (HMAC SHA256 hoặc RSA).
- **Payload:** Chứa các claims - các tuyên bố về đối tượng (subject) và metadata bổ sung như thời gian phát hành, thời gian hết hạn, vai trò người dùng.
- **Signature:** Được tạo ra bằng cách ký header và payload với secret key, đảm bảo tính toàn vẹn của token.

<!-- [Hình minh họa] Cấu trúc JWT: ba phần Header.Payload.Signature được tô màu khác nhau, bên dưới là nội dung JSON tương ứng sau khi giải mã -->

Trong hệ thống, **ecommerce-service** sử dụng JWT thông qua custom filter bảo mật để xác thực người dùng, trong khi ba dịch vụ còn lại sử dụng OAuth2 từ identity-service.

#### 3.3.3 Spring Security

Spring Security là module bảo mật toàn diện của Spring, cung cấp hai chức năng chính: **Authentication** (xác thực - ai đang gọi?) và **Authorization** (phân quyền - họ được phép làm gì?). Spring Security tích hợp sâu với Spring MVC thông qua cơ chế Filter Chain: mỗi HTTP request được đi qua một chuỗi các filter bảo mật trước khi đến controller, cho phép kiểm tra token, phân quyền và xử lý lỗi một cách tập trung.

### 3.4 JPA và Spring Data JPA

Java Persistence API (JPA) là đặc tả Java định nghĩa cách ánh xạ (mapping) giữa các đối tượng Java (POJO) và bảng trong cơ sở dữ liệu quan hệ, còn gọi là ORM (Object-Relational Mapping). Hibernate là implemention JPA phổ biến nhất, được Spring Boot mặc định sử dụng.

**Spring Data JPA** xây dựng thêm một lớp trừu tượng hóa trên JPA, cho phép khai báo các phương thức truy vấn trong interface mà không cần viết SQL hay JPQL. Ví dụ, khai báo `findByEmailAndStatus(String email, Status status)` sẽ được Spring Data tự động tạo ra câu truy vấn tương ứng tại runtime.

<!-- [Hình minh họa] Chuỗi trừu tượng hóa: Java Object ↔ JPA/Hibernate ↔ SQL ↔ PostgreSQL Database -->

---

## 4. Hệ quản trị cơ sở dữ liệu

### 4.1 PostgreSQL

PostgreSQL là hệ quản trị cơ sở dữ liệu quan hệ-đối tượng (Object-Relational Database Management System - ORDBMS) mã nguồn mở, được phát triển từ dự án POSTGRES tại Đại học California, Berkeley từ năm 1986. PostgreSQL được biết đến là một trong những hệ quản trị cơ sở dữ liệu đáng tin cậy, mạnh mẽ và có bộ tính năng phong phú nhất hiện nay.

PostgreSQL hỗ trợ đầy đủ các đặc tính ACID (Atomicity, Consistency, Isolation, Durability) đảm bảo tính toàn vẹn dữ liệu trong các giao dịch:

- **Atomicity (Tính nguyên tử):** Một giao dịch hoặc hoàn thành toàn bộ hoặc không có gì thay đổi.
- **Consistency (Tính nhất quán):** Dữ liệu luôn duy trì trạng thái hợp lệ trước và sau giao dịch.
- **Isolation (Tính cô lập):** Các giao dịch đồng thời không can thiệp lẫn nhau.
- **Durability (Tính bền vững):** Một giao dịch đã commit sẽ không bị mất ngay cả khi hệ thống gặp sự cố.

<!-- [Hình minh họa] Sơ đồ bốn cơ sở dữ liệu độc lập: identity_db, back_office_db, product_storage_db, ecommerce_db, mỗi cái tương ứng với một microservice -->

Hệ thống sử dụng PostgreSQL 17 với bốn database độc lập, mỗi database phục vụ một microservice riêng theo nguyên tắc "database per service" của kiến trúc Microservices, đảm bảo tính tự chủ và cô lập dữ liệu giữa các dịch vụ.

### 4.2 Elasticsearch

Elasticsearch là một công cụ tìm kiếm và phân tích (search and analytics engine) phân tán, mã nguồn mở, được xây dựng trên nền tảng Apache Lucene. Elasticsearch cho phép lưu trữ, tìm kiếm và phân tích lượng dữ liệu lớn theo thời gian gần thực (near real-time). Dữ liệu trong Elasticsearch được tổ chức theo mô hình document-oriented: thay vì bảng và hàng như trong cơ sở dữ liệu quan hệ, dữ liệu được lưu dưới dạng tài liệu JSON trong các index.

<!-- [Hình minh họa] Kiến trúc Elasticsearch: Documents → Index → Shards (phân tán qua các Node) → Cluster -->

Elasticsearch hỗ trợ hai phương thức tìm kiếm quan trọng được sử dụng trong hệ thống này:

**BM25 (Best Matching 25):** Là thuật toán xếp hạng tài liệu theo mức độ liên quan (relevance ranking) dựa trên tần số xuất hiện của từ khóa (term frequency) và nghịch đảo tần số tài liệu (inverse document frequency). BM25 là thuật toán mặc định của Elasticsearch và đặc biệt hiệu quả cho tìm kiếm dựa trên từ khóa chính xác.

**Tìm kiếm vector k-NN (k-Nearest Neighbors):** Elasticsearch hỗ trợ lưu trữ và tìm kiếm vector nhúng (embedding vectors) thông qua cấu trúc dữ liệu đặc biệt (HNSW - Hierarchical Navigable Small World). Tìm kiếm k-NN tìm ra k tài liệu có vector gần nhất với vector truy vấn theo khoảng cách cosine, cho phép tìm kiếm dựa trên ngữ nghĩa (semantic search) thay vì từ khóa đơn thuần.

---

## 5. Hệ thống thông điệp: Apache Kafka

Apache Kafka là một nền tảng luồng sự kiện phân tán (distributed event streaming platform) được LinkedIn phát triển và trao cho Apache Software Foundation vào năm 2011. Kafka được thiết kế để xử lý hàng triệu sự kiện mỗi giây với độ trễ thấp và độ bền cao, phù hợp làm xương sống giao tiếp trong các hệ thống Microservices lớn.

Các khái niệm cốt lõi của Kafka:

- **Topic:** Là kênh phân loại các thông điệp. Producer ghi thông điệp vào topic, Consumer đọc từ topic.
- **Partition:** Mỗi topic được chia thành nhiều partition cho phép xử lý song song và mở rộng theo chiều ngang. Trong hệ thống này, mỗi topic sử dụng 3 partition.
- **Broker:** Là máy chủ Kafka lưu trữ dữ liệu và phục vụ các yêu cầu đọc/ghi.
- **Producer:** Thành phần ghi (publish) thông điệp vào Kafka topic.
- **Consumer Group:** Nhóm các consumer phối hợp cùng nhau để đọc từ một topic, mỗi partition chỉ được đọc bởi một consumer trong nhóm tại một thời điểm.
- **Offset:** Vị trí của một thông điệp trong partition, cho phép consumer theo dõi tiến độ đọc và khôi phục sau sự cố.

<!-- [Hình minh họa] Kafka Cluster: Producers → Topics (phân thành Partitions) → Kafka Brokers → Consumer Groups, với mũi tên chỉ hướng luồng dữ liệu -->

Điểm nổi bật của Kafka so với các message queue truyền thống (như RabbitMQ) là khả năng lưu trữ bền vững (persistent log): thông điệp được lưu trên đĩa và không bị xóa ngay sau khi consumer đọc, cho phép các consumer khác nhau (hoặc consumer phục hồi sau sự cố) đọc lại cùng thông điệp trong khoảng thời gian retention được cấu hình.

Trong hệ thống, Kafka phiên bản 4.2.0 đóng vai trò trung tâm điều phối các sự kiện: back-office-service phát sự kiện khi tạo/cập nhật sản phẩm và danh mục, product-storage-service tiêu thụ các sự kiện này và phát lại sự kiện batch-detail khi hoàn tất xử lý tồn kho, ecommerce-service tiêu thụ để cập nhật danh sách sản phẩm có thể bán.

---

## 6. Lưu trữ đối tượng: Cloudflare R2

Cloudflare R2 là dịch vụ lưu trữ đối tượng (object storage) của Cloudflare, tương thích với giao thức S3 API của Amazon Web Services. Object storage là mô hình lưu trữ dữ liệu không có cấu trúc thư mục phân cấp như filesystem truyền thống, thay vào đó mỗi tệp (object) được định danh bởi một khóa (key) duy nhất trong một bucket. Mô hình này đặc biệt phù hợp để lưu trữ hình ảnh, video, tệp tĩnh quy mô lớn.

Điểm khác biệt nổi bật của Cloudflare R2 so với Amazon S3 là chính sách **zero egress fee**: người dùng không phải trả phí khi tải dữ liệu ra khỏi R2, trong khi AWS S3 tính phí truyền dữ liệu ra Internet theo dung lượng.

<!-- [Hình minh họa] Luồng tải ảnh: Client upload → Microservice → R2 SDK (S3-compatible) → Cloudflare R2 Bucket → Public URL → Client hiển thị ảnh -->

Trong hệ thống, R2 được sử dụng để lưu trữ ảnh đại diện người dùng (bucket `back-office-user-avts`) và ảnh sản phẩm (bucket `product-general-img`), truy cập thông qua AWS SDK for Java với endpoint tùy chỉnh trỏ đến tài khoản Cloudflare R2.

---

## 7. Triển khai: Docker và Docker Compose

### 7.1 Docker và Container

Docker là nền tảng mã nguồn mở cho phép đóng gói ứng dụng và tất cả các phụ thuộc của nó vào một đơn vị tiêu chuẩn gọi là **container**. Container chia sẻ nhân (kernel) của hệ điều hành máy chủ nhưng chạy trong không gian người dùng (user space) cô lập, khác với máy ảo (Virtual Machine) phải chạy toàn bộ hệ điều hành khách.

<!-- [Hình minh họa] So sánh VM và Container: VM có Guest OS đầy đủ trên Hypervisor; Container chia sẻ Host OS, chỉ có application layer riêng biệt → Container nhẹ hơn và khởi động nhanh hơn -->

Một **Docker image** là bản thiết kế chỉ đọc (read-only blueprint) chứa mọi thứ cần thiết để chạy ứng dụng: mã nguồn đã biên dịch, runtime, thư viện, biến môi trường và file cấu hình. Image được xây dựng từ **Dockerfile** - tệp văn bản chứa các lệnh để tạo image theo từng lớp (layer). Mỗi lệnh trong Dockerfile tạo ra một layer mới, và Docker cache lại các layer, chỉ xây dựng lại những layer thay đổi, giúp tối ưu thời gian build.

### 7.2 Docker Compose

Docker Compose là công cụ cho phép định nghĩa và chạy các ứng dụng Docker nhiều container thông qua một tệp YAML duy nhất (`docker-compose.yml`). Thay vì khởi động từng container riêng lẻ với các cờ cấu hình phức tạp, Docker Compose cho phép khai báo toàn bộ kiến trúc ứng dụng - bao gồm các dịch vụ, mạng nội bộ, volume dữ liệu bền vững và biến môi trường - trong một file duy nhất.

<!-- [Hình minh họa] Sơ đồ docker-compose: file YAML → docker-compose up → nhiều container (PostgreSQL, Kafka, 4 microservices) chạy trên cùng một Docker network, giao tiếp qua tên service -->

Hệ thống sử dụng Docker Compose để orchestrate toàn bộ hạ tầng: PostgreSQL (1 instance, 4 database), Kafka, Kafka UI, và bốn microservice Java. Các dịch vụ trong Docker network giao tiếp với nhau qua tên service (service discovery nội bộ), ví dụ `identity-service`, `postgres`, `kafka` thay vì địa chỉ IP.

---

## 8. Tìm kiếm thông minh và Trí tuệ nhân tạo

### 8.1 Vector Embedding và Semantic Search

Vector embedding là kỹ thuật biểu diễn dữ liệu phi cấu trúc (văn bản, hình ảnh, âm thanh) dưới dạng các vector số thực trong không gian nhiều chiều, sao cho các đối tượng có ngữ nghĩa tương đồng có vector gần nhau theo khoảng cách Euclidean hoặc cosine similarity. Quá trình này được thực hiện bởi các mô hình học sâu (deep learning models) được huấn luyện trên lượng dữ liệu lớn.

<!-- [Hình minh họa] Không gian embedding 2D (giản lược): các từ/câu liên quan đến "cà chua" nằm gần nhau, các từ liên quan đến "rau củ" nằm gần nhau, hai cụm cách xa nhau nhưng cùng vùng "thực phẩm" -->

**Semantic search** (tìm kiếm ngữ nghĩa) là phương pháp tìm kiếm dựa trên ý nghĩa thực sự của truy vấn, thay vì chỉ khớp từ khóa chính xác. Ví dụ, truy vấn "rau xanh tươi" có thể trả về kết quả "cải bắp", "bông cải xanh" dù không có từ nào khớp chính xác.

Trong hệ thống, **recommend-service** sử dụng mô hình **SentenceTransformer** (cụ thể là `all-MiniLM-L6-v2`) để chuyển đổi tên và mô tả sản phẩm thành vector 384 chiều. Các vector này được lưu vào Elasticsearch và phục vụ tìm kiếm k-NN.

### 8.2 Hybrid Search

Hybrid search là kỹ thuật kết hợp tìm kiếm từ khóa (keyword search - thông qua BM25) và tìm kiếm vector ngữ nghĩa (semantic search - thông qua k-NN) trong một truy vấn duy nhất, nhằm tận dụng ưu điểm của cả hai phương pháp:

- **BM25** cho kết quả chính xác cao khi người dùng biết rõ tên sản phẩm (ví dụ: "cà chua bi").
- **K-NN vector search** bù đắp khi người dùng dùng từ đồng nghĩa hoặc mô tả ngữ cảnh (ví dụ: "trái cây màu đỏ nhỏ dùng làm salad").

<!-- [Hình minh họa] Sơ đồ Hybrid Search: Query → [song song] BM25 Scorer và k-NN Vector Scorer → Kết hợp điểm số (Reciprocal Rank Fusion hoặc linear combination) → Ranked Results -->

Elasticsearch hỗ trợ hybrid search thông qua việc kết hợp `query` (BM25) và `knn` trong cùng một request body, với tham số `boost` kiểm soát tỷ trọng của mỗi phương pháp.

### 8.3 Mô hình ngôn ngữ lớn (Large Language Model) và Chatbot

Mô hình ngôn ngữ lớn (Large Language Model - LLM) là các mô hình học sâu được huấn luyện trên tập dữ liệu văn bản khổng lồ với hàng tỷ tham số, có khả năng sinh văn bản tự nhiên, trả lời câu hỏi, tóm tắt, dịch thuật và thực hiện nhiều tác vụ ngôn ngữ khác. Kiến trúc nền tảng của hầu hết LLM hiện đại là **Transformer**, được giới thiệu trong bài báo "Attention Is All You Need" (Vaswani et al., 2017), sử dụng cơ chế self-attention để nắm bắt mối quan hệ ngữ cảnh giữa các từ.

<!-- [Hình minh họa] Kiến trúc tổng quát của chatbot tích hợp LLM: User Input → Trích xuất từ khóa (LLM) → Hybrid Search (Elasticsearch) → Kết quả sản phẩm → Sinh phản hồi tự nhiên (LLM) → User -->

**Google Gemini** là họ mô hình ngôn ngữ lớn đa phương thức (multimodal) của Google DeepMind, được phát hành năm 2023, có khả năng xử lý và sinh ra văn bản, hình ảnh, âm thanh và video. Trong recommend-service, Gemini được sử dụng qua API để thực hiện hai nhiệm vụ: trích xuất từ khóa có ý nghĩa từ câu hỏi người dùng, và sinh ra phản hồi ngôn ngữ tự nhiên giới thiệu sản phẩm dựa trên kết quả tìm kiếm.

---

## 9. Thanh toán: Tích hợp QR SePay

SePay là cổng kết nối thanh toán qua mã QR cho phép thu tiền qua chuyển khoản ngân hàng liên ngân hàng (NAPAS). Quy trình hoạt động dựa trên ba thành phần: sinh mã QR với nội dung chuyển khoản mã hóa (số tài khoản, số tiền, nội dung), xác nhận giao dịch thông qua webhook hoặc polling API của SePay, và cập nhật trạng thái đơn hàng trong hệ thống.

<!-- [Hình minh họa] Luồng thanh toán QR: Người dùng đặt hàng → Hệ thống tạo QR SePay → Người dùng quét QR bằng app ngân hàng → Chuyển tiền → SePay nhận giao dịch → Hệ thống polling API → Phát hiện giao dịch khớp → Cập nhật đơn hàng PAID -->

Trong hệ thống, **ecommerce-service** tạo URL hình ảnh QR qua API của SePay với các tham số: mã tài khoản (`acc`), ngân hàng (`bank`), số tiền và nội dung chuyển khoản có định dạng chuẩn hóa (ví dụ: `DH00012345`). **PaymentPollingService** thực hiện polling định kỳ để kiểm tra giao dịch đã được thực hiện chưa và cập nhật trạng thái đơn hàng tương ứng.

---

## 10. Bản đồ và Định vị: Goong Maps API

Goong Maps là nền tảng bản đồ và định vị của Việt Nam, cung cấp dữ liệu bản đồ và API địa chỉ được tối ưu hóa cho thị trường Việt Nam với độ phủ và độ chính xác cao hơn so với các dịch vụ quốc tế cho địa danh và tên đường trong nước.

Goong cung cấp các API quan trọng được sử dụng trong hệ thống thương mại điện tử:

- **Geocoding API:** Chuyển đổi địa chỉ văn bản (ví dụ: "268 Lý Thường Kiệt, Phường 14, Quận 10, TP.HCM") thành tọa độ địa lý (latitude, longitude).
- **Place Autocomplete API:** Gợi ý địa chỉ khi người dùng nhập liệu, tương tự chức năng tìm kiếm địa điểm trên Google Maps.
- **Distance Matrix API:** Tính toán khoảng cách và thời gian di chuyển giữa nhiều điểm xuất phát và điểm đến, hỗ trợ tối ưu hóa lộ trình giao hàng.

<!-- [Hình minh họa] Luồng sử dụng Goong API: Người dùng nhập địa chỉ → Autocomplete API gợi ý → Người dùng chọn → Geocoding API → Tọa độ lưu vào DB → Distance Matrix API tính phí vận chuyển và thời gian giao hàng -->

Trong **ecommerce-service**, Goong API được tích hợp để hỗ trợ người mua nhập và xác thực địa chỉ giao hàng, đồng thời tính toán phí và thời gian vận chuyển dự kiến dựa trên khoảng cách thực tế từ kho hàng đến địa chỉ nhận.
