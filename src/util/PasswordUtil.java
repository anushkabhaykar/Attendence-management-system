package util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for cryptographic password hashing and verification.
 * Uses SHA-256 for secure credential storage.
 * Location: src/util/PasswordUtil.java
 */
public class PasswordUtil {

    /**
     * Hashes a plain-text password using SHA-256.
     * @param plainPassword Raw password string
     * @return Hexadecimal representation of the SHA-256 hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies whether a plain password matches a stored hash.
     * @param plainPassword Candidate plain-text password
     * @param storedHash The hashed password stored in the database
     * @return true if credentials match, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        String computedHash = hashPassword(plainPassword);
        return computedHash.equalsIgnoreCase(storedHash);
    }
}
