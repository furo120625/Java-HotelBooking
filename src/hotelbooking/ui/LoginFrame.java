package hotelbooking.ui;

import hotelbooking.model.User;
import hotelbooking.service.HotelService;
import java.awt.*;
import javax.swing.*;

public final class LoginFrame extends JFrame {
    private final HotelService service;
    private final JTextField username = new JTextField();
    private final JPasswordField password = new JPasswordField();

    public LoginFrame(HotelService service) {
        this.service = service;
        setTitle("Khách sạn Bình Minh | Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(420, 330));
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.setBackground(new Color(245, 248, 252));
        JPanel card = new JPanel(new GridLayout(0, 1, 8, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 234)),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        JLabel title = new JLabel("KHÁCH SẠN BÌNH MINH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 21f));
        JLabel subtitle = new JLabel("Đăng nhập hệ thống quản lý", SwingConstants.CENTER);
        card.add(title); card.add(subtitle);
        card.add(new JLabel("Tên đăng nhập")); card.add(username);
        card.add(new JLabel("Mật khẩu")); card.add(password);
        JButton login = new JButton("ĐĂNG NHẬP");
        JButton register = new JButton("Tạo tài khoản Guest");
        card.add(login); card.add(register);
        root.add(card);
        setContentPane(root);
        getRootPane().setDefaultButton(login);
        login.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegisterDialog(this, service).setVisible(true));
        UiStyle.apply(this);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        char[] secret = password.getPassword();
        try {
            User user = service.login(username.getText(), secret);
            if (user == null) {
                JOptionPane.showMessageDialog(this,
                        "Thông tin đăng nhập không chính xác hoặc tài khoản đã bị khóa.",
                        "Đăng nhập thất bại", JOptionPane.WARNING_MESSAGE);
                password.setText("");
                return;
            }
            new DashboardFrame(service, user, () -> {
                setVisible(true);
                username.setText(user.getUsername());
                password.setText("");
            }).setVisible(true);
            setVisible(false);
        } finally {
            java.util.Arrays.fill(secret, '\0');
        }
    }
}