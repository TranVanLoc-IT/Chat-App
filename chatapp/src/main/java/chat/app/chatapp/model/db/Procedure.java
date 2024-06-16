package chat.app.chatapp.model.db;

import java.sql.ResultSet;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Component;

import chat.app.chatapp.model.message.Model.Conversation;

import javax.sql.DataSource;

@Component
public class Procedure {
   
    private final DataSource dataSource;
    
    public Procedure(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public LatestInfo GetLatestInfo(int converId) {
        SimpleJdbcCall actor = new SimpleJdbcCall(dataSource).withProcedureName("GetLatestInfo");
        SqlParameterSource inParams = new MapSqlParameterSource().addValue("converId", converId);
         
        Map<String, Object> outParams = actor.execute(inParams);
        try {
                
            LatestInfo latestInfo = new LatestInfo();
            latestInfo.senderId = (int) outParams.get("sender_id");
            latestInfo.latestDate = (Date) outParams.get("latestDate");
            latestInfo.latestMessage= (String) outParams.get("latestMess");
            latestInfo.senderName= (String) outParams.get("senderName");
            return latestInfo;
        } catch (Exception e) {
            // TODO: handle exception
            return null;
        }
         
    }		


    public List<Message> GetMessages(int converId) {
        try {
            SimpleJdbcCall procedureActor = new SimpleJdbcCall(dataSource)
            .withProcedureName("LoadMessage")
            .returningResultSet("Messages", new RowMapper<Message>() {
                @Override
                public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Message message = new Message();
                    message.id = rs.getInt("id");
                    message.sender_id = rs.getInt("sender_id");
                    message.color = rs.getString("color");
                    message.sender_name = rs.getString("sender_name");
                    message.message_s = rs.getString("message_s");
                    message.message_type = rs.getBoolean("message_type");
                    message.created_at = rs.getDate("created_at");
                    message.conversation_id = rs.getInt("conversation_id");
                    message.fileName = rs.getString("fileName");
                    message.fileData = rs.getString("fileData");
                    message.fileType = rs.getString("fileType");
                    message.color = rs.getString("color");
                    return message;
                }
            });
            
            SqlParameterSource in = new MapSqlParameterSource().addValue("converId", converId);
            Map<String, Object> out = procedureActor.execute(in);
            
            List<Message> messages = (List<Message>) out.get("Messages");
            return messages;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
            return null;
        }
    }
    public List<User> GetParticipantsNoJoin(int converId, int uId) {
        try {
            SimpleJdbcCall procedureActor = new SimpleJdbcCall(dataSource)
            .withProcedureName("getparticipantsnotin")
            .returningResultSet("users", new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User u = new User();
                    u.setUsername(rs.getString("last_name") + ' ' + rs.getString("middle_name") + ' ' + rs.getString("first_name"));
                    u.setId(rs.getInt("id")); // conver id
                    return u;
                }
            });
            
            SqlParameterSource in = new MapSqlParameterSource().addValue("conId", converId).addValue("id", uId);
            Map<String, Object> out = procedureActor.execute(in);
            
            List<User> users = (List<User>) out.get("users");
            return users;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
            return null;
        }
    }
    
    public List<Conversation> GetConvers(int uId) {
        try {
            SimpleJdbcCall procedureActor = new SimpleJdbcCall(dataSource)
            .withProcedureName("GetConvers")
            .returningResultSet("Conversations", new RowMapper<Conversation>() {
                @Override
                public Conversation mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Conversation u = new Conversation(rs.getString("title"), rs.getInt("id"), GetLatestInfo(rs.getInt("id")));
                    u.conType = rs.getInt("contype");
                    return u;
                }
            });
            
            SqlParameterSource in = new MapSqlParameterSource().addValue("id", uId);
            Map<String, Object> out = procedureActor.execute(in);
            
            List<Conversation> Conversations = (List<Conversation>) out.get("Conversations");
            return Conversations;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
            return null;
        }
    }
    public List<User> GetFriends(int uId) {
        try {
            SimpleJdbcCall procedureActor = new SimpleJdbcCall(dataSource)
            .withProcedureName("GetFriends")
            .returningResultSet("users", new RowMapper<User>() {
                @Override
                public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                    User u = new User();
                    u.setUsername(rs.getString("title"));
                    u.setId(rs.getInt("id")); // conver id
                    return u;
                }
            });
            
            SqlParameterSource in = new MapSqlParameterSource().addValue("id", uId);
            Map<String, Object> out = procedureActor.execute(in);
            
            List<User> users = (List<User>) out.get("users");
            return users;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            // TODO: handle exception
            return null;
        }
    }
    public boolean UseCheckVerifyProcedure(String procedureName, String email)
    {
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName(procedureName);

        SqlParameterSource in = new MapSqlParameterSource().addValue("email", email);

        
        boolean result = callPrc.executeFunction(Boolean.class, in);
        return result;
    }

    public boolean AddMessagesProcedure(int id, int converId, String message)
    {
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName("AddMessages");

        SqlParameterSource in = new MapSqlParameterSource().addValue("senderId", id).addValue("converId", converId).addValue("message", message);
        
        try {
            callPrc.execute(in);
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }

        return true;
    }
    public boolean AddFileMessageProcedure(int id, int converId, String fileName, String fileType, String fileData)
    {
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName("AddFileMessage");

        SqlParameterSource in = new MapSqlParameterSource().addValue("senderId", id).addValue("converId", converId).addValue("fileName", fileName).addValue("fileType", fileType).addValue("fileData", fileData);
        
        try {
            callPrc.execute(in);
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }
    
    public boolean UseAddFriendProcedure(int yoursId, int frId, String title)
    {
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName("AddFriend");

        SqlParameterSource in = new MapSqlParameterSource().addValue("creatorId", yoursId).addValue("friendId", frId).addValue("title", title);

        try {
            
            boolean result = callPrc.executeFunction(null, in);
            return true;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return false;
    }

    public int CreateGroup(String grName, int creatorId) {
        
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName("CreateGroup").declareParameters(
                        new SqlOutParameter("converId", Types.INTEGER)
                );;

        SqlParameterSource in = new MapSqlParameterSource().addValue("grName", grName).addValue("creatorId", creatorId);
        Map<String, Object> outParams = callPrc.execute(in);

        return (Integer) outParams.get("converId");  // Assuming the stored procedure returns a result set
    }
       
    public boolean AddToGroup(int pId, int grId)
    {
        SimpleJdbcCall callPrc = new SimpleJdbcCall(dataSource).withProcedureName("AddToGroup");

        SqlParameterSource in = new MapSqlParameterSource().addValue("pId", pId).addValue("grId", grId);
        try {
            callPrc.execute(in);
            return true;
        } catch (Exception e) {
            e.printStackTrace();  // Print detailed error
        }
        return false;
    }
}
