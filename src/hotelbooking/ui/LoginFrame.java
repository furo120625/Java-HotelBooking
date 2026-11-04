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

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = new Dimension(
                Math.max(420, screen.width / 2),
                Math.max(330, screen.height / 2));
        Dimension cardSize = new Dimension(
                Math.max(300, screen.width * 3 / 10),
                Math.max(360, screen.height * 42 / 100));

        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));
        root.setBackground(UiStyle.SEA_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiStyle.BORDER),
                BorderFactory.createEmptyBorder(30, 32, 30, 32)));
        card.setPreferredSize(cardSize);

        JLabel title = new JLabel("KHÁCH SẠN BÌNH MINH", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 25f));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Đăng nhập hệ thống quản lý", SwingConstants.CENTER);
        subtitle.setForeground(UiStyle.MUTED);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel usernameLabel = new JLabel("Tên đăng nhập");
        JLabel passwordLabel = new JLabel("Mật khẩu");
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        Dimension fieldSize = new Dimension(Integer.MAX_VALUE, 40);
        username.setMaximumSize(fieldSize);
        username.setPreferredSize(new Dimension(260, 40));
        password.setMaximumSize(fieldSize);
        password.setPreferredSize(new Dimension(260, 40));

        JButton login = new JButton("ĐĂNG NHẬP");
        JButton register = new JButton("Tạo tài khoản Guest");
        login.setAlignmentX(Component.CENTER_ALIGNMENT);
        register.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension buttonSize = new Dimension(Integer.MAX_VALUE, 42);
        login.setMaximumSize(buttonSize);
        register.setMaximumSize(buttonSize);
        login.setPreferredSize(new Dimension(260, 42));
        register.setPreferredSize(new Dimension(260, 42));

        card.add(title);
        card.add(Box.createVerticalStrut(10));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(34));
        card.add(usernameLabel);
        card.add(Box.createVerticalStrut(9));
        card.add(username);
        card.add(Box.createVerticalStrut(20));
        card.add(passwordLabel);
        card.add(Box.createVerticalStrut(9));
        card.add(password);
        card.add(Box.createVerticalStrut(12));
        card.add(login);
        card.add(Box.createVerticalStrut(9));
        card.add(register);

        root.add(card);
        setContentPane(root);
        getRootPane().setDefaultButton(login);
        login.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        register.addActionListener(e -> new RegisterDialog(this, service).setVisible(true));

        UiStyle.apply(this);
        setPreferredSize(windowSize);
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
            java.util.Arrays.fill(secret, '\\0');
        }
    }
}
