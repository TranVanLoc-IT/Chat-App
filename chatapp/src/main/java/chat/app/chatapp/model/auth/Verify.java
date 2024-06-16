package chat.app.chatapp.model.auth;

import java.util.Random;

import chat.app.chatapp.model.MailService.EmailDetails;

public class Verify {
    private static Random rng = new Random();
    private static String characters = "zxcvbnmlkpoijhuygftrdseawq";
    public static void InitialForVerify(EmailDetails email, String userName, String requeString){
        String url = requeString + "/verify-email-result?code=" + GenerateString(64);
        email.setMsgBody("Gửi " + userName +",<br>"
        + "Hãy chọn đường dẫn bên dưới để xác thực:<br>"
        + "<h3><a href=\""+url+"\" target=\"_self\">Xác thực</a></h3>"
        + "Cảm ơn,<br>"
        + "MyChat - Hãy thỏa thích với cách riêng của bạn!."); 
        email.setSubject("Xác thực Email tạo tài khoản");
    }
    private static String GenerateString(int length)
    {
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}
