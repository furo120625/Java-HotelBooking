package hotelbooking.ui;

import hotelbooking.model.*;
import hotelbooking.service.HotelService;
import hotelbooking.util.DateUtil;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class DashboardFrame extends JFrame {
    private final HotelService service;
    private final User user;
    private final Runnable onLogout;
    private final JTabbedPane tabs = new JTabbedPane();
    private final DefaultTableModel roomModel = model("Phòng", "Tầng", "Loại", "Sức chứa", "Giá/đêm", "Tình trạng hôm nay");
    private final DefaultTableModel bookingModel;
    private final DefaultTableModel userModel = model("Tên đăng nhập", "Số điện thoại", "Vai trò", "Trạng thái");
    private final DefaultTableModel priceModel = model("Phòng", "Loại", "Giá/đêm");
    private final JTable bookingTable;
    private final JTable roomTable = new JTable(roomModel);
    private final JTable userTable = new JTable(userModel);
    private final JTable priceTable = new JTable(priceModel);
    private final JLabel summary = new JLabel();

    public DashboardFrame(HotelService service, User user, Runnable onLogout) {
        this.service = service;
        this.user = user;
        this.onLogout = onLogout;
        boolean privileged = user.getRole() != Role.GUEST;
        bookingModel = privileged
                ? model("Booking ID", "Phòng", "Khách hàng", "SĐT", "CCCD", "Nhận", "Trả", "Tổng tiền", "Tiền cọc", "Trạng thái")
                : model("Booking ID", "Phòng", "Nhận", "Trả", "Tổng tiền", "Tiền cọc", "Trạng thái");
        bookingTable = new JTable(bookingModel);

        setTitle("Khách sạn Bình Minh | " + user.getRole());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setLayout(new BorderLayout(10, 10));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
        refreshAll();
        UiStyle.apply(this);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBorder(BorderFactory.createEmptyBorder(12, 18, 8, 18));
        JLabel brand = new JLabel("BÌNH MINH HOTEL");
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 21f));
        JLabel identity = new JLabel("Xin chào, " + user.getUsername() + "  |  " + user.getRole());
        JButton newBooking = new JButton("＋ Đặt phòng");
        JButton profile = new JButton("Tài khoản");
        JButton logout = new JButton("Đăng xuất");
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.add(identity);
        right.add(newBooking);
        right.add(profile);
        right.add(logout);
        header.add(brand, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        newBooking.addActionListener(e -> openBooking());
        profile.addActionListener(e -> editProfile());
        logout.addActionListener(e -> {
            service.getBookings(user);
            dispose();
            onLogout.run();
        });
        return header;
    }

    private JComponent buildTabs() {
        tabs.addTab("Sơ đồ phòng", buildRoomsTab());
        tabs.addTab(user.getRole() == Role.GUEST ? "Lịch sử của tôi" : "Quản lý đặt phòng", buildBookingsTab());
        if (user.getRole() == Role.ADMIN) {
            tabs.addTab("Tài khoản", buildUsersTab());
            tabs.addTab("Giá phòng", buildPricesTab());
        }
        return tabs;
    }

    private JPanel buildRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        JLabel note = new JLabel("Giờ nhận phòng: 13:00 | Giờ trả phòng: 11:00. Ngày trả phòng vẫn được giữ chỗ đến hết ngày trên lịch đặt phòng.");
        panel.add(note, BorderLayout.NORTH);
        roomTable.setRowHeight(28);
        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);
        JButton refresh = new JButton("Làm mới");
        refresh.addActionListener(e -> refreshRooms());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refresh);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBookingsTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        bookingTable.setAutoCreateRowSorter(true);
        bookingTable.setRowHeight(28);
        panel.add(new JScrollPane(bookingTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Làm mới");
        JButton cancel = new JButton("Hủy Booking");
        actions.add(refresh);
        actions.add(cancel);
        refresh.addActionListener(e -> refreshBookings());
        cancel.addActionListener(e -> cancelSelected());
        if (user.getRole() == Role.GUEST) {
            JButton reopenPayment = new JButton("Mở QR thanh toán");
            JButton showCheckInCode = new JButton("Mở mã Check-in");
            actions.add(reopenPayment);
            actions.add(showCheckInCode);
            reopenPayment.addActionListener(e -> reopenPaymentSelected());
            showCheckInCode.addActionListener(e -> showCheckInCodeSelected());
        }
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.STAFF) {
            JButton confirm = new JButton("Xác nhận đã nhận cọc");
            JButton checkIn = new JButton("Check-in");
            JButton checkOut = new JButton("Check-out");
            actions.add(confirm); actions.add(checkIn); actions.add(checkOut);
            confirm.addActionListener(e -> confirmSelected());
            checkIn.addActionListener(e -> changeStatus(true));
            checkOut.addActionListener(e -> changeStatus(false));
        }
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildUsersTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        userTable.setRowHeight(28);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Làm mới");
        JButton createStaff = new JButton("Tạo Staff");
        JButton role = new JButton("Đổi vai trò");
        JButton status = new JButton("Khóa / Mở khóa");
        actions.add(refresh); actions.add(createStaff); actions.add(role); actions.add(status);
        refresh.addActionListener(e -> refreshUsers());
        createStaff.addActionListener(e -> createStaff());
        role.addActionListener(e -> changeRole());
        status.addActionListener(e -> toggleStatus());
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildPricesTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        priceTable.setRowHeight(28);
        panel.add(new JScrollPane(priceTable), BorderLayout.CENTER);
        JButton edit = new JButton("Sửa giá phòng đã chọn");
        edit.addActionListener(e -> editPrice());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(edit);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(BorderFactory.createEmptyBorder(4, 16, 10, 16));
        summary.setFont(summary.getFont().deriveFont(Font.BOLD, 13f));
        footer.add(summary, BorderLayout.WEST);
        footer.add(new JLabel("Dữ liệu lưu cục bộ trong thư mục data/"), BorderLayout.EAST);
        return footer;
    }

    private void openBooking() {
        BookingDialog dialog = new BookingDialog(this, service, user,
                booking -> {
                    refreshAll();
                    new PaymentDialog(this, booking, user).setVisible(true);
                    refreshAll();
                    tabs.setSelectedIndex(1);
                });
        dialog.setVisible(true);
    }

    private void refreshAll() {
        refreshRooms();
        refreshBookings();
        if (user.getRole() == Role.ADMIN) {
            refreshUsers();
            refreshPrices();
        }
        long active = service.getBookings(user).stream()
                .filter(b -> b.getStatus().blocksAvailability()).count();
        summary.setText("Số lượt đặt đang hoạt động: " + active
                + "    |    Ngày hệ thống: " + DateUtil.format(LocalDate.now()));
    }

    private void refreshRooms() {
        roomModel.setRowCount(0);
        LocalDate today = LocalDate.now();
        for (Room room : service.getRooms()) {
            boolean available = service.isAvailable(room.getId(), today, today.plusDays(1));
            roomModel.addRow(new Object[]{room.getId(), room.getFloor(), room.getType(),
                    room.getCapacity(), String.format("%,d đ", room.getPricePerNight()),
                    available ? "CÒN TRỐNG" : "ĐÃ CÓ LỊCH"});
        }
    }

    private void refreshBookings() {
        bookingModel.setRowCount(0);
        List<Booking> bookings = service.getBookings(user);
        for (Booking b : bookings) {
            if (user.getRole() == Role.GUEST) {
                bookingModel.addRow(new Object[]{b.getId(), String.join(", ", b.getRoomIds()),
                        DateUtil.format(b.getCheckIn()), DateUtil.format(b.getCheckOut()), String.format("%,d đ", b.getTotalAmount()),
                        String.format("%,d đ", b.getDepositAmount()), b.getStatus()});
            } else {
                bookingModel.addRow(new Object[]{b.getId(), String.join(", ", b.getRoomIds()),
                        b.getGuestName(), b.getPhone(), b.getCccd(), DateUtil.format(b.getCheckIn()), DateUtil.format(b.getCheckOut()),
                        String.format("%,d đ", b.getTotalAmount()), String.format("%,d đ", b.getDepositAmount()),
                        b.getStatus()});
            }
        }
    }

    private String selectedBookingId() {
        int row = bookingTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một Booking trước.");
            return null;
        }
        return String.valueOf(bookingTable.getValueAt(bookingTable.convertRowIndexToModel(row), 0));
    }

    private void showCheckInCodeSelected() {
        String id = selectedBookingId();
        if (id == null) return;
        try {
            String code = service.getCheckInCodeForGuest(user, id);
            JOptionPane.showMessageDialog(this,
                    "MÃ CHECK-IN CỦA BẠN\\n\\n" + code
                    + "\\n\\nHãy giữ mã này và xuất trình cho nhân viên khi nhận phòng.",
                    "Mã Check-in", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể mở mã Check-in", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void reopenPaymentSelected() {
        String id = selectedBookingId();
        if (id == null) return;
        try {
            Booking booking = service.getBookings(user).stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Booking này."));

            if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
                JOptionPane.showMessageDialog(this,
                        "Chỉ có thể mở lại QR với Booking đang chờ thanh toán.\n"
                        + "Trạng thái hiện tại: " + booking.getStatus(),
                        "Không thể mở QR", JOptionPane.INFORMATION_MESSAGE);
                refreshBookings();
                return;
            }

            new PaymentDialog(this, booking, user).setVisible(true);
            refreshAll();
        } catch (RuntimeException ex) {
            showError(ex);
        }
    }

    private void cancelSelected() {
        String id = selectedBookingId();
        if (id == null || JOptionPane.showConfirmDialog(this, "Hủy Booking " + id + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            service.cancelBooking(user, id);
            refreshAll();
        } catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void confirmSelected() {
        String id = selectedBookingId();
        if (id == null) return;

        JTextField reference = new JTextField();
        JTextField amount = new JTextField();
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Mã giao dịch ngân hàng"));
        form.add(reference);
        form.add(new JLabel("Số tiền thực nhận (VND)"));
        form.add(amount);
        form.add(new JLabel("Lưu ý"));
        form.add(new JLabel("<html>Chỉ xác nhận sau khi đã kiểm tra giao dịch<br>trên tài khoản ngân hàng.</html>"));

        if (JOptionPane.showConfirmDialog(this, form, "Đối soát tiền cọc: " + id,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) return;
        try {
            String rawAmount = amount.getText().trim().replace(",", "").replace(".", "");
            long receivedAmount = Long.parseLong(rawAmount);
            int approval = JOptionPane.showConfirmDialog(this,
                    "Bạn xác nhận đã trực tiếp kiểm tra sao kê ngân hàng và nhận đủ "
                    + String.format("%,d VND", receivedAmount) + " cho Booking " + id + "?",
                    "Xác nhận đối soát thủ công", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (approval != JOptionPane.YES_OPTION) return;

            service.confirmDeposit(user, id, reference.getText(), receivedAmount);
            refreshAll();
            JOptionPane.showMessageDialog(this,
                    "Đã ghi nhận xác nhận đối soát thủ công. Booking chuyển sang CONFIRMED.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số tiền phải là số nguyên VND hợp lệ.",
                    "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void changeStatus(boolean checkIn) {
        String id = selectedBookingId();
        if (id == null) return;
        try {
            if (checkIn) {
                JTextField code = new JTextField();
                JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
                panel.add(new JLabel("Nhập mã Check-in do Guest cung cấp (10 ký tự):"));
                panel.add(code);
                if (JOptionPane.showConfirmDialog(this, panel, "Xác thực mã check-in: " + id,
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.OK_OPTION) return;
                service.checkIn(user, id, code.getText());
            } else {
                service.checkOut(user, id);
            }
            refreshAll();
        } catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void refreshUsers() {
        userModel.setRowCount(0);
        for (User u : service.getUsers(user)) {
            userModel.addRow(new Object[]{u.getUsername(), u.getPhone(), u.getRole(), u.getStatus()});
        }
    }

    private String selectedUsername() {
        int row = userTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn một tài khoản trước."); return null; }
        return String.valueOf(userTable.getValueAt(userTable.convertRowIndexToModel(row), 0));
    }

    private void createStaff() {
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JTextField phone = new JTextField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Tên đăng nhập")); panel.add(username);
        panel.add(new JLabel("Mật khẩu (tối thiểu 8 ký tự)")); panel.add(password);
        panel.add(new JLabel("Số điện thoại")); panel.add(phone);
        if (JOptionPane.showConfirmDialog(this, panel, "Tạo tài khoản Staff",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        char[] secret = password.getPassword();
        try {
            service.createStaff(user, username.getText(), secret, phone.getText());
            refreshUsers();
        } catch (RuntimeException | IOException ex) { showError(ex); }
        finally { java.util.Arrays.fill(secret, '\0'); }
    }

    private void changeRole() {
        String username = selectedUsername();
        if (username == null) return;
        Role current = (Role) userTable.getValueAt(userTable.convertRowIndexToModel(userTable.getSelectedRow()), 2);
        Role selected = (Role) JOptionPane.showInputDialog(this, "Vai trò mới cho " + username,
                "Phân quyền", JOptionPane.QUESTION_MESSAGE, null, Role.values(), current);
        if (selected == null) return;
        try { service.setUserRole(user, username, selected); refreshUsers(); }
        catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void toggleStatus() {
        String username = selectedUsername();
        if (username == null) return;
        int row = userTable.convertRowIndexToModel(userTable.getSelectedRow());
        AccountStatus current = AccountStatus.valueOf(String.valueOf(userTable.getValueAt(row, 3)));
        AccountStatus next = current == AccountStatus.ACTIVE ? AccountStatus.LOCKED : AccountStatus.ACTIVE;
        try { service.setUserStatus(user, username, next); refreshUsers(); }
        catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void refreshPrices() {
        priceModel.setRowCount(0);
        for (Room room : service.getRooms()) {
            priceModel.addRow(new Object[]{room.getId(), room.getType(), room.getPricePerNight()});
        }
    }

    private void editPrice() {
        int row = priceTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Chọn phòng cần sửa giá."); return; }
        row = priceTable.convertRowIndexToModel(row);
        String id = String.valueOf(priceTable.getValueAt(row, 0));
        String value = JOptionPane.showInputDialog(this, "Giá mới mỗi đêm (VND):",
                priceTable.getValueAt(row, 2));
        if (value == null) return;
        try {
            long price = Long.parseLong(value.replace(",", "").trim());
            service.updateRoomPrice(user, id, price);
            refreshPrices(); refreshRooms();
        } catch (RuntimeException | IOException ex) { showError(ex); }
    }

    private void editProfile() {
        JTextField phone = new JTextField(user.getPhone());
        JPasswordField password = new JPasswordField();
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Số điện thoại")); panel.add(phone);
        panel.add(new JLabel("Mật khẩu mới (để trống nếu giữ nguyên)")); panel.add(password);
        if (JOptionPane.showConfirmDialog(this, panel, "Thông tin tài khoản",
                JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        char[] secret = password.getPassword();
        boolean passwordChanged = secret.length > 0;
        try {
            service.updateProfile(user, phone.getText(), secret);
            if (passwordChanged) {
                JOptionPane.showMessageDialog(this,
                        "Đã đổi mật khẩu. Vì lý do bảo mật, bạn sẽ được đăng xuất và cần đăng nhập lại.");
                dispose();
                onLogout.run();
            } else {
                JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin.");
            }
        } catch (RuntimeException | IOException ex) { showError(ex); }
        finally { java.util.Arrays.fill(secret, '\0'); }
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể thực hiện", JOptionPane.WARNING_MESSAGE);
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}