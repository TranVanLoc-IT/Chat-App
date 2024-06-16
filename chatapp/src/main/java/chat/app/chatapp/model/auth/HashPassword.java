package chat.app.chatapp.model.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashPassword {
    // Mã hóa mật khẩu sử dụng MD5
    public static String hashPassword(String password) {
        String strPassword = password + "abcjaskdaj";
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] encodedHash = digest.digest(strPassword.getBytes(StandardCharsets.UTF_8));

            // Chuyển đổi byte array sang dạng hex string
            StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Encode dữ liệu theo Base64
    public static String encodeBase64(String data) {
        return Base64.getEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    // So sánh mật khẩu với chuỗi băm
    public static boolean comparePassword(String password, String hashedPassword) {
        String inputHash = hashPassword(password); // Mã hóa mật khẩu nhập vào
        return hashedPassword.equals(inputHash); // So sánh chuỗi băm
    }
}
