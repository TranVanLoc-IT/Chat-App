package chat.app.chatapp.model.db;

import java.sql.Date;

public class Message {
    
    public int id;
    
    public int sender_id;
    public String sender_name;
    
    public String message_s;
    public String color;

    public Boolean message_type;

    public Date created_at;

    public int conversation_id;


    public String fileName;
    public String fileType;
    
    public String fileData;

}
