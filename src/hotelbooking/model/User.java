package hotelbooking.model;

public final class User {
    private final String username;
    private final String salt;
    private final String passwordHash;
    private String phone;
    private Role role;
    private AccountStatus status;

    public User(String username, String salt, String passwordHash, String phone,
                Role role, AccountStatus status) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.phone = phone == null ? "" : phone;
        this.role = role;
        this.status = status;
    }

    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public Role getRole() { return role; }
    public AccountStatus getStatus() { return status; }

    public void setPhone(String phone) { this.phone = phone == null ? "" : phone; }
    public void setRole(Role role) { this.role = role; }
    public void setStatus(AccountStatus status) { this.status = status; }
}