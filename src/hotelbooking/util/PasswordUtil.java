package hotelbooking.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    public static String newSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(char[] password, String saltText) {
        PBEKeySpec spec = new PBEKeySpec(password, Base64.getDecoder().decode(saltText),
                                         ITERATIONS, KEY_LENGTH);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể mã hóa mật khẩu.", ex);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean verify(char[] password, String salt, String expectedHash) {
        try {
            byte[] actual = Base64.getDecoder().decode(hash(password, salt));
            byte[] expected = Base64.getDecoder().decode(expectedHash);
            return MessageDigest.isEqual(actual, expected);
        } catch (RuntimeException ex) {
            return false;
        }
    }
}