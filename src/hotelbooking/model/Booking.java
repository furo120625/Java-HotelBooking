package hotelbooking.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Booking {
    private final String id;
    private final String createdBy;
    private final String guestName;
    private final String cccd;
    private final String phone;
    private final int guestCount;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final List<String> roomIds;
    private final long totalAmount;
    private final long depositAmount;
    private BookingStatus status;
    private final LocalDateTime createdAt;
    private final String checkInCodeHash;

    public Booking(String id, String createdBy, String guestName, String cccd, String phone,
                   int guestCount, LocalDate checkIn, LocalDate checkOut, List<String> roomIds,
                   long totalAmount, long depositAmount, BookingStatus status, LocalDateTime createdAt) {
        this(id, createdBy, guestName, cccd, phone, guestCount, checkIn, checkOut, roomIds,
                totalAmount, depositAmount, status, createdAt, "");
    }

    public Booking(String id, String createdBy, String guestName, String cccd, String phone,
                   int guestCount, LocalDate checkIn, LocalDate checkOut, List<String> roomIds,
                   long totalAmount, long depositAmount, BookingStatus status, LocalDateTime createdAt,
                   String checkInCodeHash) {
        this.id = id;
        this.createdBy = createdBy;
        this.guestName = guestName;
        this.cccd = cccd;
        this.phone = phone;
        this.guestCount = guestCount;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.roomIds = List.copyOf(roomIds);
        this.totalAmount = totalAmount;
        this.depositAmount = depositAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.checkInCodeHash = checkInCodeHash == null ? "" : checkInCodeHash;
    }

    public String getId() { return id; }
    public String getCreatedBy() { return createdBy; }
    public String getGuestName() { return guestName; }
    public String getCccd() { return cccd; }
    public String getPhone() { return phone; }
    public int getGuestCount() { return guestCount; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public List<String> getRoomIds() { return roomIds; }
    public long getTotalAmount() { return totalAmount; }
    public long getDepositAmount() { return depositAmount; }
    public BookingStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCheckInCodeHash() { return checkInCodeHash; }
    public void setStatus(BookingStatus status) { this.status = status; }
    public long getNights() { return java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut); }
}