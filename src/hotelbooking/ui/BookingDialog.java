package hotelbooking.ui;

import hotelbooking.model.*;
import hotelbooking.service.HotelService;
import hotelbooking.util.DateUtil;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

final class BookingDialog extends JDialog {
    private final HotelService service;
    private final User actor;
    private final Consumer<Booking> onCreated;
    private final JTextField guestName = new JTextField();
    private final JTextField cccd = new JTextField();
    private final JTextField phone = new JTextField();
    private final JSpinner guests = new JSpinner(new SpinnerNumberModel(2, 1, 100, 1));
    private final JTextField checkIn = new JTextField(DateUtil.format(LocalDate.now().plusDays(1)));
    private final JTextField checkOut = new JTextField(DateUtil.format(LocalDate.now().plusDays(2)));
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Chọn", "Phòng", "Tầng", "Loại", "Sức chứa", "Giá/đêm", "Tình trạng"}, 0) {
        @Override public Class<?> getColumnClass(int column) {
            return column == 0 ? Boolean.class : Object.class;
        }
        @Override public boolean isCellEditable(int row, int column) { return column == 0; }
    };
    private final JTable table = new JTable(model);

    BookingDialog(JFrame owner, HotelService service, User actor, Consumer<Booking> onCreated) {
        super(owner, "Tạo lượt đặt phòng", true);
        this.service = service;
        this.actor = actor;
        this.onCreated = onCreated;
        phone.setText(actor.getPhone());

        JPanel form = new JPanel(new GridLayout(0, 4, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        form.add(new JLabel("Họ tên khách")); form.add(guestName);
        form.add(new JLabel("CCCD")); form.add(cccd);
        form.add(new JLabel("Số điện thoại")); form.add(phone);
        form.add(new JLabel("Số lượng khách")); form.add(guests);
        form.add(new JLabel("Nhận phòng (dd/MM/yyyy)")); form.add(checkIn);
        form.add(new JLabel("Trả phòng (dd/MM/yyyy)")); form.add(checkOut);

        JLabel scheduleNote = new JLabel("Giờ nhận phòng: 13:00 | Giờ trả phòng: 11:00. Ngày trả phòng vẫn bị khóa trên lịch đặt phòng đến hết ngày.");
        form.add(scheduleNote); form.add(new JLabel(""));

        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setRowHeight(27);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Chọn một hoặc nhiều phòng còn trống"));
        JButton refresh = new JButton("Kiểm tra ngày");
        JButton submit = new JButton("Tạo đặt phòng và thanh toán cọc");
        JButton cancel = new JButton("Đóng");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(refresh); buttons.add(cancel); buttons.add(submit);
        add(form, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        refresh.addActionListener(e -> refreshRooms());
        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> createBooking());
        refreshRooms();
        UiStyle.apply(this);
        setMinimumSize(new Dimension(850, 620));
        setSize(900, 650);
        setLocationRelativeTo(owner);
    }

    private LocalDate parseDate(JTextField field, String label) {
        try {
            LocalDate parsed = DateUtil.parse(field.getText());
            field.setText(DateUtil.format(parsed));
            return parsed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(label + " không hợp lệ. Hãy nhập dd/MM/yyyy, ví dụ 09/01/2026.");
        }
    }

    private void refreshRooms() {
        try {
            LocalDate in = parseDate(checkIn, "Ngày nhận phòng");
            LocalDate out = parseDate(checkOut, "Ngày trả phòng");
            if (!out.isAfter(in)) throw new IllegalArgumentException("Ngày trả phòng phải sau ngày nhận phòng.");
            model.setRowCount(0);
            for (Room room : service.getRooms()) {
                boolean available = service.isAvailable(room.getId(), in, out);
                model.addRow(new Object[]{false, room.getId(), room.getFloor(), room.getType(),
                        room.getCapacity(), String.format("%,d đ", room.getPricePerNight()),
                        available ? "CÒN TRỐNG" : "ĐÃ CÓ LỊCH"});
                if (!available) model.setValueAt(false, model.getRowCount() - 1, 0);
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Ngày không hợp lệ", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createBooking() {
        try {
            List<String> roomIds = new ArrayList<>();
            for (int row = 0; row < model.getRowCount(); row++) {
                if (Boolean.TRUE.equals(model.getValueAt(row, 0))) {
                    if (!"CÒN TRỐNG".equals(model.getValueAt(row, 6))) {
                        throw new IllegalArgumentException("Có phòng được chọn không còn trống.");
                    }
                    roomIds.add(String.valueOf(model.getValueAt(row, 1)));
                }
            }
            Booking booking = service.createBooking(actor, guestName.getText(), cccd.getText(),
                    phone.getText(), (Integer) guests.getValue(), roomIds,
                    parseDate(checkIn, "Ngày nhận phòng"), parseDate(checkOut, "Ngày trả phòng"));
            JOptionPane.showMessageDialog(this,
                    "Đã tạo lượt đặt " + booking.getId() + ". Tiền cọc cần thanh toán: "
                    + String.format("%,d đ", booking.getDepositAmount())
                    + "\nLượt đặt đang ở trạng thái PENDING_PAYMENT."
                    + "\n\nMÃ CHECK-IN: " + booking.getCheckInCode()
                    + "\nHãy lưu mã này. Chỉ cung cấp cho nhân viên khi nhận phòng."
                    + "\nMã được yêu cầu khi Guest làm thủ tục nhận phòng.");
            dispose();
            onCreated.accept(booking);
        } catch (RuntimeException | java.io.IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đặt phòng", JOptionPane.WARNING_MESSAGE);
        }
    }
}