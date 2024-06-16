package chat.app.chatapp.model.message.Model;

import java.time.LocalDate;

import chat.app.chatapp.model.db.LatestInfo;

public class Conversation {
    public String username;
    public int id;
    public int conType;
    
    public LatestInfo latestInfo;
    public Conversation(String username, int id, LatestInfo latestInfo) {
        this.username = username;
        this.id = id;
        this.latestInfo = latestInfo;
    }
}
