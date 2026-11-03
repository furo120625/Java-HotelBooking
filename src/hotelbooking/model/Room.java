package hotelbooking.model;

public final class Room {
    private final String id;
    private final int floor;
    private final String type;
    private final int capacity;
    private long pricePerNight;

    public Room(String id, int floor, String type, int capacity, long pricePerNight) {
        this.id = id;
        this.floor = floor;
        this.type = type;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
    }

    public String getId() { return id; }
    public int getFloor() { return floor; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public long getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(long price) {
        if (price <= 0) throw new IllegalArgumentException("Giá phòng phải lớn hơn 0.");
        this.pricePerNight = price;
    }
}