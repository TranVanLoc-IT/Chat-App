package chat.app.chatapp.model.message.Model;

import java.sql.Date;
import java.time.LocalDateTime;

/**
 * Created by rajeevkumarsingh on 24/07/17.
 */
public class ChatMessage {
    private String fileName;
    private String fileType;
    private String fileData;
    private int converId;
    private Date createdAt;
    private MessageType type;
    private String message;
    private String sender;
    public String color;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String Message) {
        this.message = Message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
   
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileData() {
        return fileData;
    }

    public void setFileData(String fileData) {
        this.fileData = fileData;
    }

    public int getConverId() {
        return converId;
    }

    public void setConverId(int converId) {
        this.converId = converId;
    }
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}


