package chat.app.chatapp.model.db;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.swing.tree.RowMapper;

@ToString
@NoArgsConstructor
@Setter
@Getter
public class User {
    
    public int id;

    public String username;

    public String password;
    
    public String phone;
    
    public String email;

    public boolean isActive;

}