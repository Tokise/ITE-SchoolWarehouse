package Package1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PasswordHasher {

    // !! IMPORTANT: This is a simple hashing example (SHA-256).
    // For production, use a strong, modern password hashing library like BCrypt or Argon2.
    // This implementation does NOT use salting, which is a critical security measure.
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            // Encode the hash as a Base64 string for storage
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // In a real application, you would handle this error more robustly
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verifies a plain text password against a stored hashed password.
     * It hashes the input password and compares it to the stored hash.
     * @param password The plain text password entered by the user.
     * @param hashedPasswordFromDB The hashed password retrieved from the database.
     * @return true if the password matches, false otherwise.
     */
    public static boolean verifyPassword(String password, String hashedPasswordFromDB) {
        // Hash the input password using the same method used for storage
        String calculatedHash = hashPassword(password);

        // Compare the newly calculated hash with the stored hash
        // Use a constant-time comparison in production to mitigate timing attacks,
        // but for this simple example, direct string equals is shown.
        // Corrected typo: calculatedatedHash -> calculatedHash
        return calculatedHash != null && calculatedHash.equals(hashedPasswordFromDB);
    }
}
