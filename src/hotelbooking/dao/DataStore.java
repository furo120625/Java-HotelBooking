package hotelbooking.dao;

import hotelbooking.model.*;
import hotelbooking.util.PasswordUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import java.security.SecureRandom;

public final class DataStore {
    private static final Path DATA_DIR = Path.of("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.txt");
    private static final Path BOOKINGS_FILE = DATA_DIR.resolve("bookings.txt");
    private static final Path PAYMENTS_FILE = DATA_DIR.resolve("payments.txt");
    private static final Path AUDIT_FILE = DATA_DIR.resolve("audit.log");

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Room> rooms = new LinkedHashMap<>();
    private final Map<String, Booking> bookings = new LinkedHashMap<>();
    private final Map<String, String> payments = new LinkedHashMap<>();

    public DataStore() throws IOException {
        Files.createDirectories(DATA_DIR);
        initializeRooms();
        loadUsers();
        bootstrapFirstAdminIfNeeded();
        loadBookings();
        loadPayments();
        expireOldPendingPayments();
    }

    private void initializeRooms() {
        for (int i = 1; i <= 4; i++) addRoom("20" + i, 2, "Standard", 2, 800_000);
        addRoom("301", 3, "Standard", 2, 800_000);
        addRoom("302", 3, "Standard", 2, 800_000);
        addRoom("303", 3, "Deluxe", 4, 1_200_000);
        addRoom("304", 3, "Deluxe", 4, 1_200_000);
        for (int i = 1; i <= 4; i++) addRoom("40" + i, 4, "Deluxe", 4, 1_200_000);
        for (int i = 1; i <= 4; i++) addRoom("50" + i, 5, "VIP", 4, 2_000_000);
        Path prices = DATA_DIR.resolve("rooms.txt");
        if (Files.exists(prices)) {
            try {
                for (String line : Files.readAllLines(prices, StandardCharsets.UTF_8)) {
                    String[] p = line.split("\\|", -1);
                    if (p.length == 2 && rooms.containsKey(p[0])) {
                        try { rooms.get(p[0]).setPricePerNight(Long.parseLong(p[1])); }
                        catch (RuntimeException ignored) {}
                    }
                }
            } catch (IOException ignored) {}
        }
    }

    private void addRoom(String id, int floor, String type, int capacity, long price) {
        rooms.put(id, new Room(id, floor, type, capacity, price));
    }

    private void loadUsers() throws IOException {
        if (Files.exists(USERS_FILE)) {
            for (String line : Files.readAllLines(USERS_FILE, StandardCharsets.UTF_8)) {
                String[] p = line.split("\\|", -1);
                if (p.length != 6) continue;
                try {
                    User user = new User(dec(p[0]), p[1], p[2], dec(p[3]),
                            Role.valueOf(p[4]), AccountStatus.valueOf(p[5]));
                    users.put(user.getUsername().toLowerCase(Locale.ROOT), user);
                } catch (RuntimeException ignored) {}
            }
            if (users.isEmpty()) {
                throw new IOException("data/users.txt tồn tại nhưng không có tài khoản hợp lệ. "
                        + "Hãy sao lưu và kiểm tra file này trước khi chạy lại.");
            }
            return;
        }

        // Import legacy TK.txt once. Existing plaintext passwords are immediately
        // converted to salted PBKDF2 hashes in data/users.txt.
        Path legacy = Path.of("TK.txt");
        if (Files.exists(legacy)) {
            for (String line : Files.readAllLines(legacy, StandardCharsets.UTF_8)) {
                String[] p = line.split(",", 3);
                if (p.length < 3 || p[0].isBlank()) continue;
                String username = p[0].trim();
                String salt = PasswordUtil.newSalt();
                String hash = PasswordUtil.hash(p[1].trim().toCharArray(), salt);
                Role role = username.equalsIgnoreCase("admin") ? Role.ADMIN : Role.GUEST;
                User user = new User(username, salt, hash, p[2].trim(), role, AccountStatus.ACTIVE);
                users.put(username.toLowerCase(Locale.ROOT), user);
            }
        }
        saveUsers();
    }

    private void bootstrapFirstAdminIfNeeded() throws IOException {
        // A new installation or a legacy data set without an administrator
        // needs a bootstrap account. Never promote a public registration to ADMIN.
        boolean hasAdmin = users.values().stream().anyMatch(u -> u.getRole() == Role.ADMIN);
        if (hasAdmin) return;

        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        String initialPassword = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        String salt = PasswordUtil.newSalt();
        char[] initialPasswordChars = initialPassword.toCharArray();
        String initialHash;
        try {
            initialHash = PasswordUtil.hash(initialPasswordChars, salt);
        } finally {
            Arrays.fill(initialPasswordChars, (char) 0);
        }
        User admin = new User("admin", salt, initialHash,
                "", Role.ADMIN, AccountStatus.ACTIVE);
        users.put("admin", admin);

        Path credentials = DATA_DIR.resolve("INITIAL_ADMIN_CREDENTIALS.txt");
        Files.writeString(credentials,
                "Tài khoản quản trị ban đầu\n"
                + "Username: admin\n"
                + "Password: " + initialPassword + "\n"
                + "Hãy đăng nhập và đổi mật khẩu ngay sau lần chạy đầu tiên.\n",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        System.out.println("Đã tạo tài khoản Admin ban đầu. Xem thông tin tại: "
                + credentials.toAbsolutePath());
        saveUsers();
    }

    private void loadBookings() throws IOException {
        if (Files.exists(BOOKINGS_FILE)) {
            for (String line : Files.readAllLines(BOOKINGS_FILE, StandardCharsets.UTF_8)) {
                try {
                    String[] p = line.split("\\|", -1);
                    if (p.length != 13) continue;
                    Booking b = new Booking(dec(p[0]), dec(p[1]), dec(p[2]), dec(p[3]), dec(p[4]),
                            Integer.parseInt(p[5]), LocalDate.parse(p[6]), LocalDate.parse(p[7]),
                            p[8].isBlank() ? List.of() : Arrays.asList(p[8].split(",")),
                            Long.parseLong(p[9]), Long.parseLong(p[10]), BookingStatus.valueOf(p[11]),
                            LocalDateTime.parse(p[12]));
                    bookings.put(b.getId(), b);
                } catch (RuntimeException ignored) {}
            }
            return;
        }
        importLegacyBookings();
        saveBookings();
    }

    private void importLegacyBookings() throws IOException {
        Path legacy = Path.of("DP.txt");
        if (!Files.exists(legacy)) return;
        int sequence = 1;
        for (String line : Files.readAllLines(legacy, StandardCharsets.UTF_8)) {
            String[] p = line.split(",", 9);
            if (p.length < 8 || !Boolean.parseBoolean(p[1].trim())) continue;
            Room room = rooms.get(p[0].trim());
            if (room == null) continue;
            try {
                LocalDate in = LocalDate.parse(p[6].trim());
                LocalDate out = LocalDate.parse(p[7].trim());
                if (!out.isAfter(in)) continue;
                int guests = p.length >= 9 ? Math.max(1, Integer.parseInt(p[8].trim())) : 1;
                String user = p[2].trim().isEmpty() ? "legacy" : p[2].trim();
                long total = Math.multiplyExact(room.getPricePerNight(),
                        java.time.temporal.ChronoUnit.DAYS.between(in, out));
                String id = "BM-LEGACY-" + String.format(Locale.ROOT, "%04d", sequence++);
                Booking booking = new Booking(id, user, p[3].trim(), p[4].trim(), p[5].trim(),
                        guests, in, out, List.of(room.getId()), total, total / 2,
                        BookingStatus.CONFIRMED, LocalDateTime.now());
                bookings.put(id, booking);
            } catch (RuntimeException ignored) {}
        }
    }

    private void loadPayments() throws IOException {
        if (!Files.exists(PAYMENTS_FILE)) return;
        for (String line : Files.readAllLines(PAYMENTS_FILE, StandardCharsets.UTF_8)) {
            String[] p = line.split("\\|", 2);
            if (p.length == 2) payments.put(dec(p[0]), p[1]);
        }
    }

    public synchronized void saveUsers() throws IOException {
        List<String> lines = new ArrayList<>();
        for (User u : users.values()) {
            lines.add(enc(u.getUsername()) + "|" + u.getSalt() + "|" + u.getPasswordHash()
                    + "|" + enc(u.getPhone()) + "|" + u.getRole() + "|" + u.getStatus());
        }
        writeAtomically(USERS_FILE, lines);
    }

    public synchronized void saveBookings() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings.values()) {
            lines.add(enc(b.getId()) + "|" + enc(b.getCreatedBy()) + "|"
                    + enc(b.getGuestName()) + "|" + enc(b.getCccd()) + "|" + enc(b.getPhone())
                    + "|" + b.getGuestCount() + "|" + b.getCheckIn() + "|" + b.getCheckOut()
                    + "|" + String.join(",", b.getRoomIds()) + "|" + b.getTotalAmount()
                    + "|" + b.getDepositAmount() + "|" + b.getStatus() + "|" + b.getCreatedAt());
        }
        writeAtomically(BOOKINGS_FILE, lines);
    }

    public synchronized void savePayments() throws IOException {
        List<String> lines = new ArrayList<>();
        payments.forEach((id, state) -> lines.add(enc(id) + "|" + state));
        writeAtomically(PAYMENTS_FILE, lines);
    }

    public synchronized void savePrices() throws IOException {
        List<String> lines = new ArrayList<>();
        rooms.values().forEach(r -> lines.add(r.getId() + "|" + r.getPricePerNight()));
        writeAtomically(DATA_DIR.resolve("rooms.txt"), lines);
    }

    public synchronized void audit(String actor, String action, String detail) {
        try {
            String row = LocalDateTime.now() + "|" + enc(actor) + "|" + enc(action)
                    + "|" + enc(detail) + System.lineSeparator();
            Files.writeString(AUDIT_FILE, row, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("Không ghi được audit log: " + ex.getMessage());
        }
    }

    private void expireOldPendingPayments() throws IOException {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(20);
        boolean changed = false;
        for (Booking b : bookings.values()) {
            if (b.getStatus() == BookingStatus.PENDING_PAYMENT && b.getCreatedAt().isBefore(cutoff)) {
                b.setStatus(BookingStatus.EXPIRED);
                changed = true;
            }
        }
        if (changed) saveBookings();
    }

    private static void writeAtomically(Path path, List<String> lines) throws IOException {
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static String enc(String value) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
    }

    private static String dec(String value) {
        return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
    }

    public Map<String, User> users() { return users; }
    public Map<String, Room> rooms() { return rooms; }
    public Map<String, Booking> bookings() { return bookings; }
    public Map<String, String> payments() { return payments; }
}