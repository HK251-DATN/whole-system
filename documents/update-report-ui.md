# Các màn hình còn thiếu trong 5_HienThuc.tex

Dưới đây là danh sách các màn hình chính đã hiện thực nhưng **chưa được đề cập** trong phần `\subsubsection{Giao diện ...}` của file `5_HienThuc.tex`.

---

## 1. Ứng dụng người mua (ecommerce-ui)

### 1.1 Đã có nhưng chưa có ảnh / mô tả đầy đủ

| Màn hình | Ghi chú |
|---|---|
| Màn hình đăng nhập (`Login.jsx`) | Chỉ có màn hình đăng **ký**; đăng nhập chưa được nhắc đến |
| Màn hình thanh toán (`PaymentPage.jsx`) | Chỉ được đề cập ngắn gọn, không có ảnh minh họa |
| Màn hình theo dõi / kết quả đơn hàng (`OrderResultPage.jsx`) | Có comment `% [TO DO: ...]` trong file .tex |

### 1.2 Hoàn toàn chưa được đề cập

| Màn hình | File nguồn | Mô tả chức năng |
|---|---|---|
| Trang Landing / Trang chủ | `modules/landing/features/index.jsx` | Hero section, Flash Sale đếm ngược, sản phẩm nổi bật, danh mục, testimonials |
| Flash Sale | `landing/components/FlashSale/` | Bộ đếm ngược và danh sách sản phẩm đang giảm giá |
| Trang danh mục sản phẩm | `modules/category/features/index.jsx` | Lọc sản phẩm theo danh mục, sidebar lọc, sắp xếp |
| Màn hình đặt hàng / xác nhận | `modules/order/features/index.jsx` | Chọn địa chỉ, xem tóm tắt đơn, áp mã giảm giá |
| Màn hình thanh toán thành công | `modules/successPayment/features/index.jsx` | Thông báo đặt hàng thành công |
| Trang cá nhân — Thông tin tài khoản | `profile/components/account/ProfileSection.jsx` | Xem và cập nhật thông tin cá nhân, avatar |
| Trang cá nhân — Đổi mật khẩu | `profile/components/account/PasswordSection.jsx` | Form đổi mật khẩu |
| Trang cá nhân — Địa chỉ giao hàng | `profile/components/account/AddressSection.jsx` | Thêm, sửa, xóa địa chỉ; tích hợp bản đồ Goong |
| Trang cá nhân — Lịch sử đơn hàng | `profile/components/account/OrderSection.jsx` | Xem danh sách đơn hàng, xem chi tiết từng đơn |
| Trang cá nhân — Voucher | `profile/components/account/VoucherSection.jsx` | Xem các mã voucher của người dùng |
| Chatbot nổi (Floating Chat) | `components/FloatingChat/FloatingChat.jsx` | Widget chat AI hỗ trợ khách hàng tích hợp search-chat-service |

---

## 2. Ứng dụng quản trị viên (back-office-ui)

### 2.1 Đã có nhưng chưa đầy đủ

| Màn hình | Ghi chú |
|---|---|
| Dashboard | Chỉ mô tả bằng text, chưa có ảnh minh họa |
| Màn hình đăng nhập | Chưa được đề cập |

### 2.2 Hoàn toàn chưa được đề cập

| Màn hình | Route | Mô tả chức năng |
|---|---|---|
| Quản lý nhân viên | `/manage-employee` | Xem, tạo, cập nhật nhân viên; phân biệt nhân viên đã/chưa có tài khoản hệ thống |
| Quản lý khách hàng | `/manage-customer` | Xem danh sách người mua, thông tin liên hệ, trạng thái tài khoản |
| Quản lý kho — Tab Sản phẩm & Batch | `/manage-warehouse` | Tạo và xem Product Batch, Product Detail |
| Quản lý kho — Tab Kho hàng | `/manage-warehouse` | CRUD kho vật lý, tỉ lệ sử dụng, số lượng kệ/tủ lạnh |
| Quản lý kho — Tab Công cụ lưu trữ | `/manage-warehouse` | CRUD kệ (Rack) và tủ lạnh (Fridge); xem nhiệt độ tủ lạnh, các tầng của kệ |
| Quản lý đóng gói (Quản lý) | `/manage-packaging` | Xem tổng quan nhiệm vụ đóng gói, danh sách nhân viên đóng gói |
| Quản lý vận chuyển | `/manage-shipping` | Quản lý đơn hàng đang giao, phân công tài xế |
| Quản lý sự kiện khuyến mãi | `/manage-sale-event` | Tạo/sửa/xóa sale event, thêm sản phẩm vào event, xem chi tiết event |
| Quản lý mã giảm giá | `/manage-coupon` | Tạo và quản lý coupon cho người mua |
| Quản lý nhà cung cấp | `/manage-provider` | Duyệt hồ sơ nhà cung cấp mới, xem chứng chỉ/video, chấp thuận hoặc từ chối |
| Quản lý nhu cầu nguyên liệu | `/manage-raw-product-demand` | Tạo nhu cầu thu mua nguyên liệu thô, xem đơn giao hàng đang chờ, xác nhận nhận hàng |
| Giao diện nhân viên đóng gói | `/packaging/employee` | Tab đơn sẵn sàng (CONFIRMED), tab nhiệm vụ của tôi (PACKING), modal thực hiện đóng gói từng bước, chọn sản phẩm theo pick-list |
| Giao diện nhân viên giao hàng | `/delivery/employee` | Tab đơn sẵn sàng giao (READY\_FOR\_PICKUP), tab nhiệm vụ đang giao (SHIPPING), tích hợp gọi điện và bản đồ |

---

## 3. Ứng dụng nhà cung cấp (provider-ui)

### 3.1 Đã có nhưng chưa đầy đủ

| Màn hình | Ghi chú |
|---|---|
| Trang Nhu cầu sản phẩm (`/nhu-cau`) | Đã có ảnh và mô tả tốt |
| Trang Lịch sử giao dịch (`/giao-dich`) | Đã có ảnh và mô tả tốt |
| Trang Tổng quan (`/dashboard`) | Đã có ảnh nhưng thiếu mô tả biểu đồ Recharts |

### 3.2 Hoàn toàn chưa được đề cập

| Màn hình | Route | Mô tả chức năng |
|---|---|---|
| Đăng nhập | `/login` | Form đăng nhập cho nhà cung cấp |
| Đăng ký tài khoản | `/register` | Tạo tài khoản mới |
| Cổng kiểm tra nhà cung cấp | `/provider-check` | Kiểm tra trạng thái phê duyệt tài khoản nhà cung cấp |
| Đăng ký trở thành nhà cung cấp | `/become-provider` | Điền thông tin hồ sơ để nộp đơn xin trở thành nhà cung cấp |
| Tải lên bằng chứng / chứng chỉ | `/upload-evidence` | Upload ảnh/video chứng minh năng lực sản xuất |
| Xem đơn đăng ký đã nộp | `/my-submissions` | Xem trạng thái các hồ sơ đăng ký đã gửi |
| Tài khoản bị tạm khóa | `/account-suspended` | Thông báo tài khoản bị tạm đình chỉ |
| Trang hồ sơ | `/profile` | Xem và cập nhật thông tin cá nhân nhà cung cấp |

---

## Gợi ý thứ tự bổ sung vào báo cáo

Nên bổ sung theo thứ tự sau để đảm bảo tính mạch lạc theo từng nhóm người dùng:

### Phần ecommerce-ui (người mua)
1. Màn hình đăng nhập
2. Trang chủ / Landing page (Flash Sale, sản phẩm nổi bật)
3. Trang danh mục sản phẩm
4. Màn hình đặt hàng và thanh toán (bổ sung ảnh)
5. Màn hình xác nhận đặt hàng thành công
6. Trang cá nhân (thông tin, địa chỉ, lịch sử đơn, voucher)
7. Floating Chatbot

### Phần back-office-ui (quản trị viên)
1. Màn hình đăng nhập
2. Dashboard (bổ sung ảnh)
3. Quản lý nhân viên
4. Quản lý khách hàng
5. Quản lý kho (3 tab: Sản phẩm/Batch, Kho, Công cụ lưu trữ)
6. Quản lý nhà cung cấp (duyệt hồ sơ)
7. Quản lý nhu cầu nguyên liệu thô
8. Quản lý sự kiện khuyến mãi và mã giảm giá
9. Quản lý đóng gói (view quản lý)
10. Quản lý vận chuyển
11. Giao diện nhân viên đóng gói
12. Giao diện nhân viên giao hàng

### Phần provider-ui (nhà cung cấp)
1. Màn hình đăng ký và luồng trở thành nhà cung cấp (upload chứng chỉ → xem trạng thái hồ sơ)
2. Trang hồ sơ
