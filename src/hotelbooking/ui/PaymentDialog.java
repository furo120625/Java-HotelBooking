package hotelbooking.ui;

import hotelbooking.model.Booking;
import hotelbooking.model.User;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.*;

final class PaymentDialog extends JDialog {
    private static final String LOCAL_QR_PATH = "config/payment-qr.png";

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
        qrPanel.setBorder(BorderFactory.createTitledBorder("Mã QR chuyển khoản"));
        JLabel qr = new JLabel("", SwingConstants.CENTER);
        qr.setPreferredSize(new Dimension(320, 280));
        qrPanel.add(qr, BorderLayout.CENTER);

        // Ưu tiên QR cá nhân được lưu trên máy, không đưa ảnh chứa thông tin tài khoản lên GitHub.
        File localQrFile = new File(LOCAL_QR_PATH);
        boolean localQrLoaded = false;
        if (localQrFile.isFile()) {
            try {
                java.awt.image.BufferedImage image = ImageIO.read(localQrFile);
                if (image != null) {
                    qr.setIcon(new ImageIcon(scaleImage(image, 260, 260)));
                    qr.setText("");
                    localQrLoaded = true;
                }
            } catch (Exception ex) {
                qr.setText("Không đọc được ảnh QR tại " + LOCAL_QR_PATH);
            }
        }

        String bank = System.getenv("VIETQR_BANK_ID");
        String account = System.getenv("VIETQR_ACCOUNT_NO");
        String accountName = System.getenv("VIETQR_ACCOUNT_NAME");
        String transferText = "DATCOC " + booking.getId();

        // Nếu chưa có ảnh QR cục bộ, giữ cơ chế tạo VietQR động như trước.
        if (!localQrLoaded && bank != null && account != null && accountName != null
                && !bank.isBlank() && !account.isBlank() && !accountName.isBlank()) {
            String url = "https://img.vietqr.io/image/" + enc(bank) + "-" + enc(account)
                    + "-compact2.png?amount=" + booking.getDepositAmount()
                    + "&addInfo=" + enc(transferText) + "&accountName=" + enc(accountName);
            qr.setText("Đang tải mã QR...");
            qr.setIcon(null);
            SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    java.awt.image.BufferedImage image = ImageIO.read(URI.create(url).toURL());
                    if (image == null) {
                        throw new IllegalStateException("Không đọc được ảnh QR.");
                    }
                    return new ImageIcon(scaleImage(image, 260, 260));
                }

                @Override
                protected void done() {
                    try {
                        qr.setText("");
                        qr.setIcon(get());
                    } catch (Exception ex) {
                        qr.setText("Không tải được QR. Hãy chuyển khoản theo nội dung bên dưới.");
                    }
                }
            };
            worker.execute();
        } else if (!localQrLoaded) {
            qr.setText("<html><center>Chưa tìm thấy ảnh QR tại <b>" + LOCAL_QR_PATH + "</b>.<br>"
                    + "Hoặc chưa cấu hình tài khoản VietQR động.</center></html>");
        }

        add(qrPanel, BorderLayout.CENTER);

        JTextArea transfer = new JTextArea("Nội dung chuyển khoản: " + transferText
                + "\nSố tiền: " + booking.getDepositAmount() + " VND"
                + "\n\nLưu ý: QR cá nhân là QR tĩnh; khách cần kiểm tra và nhập đúng số tiền, nội dung chuyển khoản."
                + "\nQR chỉ tạo yêu cầu chuyển khoản, không chứng minh giao dịch đã thành công."
                + "\nNhân viên/Admin phải đối soát và xác nhận giao dịch trước khi Booking được CONFIRMED.");
        transfer.setEditable(false);
        transfer.setLineWrap(true);
        transfer.setWrapStyleWord(true);
        transfer.setBackground(getBackground());

        JPanel bottom = new JPanel(new BorderLayout(8, 8));
        bottom.add(transfer, BorderLayout.CENTER);
        JButton close = new JButton("Đóng");
        close.addActionListener(e -> dispose());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(close);
        bottom.add(actions, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        UiStyle.apply(this);
        setMinimumSize(new Dimension(520, 620));
        pack();
        setLocationRelativeTo(owner);
    }

    private static Image scaleImage(Image image, int maxWidth, int maxHeight) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        double scale = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int scaledWidth = Math.max(1, (int) Math.round(width * scale));
        int scaledHeight = Math.max(1, (int) Math.round(height * scale));
        return image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
