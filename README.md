# Java Hotel Booking – Bình Minh

Ứng dụng quản lý khách sạn desktop bằng Java Swing (NetBeans Ant project).

## Yêu cầu chạy
- JDK 21 trở lên
- NetBeans hoặc Apache Ant

## Chạy ứng dụng
Mở project bằng NetBeans, Clean and Build, sau đó Run. Entry point: `hotelbooking.Main`.

## Phân quyền
- **ADMIN**: quản lý tài khoản, vai trò, khóa/mở khóa tài khoản, giá phòng, toàn bộ booking.
- **STAFF**: quản lý booking, xác nhận tiền cọc thủ công, check-in/check-out.
- **GUEST**: xem phòng, tự đặt phòng, thanh toán cọc, xem và hủy booking của chính mình.

Tài khoản đăng ký công khai luôn được tạo với vai trò GUEST. Chỉ Admin có thể cấp quyền cao hơn. Tài khoản có giao dịch không bị xóa; dùng trạng thái LOCKED để khóa.

## Dữ liệu và di trú
Khi chạy lần đầu, ứng dụng tạo thư mục `data/`:
- `users.txt`: tài khoản, salt và PBKDF2 hash; không lưu mật khẩu rõ.
- `bookings.txt`: từng lượt đặt riêng, có Booking ID.
- `payments.txt`: trạng thái và ghi chú đối soát tiền cọc.
- `rooms.txt`: giá phòng.
- `audit.log`: nhật ký thao tác.

Nếu chưa có `data/users.txt`, ứng dụng nhập tài khoản từ `TK.txt`; mật khẩu cũ được chuyển thành PBKDF2 hash. Nếu chưa có `data/bookings.txt`, ứng dụng nhập các phòng đang đặt từ `DP.txt` thành Booking lịch sử. Hai file nguồn cũ chỉ được đọc, không bị ghi đè.

**Quan trọng khi nâng cấp từ repository cũ:** `TK.txt` và `DP.txt` đã được loại khỏi branch vì chúng chứa mật khẩu rõ và dữ liệu cá nhân. Trước khi chuyển branch hoặc pull/merge phiên bản mới, hãy sao lưu hai file này ra ngoài thư mục repository. Sau khi cập nhật code, chép bản sao vào thư mục gốc project, chạy ứng dụng đúng một lần để di trú sang `data/`; sau khi xác nhận đăng nhập và lịch sử đặt phòng đã được nhập đủ, cất bản sao legacy ở nơi an toàn và không commit chúng. Nếu `data/users.txt` hoặc `data/bookings.txt` đã tồn tại, chương trình sẽ không nhập lại file legacy tương ứng.

Thư mục `data/` là dữ liệu runtime, không nên commit lên GitHub. Không đưa file chứa mật khẩu, CCCD hoặc số điện thoại thật vào repository công khai.

## Đặt phòng
Khả năng đặt được tính theo khoảng ngày bằng điều kiện giao nhau; phòng có thể được đặt nhiều lần nếu các khoảng lưu trú không chồng lấn. Booking mới ở trạng thái `PENDING_PAYMENT` trong 20 phút; hết hạn sẽ chuyển `EXPIRED` và nhả phòng. Tiền cọc bằng 50% tổng giá phòng × số đêm.

## VietQR
QR chỉ được hiển thị khi máy chạy ứng dụng có đủ biến môi trường:
- `VIETQR_BANK_ID`
- `VIETQR_ACCOUNT_NO`
- `VIETQR_ACCOUNT_NAME`

Nếu chưa cấu hình, ứng dụng vẫn tạo Booking chờ cọc và hiển thị nội dung chuyển khoản. Staff/Admin phải đối soát giao dịch và xác nhận thủ công; việc quét QR không tự động làm Booking chuyển sang CONFIRMED.

## Giới hạn triển khai hiện tại
Dữ liệu đang được lưu cục bộ bằng các file TXT có cấu trúc trong `data/`, chưa phải SQLite/MySQL và chưa phù hợp cho nhiều máy cùng thao tác đồng thời. Tích hợp xác nhận thanh toán tự động cần API/webhook từ ngân hàng hoặc đơn vị trung gian và thông tin tài khoản nhận tiền hợp lệ.
