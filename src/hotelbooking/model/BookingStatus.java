package hotelbooking.model;

public enum BookingStatus {
    PENDING_PAYMENT, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, EXPIRED;

    public boolean blocksAvailability() {
        return this == PENDING_PAYMENT || this == CONFIRMED || this == CHECKED_IN || this == CHECKED_OUT;
    }
}