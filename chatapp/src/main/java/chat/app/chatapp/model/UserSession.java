package chat.app.chatapp.model;

import java.util.List;

public class UserSession {
    public int id;
    public String username;
    public static boolean hasUserSessionWithId(List<UserSession> userSessions, int requestId) {
        for (UserSession userSession : userSessions) {
            if (userSession.id == requestId) {
                return true; // Nếu tìm thấy id giống, trả về true
            }
        }
        return false; // Nếu không tìm thấy id giống, trả về false
    }
}
