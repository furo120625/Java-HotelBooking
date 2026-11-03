package hotelbooking.ui;

import hotelbooking.service.HotelService;
import java.awt.*;
import javax.swing.*;

final class RegisterDialog extends JDialog {
    RegisterDialog(JFrame owner, HotelService service) {
        super(owner, "Đăng ký tài khoản Guest", true);
        setLayout(new BorderLayout(12, 12));
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(18, 18, 8, 18));
        JTextField username = new JTextField();
        JPasswordField password = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JTextField phone = new JTextField();
        form.add(new JLabel("Tên đăng nhập")); form.add(username);
        form.add(new JLabel("Mật khẩu (ít nhất 8 ký tự)")); form.add(password);
        form.add(new JLabel("Nhập lại mật khẩu")); form.add(confirm);
        form.add(new JLabel("Số điện thoại")); form.add(phone);
        add(form, BorderLayout.CENTER);

        JButton submit = new JButton("Tạo tài khoản");
        JButton cancel = new JButton("Hủy");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(cancel); actions.add(submit); add(actions, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        submit.addActionListener(e -> {
            char[] p = password.getPassword();
            char[] c = confirm.getPassword();
            if (!java.util.Arrays.equals(p, c)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu xác nhận không khớp.");
                return;
            }
            try {
                service.register(username.getText(), p, phone.getText());
                JOptionPane.showMessageDialog(this, "Đăng ký thành công. Hãy đăng nhập.");
                dispose();
            } catch (RuntimeException | java.io.IOException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể đăng ký", JOptionPane.WARNING_MESSAGE);
            } finally {
                java.util.Arrays.fill(p, '\0');
                java.util.Arrays.fill(c, '\0');
            }
        });
        setMinimumSize(new Dimension(470, 260));
        pack();
        setLocationRelativeTo(owner);
    }
}