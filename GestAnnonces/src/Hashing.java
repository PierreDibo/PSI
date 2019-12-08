
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pierre Dibo
 * @author Aillerie Anthony
 */
public class Hashing {

    public static char[] hash(String passwordToHash, byte[] salt) {
        try {
            String generatedPassword;
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt);
            byte[] bytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
            return generatedPassword.toCharArray();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Hashing.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean checkPassword(char[] hash, String attempt, byte[] salt) {
        char[] generatedHash = hash(attempt, salt);
        return Arrays.equals(hash, generatedHash);
    }
}
