# Tài Liệu Kỹ Thuật: Search-Chat Service

**Hệ thống**: Nền tảng thương mại điện tử vi dịch vụ  
**Phiên bản tài liệu**: 1.0  
**Ngày**: 06/05/2026

---

## Mục Lục

1. [Tổng Quan](#1-tổng-quan)
2. [Cấu Trúc Package](#2-cấu-trúc-package)
3. [Mô Hình Dữ Liệu](#3-mô-hình-dữ-liệu)
4. [Tầng Nghiệp Vụ (Service Layer)](#4-tầng-nghiệp-vụ-service-layer)
5. [Tầng Trình Bày — REST API](#5-tầng-trình-bày--rest-api)
6. [Bảo Mật và Kiểm Soát Truy Cập](#6-bảo-mật-và-kiểm-soát-truy-cập)
7. [Giao Tiếp Hướng Sự Kiện (Kafka)](#7-giao-tiếp-hướng-sự-kiện-kafka)
8. [Lưu Trữ Tệp](#8-lưu-trữ-tệp)
9. [Giao Tiếp Liên Dịch Vụ (REST calls)](#9-giao-tiếp-liên-dịch-vụ-rest-calls)
10. [Khởi Tạo Dữ Liệu (DataSeeder)](#10-khởi-tạo-dữ-liệu-dataseeder)
11. [Cấu Hình Ứng Dụng](#11-cấu-hình-ứng-dụng)
12. [Phụ Thuộc Kỹ Thuật Chính](#12-phụ-thuộc-kỹ-thuật-chính)
13. [Nhận Xét Kiến Trúc](#13-nhận-xét-kiến-trúc)

---

## 1. Tổng Quan

### 1.1 Mục Đích

Search-Chat Service là một vi dịch vụ Python chuyên biệt trong hệ thống thương mại điện tử đa dịch vụ, đảm nhận hai chức năng cốt lõi:

1. **Tìm kiếm sản phẩm thông minh**: Cung cấp ba phương thức tìm kiếm — tìm kiếm từ khóa truyền thống (BM25), tìm kiếm ngữ nghĩa theo vectơ (k-NN), và tìm kiếm lai (hybrid) kết hợp cả hai phương pháp — thông qua công cụ Elasticsearch.

2. **Giao tiếp hội thoại (Chatbot)**: Cho phép người dùng đặt câu hỏi bằng ngôn ngữ tự nhiên tiếng Việt để nhận được gợi ý sản phẩm phù hợp, với sự hỗ trợ của mô hình ngôn ngữ lớn Google Gemini.

Dịch vụ phục vụ tầng giao diện người dùng (frontend) của nền tảng thương mại điện tử, đóng vai trò là điểm tập trung xử lý yêu cầu tìm kiếm và trợ lý ảo mua sắm.

### 1.2 Công Nghệ Sử Dụng

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ lập trình | Python 3.10 |
| Web framework | Flask |
| Công cụ tìm kiếm | Elasticsearch 8.13.0 |
| Mô hình nhúng vectơ | SentenceTransformer `all-MiniLM-L6-v2` (384 chiều) |
| Mô hình ngôn ngữ lớn | Google Gemini `gemini-1.5-flash` |
| Nguồn dữ liệu | PostgreSQL (qua psycopg2 + pandas) |
| Container hóa | Docker + Docker Compose 3.8 |

### 1.3 Cổng Dịch Vụ

| Môi trường | Cổng |
|---|---|
| Flask API (host) | `5000` |
| Elasticsearch (host) | `9250` (ánh xạ tới container port `9200`) |

### 1.4 Kiểu Kiến Trúc

Dịch vụ áp dụng kiến trúc **phân lớp chức năng (functional layered)** theo đặc thù của Python, không theo mô hình Clean Architecture hay Domain-Driven Design. Các lớp chức năng bao gồm:

- **Lớp trình bày** (`app.py`): Tiếp nhận HTTP request, xác thực đầu vào, điều phối luồng xử lý, trả về HTTP response.
- **Lớp nghiệp vụ tìm kiếm** (`search_engine.py`): Đóng gói toàn bộ logic tương tác với Elasticsearch (BM25, k-NN, hybrid).
- **Lớp nghiệp vụ AI** (`chatbot_agent.py`): Tích hợp Google Gemini cho trích xuất từ khóa và sinh phản hồi ngôn ngữ tự nhiên.
- **Lớp nhập liệu dữ liệu** (`ingest_data.py`, `data_cronjob.py`): Đường ống nạp dữ liệu từ PostgreSQL vào Elasticsearch.
- **Lớp tiện ích CLI** (`main.py`): Giao diện dòng lệnh dùng cho kiểm thử và phát triển.

---

## 2. Cấu Trúc Package

```
services/search-chat-service/
├── .env                      # Biến môi trường thực tế (không commit vào git)
├── .env.example              # Mẫu biến môi trường cho các thành viên mới
├── .gitignore                # Quy tắc bỏ qua file khi commit
├── CLAUDE.md                 # Hướng dẫn codebase dành cho Claude Code
├── Dockerfile                # Định nghĩa Docker image cho dịch vụ
├── README.md                 # Tài liệu giới thiệu dịch vụ
├── app.py                    # Điểm vào chính: Flask REST API server
├── chatbot_agent.py          # Module tích hợp Google Gemini (NLP)
├── data_cronjob.py           # Script xuất dữ liệu từ PostgreSQL ra CSV
├── docker-compose.yml        # Điều phối Docker: Elasticsearch + Flask API
├── ingest_data.py            # Script nhập dữ liệu CSV vào Elasticsearch
├── main.py                   # Giao diện CLI tương tác cho kiểm thử
├── postman_collection.json   # Bộ test API Postman
├── product.csv               # File CSV trung gian (đầu ra của data_cronjob.py)
├── requirements.txt          # Danh sách thư viện Python phụ thuộc
└── search_engine.py          # Module tìm kiếm Elasticsearch (BM25/kNN/hybrid)
```

**Mô tả từng file:**

| File | Mô tả |
|---|---|
| `app.py` | Flask application: định nghĩa 4 route REST API, xử lý request/response, cấu hình CORS |
| `search_engine.py` | Đóng gói logic tìm kiếm Elasticsearch: khởi tạo client, lazy-load model, 3 hàm tìm kiếm |
| `chatbot_agent.py` | Tích hợp Gemini API: trích xuất từ khóa từ câu hỏi và sinh phản hồi hội thoại |
| `ingest_data.py` | Xây dựng lại chỉ mục Elasticsearch: tạo index, nhúng vectơ, bulk insert |
| `data_cronjob.py` | Kết nối PostgreSQL, thực thi SQL join, xuất kết quả ra `product.csv` |
| `main.py` | CLI chatbot vòng lặp: đọc input, trích xuất từ khóa, tìm kiếm, in phản hồi |
| `requirements.txt` | Khai báo phụ thuộc Python: Flask, elasticsearch, sentence-transformers, v.v. |
| `Dockerfile` | Multi-step Docker build: cài requirements, pre-load model, chạy `app.py` |
| `docker-compose.yml` | Định nghĩa 2 service: `elasticsearch` và `api` trong mạng nội bộ `recommend_net` |
| `product.csv` | File trung gian chứa dữ liệu sản phẩm đã join từ PostgreSQL |
| `postman_collection.json` | 4 test case Postman với ví dụ truy vấn tiếng Việt |

---

## 3. Mô Hình Dữ Liệu

### 3.1 Chỉ Mục Elasticsearch: `products_index`

Dịch vụ không sử dụng cơ sở dữ liệu quan hệ làm kho lưu trữ chính trong thời gian chạy. Thay vào đó, toàn bộ dữ liệu sản phẩm được lập chỉ mục trong Elasticsearch với cấu trúc document như sau:

**Tên chỉ mục**: `products_index`

**Mapping của chỉ mục:**

```json
{
  "mappings": {
    "properties": {
      "product_general_id": { "type": "integer" },
      "product_name":        { "type": "text" },
      "product_description": { "type": "text" },
      "tags":                { "type": "text" },
      "category_name":       { "type": "keyword" },
      "min_price":           { "type": "float" },
      "img":                 { "type": "keyword" },
      "embedding": {
        "type":       "dense_vector",
        "dims":       384,
        "index":      true,
        "similarity": "cosine"
      }
    }
  }
}
```

**Mô tả từng trường:**

| Trường | Kiểu Elasticsearch | Ràng buộc / Ghi chú |
|---|---|---|
| `product_general_id` | `integer` | Khóa định danh sản phẩm, lấy từ bảng `product_general` trong PostgreSQL |
| `product_name` | `text` | Tên sản phẩm; được boost `3x` trong truy vấn BM25 |
| `product_description` | `text` | Mô tả chi tiết sản phẩm; trọng số mặc định trong BM25 |
| `tags` | `text` | Thẻ gắn nhãn sản phẩm (mảng, chuyển đổi thành chuỗi phân cách bởi dấu phẩy); boost `2x` trong BM25 |
| `category_name` | `keyword` | Tên danh mục sản phẩm; kiểu `keyword` phù hợp lọc chính xác |
| `min_price` | `float` | Giá thấp nhất của sản phẩm, tính từ bảng `batch_detail` (hàm `MIN`) |
| `img` | `keyword` | URL ảnh sản phẩm; không phân tích từ (raw string) |
| `embedding` | `dense_vector` (384 chiều) | Vectơ ngữ nghĩa sinh bởi `all-MiniLM-L6-v2`; hỗ trợ k-NN với độ đo cosine |

### 3.2 Nguồn Dữ Liệu — Sơ Đồ Quan Hệ PostgreSQL

Dữ liệu trong `products_index` được tổng hợp từ 3 bảng trong cơ sở dữ liệu `ecommercev3` (PostgreSQL):

```
+------------------+          +------------------+          +------------------+
|  product_general |          |     category     |          |   batch_detail   |
+------------------+          +------------------+          +------------------+
| product_general_id (PK)     | category_id (PK) |          | batch_detail_id  |
| name             |          | category_name    |          | product_general_id (FK) |
| description      |  N:1     | description      |  1:N     | price            |
| unit             |--------->|                  |<---------|                  |
| unit_quantity    |          |                  |          |                  |
| tags (array)     |          +------------------+          +------------------+
| img              |
| category_id (FK) |
| created_at       |
+------------------+
```

**Câu truy vấn SQL để tổng hợp dữ liệu:**

```sql
SELECT 
    p.product_general_id,
    p.name             AS product_name,
    p.description      AS product_description,
    p.unit,
    p.unit_quantity,
    p.tags,
    p.img,
    p.created_at,
    c.category_id,
    c.category_name,
    c.description      AS category_description,
    MIN(b.price)       AS min_price
FROM product_general p
LEFT JOIN category c
    ON p.category_id = c.category_id
RIGHT JOIN batch_detail b
    ON p.product_general_id = b.product_general_id
GROUP BY p.product_general_id, c.category_id;
```

**Ghi chú thiết kế**: Câu truy vấn dùng `RIGHT JOIN` với `batch_detail`, nghĩa là chỉ những sản phẩm đã có ít nhất một lô hàng (`batch_detail`) mới được đưa vào chỉ mục. Đây là ràng buộc nghiệp vụ ngầm định: sản phẩm chưa nhập kho sẽ không xuất hiện trong kết quả tìm kiếm.

### 3.3 Cấu Trúc Document Elasticsearch (Ví Dụ)

```json
{
  "product_general_id": 42,
  "product_name": "Thịt heo ba chỉ tươi",
  "product_description": "Thịt heo ba chỉ tươi, nhập từ trang trại đạt tiêu chuẩn VietGAP",
  "tags": "thịt heo, thịt tươi, ba chỉ, nướng, chiên",
  "category_name": "Thịt & Hải Sản",
  "min_price": 85000.0,
  "img": "https://cdn.example.com/products/ba-chi-heo.jpg",
  "embedding": [0.023, -0.147, 0.381, ...]
}
```

---

## 4. Tầng Nghiệp Vụ (Service Layer)

### 4.1 Module `search_engine.py` — Tầng Tìm Kiếm

Module này đóng gói toàn bộ logic tương tác với Elasticsearch, cung cấp ba chiến lược tìm kiếm khác nhau.

#### Hàm tiện ích

| Hàm | Chữ ký | Mô tả |
|---|---|---|
| `get_model` | `() -> SentenceTransformer` | Tải lazy và cache model `all-MiniLM-L6-v2`; chỉ khởi tạo một lần trong vòng đời tiến trình |
| `get_es_client` | `() -> Elasticsearch` | Tạo Elasticsearch client với xác thực basic auth tùy điều kiện (nếu `ES_USER` và `ES_PASSWORD` được cấu hình) |
| `format_results` | `(hits: list[dict]) -> list[dict]` | Chuyển đổi danh sách hits từ Elasticsearch thành danh sách dict thuần túy với 7 trường chuẩn hóa |

#### Hàm tìm kiếm chính

**`search_keyword(query: str, top_k: int = 5) -> list[dict]`**

Thực hiện tìm kiếm BM25 (Best Match 25) — thuật toán tìm kiếm toàn văn bản truyền thống dựa trên tần suất từ.

- Truy vấn loại `multi_match` trên 4 trường với trọng số khác nhau:
  - `product_name^3` (boost 3): tên sản phẩm được ưu tiên cao nhất
  - `tags^2` (boost 2): thẻ gắn nhãn được ưu tiên thứ hai
  - `product_description` (boost 1): mô tả sản phẩm
  - `category_name` (boost 1): tên danh mục
- Fuzziness: `AUTO` — tự động điều chỉnh khoảng cách edit distance dựa trên độ dài từ, giúp xử lý lỗi chính tả
- Trả về: danh sách tối đa `top_k` kết quả, sắp xếp giảm dần theo điểm BM25

**`search_vector(query: str, top_k: int = 5) -> list[dict]`**

Thực hiện tìm kiếm ngữ nghĩa theo vectơ (k-nearest neighbors).

- Mã hóa `query` thành vectơ 384 chiều bằng `all-MiniLM-L6-v2`
- Thực hiện truy vấn `knn` trên trường `embedding` của chỉ mục
- Tham số k-NN:
  - `k = top_k`: số kết quả trả về
  - `num_candidates = 100`: số ứng viên gần đúng để Elasticsearch xem xét trước khi lọc k kết quả tốt nhất (HNSW algorithm)
- Độ đo tương đồng: **cosine similarity** (được cấu hình lúc tạo chỉ mục)
- Trả về: danh sách tối đa `top_k` kết quả, sắp xếp giảm dần theo điểm cosine

**`search_hybrid(query: str, top_k: int = 5) -> list[dict]`**

Kết hợp BM25 và k-NN trong một truy vấn Elasticsearch duy nhất (Reciprocal Rank Fusion tự nhiên của Elasticsearch).

Cấu trúc truy vấn:

```python
{
    "query": {
        "multi_match": {
            "query":     query,
            "fields":    ["product_name^3", "product_description", "tags^2", "category_name"],
            "fuzziness": "AUTO",
            "boost":     0.5
        }
    },
    "knn": {
        "field":          "embedding",
        "query_vector":   query_vector,
        "k":              top_k,
        "num_candidates": 100,
        "boost":          0.5
    },
    "size": top_k
}
```

- BM25 và k-NN được đặt trọng số đều nhau (boost `0.5` mỗi loại)
- Elasticsearch nội bộ kết hợp điểm của hai phương pháp và trả về `top_k` kết quả tổng hợp
- Đây là phương pháp được sử dụng mặc định trong luồng chatbot

**Quy tắc nghiệp vụ đáng chú ý:**
- Model SentenceTransformer được cache toàn cục (biến `_model`), tránh tải lại tốn kém giữa các request.
- Khi `ES_USER` và `ES_PASSWORD` đều rỗng, client kết nối không xác thực (phù hợp môi trường phát triển với `xpack.security.enabled=false`).

---

### 4.2 Module `chatbot_agent.py` — Tầng AI Hội Thoại

Module tích hợp Google Gemini API để xử lý ngôn ngữ tự nhiên tiếng Việt.

**`extract_keywords(user_message: str) -> str`**

Trích xuất từ khóa tìm kiếm từ câu hỏi của người dùng bằng Gemini API.

- Mô hình sử dụng: `gemini-1.5-flash`
- Prompt hướng dẫn mô hình:
  - Chỉ trả về từ khóa tìm kiếm quan trọng cho sản phẩm thực phẩm thương mại điện tử
  - Các từ khóa phân cách bởi dấu phẩy, không kèm giải thích
  - Trả về chuỗi rỗng nếu tin nhắn chỉ là lời chào hỏi không có ý định mua hàng
- Fallback: nếu API key không được cấu hình hoặc API thất bại, hàm trả về chính `user_message` gốc
- Trả về: chuỗi từ khóa phân cách bởi dấu phẩy (ví dụ: `"thịt bò, rau, lẩu"`)

**`generate_chat_response(user_message: str, recommended_products: list[dict]) -> str`**

Sinh phản hồi ngôn ngữ tự nhiên dựa trên câu hỏi người dùng và danh sách sản phẩm gợi ý.

- Mô hình sử dụng: `gemini-1.5-flash`
- Danh sách sản phẩm được định dạng thành chuỗi: `"- {product_name} (Giá: {min_price} VND)"`
- Prompt hướng dẫn mô hình đóng vai nhân viên bán hàng thân thiện, viết phản hồi ngắn gọn giới thiệu sản phẩm phù hợp
- Fallback: trả về thông báo chung nếu API key không được cấu hình hoặc lỗi
- Trả về: chuỗi phản hồi tiếng Việt

---

### 4.3 Module `ingest_data.py` — Tầng Nhập Dữ Liệu

Đường ống xử lý dữ liệu một chiều: CSV → Elasticsearch.

**`create_index(es: Elasticsearch) -> None`**

- Xóa chỉ mục `products_index` nếu đã tồn tại (thao tác phá hủy)
- Tạo lại chỉ mục với toàn bộ mapping được định nghĩa sẵn

**`ingest_data() -> None`**

Luồng nhập dữ liệu hoàn chỉnh:

1. Khởi tạo Elasticsearch client và kiểm tra kết nối (ping)
2. Tạo/tái tạo chỉ mục qua `create_index()`
3. Tải model SentenceTransformer `all-MiniLM-L6-v2`
4. Đọc `product.csv`, điền `NaN` bằng chuỗi rỗng
5. Với mỗi hàng: ghép `product_name + product_description + category_name + tags` thành chuỗi văn bản tổng hợp, sinh vectơ 384 chiều
6. Bulk insert toàn bộ document vào Elasticsearch qua `helpers.bulk()`

**Quy tắc nghiệp vụ**: Mỗi lần chạy `ingest_data()` sẽ xóa và xây dựng lại toàn bộ chỉ mục từ đầu — không có cơ chế cập nhật tăng dần (incremental update).

---

### 4.4 Module `data_cronjob.py` — Tầng Xuất Dữ Liệu

**`main() -> None`**

Kết nối PostgreSQL và xuất dữ liệu ra CSV:

1. Mở kết nối psycopg2 đến `ecommercev3`
2. Thực thi câu truy vấn SQL join 3 bảng
3. Đọc kết quả vào pandas DataFrame
4. Chuyển đổi trường `tags` (kiểu mảng PostgreSQL) thành chuỗi phân cách bởi dấu phẩy
5. Xuất ra `product.csv` với encoding `UTF-8-SIG` (hỗ trợ Excel trên Windows)
6. Đóng kết nối trong khối `finally`

---

## 5. Tầng Trình Bày — REST API

### 5.1 Tổng Quan Controller

Tất cả endpoint được định nghĩa trong `app.py`. Flask được khởi động với `CORS(app)` cho phép tất cả nguồn gốc (origins).

### 5.2 Bảng Mô Tả Endpoint

| Phương thức HTTP | Đường dẫn | Xác thực | Vai trò yêu cầu | Kiểu request body | Kiểu response |
|---|---|---|---|---|---|
| `GET` | `/api/search/keyword` | Không | Không có | Query params | `application/json` |
| `GET` | `/api/search/vector` | Không | Không có | Query params | `application/json` |
| `GET` | `/api/search/hybrid` | Không | Không có | Query params | `application/json` |
| `POST` | `/api/chat` | Không | Không có | `application/json` | `application/json` |

### 5.3 Mô Tả Chi Tiết Từng Endpoint

#### GET `/api/search/keyword`

Tìm kiếm toàn văn bản sử dụng thuật toán BM25 của Elasticsearch.

**Tham số truy vấn (Query Parameters):**

| Tham số | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `q` | `string` | Có | Chuỗi truy vấn tìm kiếm |
| `top_k` | `integer` | Không (mặc định: 5) | Số lượng kết quả trả về |

**Phản hồi thành công (HTTP 200):**

```json
{
  "status": "success",
  "query": "thịt heo",
  "top_k": 5,
  "data": [
    {
      "product_general_id": 42,
      "product_name": "Thịt heo ba chỉ tươi",
      "category_name": "Thịt & Hải Sản",
      "min_price": 85000.0,
      "img": "https://cdn.example.com/ba-chi.jpg",
      "product_description": "...",
      "score": 3.847
    }
  ]
}
```

**Phản hồi lỗi:**

| HTTP Status | Điều kiện | Response body |
|---|---|---|
| 400 | Thiếu tham số `q` | `{"error": "Vui lòng truyền tham số 'q' (query)"}` |
| 500 | Ngoại lệ không xác định | `{"status": "error", "message": "<chi tiết lỗi>"}` |

---

#### GET `/api/search/vector`

Tìm kiếm ngữ nghĩa theo vectơ (k-nearest neighbors) dựa trên cosine similarity.

**Tham số truy vấn:** Giống endpoint `/api/search/keyword`.

**Cơ chế hoạt động:**
1. Mã hóa `q` thành vectơ 384 chiều bằng `all-MiniLM-L6-v2`
2. Thực hiện truy vấn k-NN trên trường `embedding` với `num_candidates=100`
3. Trả về `top_k` kết quả có cosine similarity cao nhất

**Phản hồi:** Cấu trúc giống endpoint keyword search.

---

#### GET `/api/search/hybrid`

Tìm kiếm lai kết hợp BM25 và k-NN với trọng số đều nhau.

**Tham số truy vấn:** Giống endpoint `/api/search/keyword`.

**Cơ chế hoạt động:**
1. Mã hóa `q` thành vectơ 384 chiều
2. Gửi truy vấn kết hợp `query` (BM25, boost 0.5) và `knn` (k-NN, boost 0.5) tới Elasticsearch
3. Elasticsearch hợp nhất và sắp xếp kết quả từ cả hai nguồn

**Phản hồi:** Cấu trúc giống endpoint keyword search.

---

#### POST `/api/chat`

Endpoint chatbot hội thoại: nhận câu hỏi tiếng Việt, gợi ý sản phẩm phù hợp và sinh phản hồi ngôn ngữ tự nhiên.

**Nội dung request body (Content-Type: application/json):**

```json
{
  "message": "tôi muốn mua thịt bò và rau về làm lẩu cuối tuần"
}
```

| Trường | Kiểu | Bắt buộc | Mô tả |
|---|---|---|---|
| `message` | `string` | Có | Câu hỏi hoặc yêu cầu của người dùng bằng tiếng Việt |

**Luồng xử lý nội bộ:**

```
[message] 
   → extract_keywords(message)       [Gemini API]
   → search_hybrid(keywords, top_k=5) [Elasticsearch]
   → generate_chat_response(message, results) [Gemini API]
   → response JSON
```

**Phản hồi thành công — có sản phẩm phù hợp (HTTP 200):**

```json
{
  "status": "success",
  "chat_response": "Chào bạn! Tôi tìm thấy một số sản phẩm phù hợp để làm lẩu...",
  "recommended_products": [
    {
      "product_general_id": 42,
      "product_name": "Thịt bò nạm",
      "category_name": "Thịt & Hải Sản",
      "min_price": 180000.0,
      "img": "https://cdn.example.com/bo-nam.jpg",
      "product_description": "...",
      "score": 0.923
    }
  ],
  "extracted_keywords": "thịt bò, rau, lẩu"
}
```

**Phản hồi thành công — không có từ khóa (lời chào hỏi) (HTTP 200):**

```json
{
  "status": "success",
  "chat_response": "Xin chào! Bạn đang cần tìm kiếm hoặc mua sản phẩm gì hôm nay?",
  "recommended_products": [],
  "extracted_keywords": ""
}
```

**Phản hồi thành công — không tìm thấy sản phẩm (HTTP 200):**

```json
{
  "status": "success",
  "chat_response": "Xin lỗi, hiện tại tôi không tìm thấy sản phẩm nào phù hợp với yêu cầu của bạn.",
  "recommended_products": [],
  "extracted_keywords": "..."
}
```

**Phản hồi lỗi:**

| HTTP Status | Điều kiện | Response body |
|---|---|---|
| 400 | Thiếu trường `message` | `{"error": "Vui lòng truyền tham số 'message'"}` |
| 500 | Ngoại lệ không xác định | `{"status": "error", "message": "<chi tiết lỗi>"}` |

---

## 6. Bảo Mật và Kiểm Soát Truy Cập

### 6.1 Xác Thực và Phân Quyền

Search-Chat Service **không triển khai bất kỳ cơ chế xác thực** nào tại thời điểm phân tích. Toàn bộ 4 endpoint đều là public endpoint, có thể gọi trực tiếp không cần token hay session.

**Đánh giá rủi ro**: Đây là điểm yếu bảo mật đáng kể nếu dịch vụ được triển khai trong môi trường production mà không có API Gateway bảo vệ phía trước.

### 6.2 CORS (Cross-Origin Resource Sharing)

```python
from flask_cors import CORS
app = Flask(__name__)
CORS(app)
```

Cấu hình `CORS(app)` mà không chỉ định `origins` tương đương với việc cho phép tất cả nguồn gốc (`*`). Trong môi trường production, cần giới hạn xuống các domain cụ thể của frontend.

### 6.3 Bảo Mật Elasticsearch

Trong `docker-compose.yml`:

```yaml
environment:
  - xpack.security.enabled=false
```

Tính năng bảo mật tích hợp của Elasticsearch bị **tắt hoàn toàn** — không có xác thực, không có mã hóa TLS giữa Elasticsearch và Flask API. Điều này chỉ phù hợp cho môi trường phát triển.

### 6.4 Các Endpoint và Trạng Thái Xác Thực

| Endpoint | Loại | Cần xác thực |
|---|---|---|
| `GET /api/search/keyword` | Public | Không |
| `GET /api/search/vector` | Public | Không |
| `GET /api/search/hybrid` | Public | Không |
| `POST /api/chat` | Public | Không |

---

## 7. Giao Tiếp Hướng Sự Kiện (Kafka)

Search-Chat Service **không tích hợp Apache Kafka** và không có producer hay consumer nào được triển khai. Dịch vụ này hoạt động hoàn toàn độc lập với hệ thống message broker của nền tảng.

Dữ liệu sản phẩm được đồng bộ thủ công qua đường ống: PostgreSQL → CSV → Elasticsearch (xem phần [Khởi Tạo Dữ Liệu](#10-khởi-tạo-dữ-liệu-dataseeder)).

---

## 8. Lưu Trữ Tệp

Search-Chat Service **không tích hợp bất kỳ dịch vụ lưu trữ đối tượng** nào (Cloudflare R2, AWS S3, hay tương đương). URL ảnh sản phẩm (`img`) được lấy trực tiếp từ cơ sở dữ liệu PostgreSQL và trả về trong kết quả tìm kiếm mà không qua xử lý nào.

Tệp trung gian duy nhất là `product.csv` được lưu trên hệ thống tệp cục bộ của container.

---

## 9. Giao Tiếp Liên Dịch Vụ (REST calls)

Search-Chat Service **không thực hiện bất kỳ lời gọi HTTP đồng bộ** nào tới các vi dịch vụ khác trong hệ thống (identity-service, back-office-service, product_storage_service, ecommerce-service).

Dịch vụ là một node độc lập: nó nhận dữ liệu từ PostgreSQL qua `data_cronjob.py`, sau đó phục vụ các request tìm kiếm và chat một cách độc lập.

---

## 10. Khởi Tạo Dữ Liệu (DataSeeder)

Search-Chat Service không có cơ chế DataSeeder tự động khi khởi động. Thay vào đó, việc chuẩn bị dữ liệu là một quy trình thủ công hai bước:

### Bước 1: Xuất dữ liệu từ PostgreSQL

**Script**: `data_cronjob.py`

**Điều kiện chạy**: Thủ công (manual trigger), không có lịch trình tự động trong code.

**Dữ liệu được tạo ra**:
- File `product.csv` chứa tất cả sản phẩm đã có ít nhất một lô hàng (`batch_detail`)
- Thông tin bao gồm: `product_general_id`, `product_name`, `product_description`, `unit`, `unit_quantity`, `tags`, `img`, `created_at`, `category_id`, `category_name`, `category_description`, `min_price`

**Nguồn dữ liệu**: Cơ sở dữ liệu `ecommercev3` (PostgreSQL) tại `localhost:5432`

### Bước 2: Nạp dữ liệu vào Elasticsearch

**Script**: `ingest_data.py`

**Điều kiện chạy**: Thủ công, sau khi Bước 1 hoàn thành.

**Hành vi**: Xóa và tái tạo toàn bộ chỉ mục `products_index`. Mỗi document được bổ sung vectơ nhúng 384 chiều trước khi được bulk insert.

**Ước tính thời gian**: Phụ thuộc vào số lượng sản phẩm và thời gian sinh embedding. Quá trình sinh embedding (`SentenceTransformer.encode()`) là tính toán nặng và chạy trên CPU trong container `python:3.10-slim`.

**Cấu hình kết nối PostgreSQL** (hardcoded trong `data_cronjob.py`):

| Tham số | Giá trị |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `ecommercev3` |
| User | `postgres` |
| Password | `12345678` |

**Cảnh báo bảo mật**: Thông tin xác thực PostgreSQL được hardcode trực tiếp trong mã nguồn — đây là thực hành không an toàn cần khắc phục.

---

## 11. Cấu Hình Ứng Dụng

### 11.1 Cổng và Host

| Thành phần | Host | Port |
|---|---|---|
| Flask API | `0.0.0.0` | `5000` |
| Elasticsearch (container) | `elasticsearch` (tên service Docker) | `9200` |
| Elasticsearch (host access) | `localhost` | `9250` |

### 11.2 Mạng Docker

```yaml
networks:
  recommend_net:
    driver: bridge
```

Hai service (`elasticsearch` và `api`) giao tiếp trong mạng nội bộ `recommend_net`. Service `api` kết nối Elasticsearch qua URL `http://elasticsearch:9200`.

### 11.3 Biến Môi Trường

| Biến | Giá trị mặc định | Mô tả | Bắt buộc |
|---|---|---|---|
| `GEMINI_API_KEY` | _(không có)_ | API key Google Generative AI | Có (cho tính năng AI) |
| `ELASTICSEARCH_HOST` | `http://localhost:9200` | URL kết nối Elasticsearch | Có |
| `ELASTICSEARCH_USER` | _(rỗng)_ | Tên người dùng basic auth Elasticsearch | Không |
| `ELASTICSEARCH_PASSWORD` | _(rỗng)_ | Mật khẩu basic auth Elasticsearch | Không |

File `.env.example`:

```
GEMINI_API_KEY="your_gemini_api_key_here"
ELASTICSEARCH_HOST="http://localhost:9200"
ELASTICSEARCH_USER="admin"
ELASTICSEARCH_PASSWORD="admin"
```

### 11.4 Cấu Hình Elasticsearch trong Docker Compose

```yaml
environment:
  - discovery.type=single-node
  - xpack.security.enabled=false
  - ES_JAVA_OPTS=-Xms512m -Xmx512m
```

- `discovery.type=single-node`: Chế độ single-node, không cần cluster formation
- `xpack.security.enabled=false`: Tắt xác thực (chỉ dùng cho phát triển)
- `ES_JAVA_OPTS=-Xms512m -Xmx512m`: Giới hạn heap JVM Elasticsearch ở 512MB

### 11.5 Cấu Hình Tìm Kiếm

| Hằng số | Giá trị | Vị trí |
|---|---|---|
| `INDEX_NAME` | `products_index` | `search_engine.py`, `ingest_data.py` |
| `MODEL_NAME` | `all-MiniLM-L6-v2` | `search_engine.py`, `ingest_data.py` |
| Số chiều vectơ | `384` | `ingest_data.py` (mapping) |
| KNN num_candidates | `100` | `search_engine.py` |
| Hybrid BM25 boost | `0.5` | `search_engine.py` |
| Hybrid KNN boost | `0.5` | `search_engine.py` |

---

## 12. Phụ Thuộc Kỹ Thuật Chính

### 12.1 Thư Viện Python (`requirements.txt`)

| Thư viện | Phiên bản | Mục đích |
|---|---|---|
| `Flask` | Latest | Web framework tối giản, định nghĩa REST API, routing, request/response handling |
| `Flask-Cors` | Latest | Xử lý CORS headers cho phép frontend cross-origin request |
| `elasticsearch` | `8.13.0` | Client Python chính thức của Elasticsearch; hỗ trợ full-text search và vector search |
| `sentence-transformers` | Latest | Thư viện sinh vectơ nhúng văn bản; sử dụng model `all-MiniLM-L6-v2` |
| `google-generativeai` | Latest | SDK Python chính thức của Google Gemini API |
| `python-dotenv` | Latest | Tải biến môi trường từ file `.env` |
| `pandas` | Latest | Xử lý dữ liệu dạng bảng (DataFrame), đọc/ghi CSV |
| `psycopg2` | Latest (implied) | PostgreSQL database adapter cho Python (dùng trong `data_cronjob.py`) |

### 12.2 Infrastructure

| Thành phần | Phiên bản | Mục đích |
|---|---|---|
| Python | `3.10` | Runtime ngôn ngữ (base image Docker) |
| Elasticsearch | `8.13.0` | Công cụ tìm kiếm và lưu trữ vectơ |
| Docker | — | Container hóa ứng dụng |
| Docker Compose | `3.8` | Điều phối multi-container (Flask + Elasticsearch) |

### 12.3 Mô Hình AI

| Mô hình | Nhà cung cấp | Mục đích |
|---|---|---|
| `all-MiniLM-L6-v2` | Hugging Face / SBERT | Sinh vectơ nhúng 384 chiều cho tìm kiếm ngữ nghĩa |
| `gemini-1.5-flash` | Google DeepMind | Trích xuất từ khóa và sinh phản hồi ngôn ngữ tự nhiên tiếng Việt |

**Đặc điểm kỹ thuật của `all-MiniLM-L6-v2`:**
- Kiến trúc: BERT-based, 6 lớp transformer
- Số chiều đầu ra: 384
- Kích thước model: ~22MB
- Tốc độ inference: Nhanh (phù hợp realtime)
- Chất lượng: Tốt cho tìm kiếm ngữ nghĩa đa ngôn ngữ

---

## 13. Nhận Xét Kiến Trúc

### 13.1 Điểm Mạnh Thiết Kế

**Chiến lược tìm kiếm lai (Hybrid Search)**
Việc kết hợp BM25 và k-NN là một quyết định thiết kế có cơ sở khoa học vững chắc. BM25 xử lý tốt các truy vấn chính xác (từ khóa cụ thể), trong khi k-NN xử lý các truy vấn ngữ nghĩa mơ hồ hơn. Hybrid search thừa hưởng ưu điểm của cả hai, giảm thiểu nhược điểm của mỗi phương pháp riêng lẻ.

**Kiến trúc đơn giản và rõ ràng**
Sự phân tách chức năng thành các module riêng biệt (`search_engine.py`, `chatbot_agent.py`, `ingest_data.py`, `data_cronjob.py`) giúp code dễ đọc, dễ kiểm thử độc lập, và dễ thay thế từng thành phần.

**Lazy loading và caching model**
Biến toàn cục `_model` trong `search_engine.py` đảm bảo SentenceTransformer chỉ được tải một lần duy nhất vào bộ nhớ, tránh overhead khởi tạo lại tốn kém (~1-2 giây) cho mỗi request.

**Pre-loading model trong Dockerfile**
```dockerfile
RUN python -c "from sentence_transformers import SentenceTransformer; SentenceTransformer('all-MiniLM-L6-v2')"
```
Model được tải xuống và cache trong quá trình build Docker image, giúp container khởi động nhanh hơn và không phụ thuộc vào kết nối mạng khi chạy.

**Graceful fallback trong AI modules**
Cả `extract_keywords()` và `generate_chat_response()` đều có cơ chế fallback: nếu `GEMINI_API_KEY` không được cấu hình hoặc API call thất bại, hàm trả về giá trị mặc định thay vì ném ngoại lệ. Điều này đảm bảo tính sẵn sàng của dịch vụ (service availability) ngay cả khi AI backend gặp sự cố.

### 13.2 Quyết Định Thiết Kế Đáng Chú Ý

**Lựa chọn Elasticsearch thay vì pgvector**
Thay vì thêm extension `pgvector` vào PostgreSQL đã có sẵn trong hệ thống, nhóm phát triển chọn triển khai Elasticsearch riêng. Điều này gia tăng độ phức tạp vận hành nhưng đem lại khả năng tìm kiếm toàn văn bản (full-text search) mạnh mẽ hơn nhiều, cùng với hỗ trợ native cho hybrid search mà pgvector không có.

**Chọn `all-MiniLM-L6-v2` thay vì model đa ngôn ngữ**
Model này được tối ưu cho tiếng Anh nhưng dữ liệu sản phẩm là tiếng Việt. Tuy nhiên, model này nhỏ, nhanh và dễ triển khai. Đây là đánh đổi hợp lý trong giai đoạn đầu phát triển.

**Đường ống dữ liệu thủ công (Manual Pipeline)**
Thay vì tích hợp Kafka consumer để tự động đồng bộ khi có sự kiện `product-general-events`, dịch vụ dùng pipeline thủ công PostgreSQL → CSV → Elasticsearch. Điều này đơn giản hóa việc phát triển nhưng tạo ra độ trễ dữ liệu (data staleness).

### 13.3 Nợ Kỹ Thuật (Technical Debt)

**1. Thông tin xác thực PostgreSQL hardcoded**

Trong `data_cronjob.py`:
```python
DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "ecommercev3",
    "user": "postgres",
    "password": "12345678"
}
```
Thông tin nhạy cảm không được đọc từ biến môi trường. Đây là vi phạm nguyên tắc Twelve-Factor App và là rủi ro bảo mật nếu code được commit vào repository công khai.

**2. Không có xác thực API**

Tất cả endpoint đều là public, không có middleware xác thực (JWT, API key, OAuth2). Trong môi trường production, bất kỳ ai biết địa chỉ IP cũng có thể gọi API không giới hạn, gây nguy cơ lạm dụng tài nguyên và tốn chi phí Gemini API.

**3. Flask debug mode bật trong production**

```python
app.run(host="0.0.0.0", port=5000, debug=True)
```

`debug=True` kích hoạt Werkzeug debugger tương tác, có thể cho phép thực thi code tùy ý nếu bị khai thác. Phải tắt trong môi trường production và sử dụng WSGI server (Gunicorn, uWSGI).

**4. Đường ống dữ liệu thủ công và không có cơ chế đồng bộ tự động**

Khi sản phẩm mới được tạo trong back-office-service, search-chat-service không tự động cập nhật chỉ mục. Người vận hành phải chạy thủ công `data_cronjob.py` rồi `ingest_data.py`. Giải pháp lý tưởng là consume event `product-general-events` từ Kafka và cập nhật chỉ mục Elasticsearch theo thời gian thực.

**5. Xóa và tái tạo chỉ mục toàn bộ (Destructive Reindex)**

Mỗi lần chạy `ingest_data.py` sẽ xóa toàn bộ `products_index` và tái tạo từ đầu. Trong thời gian reindex, dịch vụ tìm kiếm vẫn hoạt động nhưng kết quả trả về có thể không đầy đủ. Cần triển khai blue-green reindexing (tạo chỉ mục mới, sau đó alias switch).

**6. Thiếu giới hạn tốc độ (Rate Limiting)**

Không có cơ chế giới hạn số lượng request, đặc biệt nguy hiểm với endpoint `/api/chat` vì mỗi request có thể tốn 2 lần gọi Gemini API (trích xuất từ khóa + sinh phản hồi).

**7. Elasticsearch bảo mật bị tắt**

`xpack.security.enabled=false` trong môi trường phát triển là chấp nhận được, nhưng cần bật và cấu hình TLS + xác thực khi chuyển sang production.

**8. Không có health check endpoint**

Flask không expose endpoint `/actuator/health` hay tương đương, gây khó khăn khi tích hợp với Docker Compose health checks hay Kubernetes liveness probes.

**9. Lựa chọn model nhúng chưa tối ưu cho tiếng Việt**

`all-MiniLM-L6-v2` được huấn luyện chủ yếu trên văn bản tiếng Anh. Để tối ưu cho dữ liệu tiếng Việt, nên xem xét các model đa ngôn ngữ như `paraphrase-multilingual-MiniLM-L12-v2` hoặc các model được fine-tune trên tiếng Việt.

---

*Tài liệu này được biên soạn phục vụ mục đích học thuật cho đề tài tốt nghiệp. Mọi thông tin kỹ thuật phản ánh trạng thái mã nguồn tại thời điểm phân tích (06/05/2026).*
