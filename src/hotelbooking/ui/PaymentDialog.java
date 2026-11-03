package hotelbooking.ui;

import hotelbooking.model.Booking;
import hotelbooking.model.User;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.swing.*;

final class PaymentDialog extends JDialog {
    PaymentDialog(JFrame owner, Booking booking, User actor) {
        super(owner, "Thanh toán tiền cọc", true);
        setLayout(new BorderLayout(12, 12));
        JPanel details = new JPanel(new GridLayout(0, 1, 6, 6));
        details.setBorder(BorderFactory.createEmptyBorder(16, 18, 8, 18));
        details.add(new JLabel("Mã đặt phòng: " + booking.getId()));
        details.add(new JLabel("Phòng: " + String.join(", ", booking.getRoomIds())));
        details.add(new JLabel("Thời gian: " + booking.getCheckIn() + " đến " + booking.getCheckOut()));
        details.add(new JLabel("Tổng tiền: " + String.format("%,d đ", booking.getTotalAmount())));
        JLabel deposit = new JLabel("Tiền cọc 50%: " + String.format("%,d đ", booking.getDepositAmount()));
        deposit.setFont(deposit.getFont().deriveFont(Font.BOLD, 17f));
        details.add(deposit);
        add(details, BorderLayout.NORTH);

        JPanel qrPanel = new JPanel(new BorderLayout(8, 8));
        qrPanel.setBorder(BorderFactory.createTitledBorder("Mã QR chuyển khoản VietQR"));
        JLabel qr = new JLabel("Đang kiểm tra cấu hình tài khoản nhận tiền...", SwingConstants.CENTER);
        qr.setPreferredSize(new Dimension(320, 270));
        qrPanel.add(qr, BorderLayout.CENTER);
        String bank = System.getenv("VIETQR_BANK_ID");
        String account = System.getenv("VIETQR_ACCOUNT_NO");
        String accountName = System.getenv("VIETQR_ACCOUNT_NAME");
        String transferText = "DATCOC " + booking.getId();
        if (bank != null && account != null && accountName != null
                && !bank.isBlank() && !account.isBlank() && !accountName.isBlank()) {
            String url = "https://img.vietqr.io/image/" + enc(bank) + "-" + enc(account)
                    + "-compact2.png?amount=" + booking.getDepositAmount()
                    + "&addInfo=" + enc(transferText) + "&accountName=" + enc(accountName);
            qr.setText("Đang tải mã QR...");
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                @Override protected ImageIcon doInBackground() throws Exception {
                    java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(URI.create(url).toURL());
                    if (image == null) throw new IllegalStateException("Không đọc được ảnh QR.");
                    Image scaled = image.getScaledInstance(260, 260, Image.SCALE_SMOOTH);
                    return new ImageIcon(scaled);
                }
                @Override protected void done() {
                    try { qr.setText(""); qr.setIcon(get()); }
                    catch (Exception ex) { qr.setText("Không tải được QR. Hãy chuyển khoản theo nội dung bên dưới."); }
                }
            };
            worker.execute();
        } else {
            qr.setText("<html><center>Chưa cấu hình tài khoản nhận tiền.<br>"
                    + "Thiết lập VIETQR_BANK_ID, VIETQR_ACCOUNT_NO,<br>"
                    + "VIETQR_ACCOUNT_NAME trên máy chạy ứng dụng.</center></html>");
        }
        add(qrPanel, BorderLayout.CENTER);

        JTextArea transfer = new JTextArea("Nội dung chuyển khoản: " + transferText
                + "\nSố tiền: " + booking.getDepositAmount() + " VND"
                + "\n\nLưu ý: QR chỉ tạo yêu cầu chuyển khoản, không chứng minh giao dịch đã thành công."
                + "\nNhân viên/Admin phải đối soát và xác nhận giao dịch trước khi Booking được CONFIRMED.");
        transfer.setEditable(false);
        transfer.setLineWrap(true);
        transfer.setWrapStyleWord(true);
        transfer.setBackground(getBackground());
        add(transfer, BorderLayout.SOUTH);

        JButton close = new JButton("Đóng");
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(close);
        add(actions, BorderLayout.PAGE_END);
        setMinimumSize(new Dimension(520, 620));
        pack();
        setLocationRelativeTo(owner);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}