package Package1;

public class User {
    private String username;
    private String fullName;
    private String role;
    private int userId;
    private byte[] profilePicture;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public boolean isAdmin() {
        return "Admin".equals(role);
    }

    @Override
    public String toString() {
        return "User{" +
               "userId=" + userId +
               ", username='" + username + '\'' +
               ", fullName='" + fullName + '\'' +
               ", role='" + role + '\'' +
               ", profilePicture=" + (profilePicture != null ? "present (" + profilePicture.length + " bytes)" : "absent") +
               '}';
    }
}
