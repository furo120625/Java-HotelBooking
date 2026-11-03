package hotelbooking.service;

import hotelbooking.dao.DataStore;
import hotelbooking.model.*;
import hotelbooking.util.PasswordUtil;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public final class HotelService {
    private final DataStore data;

    public HotelService(DataStore data) {
        this.data = data;
    }

    public User login(String username, char[] password) {
        User user = data.users().get(key(username));
        if (user == null || user.getStatus() != AccountStatus.ACTIVE
                || !PasswordUtil.verify(password, user.getSalt(), user.getPasswordHash())) {
            return null;
        }
        data.audit(user.getUsername(), "LOGIN", "Đăng nhập thành công");
        return user;
    }

    public void register(String username, char[] password, String phone) throws IOException {
        String name = username == null ? "" : username.trim();
        if (!name.matches("[A-Za-z0-9_.-]{4,20}")) {
            throw new IllegalArgumentException("Tên đăng nhập phải có 4–20 ký tự chữ, số, _, dấu chấm hoặc -.");
        }
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (phone == null || !phone.matches("[0-9+() .-]{9,15}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        if (data.users().containsKey(key(name))) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        String salt = PasswordUtil.newSalt();
        User user = new User(name, salt, PasswordUtil.hash(password, salt), phone.trim(),
                Role.GUEST, AccountStatus.ACTIVE);
        data.users().put(key(name), user);
        data.saveUsers();
        data.audit(name, "REGISTER", "Tạo tài khoản GUEST");
    }

    public void updateProfile(User actor, String phone, char[] newPassword) throws IOException {
        requireActive(actor);
        if (phone == null || !phone.matches("[0-9+() .-]{9,15}")) {
            throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        }
        actor.setPhone(phone.trim());
        if (newPassword != null && newPassword.length > 0) {
            if (newPassword.length < 8) throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự.");
            // User is replaced because its salt/hash are immutable.
            String salt = PasswordUtil.newSalt();
            User replacement = new User(actor.getUsername(), salt,
                    PasswordUtil.hash(newPassword, salt), phone.trim(),
                    actor.getRole(), actor.getStatus());
            data.users().put(key(actor.getUsername()), replacement);
        }
        data.saveUsers();
        data.audit(actor.getUsername(), "UPDATE_PROFILE", "Cập nhật thông tin cá nhân");
    }

    public List<Room> getRooms() {
        return data.rooms().values().stream()
                .sorted(Comparator.comparing(Room::getId)).collect(Collectors.toList());
    }

    public boolean isAvailable(String roomId, LocalDate checkIn, LocalDate checkOut) {
        expirePending();
        if (!data.rooms().containsKey(roomId) || checkIn == null || checkOut == null
                || !checkOut.isAfter(checkIn)) return false;
        return data.bookings().values().stream().noneMatch(b ->
                b.getRoomIds().contains(roomId)
                && b.getStatus().blocksAvailability()
                && checkIn.isBefore(b.getCheckOut())
                && b.getCheckIn().isBefore(checkOut));
    }

    public synchronized Booking createBooking(User actor, String guestName, String cccd, String phone,
                                 int guestCount, List<String> roomIds,
                                 LocalDate checkIn, LocalDate checkOut) throws IOException {
        requireActive(actor);
        if (roomIds == null || roomIds.isEmpty()) throw new IllegalArgumentException("Chọn ít nhất một phòng.");
        if (guestName == null || guestName.trim().isEmpty()) throw new IllegalArgumentException("Nhập họ tên khách.");
        if (cccd == null || !cccd.matches("\\d{9}|\\d{12}")) throw new IllegalArgumentException("CCCD phải gồm 9 hoặc 12 chữ số.");
        if (phone == null || !phone.matches("[0-9+() .-]{9,15}")) throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        if (guestCount <= 0) throw new IllegalArgumentException("Số lượng khách phải lớn hơn 0.");
        if (checkIn == null || checkOut == null || checkIn.isBefore(LocalDate.now()) || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Khoảng ngày nhận/trả phòng không hợp lệ.");
        }
        int capacity = 0;
        long pricePerNight = 0;
        Set<String> uniqueRooms = new LinkedHashSet<>(roomIds);
        if (uniqueRooms.size() != roomIds.size()) throw new IllegalArgumentException("Danh sách phòng bị trùng.");
        for (String id : uniqueRooms) {
            Room room = data.rooms().get(id);
            if (room == null) throw new IllegalArgumentException("Không tìm thấy phòng " + id + ".");
            if (!isAvailable(id, checkIn, checkOut)) throw new IllegalArgumentException("Phòng " + id + " đã có lượt đặt trùng ngày.");
            capacity += room.getCapacity();
            pricePerNight = Math.addExact(pricePerNight, room.getPricePerNight());
        }
        if (guestCount > capacity) throw new IllegalArgumentException("Sức chứa đã chọn chỉ tối đa " + capacity + " khách.");
        long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        long total = Math.multiplyExact(pricePerNight, nights);
        long deposit = (total + 1) / 2;
        String id = "BM-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        Booking booking = new Booking(id, actor.getUsername(), guestName.trim(), cccd.trim(),
                phone.trim(), guestCount, checkIn, checkOut, new ArrayList<>(uniqueRooms),
                total, deposit, BookingStatus.PENDING_PAYMENT, LocalDateTime.now());
        data.bookings().put(id, booking);
        data.payments().put(id, "PENDING");
        data.saveBookings();
        data.savePayments();
        data.audit(actor.getUsername(), "CREATE_BOOKING", id + " | " + String.join(",", uniqueRooms));
        return booking;
    }

    public List<Booking> getBookings(User actor) {
        expirePending();
        return data.bookings().values().stream()
                .filter(b -> actor.getRole() != Role.GUEST || b.getCreatedBy().equalsIgnoreCase(actor.getUsername()))
                .sorted(Comparator.comparing(Booking::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void confirmDeposit(User actor, String bookingId, String reference) throws IOException {
        requireStaffOrAdmin(actor);
        Booking booking = requireBooking(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalArgumentException("Lượt đặt không ở trạng thái chờ thanh toán.");
        }
        if (reference == null || reference.trim().length() < 3) {
            throw new IllegalArgumentException("Nhập mã giao dịch hoặc ghi chú xác nhận.");
        }
        booking.setStatus(BookingStatus.CONFIRMED);
        data.payments().put(bookingId, "PAID|" + reference.trim() + "|" + actor.getUsername());
        data.saveBookings();
        data.savePayments();
        data.audit(actor.getUsername(), "CONFIRM_DEPOSIT", bookingId + " | " + reference.trim());
    }

    public void cancelBooking(User actor, String bookingId) throws IOException {
        Booking booking = requireBooking(bookingId);
        boolean privileged = actor.getRole() == Role.ADMIN || actor.getRole() == Role.STAFF;
        if (!privileged && !booking.getCreatedBy().equalsIgnoreCase(actor.getUsername())) {
            throw new SecurityException("Bạn không có quyền hủy lượt đặt này.");
        }
        if (booking.getStatus() == BookingStatus.CHECKED_IN
                || booking.getStatus() == BookingStatus.CHECKED_OUT
                || booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.EXPIRED) {
            throw new IllegalArgumentException("Không thể hủy lượt đặt ở trạng thái " + booking.getStatus() + ".");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        String paymentState = data.payments().getOrDefault(bookingId, "UNPAID");
        data.payments().put(bookingId, paymentState.startsWith("PAID|")
                ? "REFUND_REVIEW|" + paymentState : "CANCELLED");
        data.saveBookings();
        data.savePayments();
        data.audit(actor.getUsername(), "CANCEL_BOOKING", bookingId);
    }

    public void checkIn(User actor, String bookingId) throws IOException {
        requireStaffOrAdmin(actor);
        Booking b = requireBooking(bookingId);
        if (b.getStatus() != BookingStatus.CONFIRMED) throw new IllegalArgumentException("Chỉ nhận phòng với lượt đặt đã xác nhận.");
        if (LocalDate.now().isBefore(b.getCheckIn())) throw new IllegalArgumentException("Chưa đến ngày nhận phòng.");
        if (LocalDate.now().isAfter(b.getCheckOut())) throw new IllegalArgumentException("Đã quá ngày trả phòng.");
        b.setStatus(BookingStatus.CHECKED_IN);
        data.saveBookings();
        data.audit(actor.getUsername(), "CHECK_IN", bookingId);
    }

    public void checkOut(User actor, String bookingId) throws IOException {
        requireStaffOrAdmin(actor);
        Booking b = requireBooking(bookingId);
        if (b.getStatus() != BookingStatus.CHECKED_IN) throw new IllegalArgumentException("Lượt đặt chưa check-in.");
        b.setStatus(BookingStatus.CHECKED_OUT);
        data.saveBookings();
        data.audit(actor.getUsername(), "CHECK_OUT", bookingId);
    }

    public List<User> getUsers(User actor) {
        requireAdmin(actor);
        return data.users().values().stream()
                .sorted(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public void createStaff(User actor, String username, char[] password, String phone) throws IOException {
        requireAdmin(actor);
        String name = username == null ? "" : username.trim();
        if (!name.matches("[A-Za-z0-9_.-]{4,20}") || password == null || password.length < 8) {
            throw new IllegalArgumentException("Tên tài khoản 4–20 ký tự và mật khẩu tối thiểu 8 ký tự.");
        }
        if (phone == null || !phone.matches("[0-9+() .-]{9,15}")) throw new IllegalArgumentException("Số điện thoại không hợp lệ.");
        if (data.users().containsKey(key(name))) throw new IllegalArgumentException("Tài khoản đã tồn tại.");
        String salt = PasswordUtil.newSalt();
        data.users().put(key(name), new User(name, salt, PasswordUtil.hash(password, salt),
                phone.trim(), Role.STAFF, AccountStatus.ACTIVE));
        data.saveUsers();
        data.audit(actor.getUsername(), "CREATE_STAFF", name);
    }

    public void setUserRole(User actor, String username, Role role) throws IOException {
        requireAdmin(actor);
        User target = data.users().get(key(username));
        if (target == null) throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        if (target.getRole() == Role.ADMIN && role != Role.ADMIN && countActiveAdmins() <= 1) {
            throw new IllegalArgumentException("Không thể hạ quyền Admin cuối cùng.");
        }
        target.setRole(role);
        data.saveUsers();
        data.audit(actor.getUsername(), "CHANGE_ROLE", username + " -> " + role);
    }

    public void setUserStatus(User actor, String username, AccountStatus status) throws IOException {
        requireAdmin(actor);
        User target = data.users().get(key(username));
        if (target == null) throw new IllegalArgumentException("Không tìm thấy tài khoản.");
        if (target.getUsername().equalsIgnoreCase(actor.getUsername()) && status == AccountStatus.LOCKED) {
            throw new IllegalArgumentException("Không thể tự khóa tài khoản đang đăng nhập.");
        }
        if (target.getRole() == Role.ADMIN && status == AccountStatus.LOCKED && countActiveAdmins() <= 1) {
            throw new IllegalArgumentException("Không thể khóa Admin cuối cùng đang hoạt động.");
        }
        target.setStatus(status);
        data.saveUsers();
        data.audit(actor.getUsername(), "CHANGE_ACCOUNT_STATUS", username + " -> " + status);
    }

    public void updateRoomPrice(User actor, String roomId, long price) throws IOException {
        requireAdmin(actor);
        Room room = data.rooms().get(roomId);
        if (room == null) throw new IllegalArgumentException("Không tìm thấy phòng.");
        room.setPricePerNight(price);
        data.savePrices();
        data.audit(actor.getUsername(), "UPDATE_ROOM_PRICE", roomId + " -> " + price);
    }

    public Map<String, String> getPaymentStates() {
        return Collections.unmodifiableMap(data.payments());
    }

    private void expirePending() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(20);
        boolean changed = false;
        for (Booking b : data.bookings().values()) {
            if (b.getStatus() == BookingStatus.PENDING_PAYMENT && b.getCreatedAt().isBefore(cutoff)) {
                b.setStatus(BookingStatus.EXPIRED);
                changed = true;
            }
        }
        if (changed) {
            try { data.saveBookings(); }
            catch (IOException ex) { throw new IllegalStateException(ex); }
        }
    }

    private long countActiveAdmins() {
        return data.users().values().stream()
                .filter(u -> u.getRole() == Role.ADMIN && u.getStatus() == AccountStatus.ACTIVE).count();
    }

    private Booking requireBooking(String id) {
        Booking b = data.bookings().get(id);
        if (b == null) throw new IllegalArgumentException("Không tìm thấy lượt đặt " + id + ".");
        return b;
    }

    private void requireActive(User actor) {
        if (actor == null || actor.getStatus() != AccountStatus.ACTIVE
                || data.users().get(key(actor.getUsername())) == null) {
            throw new SecurityException("Tài khoản không hợp lệ hoặc đã bị khóa.");
        }
    }

    private void requireStaffOrAdmin(User actor) {
        requireActive(actor);
        if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.STAFF) {
            throw new SecurityException("Chức năng này chỉ dành cho Staff/Admin.");
        }
    }

    private void requireAdmin(User actor) {
        requireActive(actor);
        if (actor.getRole() != Role.ADMIN) throw new SecurityException("Chỉ Admin được sử dụng chức năng này.");
    }

    private static String key(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}