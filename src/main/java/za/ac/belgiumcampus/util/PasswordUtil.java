package za.ac.belgiumcampus.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing utility using SHA-256 with a random per-user salt.
 *
 * No external dependency required (uses only java.security, already on
 * the classpath) - good enough for this project's requirements. If your
 * team later wants a stronger option, jBCrypt (org.mindrot:jbcrypt) is
 * a drop-in replacement, just add it to pom.xml and swap the two methods
 * below.
 *
 * Stored format: "salt:hash" (both Base64-encoded), so verification
 * doesn't need a separate salt column - it's all in one string that
 * fits in the existing password_hash VARCHAR(255) column.
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH_BYTES = 16;

    private PasswordUtil() {
        // Utility class - not meant to be instantiated
    }

    /**
     * Hashes a plain-text password with a freshly generated random salt.
     * Call this once, when registering a new user, and store the result
     * directly in the password_hash column.
     */
    public static String hash(String plainTextPassword) {
        byte[] salt = generateSalt();
        byte[] hashedBytes = hashWithSalt(plainTextPassword, salt);

        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hashedBytes);

        return saltBase64 + ":" + hashBase64;
    }

    /**
     * Verifies a plain-text password against a previously stored
     * "salt:hash" string (as produced by {@link #hash(String)}).
     * Call this during login.
     * @param plainTextPassword the password the user just typed in
     * @param storedHash the "salt:hash" string previously saved in password_hash
     * @return true if the password matches, false otherwise
     */
    public static boolean verify(String plainTextPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }

        String[] parts = storedHash.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        byte[] actualHash = hashWithSalt(plainTextPassword, salt);

        return MessageDigest.isEqual(expectedHash, actualHash);
    }
    
    public static boolean isStrongEnough(String plainTextPassword) {
    if (plainTextPassword == null || plainTextPassword.length() < 8) {
        return false;
    }
    boolean hasUpper = plainTextPassword.chars().anyMatch(Character::isUpperCase);
    boolean hasLower = plainTextPassword.chars().anyMatch(Character::isLowerCase);
    boolean hasDigit = plainTextPassword.chars().anyMatch(Character::isDigit);
    return hasUpper && hasLower && hasDigit;
}

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[] hashWithSalt(String plainTextPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            return digest.digest(plainTextPassword.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // SHA-256 and UTF-8 are always available on any standard JVM,
            // so this should never actually happen.
            throw new IllegalStateException("Password hashing failed", e);
        }
    }
}