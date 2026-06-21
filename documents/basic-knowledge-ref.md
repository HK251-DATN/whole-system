III KIẾN THỨC NỀN TẢNG
1 Công nghệ
1.1 Các công nghệ về Front-end
1.1.1 HTML, CSS và JS
Không cần phải nói quá nhiều về HTML, CSS và JS - đây chính là bộ ba nguyên tử không thể thay thế được trong quá trình tạo dựng nên một trang web từ cơ bản đến nâng cao.
• HTML cung cấp cấu trúc cơ bản của các trang web, được cải tiến và sửa đổi bởi các công nghệ khác như CSS và JavaScript.
• CSS được sử dụng để kiểm soát trình bày, định dạng và bố cục.
• JavaScript được sử dụng để kiểm soát hành vi của các yếu tố khác nhau.
Cả ba đều mang những đặc điểm như cú pháp đơn giản, không yêu cầu nền tảng quá cao từ đó giúp người dùng dễ tiếp cận và sử dụng hơn.
1.1.2 ReactJS
ReactJS là một thư viện chứa nhiều JavaScript mã nguồn mở được Facebook xây dựng và phát triển. Thư viện này được sử dụng để tạo ra các ứng dụng trang web hấp dẫn với hiệu quả cao, tốc độ load nhanh và mã tối thiểu. Các website sử dụng ReactJS có khả năng chạy nhanh, mượt và có khả năng mở rộng cao, thao tác thực hiện đơn giản.
Trước khi có ReactJS, lập trình viên thường gặp rất nhiều khó khăn trong việc sử dụng “vanilla JavaScript” (JavaScript thuần) và JQuery để xây dựng UI. Điều đó đồng nghĩa với việc quá trình phát triển ứng dụng sẽ lâu hơn và xuất hiện nhiều bug, rủi ro hơn. Vì vậy vào năm 2011, Jordan Walke – một nhân viên của Facebook đã khởi tạo ReactJS với mục đích chính là cải thiện quá trình phát triển UI.
Ngoài việc hỗ trợ xây dựng giao diện nhanh, hạn chế lỗi trong quá trình code, cải thiện hiệu suất website thì ReactJS còn có các ưu điểm dưới đây:
• Phù hợp với đa dạng thể loại website: ReactJS khiến cho việc khởi tạo website dễ dàng hơn bởi vì bạn không cần phải code nhiều như khi tạo trang web thuần chỉ dùng JavaScript, HTML và nó có thể cung cấp cho bạn nhiều thư viện để bạn có thể dùng cho nhiều trường hợp.
• Tái sử dụng các Component: Nếu bạn xây dựng các Component đủ tốt, đủ flexible để có thể thoả các “yêu cầu” của nhiều dự án khác nhau, bạn chỉ tốn thời gian xây dựng ban đầu và sử dụng lại hầu như toàn bộ ở các dự án sau.
• Có thể sử dụng cho cả Mobile Application: Hầu hết chúng ta đều biết rằng ReactJS được sử dụng cho việc lập trình website, nhưng thực chất nó được sinh ra không chỉ làm mỗi đều
đó. Nếu bạn cần phát triển thêm ứng dụng Mobile thì có thể sử dụng thêm React Native –
một framework khác được phát triển bởi chính Facebook, do đó có thể dễ dàng “chia sẻ”
các Component hoặc sử dụng lại các Business Logic trong ứng dụng.
• Debug dễ dàng: Facebook đã phát hành 1 Chrome Extension dùng để hỗ trợ việc debug
trong quá trình phát triển ứng dụng. Điều đó giúp tăng tốc quá trình hoàn thiện sản phẩm
cung như quá trình phát triển của lập trình viên.
Có thể thấy ReactJS là một thư viện rất thú vị và dễ sử dụng. Chính vì những lý do trên mà
nhóm quyết định sử dụng thư viện ReactJS để hiện thực hệ thống trên nền tảng Web.
1.1.3 Tailwind v4
Tailwind CSS v4 là phiên bản mới nhất của framework CSS tiện ích đầu cuối phổ biến –
Tailwind CSS. Khác với các framework như Bootstrap vốn cung cấp các thành phần UI được
định nghĩa sẵn, Tailwind hoạt động dựa trên triết lý "utility-first", nghĩa là nhà phát triển sử
dụng các lớp CSS nhỏ, tái sử dụng được để xây dựng giao diện một cách linh hoạt và tùy biến
cao.
Một trong những ưu điểm nổi bật của Tailwind CSS v4 là khả năng tối ưu hóa kích thước tệp
CSS vượt trội nhờ tree-shaking tự động – loại bỏ tất cả các lớp không được sử dụng khi build,
giúp thời gian tải trang nhanh hơn. Tailwind còn tương thích tốt với nhiều framework hiện đại
như React, Vue, Svelte và cả HTML thuần.

Tailwind v4 cũng đi kèm với hệ thống JIT (Just-In-Time) compiler được cải tiến, cho phép
hiển thị lớp ngay khi đang code, không cần định nghĩa trước. Điều này không chỉ giúp tăng tốc
độ phát triển mà còn mang lại trải nghiệm phát triển linh hoạt và trực quan.
Với hệ sinh thái mạnh mẽ, tài liệu rõ ràng và cộng đồng đông đảo, Tailwind CSS v4 đang dần
trở thành lựa chọn hàng đầu cho việc xây dựng giao diện hiện đại, tùy chỉnh cao và tối ưu hiệu
suất. Chính vì vậy, nhóm quyết định sử dụng Tailwind CSS v4 để thiết kế giao diện hệ thống
web của mình.
1.2 Các công nghệ về Mobile
1.2.1 React Native
Hình 22: React Native
React Native được phát triển bởi Facebook với mục đích ban đầu là áp dụng vào mạng xã
hội lớn nhất hành tinh: Facebook. Do đặc tính công nghệ của mạng xã hội, Facebook cần phải
tạo ra nền tảng phát triển ứng dụng di động đa nền tảng có hiệu năng không thua kém so với
ứng dụng được phát triển độc lập cho từng nền tảng. React Native hiện tại chỉ hỗ trợ phát triển
ứng dụng di động hệ điều hành Android và iOS, ít hơn so với Ionic (Android, iOS, Windows
Phone). React Native chính thức trở thành mã nguồn mở vào tháng 3 năm 2015. Cho đến nay,
React Native được áp dụng trong nhiều ứng dụng của nước ta và cả nước ngoài. React Native có
các ưu điểm sau đây:
• Có khả năng tái sử dụng code: Đối với React Native, các lập trình viên hoàn toàn có thể
tái sử dụng lại code trong khi phát triển ứng dụng đa nền tảng. Khả năng này đóng một vai
trò cực kỳ quan trọng. Nó mang đến khá nhiều lợi ích cho người dùng như: Tiết kiệm thời
gian và chi phí để tạo ra một ứng dụng tuyệt vời phục vụ cho nhu cầu của con người, tận
dụng tối đa nguồn nhân lực, duy trì ít code và hiếm khi xảy ra lỗi trong quá trình vận hành,
các tính năng giữa hai nền tảng cũng gần như có nét tương đồng.
• Sở hữu cộng đồng hỗ trợ cực kỳ lớn mạnh: Nhờ có nhiều ưu điểm tuyệt vời mà hiện nay,
React Native đang dần trở nên phổ biến hơn bao giờ hết. Bên cạnh đó, nó còn nhận được
sự đóng góp nhiệt tình của đại đa số các lập trình viên, ngày càng trở nên hoàn thiện và tốt
hơn rất nhiều. Quan trọng hơn hết, React Native còn nhận được sự hậu thuẫn của tập đoàn
lớn mạnh nhất thế giới hiện nay, đó chính là Facebook.
• Có tính ổn định và khả năng tối ưu cao: Do được phát triển bởi một tập đoàn lớn như
Facebook nên React Native có tính ổn định khá cao bởi vì: Các mã code trong ngôn ngữ
này có thể đơn giản hóa quá trình xử lý dữ liệu, đội ngũ phát triển ứng dụng không cần
có quá nhiều người, việc xây dựng ứng dụng không cần quá nhiều native code cho mỗi hệ
điều hành khác nhau.
1.3 Các công nghệ về Back-end
1.3.1 Spring Framework
Spring Framework là một trong những framework Java nổi tiếng nhất, được phát triển bởi
Rod Johnson vào năm 2003. Nó cung cấp một mô hình lập trình và cấu trúc toàn diện cho các
ứng dụng Java hiện đại - từ các ứng dụng doanh nghiệp mức độ cao đến các dịch vụ web nhẹ và
viễn thông. Spring tạo ra một khung làm việc mạnh mẽ dựa trên nguyên tắc IoC (Inversion of
Control) để quản lý các thành phần ứng dụng. Các thành phần chính của Spring Framework:
• Spring Core Container: Là trái tim của Spring Framework, bao gồm Spring IoC (Inversion
of Control) container. Container này quản lý việc tạo và cấu hình các bean, các thành phần
Java được quản lý bởi Spring. Quản lý này dựa trên cấu hình định nghĩa trong XML hoặc
các annotation.
• Spring AOP (Aspect-Oriented Programming): Cho phép các nhà phát triển tách biệt các
mối quan tâm chéo (cross-cutting concerns) như ghi log, giao dịch, hoặc kiểm tra bảo mật
từ logic nghiệp vụ chính.
• Spring MVC (Model-View-Controller): Là một framework web mạnh mẽ và linh hoạt dựa
trên mô hình MVC. Nó giúp xây dựng các ứng dụng web và RESTful services dễ dàng hơn
và linh hoạt hơn.
• Spring Data Access/Integration: Spring cung cấp trợ giúp cho việc làm việc với các công
nghệ truy cập dữ liệu như JDBC, Hibernate, JPA hoặc JDO, giúp giảm bớt công việc cần
thiết để tương tác trực tiếp với cơ sở dữ liệu.
• Spring Transaction Management: Cung cấp một mô hình trừu tượng hóa cho quản lý giao
dịch, làm cho mã của bạn không bị ràng buộc với JTA hoặc một container J2EE cụ thể.
Các module đặc biệt khác của Spring:
• Spring Security: Là một module mạnh mẽ cho xác thực và phân quyền, bảo vệ các ứng
dụng web.
• Spring Boot: Được thiết kế để đơn giản hóa việc thiết lập và phát triển các ứng dụng mới
Spring, cung cấp các cấu hình mặc định cho ứng dụng để bạn có thể nhanh chóng bắt đầu
phát triển.
• Spring Cloud: Tập trung vào việc cung cấp các công cụ tốt để phát triển các ứng dụng dựa
trên đám mây.
Spring làm việc dựa trên nguyên lý "convention over configuration" (quy ước thay vì cấu
hình), nghĩa là nếu chúng ta theo một số quy tắc đơn giản khi thiết kế ứng dụng, Spring sẽ yêu
cầu rất ít cấu hình. Kiến trúc của Spring được thiết kế để làm cho các ứng dụng của ta trở nên
dễ dàng bảo trì và mở rộng hơn:
• Inversion of Control (IoC): Trong Spring, IoC được sử dụng để tăng cường sự khớp nối
lỏng lẻo thông qua cơ chế điều khiển ngược. Thay vì các thành phần phần mềm tạo ra hoặc
tìm kiếm các đối tượng phụ thuộc, chúng chỉ đơn giản là khai báo các phụ thuộc. Container
Spring sau đó sẽ chịu trách nhiệm liên kết các phụ thuộc này.
• Dependency Injection (DI): Là một mẫu thiết kế mà Spring sử dụng để triển khai IoC, cho
phép loại bỏ sự phụ thuộc cứng rắn giữa các thành phần.
Các lợi ích khi sử dụng Spring có thể kể đến như:

• Dễ dàng tích hợp: Spring có thể dễ dàng tích hợp với các công nghệ khác, bao gồm các
ORM framework, các công cụ đám mây, và các dịch vụ web khác nhau.
• Quản lý vòng đời ứng dụng: Spring quản lý vòng đời của các bean và nhiều đối tượng khác
bằng cách sử dụng IoC, giảm bớt gánh nặng quản lý các đối tượng này.
• Cộng đồng và hỗ trợ mạnh mẽ: Có một cộng đồng lớn các nhà phát triển sử dụng và hỗ trợ
Spring, làm cho việc tìm kiếm tài nguyên học tập và hỗ trợ trở nên dễ dàng.
1.3.2 RESTful API
RESTful API là một tiêu chuẩn dùng trong việc thiết kế API cho các ứng dụng web (thiết kế
Web services) để tiện cho việc quản lý các resource. Nó chú trọng vào tài nguyên hệ thống (tệp
văn bản, ảnh, âm thanh, video, hoặc dữ liệu động...), bao gồm các trạng thái tài nguyên được
định dạng và được truyền tải qua HTTP.
• API (Application Programming Interface) là một tập các quy tắc và cơ chế mà theo đó,
một ứng dụng hay một thành phần sẽ tương tác với một ứng dụng hay thành phần khác.
API có thể trả về dữ liệu mà bạn cần cho ứng dụng của mình ở những kiểu dữ liệu phổ biến
như JSON hay XML.
• REST (REpresentational State Transfer) là một dạng chuyển đổi cấu trúc dữ liệu, một kiểu
kiến trúc để viết API. Nó sử dụng phương thức HTTP đơn giản để tạo cho giao tiếp giữa
các máy. Vì vậy, thay vì sử dụng một URL cho việc xử lý một số thông tin người dùng,
REST gửi một yêu cầu HTTP như GET, POST, DELETE,... đến một URL để xử lý dữ liệu.
• RESTful API là một tiêu chuẩn dùng trong việc thiết kế các API cho các ứng dụng web
để quản lý các resource. RESTful là một trong những kiểu thiết kế API được sử dụng phổ
biến ngày nay để cho các ứng dụng (web, mobile...) khác nhau giao tiếp với nhau.
REST hoạt động chủ yếu dựa vào giao thức HTTP. Các hoạt động cơ bản nêu trên sẽ sử dụng
những phương thức HTTP riêng. Những phương thức hay hoạt động này thường được gọi là
CRUD tương ứng với Create, Read, Update, Delete – Tạo, Đọc, Sửa, Xóa.
• GET (SELECT): Trả về một Resource hoặc một danh sách Resource.
• POST (CREATE): Tạo mới một Resource.
• PUT (UPDATE): Cập nhật thông tin cho Resource.
Xây dựng Hệ thống quản lý cho hệ thống các quán cà phê văn phòng Trang 27/127
Trường Đại Học Bách Khoa - Đại học Quốc gia TP.HCM
Khoa Khoa Học & Kỹ Thuật Máy Tính
• DELETE (DELETE): Xoá một Resource.
Dưới đây là một số ưu điểm của RESTful API:
• Dễ hiểu, dễ học, đơn giản.
• Cho phép tổ chức các ứng dụng phức tạp, dễ dàng sử dụng tài nguyên. Quản lý tải cao nhờ
HTTP proxy server và cache.
• Các client mới có thể dễ dàng làm việc trên những ứng dụng khác.
• Cho phép sử dụng các lệnh gọi thủ tục HTTP tiêu chuẩn để truy xuất dữ liệu và request.
• RESTful API dựa trên code và có thể sử dụng nó để đồng bộ hoá dữ liệu bằng website.
• Cung cấp các định dạng linh hoạt bằng cách tuần tự hoá (serialize) dữ liệu ở dạng XML
hay JSON.
• Cho phép sử dụng các giao thức OAuth để xác thực request REST.
1.4 Hệ quản trị cơ sở dữ liệu
1.4.1 MySQL
MySQL là một hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) được phát triển lần đầu vào năm
1995. Là một phần mềm nguồn mở, nó được phát triển và hỗ trợ bởi Oracle, và nổi tiếng với khả
năng tương thích tốt với các ứng dụng web. MySQL sử dụng ngôn ngữ truy vấn cấu trúc SQL
(Structured Query Language) để quản lý và xử lý dữ liệu trong các cơ sở dữ liệu quan hệ. Dưới
đây là các ưu điểm khi sử dụng MySQL:
• Dễ sử dụng: MySQL nổi bật với giao diện người dùng thân thiện và tài liệu hướng dẫn rõ
ràng, giúp người mới bắt đầu có thể dễ dàng làm quen và sử dụng.
• Hiệu suất cao: MySQL được thiết kế với kiến trúc đa luồng và đa người dùng, giúp nó có
khả năng xử lý một lượng lớn truy vấn và giao dịch cùng một lúc mà vẫn đảm bảo hiệu
suất ổn định.
• Bảo mật mạnh mẽ: MySQL cung cấp nhiều cơ chế bảo mật, bao gồm xác thực mật khẩu,
mã hóa dữ liệu trên đĩa, và quản lý quyền truy cập chi tiết để bảo vệ dữ liệu khỏi các mối
đe dọa bên ngoài và bên trong.
• Tính linh hoạt và khả năng mở rộng: MySQL có thể chạy trên nhiều hệ điều hành khác
nhau như Linux, Windows, và macOS. Nó cũng cho phép người dùng mở rộng cơ sở dữ
liệu một cách linh hoạt để đáp ứng nhu cầu của doanh nghiệp mà không ảnh hưởng đến
hiệu suất.
• Chi phí thấp: Là một giải pháp nguồn mở, MySQL giúp giảm thiểu chi phí bản quyền và
phát triển, làm cho nó trở thành lựa chọn hấp dẫn cho cả doanh nghiệp nhỏ và lớn.
Xây dựng Hệ thống quản lý cho hệ thống các quán cà phê văn phòng Trang 28/127
So sánh với các hệ quản trị cơ sở dữ liệu khác phổ biến trên thế giới hiện nay thì MySQL có
những đặc điểm nổi trội sau:
• So với Oracle Database: Tuy không có đầy đủ các tính năng cao cấp như Oracle, MySQL
vẫn cung cấp hiệu suất rất tốt cho đa số các ứng dụng web và doanh nghiệp, với chi phí
thấp hơn đáng kể so với Oracle, đặc biệt là trong môi trường nguồn mở.
• So với Microsoft SQL Server: Mặc dù không có các công cụ phân tích mạnh mẽ như
SQL Server, MySQL lại rất mạnh mẽ khi được sử dụng trong các ứng dụng web, dễ dàng
tích hợp với các nền tảng như PHP và các máy chủ web phổ biến.
• So với PostgreSQL: Không có sự hỗ trợ đầy đủ cho một số kiểu dữ liệu phức tạp như
PostgreSQL, nhưng lại nổi bật với tính sẵn sàng cao và dễ quản lý, đặc biệt thích hợp cho
các ứng dụng yêu cầu truy cập nhanh và hiệu suất cao.
1.4.2 Firebase Storage
Firebase Storage, một phần quan trọng của nền tảng Firebase do Google phát triển, là một
dịch vụ lưu trữ đám mây được thiết kế để cung cấp giải pháp lưu trữ dữ liệu an toàn, hiệu quả
và dễ dàng mở rộng cho các nhà phát triển ứng dụng di động và web. Dịch vụ này được tích hợp
chặt chẽ với các sản phẩm Firebase khác và Google Cloud Platform, mang lại lợi ích đáng kể
trong việc xử lý các tác vụ liên quan đến lưu trữ và truyền tải dữ liệu.
Hình 26: Firebase Cloud Storage
Firebase Storage đặc biệt hữu ích trong các tình huống mà ứng dụng yêu cầu khả năng quản
lý các tệp đa phương tiện như hình ảnh, video hoặc các tệp lớn khác. Với cơ sở hạ tầng mạnh
mẽ của Google đứng sau, Firebase Storage có thể xử lý lưu lượng truy cập lớn và tải dữ liệu
nặng một cách dễ dàng, đồng thời cung cấp khả năng mở rộng tự động để đáp ứng nhu cầu tăng
trưởng của ứng dụng mà không cần can thiệp thủ công vào hệ thống lưu trữ. Firebase Storage
bao gồm các ưu điểm nổi bật như sau:
• Tính năng tự động mở rộng: Firebase Storage được thiết kế để tự động mở rộng, cho phép
lưu trữ dữ liệu từ hàng trăm đến hàng triệu người dùng mà không cần phải lo lắng về việc
mở rộng hạ tầng. Dịch vụ này sử dụng cơ sở hạ tầng của Google Cloud Storage, đảm bảo
tính sẵn có cao và độ tin cậy cho dữ liệu.
• Bảo mật và quản lý quyền truy cập: Firebase Storage cung cấp các tùy chọn bảo mật mạnh
mẽ, cho phép nhà phát triển định cấu hình quyền truy cập tệp chi tiết thông qua Firebase
Security Rules. Các quy tắc này có thể được thiết lập dựa trên xác thực người dùng, vai trò
người dùng, và các thuộc tính tệp khác, đảm bảo rằng chỉ có người dùng được phép mới
có thể truy cập hoặc sửa đổi dữ liệu.
• Hiệu quả chi phí: Với Firebase Storage, bạn chỉ trả tiền cho lượng lưu trữ và băng thông
mà bạn sử dụng, không có chi phí ẩn hoặc yêu cầu cam kết sử dụng trước. Điều này làm
cho Firebase Storage trở thành một lựa chọn hấp dẫn cho các dự án với mọi quy mô, từ nhỏ
đến lớn.
• Tối ưu hóa cho việc tải lên và tải xuống: Firebase Storage tối ưu hóa quá trình tải lên và tải
xuống, đặc biệt là với các tệp kích thước lớn hoặc các mạng có độ trễ cao. Nó hỗ trợ tải lên
tiếp tục, cho phép người dùng tạm dừng và tiếp tục tải lên mà không mất dữ liệu đã tải lên
trước đó.
• Tích hợp mạnh mẽ: Firebase Storage có thể được dễ dàng tích hợp với các dịch vụ khác
của Firebase như Firestore, Firebase Functions, và Firebase Authentication, tạo điều kiện
cho việc phát triển ứng dụng đa năng và mạnh mẽ hơn. Ví dụ, có thể tự động kích hoạt một
chức năng để xử lý hình ảnh khi một hình ảnh mới được tải lên, như thay đổi kích thước
hoặc áp dụng bộ lọc.
• Thân thiện với nhà phát triển: Firebase Storage cung cấp SDK cho cả iOS, Android, và
web, giúp việc tương tác với dịch vụ lưu trữ trở nên dễ dàng và trực quan. SDK bao gồm
nhiều tính năng giúp xử lý các nhiệm vụ phổ biến như tải lên, tải xuống, và hiển thị hình
ảnh trực tiếp từ kho lưu trữ.



