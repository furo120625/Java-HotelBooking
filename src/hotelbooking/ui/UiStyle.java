package hotelbooking.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

/** Shared coastal sapphire theme for the Bình Minh Hotel Swing application. */
public final class UiStyle {
    public static final Color SAPPHIRE = new Color(15, 82, 145);
    public static final Color SAPPHIRE_DARK = new Color(10, 61, 112);
    public static final Color SAPPHIRE_LIGHT = new Color(226, 240, 250);
    public static final Color SEA_BACKGROUND = new Color(244, 249, 252);
    public static final Color SURFACE = Color.WHITE;
    public static final Color TEXT = new Color(30, 48, 64);
    public static final Color MUTED = new Color(100, 119, 135);
    public static final Color BORDER = new Color(213, 225, 235);
    private static final int RADIUS = 5;

    private UiStyle() {}

    public static void install() {
        UIManager.put("Panel.background", SEA_BACKGROUND);
        UIManager.put("OptionPane.background", SEA_BACKGROUND);
        UIManager.put("OptionPane.messageForeground", TEXT);
        UIManager.put("TabbedPane.background", SEA_BACKGROUND);
        UIManager.put("TabbedPane.foreground", TEXT);
        UIManager.put("TabbedPane.selected", SURFACE);
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.foreground", TEXT);
        UIManager.put("Table.selectionBackground", SAPPHIRE_LIGHT);
        UIManager.put("Table.selectionForeground", SAPPHIRE_DARK);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("TableHeader.background", SAPPHIRE_LIGHT);
        UIManager.put("TableHeader.foreground", SAPPHIRE_DARK);
        UIManager.put("TextField.background", SURFACE);
        UIManager.put("TextField.foreground", TEXT);
        UIManager.put("PasswordField.background", SURFACE);
        UIManager.put("PasswordField.foreground", TEXT);
        UIManager.put("Label.foreground", TEXT);
    }

    public static void apply(Window window) {
        if (window == null) return;
        styleTree(window);
        window.setBackground(SEA_BACKGROUND);
    }

    private static void styleTree(Component component) {
        if (component instanceof JButton button) {
            styleButton(button);
        } else if (component instanceof JTextField field) {
            field.setForeground(TEXT);
            field.setBackground(SURFACE);
            field.setCaretColor(SAPPHIRE);
            field.setBorder(roundedBorder(BORDER, RADIUS, 8, 8, 8, 8));
            field.setPreferredSize(new Dimension(Math.max(120, field.getPreferredSize().width), 34));
        } else if (component instanceof JTable table) {
            table.setForeground(TEXT);
            table.setBackground(SURFACE);
            table.setSelectionBackground(SAPPHIRE_LIGHT);
            table.setSelectionForeground(SAPPHIRE_DARK);
            table.setGridColor(BORDER);
            table.setFillsViewportHeight(true);
            table.setShowVerticalLines(false);
            table.setIntercellSpacing(new Dimension(0, 1));
            table.setRowHeight(Math.max(34, table.getRowHeight()));
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(SAPPHIRE_LIGHT);
                table.getTableHeader().setForeground(SAPPHIRE_DARK);
                table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
                table.getTableHeader().setPreferredSize(new Dimension(0, 38));
            }
        } else if (component instanceof JTabbedPane pane) {
            pane.setBackground(SEA_BACKGROUND);
            pane.setForeground(TEXT);
            pane.setFont(pane.getFont().deriveFont(Font.BOLD));
        } else if (component instanceof JScrollPane scroll) {
            scroll.setBorder(roundedBorder(BORDER, RADIUS, 1, 1, 1, 1));
            scroll.getViewport().setBackground(SURFACE);
        } else if (component instanceof JLabel label) {
            label.setForeground(TEXT);
        } else if (component instanceof JTextArea area) {
            area.setForeground(TEXT);
            area.setCaretColor(SAPPHIRE);
        }

        if (component instanceof Container container) {
            for (Component child : container.getComponents()) styleTree(child);
        }
    }

    private static void styleButton(JButton button) {
        String label = button.getText() == null ? "" : button.getText().toLowerCase();
        boolean primary = label.contains("đăng nhập") || label.contains("tạo đặt")
                || label.contains("tạo tài khoản") || label.contains("xác nhận")
                || label.contains("đặt phòng") || label.contains("check-in")
                || label.contains("check-out") || label.contains("lưu");
        button.setForeground(primary ? Color.WHITE : SAPPHIRE_DARK);
        button.setBackground(primary ? SAPPHIRE : SURFACE);
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(roundedBorder(primary ? SAPPHIRE : BORDER, RADIUS, 8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setUI(new BasicButtonUI() {
            @Override public void paint(Graphics graphics, JComponent component) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                AbstractButton b = (AbstractButton) component;
                ButtonModel model = b.getModel();
                Color color = b.getBackground();
                if (!b.isEnabled()) color = new Color(225, 231, 236);
                else if (model.isPressed() || model.isRollover()) {
                    color = primary ? SAPPHIRE_DARK : SAPPHIRE_LIGHT;
                }
                g.setColor(color);
                g.fill(new RoundRectangle2D.Float(0, 0, component.getWidth() - 1,
                        component.getHeight() - 1, RADIUS * 2, RADIUS * 2));
                g.dispose();
                super.paint(graphics, component);
            }
        });
    }

    private static Border roundedBorder(Color color, int radius, int top, int left, int bottom, int right) {
        Border outline = new Border() {
            @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
            @Override public boolean isBorderOpaque() { return false; }
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.drawRoundRect(x, y, w - 1, h - 1, radius * 2, radius * 2);
                g2.dispose();
            }
        };
        return BorderFactory.createCompoundBorder(outline, BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
}
