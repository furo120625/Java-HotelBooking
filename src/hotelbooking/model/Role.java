package hotelbooking.model;

public enum Role {
    ADMIN(3), STAFF(2), GUEST(1);

    private final int level;

    Role(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}