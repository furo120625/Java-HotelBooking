    package hotelbooking;

    import javax.swing.*;
    import javax.swing.border.EmptyBorder;
    import java.awt.*;
    import java.io.*;
    import java.util.HashMap;
    import java.util.LinkedHashMap;
    import java.util.Map;

    public class HotelBinhMinhApp {
        static class UserInfo {
            String password;
            String phone;
            UserInfo(String password, String phone) {
                this.password = password;
                this.phone = phone;
            }
        }

        static class Room {
            String id;       
            int floor;       
            String type;     
            long price;      
            int maxGuests;   
            boolean isBooked;
            String bookedBy; 
            String customerName = "";
            String cccd = "";
            String phone = "";
            String checkIn = "";
            String checkOut = "";
            String numberOfGuests = "1";

            Room(String id, int floor, String type, long price, int maxGuests) {
                this.id = id;
                this.floor = floor;
                this.type = type;
                this.price = price;
                this.maxGuests = maxGuests;
                this.isBooked = false;
            }
        }

        private static final Map<String, UserInfo> USERS = new HashMap<>();
        private static final Map<String, Room> ROOMS = new LinkedHashMap<>();
        private static final String USER_FILE_NAME = "TK.txt";
        private static final String BOOKING_FILE_NAME = "DP.txt";

        public static void main(String[] args) {
            SwingUtilities.invokeLater(() -> {
                try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
                
                loadUsersFromfile();

                for(int i=1; i<=4; i++) ROOMS.put("20"+i, new Room("20"+i, 2, "Standard", 800000, 2));
                ROOMS.put("301", new Room("301", 3, "Standard", 800000, 2));
                ROOMS.put("302", new Room("302", 3, "Standard", 800000, 2));
                ROOMS.put("303", new Room("303", 3, "Deluxe", 1200000, 4));
                ROOMS.put("304", new Room("304", 3, "Deluxe", 1200000, 4));
                for(int i=1; i<=4; i++) ROOMS.put("40"+i, new Room("40"+i, 4, "Deluxe", 1200000, 4));
                for(int i=1; i<=4; i++) ROOMS.put("50"+i, new Room("50"+i, 5, "VIP", 2000000, 4));

                loadBookingsFromFile();

                new LoginFrame().setVisible(true);
            });
        }

        static void loadUsersFromfile() {
            USERS.clear();
            File file = new File(USER_FILE_NAME);
            if (!file.exists()) {
                USERS.put("admin", new UserInfo("BinhMinh123", "0978805955"));
                saveUsersToFile();
            } else {
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] parts = line.split(",", 3);
                        if (parts.length >= 3) {
                            USERS.put(parts[0].trim(), new UserInfo(parts[1].trim(), parts[2].trim()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!USERS.containsKey("admin")) {
                    USERS.put("admin", new UserInfo("BinhMinh123", "0978805955"));
                }
            }
        }

        static void saveUsersToFile() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(USER_FILE_NAME))) {
                for (Map.Entry<String, UserInfo> entry : USERS.entrySet()) {
                    pw.println(entry.getKey() + "," + entry.getValue().password + "," + entry.getValue().phone);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static void loadBookingsFromFile() {
            File file = new File(BOOKING_FILE_NAME);
            if (!file.exists()) return;
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",", 9);
                    if (parts.length >= 8) {
                        String roomId = parts[0].trim();
                        Room room = ROOMS.get(roomId);
                        if (room != null) {
                            room.isBooked = Boolean.parseBoolean(parts[1].trim());
                            room.bookedBy = parts[2].trim();
                            room.customerName = parts[3].trim();
                            room.cccd = parts[4].trim();
                            room.phone = parts[5].trim();
                            room.checkIn = parts[6].trim();
                            room.checkOut = parts[7].trim();
                            if (parts.length >= 9) {
                                room.numberOfGuests = parts[8].trim();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static void saveBookingsToFile() {
            try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKING_FILE_NAME))) {
                for (Room room : ROOMS.values()) {
                    pw.println(room.id + "," + 
                            room.isBooked + "," + 
                            (room.bookedBy != null ? room.bookedBy : "") + "," + 
                            (room.customerName != null ? room.customerName : "") + "," + 
                            (room.cccd != null ? room.cccd : "") + "," + 
                            (room.phone != null ? room.phone : "") + "," + 
                            (room.checkIn != null ? room.checkIn : "") + "," + 
                            (room.checkOut != null ? room.checkOut : "") + "," +
                            (room.numberOfGuests != null ? room.numberOfGuests : "1"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        static final Color NAVY = new Color(20, 45, 75);
        static final Color FB_BLUE = new Color(24, 119, 242);   
        static final Color PALE = new Color(244, 247, 251); 
        static final Color TEXT = new Color(30, 40, 55);

        static JLabel label(String text, int size, boolean bold, Color color) {
            JLabel l = new JLabel(text);
            l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
            l.setForeground(color);
            return l;
        }
        
        static class RoundedButton extends JButton {
            Color bgColor;
            Color fgColor;
            boolean outline;
            int radius;

            public RoundedButton(String text, Color bgColor, Color fgColor, boolean outline, int radius, Dimension size) {
                super(text);
                this.bgColor = bgColor;
                this.fgColor = fgColor;
                this.outline = outline;
                this.radius = radius;
                setFont(new Font("SansSerif", Font.BOLD, 13));
                setForeground(fgColor);
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                if (size != null) {
                    setPreferredSize(size);
                    setMaximumSize(size);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();

                if (outline) {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, width, height, radius, radius);
                    g2.setColor(bgColor);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(0, 0, width - 1, height - 1, radius, radius);
                } else {
                    g2.setColor(bgColor);
                    g2.fillRoundRect(0, 0, width, height, radius, radius);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        }

        static class RoomButton extends RoundedButton {
            public RoomButton(String text, Color bgColor, Color fgColor, Dimension size) {
                super(text, bgColor, fgColor, false, 16, size);
            }

            public void updateStyle(String text, Color bgColor, Color fgColor) {
                setText(text);
                this.bgColor = bgColor;
                this.fgColor = fgColor;
                setForeground(fgColor);
                repaint();
            }
        }

        static JButton solidButton(String text) {
            JButton b = new RoundedButton(text, FB_BLUE, Color.WHITE, false, 12, new Dimension(280, 44));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            return b;
        }

        static JButton outlineButton(String text) {
            JButton b = new RoundedButton(text, FB_BLUE, FB_BLUE, true, 12, new Dimension(280, 44));
            b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
            return b;
        }

        static JPanel card() {
            JPanel p = new JPanel();
            p.setBackground(Color.WHITE);
            p.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 220, 230)), new EmptyBorder(20,22,20,22)));
            return p;
        }
        static JTextField field() {
            JTextField f = new JTextField(); f.setFont(new Font("SansSerif", Font.PLAIN, 14));
            f.setPreferredSize(new Dimension(280, 38)); return f;
        }
        static JPasswordField passwordField() {
            JPasswordField f = new JPasswordField(); f.setFont(new Font("SansSerif", Font.PLAIN, 14));
            f.setPreferredSize(new Dimension(280, 38)); return f;
        }
        
        // Tạo JSpinner chọn ngày chuẩn định dạng yyyy-MM-dd
        static JSpinner dateSpinner(java.time.LocalDate initialDate) {
            SpinnerDateModel model = new SpinnerDateModel(
                java.util.Date.from(initialDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()),
                null, null, java.util.Calendar.DAY_OF_MONTH
            );
            JSpinner spinner = new JSpinner(model);
            JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
            spinner.setEditor(editor);
            spinner.setFont(new Font("SansSerif", Font.PLAIN, 14));
            spinner.setPreferredSize(new Dimension(280, 38));
            return spinner;
        }
        static class DatePickerField extends JPanel {
        private final JTextField dateField = field();
        private final JButton calendarButton = new JButton("Lịch");

        DatePickerField(java.time.LocalDate initialDate) {
            setLayout(new BorderLayout(5, 0));
            setBackground(Color.WHITE);

            dateField.setText(formatDate(initialDate));
            dateField.setToolTipText("Nhập ngày theo dạng ngày/tháng/năm");

            calendarButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
            calendarButton.setFocusPainted(false);

            add(dateField, BorderLayout.CENTER);
            add(calendarButton, BorderLayout.EAST);

            calendarButton.addActionListener(e -> showCalendar());

            // Chuẩn hóa ngày khi người dùng rời khỏi ô nhập.
            dateField.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    java.time.LocalDate date = parseDate();
                    if (date != null) {
                        dateField.setText(formatDate(date));
                    }
                }
            });
        }

        private String formatDate(java.time.LocalDate date) {
            return date.format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu")
            );
        }

        // Chấp nhận cả 1/1/2027, 01/01/2027 và 01-01-2027.
        private java.time.LocalDate parseDate() {
            try {
                String text = dateField.getText().trim()
                                    .replace('-', '/');

                String[] parts = text.split("/", -1);
                if (parts.length != 3) {
                    return null;
                }

                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                return java.time.LocalDate.of(year, month, day);
            } catch (Exception e) {
                return null;
            }
        }

        java.time.LocalDate getDate() {
            return parseDate();
        }

        void setDate(java.time.LocalDate date) {
            dateField.setText(formatDate(date));
        }

        private void showCalendar() {
            java.time.LocalDate selectedDate = parseDate();

            if (selectedDate == null) {
                selectedDate = java.time.LocalDate.now();
            }

            final java.time.YearMonth[] currentMonth = {
                java.time.YearMonth.from(selectedDate)
            };

            JDialog dialog = new JDialog(
                SwingUtilities.getWindowAncestor(this),
                "Chọn ngày",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL
            );

            dialog.setLayout(new BorderLayout(8, 8));

            JPanel header = new JPanel(new BorderLayout(8, 0));
            JButton previous = new JButton("<");
            JButton next = new JButton(">");
            JLabel monthLabel = label("", 14, true, TEXT);
            monthLabel.setHorizontalAlignment(SwingConstants.CENTER);

            header.add(previous, BorderLayout.WEST);
            header.add(monthLabel, BorderLayout.CENTER);
            header.add(next, BorderLayout.EAST);

            JPanel calendar = new JPanel(new GridLayout(7, 7, 3, 3));

            Runnable renderCalendar = () -> {
                monthLabel.setText(
                    currentMonth[0].getMonthValue()
                    + "/" + currentMonth[0].getYear()
                );

                calendar.removeAll();

                String[] weekdays = {
                    "T2", "T3", "T4", "T5", "T6", "T7", "CN"
                };

                for (String weekday : weekdays) {
                    JLabel label = new JLabel(weekday, SwingConstants.CENTER);
                    label.setFont(new Font("SansSerif", Font.BOLD, 12));
                    calendar.add(label);
                }

                int firstDay = currentMonth[0]
                    .atDay(1).getDayOfWeek().getValue();

                int daysInMonth = currentMonth[0].lengthOfMonth();

                for (int i = 1; i < firstDay; i++) {
                    calendar.add(new JLabel(""));
                }

                for (int day = 1; day <= daysInMonth; day++) {
                    final int selectedDay = day;
                    JButton dayButton = new JButton(String.valueOf(day));

                    dayButton.addActionListener(e -> {
                        java.time.LocalDate date = currentMonth[0]
                            .atDay(selectedDay);

                        setDate(date);
                        dialog.dispose();
                    });

                    calendar.add(dayButton);
                }

                calendar.revalidate();
                calendar.repaint();
            };

            previous.addActionListener(e -> {
                currentMonth[0] = currentMonth[0].minusMonths(1);
                renderCalendar.run();
            });

            next.addActionListener(e -> {
                currentMonth[0] = currentMonth[0].plusMonths(1);
                renderCalendar.run();
            });

            renderCalendar.run();

            JPanel content = new JPanel(new BorderLayout(8, 8));
            content.setBorder(new EmptyBorder(10, 10, 10, 10));
            content.add(header, BorderLayout.NORTH);
            content.add(calendar, BorderLayout.CENTER);

            dialog.add(content);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(300, 280));
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        }
    }
        static class Header extends JPanel {
            Header() {
                setBackground(NAVY); setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                setBorder(new EmptyBorder(18,24,18,24));
                add(label("KHÁCH SẠN BÌNH MINH",24,true,Color.WHITE));
                add(Box.createVerticalStrut(4));
                add(label("201 Bà Triệu, Cẩm Phả, Quảng Ninh",13,false,Color.WHITE));
                add(Box.createVerticalStrut(3));
                add(label("HOTLINE: 0978805955",13,true,new Color(255,218,130)));
            }
        }
        static void addField(JPanel p, String title, JComponent input) {
            JLabel l = label(title,13,true,TEXT); l.setAlignmentX(Component.LEFT_ALIGNMENT);
            input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38)); input.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l); p.add(Box.createVerticalStrut(4)); p.add(input);
        }

        static class LoginFrame extends JFrame {
            JTextField username = field(); JPasswordField password = passwordField();
            LoginFrame() {
                setTitle("Đăng nhập - Khách sạn Bình Minh"); setDefaultCloseOperation(EXIT_ON_CLOSE);
                setSize(720,560); setMinimumSize(new Dimension(640,500)); setLocationRelativeTo(null);
                JPanel root = new JPanel(new BorderLayout()); root.setBackground(PALE); root.add(new Header(),BorderLayout.NORTH);
                JPanel center = new JPanel(new GridBagLayout()); center.setBackground(PALE);
                JPanel c = card(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.setPreferredSize(new Dimension(390,360));
                JLabel title=label("Đăng nhập tài khoản",20,true,TEXT); title.setAlignmentX(Component.LEFT_ALIGNMENT); c.add(title);
                c.add(Box.createVerticalStrut(16)); addField(c,"Tên đăng nhập",username);
                c.add(Box.createVerticalStrut(10)); addField(c,"Mật khẩu",password); c.add(Box.createVerticalStrut(18));
                
                JButton login = solidButton("ĐĂNG NHẬP"); login.setAlignmentX(Component.LEFT_ALIGNMENT);
                login.addActionListener(e -> doLogin()); c.add(login); c.add(Box.createVerticalStrut(14));
                
                JButton register = outlineButton("Tạo tài khoản mới"); register.setAlignmentX(Component.LEFT_ALIGNMENT);
                register.addActionListener(e->{new RegisterFrame(this).setVisible(true);setVisible(false);}); 
                c.add(register);
                
                center.add(c); root.add(center,BorderLayout.CENTER);
                setContentPane(root); getRootPane().setDefaultButton(login);
            }
            void doLogin() {
                String u=username.getText().trim(), p=new String(password.getPassword());
                if(u.isEmpty()||p.isEmpty()){msg("Vui lòng nhập tên đăng nhập và mật khẩu.",JOptionPane.WARNING_MESSAGE);return;}
                UserInfo userInfo = USERS.get(u);
                if(userInfo != null && userInfo.password.equals(p)){
                    JOptionPane.showMessageDialog(this,"Đăng nhập thành công. Xin chào "+u+"!");
                    new HomeFrame(u).setVisible(true);dispose();
                } else msg("Tên đăng nhập hoặc mật khẩu không đúng.",JOptionPane.ERROR_MESSAGE);
            }
            void msg(String s,int type){JOptionPane.showMessageDialog(this,s,"Thông báo",type);}
        }

        static class RegisterFrame extends JFrame {
            final JFrame loginFrame; 
            JTextField username = field();
            JTextField phone = field();
            JPasswordField password = passwordField(), confirm = passwordField();

            RegisterFrame(JFrame loginFrame) {
                this.loginFrame=loginFrame; setTitle("Đăng ký - Khách sạn Bình Minh");
                setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); setSize(720,640); setLocationRelativeTo(loginFrame);
                JPanel root=new JPanel(new BorderLayout()); root.setBackground(PALE); root.add(new Header(),BorderLayout.NORTH);
                JPanel center=new JPanel(new GridBagLayout()); center.setBackground(PALE); 
                JPanel c=card(); c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.setPreferredSize(new Dimension(390,460));
                
                JLabel title=label("Tạo tài khoản mới",20,true,TEXT); title.setAlignmentX(Component.LEFT_ALIGNMENT); c.add(title);
                c.add(Box.createVerticalStrut(12)); addField(c,"Tên đăng nhập (4-20 ký tự)",username);
                c.add(Box.createVerticalStrut(8)); addField(c,"Số điện thoại",phone);
                c.add(Box.createVerticalStrut(8)); addField(c,"Mật khẩu (ít nhất 6 ký tự)",password);
                c.add(Box.createVerticalStrut(8)); addField(c,"Nhập lại mật khẩu",confirm); c.add(Box.createVerticalStrut(16));
                
                JButton submit = solidButton("TẠO TÀI KHOẢN"); submit.setAlignmentX(Component.LEFT_ALIGNMENT);
                submit.addActionListener(e->register()); c.add(submit); c.add(Box.createVerticalStrut(10));
                
                JButton back = outlineButton("Quay lại đăng nhập"); back.setAlignmentX(Component.LEFT_ALIGNMENT);
                back.addActionListener(e->back()); c.add(back); 
                
                center.add(c); root.add(center,BorderLayout.CENTER); setContentPane(root);
                addWindowListener(new java.awt.event.WindowAdapter(){public void windowClosing(java.awt.event.WindowEvent e){back();}});
            }
            void register() {
                String u = username.getText().trim(), ph = phone.getText().trim();
                String p = new String(password.getPassword()), cp = new String(confirm.getPassword());
                if(u.isEmpty()||ph.isEmpty()||p.isEmpty()||cp.isEmpty()){error("Vui lòng điền đầy đủ thông tin.");return;}
                if(!u.matches("[A-Za-z0-9_.-]{4,20}")){error("Tên đăng nhập cần 4-20 ký tự.");return;}
                if(!ph.matches("[0-9+() .-]{9,15}")){error("Số điện thoại chưa đúng định dạng.");return;}
                if(p.length()<6){error("Mật khẩu phải có ít nhất 6 ký tự.");return;}
                if(!p.equals(cp)){error("Mật khẩu nhập lại không khớp.");return;}
                if(USERS.containsKey(u)){error("Tên đăng nhập đã tồn tại.");return;}

                USERS.put(u, new UserInfo(p, ph)); 
                saveUsersToFile(); 

                JOptionPane.showMessageDialog(this,"Đăng ký thành công! Bạn có thể đăng nhập ngay."); 
                back();
            }
            void error(String s){JOptionPane.showMessageDialog(this,s,"Không thể đăng ký",JOptionPane.WARNING_MESSAGE);}
            void back(){loginFrame.setVisible(true);dispose();}
        }

        static class HomeFrame extends JFrame {
            HomeFrame(String user) {
                setTitle("Khách sạn Bình Minh"); setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(720,540); setLocationRelativeTo(null);
                JPanel root=new JPanel(new BorderLayout()); root.setBackground(PALE); root.add(new Header(),BorderLayout.NORTH);
                JPanel center=new JPanel(new GridBagLayout()); center.setBackground(PALE); JPanel c=card();
                c.setLayout(new BoxLayout(c,BoxLayout.Y_AXIS)); c.setPreferredSize(new Dimension(420,310));
                JLabel welcome=label("Xin chào, "+user+"!",22,true,TEXT); welcome.setAlignmentX(Component.LEFT_ALIGNMENT); c.add(welcome);
                c.add(Box.createVerticalStrut(10)); JLabel info=label("Hệ thống quản lý đặt phòng Khách sạn Bình Minh.",13,false,TEXT);
                info.setAlignmentX(Component.LEFT_ALIGNMENT); c.add(info); c.add(Box.createVerticalStrut(20));
                
                JButton booking = solidButton("XEM SƠ ĐỒ & ĐẶT PHÒNG"); booking.setAlignmentX(Component.LEFT_ALIGNMENT);
                booking.addActionListener(e->{new BookingFrame(user).setVisible(true);dispose();}); c.add(booking);
                c.add(Box.createVerticalStrut(10)); 

                JButton history = outlineButton("LỊCH SỬ & HUỶ PHÒNG"); history.setAlignmentX(Component.LEFT_ALIGNMENT);
                history.addActionListener(e->{new HistoryFrame(user).setVisible(true);dispose();}); c.add(history);
                c.add(Box.createVerticalStrut(10));
                
                JButton logout = outlineButton("ĐĂNG XUẤT"); logout.setAlignmentX(Component.LEFT_ALIGNMENT);
                logout.addActionListener(e->{new LoginFrame().setVisible(true);dispose();});
                c.add(logout); center.add(c); root.add(center,BorderLayout.CENTER); setContentPane(root);
            }
        }

        static class HistoryFrame extends JFrame {
            HistoryFrame(String user) {
                setTitle("Lịch sử đặt phòng - Khách sạn Bình Minh");
                setDefaultCloseOperation(EXIT_ON_CLOSE);
                setSize(950, 550);
                setLocationRelativeTo(null);

                JPanel root = new JPanel(new BorderLayout());
                root.setBackground(PALE);
                root.add(new Header(), BorderLayout.NORTH);

                JPanel center = new JPanel(new BorderLayout(10, 10));
                center.setBackground(PALE);
                center.setBorder(new EmptyBorder(15, 15, 15, 15));

                JLabel title = label(user.equals("admin") ? "Tất cả lịch sử đặt phòng trong hệ thống (Admin)" : "Lịch sử đặt phòng của bạn", 18, true, TEXT);
                center.add(title, BorderLayout.NORTH);

                String[] columns = {"Số phòng", "Loại phòng", "Người đặt", "Họ tên khách", "CCCD", "SĐT", "Số khách", "Nhận phòng", "Trả phòng"};
                javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(columns, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };

                for (Room room : ROOMS.values()) {
                    if (room.isBooked && (user.equals("admin") || user.equals(room.bookedBy))) {
                        model.addRow(new Object[]{
                            room.id, room.type, room.bookedBy, room.customerName, room.cccd, room.phone, room.numberOfGuests, room.checkIn, room.checkOut
                        });
                    }
                }

                JTable table = new JTable(model);
                table.setRowHeight(28);
                table.setFont(new Font("SansSerif", Font.PLAIN, 13));
                table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
                JScrollPane scrollPane = new JScrollPane(table);
                center.add(scrollPane, BorderLayout.CENTER);

                JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
                bottomPanel.setBackground(PALE);

                JButton cancelBtn = solidButton("HUỶ PHÒNG ĐÃ CHỌN");
                cancelBtn.setPreferredSize(new Dimension(200, 40));
                cancelBtn.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(this, "Vui lòng chọn dòng phòng cần huỷ trong bảng.");
                        return;
                    }
                    String roomId = (String) table.getValueAt(selectedRow, 0);
                    int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn huỷ phòng " + roomId + " không?", "Xác nhận huỷ phòng", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        Room room = ROOMS.get(roomId);
                        if (room != null) {
                            room.isBooked = false;
                            room.bookedBy = "";
                            room.customerName = "";
                            room.cccd = "";
                            room.phone = "";
                            room.numberOfGuests = "1";
                            room.checkIn = "";
                            room.checkOut = "";
                            saveBookingsToFile();
                            JOptionPane.showMessageDialog(this, "Đã huỷ phòng " + roomId + " thành công!");
                            new HistoryFrame(user).setVisible(true);
                            dispose();
                        }
                    }
                });

                JButton backBtn = outlineButton("Quay lại");
                backBtn.setPreferredSize(new Dimension(130, 40));
                backBtn.addActionListener(e -> {
                    new HomeFrame(user).setVisible(true);
                    dispose();
                });

                bottomPanel.add(cancelBtn);
                bottomPanel.add(backBtn);
                center.add(bottomPanel, BorderLayout.SOUTH);

                root.add(center, BorderLayout.CENTER);
                setContentPane(root);
            }
        }

        static class BookingFrame extends JFrame {
            JTextField name = field();
            JTextField cccd = field();   
            JTextField phone = field();
            JTextField guests = field();
            // Thay JTextField bằng JSpinner chọn ngày trực quan
            DatePickerField checkInPicker =
                new DatePickerField(java.time.LocalDate.now().plusDays(1));

            DatePickerField checkOutPicker =
                new DatePickerField(java.time.LocalDate.now().plusDays(2));
            
            String selectedRoomId = null;
            JLabel selectedRoomLabel = label("Chưa chọn phòng nào", 13, true, new Color(180, 40, 40));
            JPanel roomGridPanel;

            BookingFrame(String user) {
                setTitle("Chọn và đặt phòng - Khách sạn Bình Minh"); setDefaultCloseOperation(EXIT_ON_CLOSE);
                setSize(980, 780); setLocationRelativeTo(null);
                JPanel root=new JPanel(new BorderLayout()); root.setBackground(PALE); root.add(new Header(),BorderLayout.NORTH);

                JPanel mainContent = new JPanel(new GridLayout(1, 2, 15, 0));
                mainContent.setBackground(PALE);
                mainContent.setBorder(new EmptyBorder(15, 15, 15, 15));

                JPanel leftCard = card();
                leftCard.setLayout(new BoxLayout(leftCard, BoxLayout.Y_AXIS));
                JLabel title=label("Thông tin đặt phòng", 18, true, TEXT); title.setAlignmentX(Component.LEFT_ALIGNMENT); leftCard.add(title);
                
                leftCard.add(Box.createVerticalStrut(10)); addField(leftCard, "Họ tên khách", name);
                leftCard.add(Box.createVerticalStrut(6));  addField(leftCard, "Số CCCD", cccd); 
                leftCard.add(Box.createVerticalStrut(6));  addField(leftCard, "Số điện thoại liên hệ", phone);
                leftCard.add(Box.createVerticalStrut(6));  addField(leftCard, "Số lượng khách (người)", guests);
                leftCard.add(Box.createVerticalStrut(6));
                addField(leftCard, "Ngày nhận phòng (dd/MM/yyyy)", checkInPicker);

                leftCard.add(Box.createVerticalStrut(6));
                addField(leftCard, "Ngày trả phòng (dd/MM/yyyy)", checkOutPicker);
                
                leftCard.add(Box.createVerticalStrut(10));
                leftCard.add(label("Phòng đang chọn:", 13, true, TEXT));
                leftCard.add(Box.createVerticalStrut(4));
                selectedRoomLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                leftCard.add(selectedRoomLabel);
                
                leftCard.add(Box.createVerticalStrut(14));
                JButton submit = solidButton("GỬI YÊU CẦU ĐẶT PHÒNG");
                submit.setAlignmentX(Component.LEFT_ALIGNMENT); submit.addActionListener(e->submit(user)); leftCard.add(submit);
                
                leftCard.add(Box.createVerticalStrut(10));
                JButton back = outlineButton("Quay lại"); back.setAlignmentX(Component.LEFT_ALIGNMENT);
                back.addActionListener(e->{new HomeFrame(user).setVisible(true);dispose();}); leftCard.add(back);

                JPanel rightCard = card();
                rightCard.setLayout(new BorderLayout());
                JLabel mapTitle = label("Sơ đồ phòng (Chọn phòng bên dưới)", 16, true, TEXT);
                rightCard.add(mapTitle, BorderLayout.NORTH);

                roomGridPanel = new JPanel();
                roomGridPanel.setLayout(new BoxLayout(roomGridPanel, BoxLayout.Y_AXIS));
                roomGridPanel.setBackground(Color.WHITE);
                refreshRoomGrid();

                JScrollPane scrollPane = new JScrollPane(roomGridPanel);
                scrollPane.setBorder(BorderFactory.createEmptyBorder());
                rightCard.add(scrollPane, BorderLayout.CENTER);

                JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
                legend.setBackground(Color.WHITE);
                legend.add(createLegendItem(FB_BLUE, "Trống"));
                legend.add(createLegendItem(new Color(40, 167, 69), "Đang chọn"));
                legend.add(createLegendItem(new Color(220, 224, 230), "Đã đặt"));
                rightCard.add(legend, BorderLayout.SOUTH);

                mainContent.add(leftCard);
                mainContent.add(rightCard);
                root.add(mainContent, BorderLayout.CENTER);
                setContentPane(root);

                if (USERS.containsKey(user)) {
                    if (USERS.get(user).phone != null) phone.setText(USERS.get(user).phone);
                }
                guests.setText("2");
            }

            JPanel createLegendItem(Color color, String text) {
                JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
                p.setBackground(Color.WHITE);
                JPanel box = new JPanel();
                box.setPreferredSize(new Dimension(14, 14));
                box.setBackground(color);
                p.add(box);
                p.add(label(text, 11, false, TEXT));
                return p;
            }

            void refreshRoomGrid() {
                roomGridPanel.removeAll();
                for (int floor = 5; floor >= 2; floor--) {
                    JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                    floorPanel.setBackground(Color.WHITE);
                    floorPanel.setBorder(BorderFactory.createTitledBorder("Tầng " + floor));

                    for (int i = 1; i <= 4; i++) {
                        String roomId = floor + "0" + i;
                        Room room = ROOMS.get(roomId);
                        if (room == null) continue;

                        RoomButton roomBtn = new RoomButton("", FB_BLUE, Color.WHITE, new Dimension(110, 70));

                        if (room.isBooked) {
                            roomBtn.updateStyle("<html><center><b>Phòng " + room.id + "</b><br><font size='2'>" + room.type + "<br>" + (room.price / 1000) + "k</font></center></html>",
                                                new Color(220, 224, 230), new Color(90, 100, 110));
                        } else if (roomId.equals(selectedRoomId)) {
                            roomBtn.updateStyle("<html><center><b>Phòng " + room.id + "</b><br><font size='2'>" + room.type + "<br>" + (room.price / 1000) + "k</font></center></html>",
                                                new Color(40, 167, 69), Color.WHITE);
                        } else {
                            roomBtn.updateStyle("<html><center><b>Phòng " + room.id + "</b><br><font size='2'>" + room.type + "<br>" + (room.price / 1000) + "k</font></center></html>",
                                                FB_BLUE, Color.WHITE);
                        }

                        roomBtn.addActionListener(e -> {
                            if (room.isBooked) {
                                JOptionPane.showMessageDialog(this, 
                                    "Phòng " + room.id + " đã được đặt!\n" +
                                    "- Khách: " + room.customerName + "\n" +
                                    "- Số người: " + room.numberOfGuests + " người\n" +
                                    "- SĐT: " + room.phone + "\n" +
                                    "- Nhận phòng: " + room.checkIn + "\n" +
                                    "- Trả phòng: " + room.checkOut, 
                                    "Thông tin phòng", JOptionPane.INFORMATION_MESSAGE);
                                return;
                            }
                            selectedRoomId = roomId;
                            selectedRoomLabel.setText("Phòng " + room.id + " (" + room.type + " - " + String.format("%,d", room.price) + " đ/đêm - Tối đa " + room.maxGuests + " khách)");
                            selectedRoomLabel.setForeground(FB_BLUE);
                            refreshRoomGrid();
                        });

                        floorPanel.add(roomBtn);
                    }
                    roomGridPanel.add(floorPanel);
                }
                roomGridPanel.revalidate();
                roomGridPanel.repaint();
            }

            void submit(String user) {
                String customerName = name.getText().trim();
                String customerCccd = cccd.getText().trim();
                String customerPhone = phone.getText().trim();
                String numGuests = guests.getText().trim();

                if (customerName.isEmpty() || customerCccd.isEmpty() || customerPhone.isEmpty() || numGuests.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin, CCCD, SĐT và số lượng khách."); return;
                }
                if (!customerCccd.matches("\\d{9,12}")) {
                    JOptionPane.showMessageDialog(this, "Số CCCD không hợp lệ (Phải gồm 9 hoặc 12 chữ số)."); return;
                }
                if (!customerPhone.matches("[0-9+() .-]{9,15}")) {
                    JOptionPane.showMessageDialog(this, "Số điện thoại chưa đúng định dạng."); return;
                }
                if (!numGuests.matches("\\d+") || Integer.parseInt(numGuests) <= 0) {
                    JOptionPane.showMessageDialog(this, "Số lượng khách phải là số nguyên dương."); return;
                }
                if (selectedRoomId == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng trong sơ đồ khách sạn."); return;
                }

                Room room = ROOMS.get(selectedRoomId);

                int guestCount = Integer.parseInt(numGuests);
                if (guestCount > room.maxGuests) {
                    JOptionPane.showMessageDialog(this, "Phòng " + room.type + " (" + room.id + ") chỉ cho phép tối đa " + room.maxGuests + " người! Vui lòng chọn loại phòng khác lớn hơn.", "Vượt quá giới hạn người", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Lấy giá trị ngày từ JSpinner
    java.time.LocalDate in = checkInPicker.getDate();
    java.time.LocalDate out = checkOutPicker.getDate();

    if (in == null) {
        JOptionPane.showMessageDialog(
            this,
            "Ngày nhận phòng không hợp lệ. Vui lòng nhập ngày/tháng/năm."
        );
        return;
    }

    if (out == null) {
        JOptionPane.showMessageDialog(
            this,
            "Ngày trả phòng không hợp lệ. Vui lòng nhập ngày/tháng/năm."
        );
        return;
    }

    if (!out.isAfter(in)) {
        JOptionPane.showMessageDialog(
            this,
            "Ngày trả phòng phải sau ngày nhận phòng."
        );
        return;
    }

    if (in.isBefore(java.time.LocalDate.now())) {
        JOptionPane.showMessageDialog(
            this,
            "Ngày nhận phòng không được ở quá khứ."
        );
        return;
    }
            }
        }
    }