package chat.app.chatapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import chat.app.chatapp.model.db.*;
import chat.app.chatapp.model.message.Model.ChatMessage;
import chat.app.chatapp.model.message.Model.Conversation;
import chat.app.chatapp.model.message.Security.MessageProtection;
import chat.app.chatapp.model.UserSession;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;


import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class ChatController {
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Procedure proc;

    @GetMapping("{id}/tro-chuyen/tin-nhan/{friendId}/{friendName}")
    public ModelAndView LoadConversation(@PathVariable("id") int id, @PathVariable("friendId") int converId,
                                     @PathVariable("friendName") String nameChat, HttpSession session) {
        List<Message> messages = proc.GetMessages(converId);
        ModelAndView result = new ModelAndView();
        session.setAttribute("id", id);
        List<Conversation> users = proc.GetConvers(id);
        List<User> friends = proc.GetFriends(id);
        @SuppressWarnings("deprecation")
        String chatColor= (String) jdbcTemplate.queryForObject(
        "select color from users where id = ?", new Object[] { id }, String.class);
        
        @SuppressWarnings("deprecation")
        int chatType= (Integer) jdbcTemplate.queryForObject(
        "select conversationType from conversations where id = ?", new Object[] { converId }, Integer.class);
        String typeString = chatType == 1?"Friend": "Group";
        session.setAttribute("chatColor", chatColor);
        session.setAttribute("friends", friends);
        session.setAttribute("convers", users);
        result.addObject("chatType", typeString);
        result.setViewName("Chat");
        result.addObject("messages", messages);
        result.addObject("conversationId", converId);
        result.addObject("username", nameChat);
        return result;
    }
    
    @PostMapping("{id}/tro-chuyen/tin-nhan/{converId}/{friendName}")
    public ResponseEntity<Map<String, String>> HandleMessage(@PathVariable int id, @RequestBody ChatMessage messageR, @PathVariable(name="converId", required = true) int conId) {
        Map<String, String> response = new HashMap<>();
        try {
            if(messageR.getFileName() != null)
            {
                if(proc.AddFileMessageProcedure(id,conId, messageR.getFileName(), messageR.getFileType(),messageR.getFileData()))
                {
                    response.put("message", "Send success");
                }
                else{
                    response.put("message", "Send failed ");
                }
            }
            else{

                if(proc.AddMessagesProcedure(id,conId,messageR.getMessage()))
                {
                    response.put("message", "Send success");
                }
                else{
                    response.put("message", "Send failed ");
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }   
        return ResponseEntity.ok(response);
    }

    @PostMapping("{id}/tao-nhom")
    public String CreateGroupConversation(@PathVariable int id, @RequestParam(name="groupName", required = true) String grName, @RequestParam(name="cbParticipants", required = false) List<String> pars
    , HttpSession session) {
        if(pars != null && !pars.isEmpty()){
            int converId = proc.CreateGroup(grName, id);
            try {
                for(String p: pars){
                    proc.AddToGroup(Integer.parseInt(p), converId);
                }
            } catch (Exception e) {
                // TODO: handle exception
                return "Loi tao group";
                }
        }
        return "redirect:/trang-chu/"+ id;
        
    }
    @PostMapping("{id}/them-tv/{conId}")
    public String AddMemeber(@PathVariable int id, @PathVariable int conId, @RequestParam(name="cbParticipants", required = false) List<String> pars
    , HttpSession session) {
        if(pars != null && !pars.isEmpty()){
            try {
                for(String p: pars){
                    proc.AddToGroup(Integer.parseInt(p), conId);
                }
            } catch (Exception e) {
                // TODO: handle exception
                return "Loi tao group";
                }
        }
        return "redirect:/trang-chu/"+ id;
        
    }
    @PostMapping("/unsend")
    @ResponseBody
    public ResponseEntity<Map<String, String>> Unsend(@RequestBody ChatMessage message
    , HttpSession session) {
        Map<String, String> response = new HashMap<>();
        if(message.getFileName() == "")
        {
            String sql = "update messages set deleted_at = created_at, message_s=N'Đã thu hồi' where message_s = ?  and DATEADD(dd, 0, DATEDIFF(dd, 0, created_at)) = ? and conversation_id = ?";
            int rowsAffected = jdbcTemplate.update(sql, message.getMessage(),message.getCreatedAt(), message.getConverId());
            if (rowsAffected > 0) {
                response.put("Result", "success");
            } else {
                response.put("Result", "error");
                
            }
        }
        else{
            String delsql = "delete fileStorage where id = (select id from messages where message_s = ?)";
            String upsql = "update messages set deleted_at = created_at where message_s = ? and DATEADD(dd, 0, DATEDIFF(dd, 0, created_at)) = ? and conversation_id = ?";
            int delAffected = jdbcTemplate.update(delsql, message.getMessage());
            int upAffected = jdbcTemplate.update(upsql, message.getMessage(),message.getCreatedAt(), message.getConverId());
            if (delAffected > 0 && upAffected > 0) {
                response.put("Result", "success");
            } else {
                response.put("Result", "error");
            }
            
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}/getParticipants/{grId}")
    @ResponseBody
    public List<User> GetParticipants(@PathVariable int grId, @PathVariable int id) {
        List<User> users = jdbcTemplate.query(
                        "select conversations.id, users.* from Users, conversations, participants" + //
                        " where Conversations.id = Participants.conversation_id" + //
                        " and conversations.id = "+ grId + //
                        " and Participants.users_id != " + id + //
                        " and users.id = Participants.users_id",
        new RowMapper<User>() {
            @Override
            public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                // TODO Auto-generated method stub
                User u = new User();
                    u.setUsername(rs.getString("last_name") + ' ' + rs.getString("middle_name") + ' ' + rs.getString("first_name"));
                    u.setId(rs.getInt("id")); // conver id
                return u;
            }
        });
        return users;
    }
    
    @GetMapping("{id}/getParticipantsNoJoin/{grId}")
    @ResponseBody
    public List<User> GetParticipantsNoJoin(@PathVariable int grId, @PathVariable int id) {
        List<User> users = proc.GetParticipantsNoJoin(grId, id);
        return users;
    }

    @MessageMapping("/chatapp.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        return chatMessage;
    }

    @MessageMapping("/chatapp.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        return chatMessage;
    }

    @GetMapping("{id}/groupOnly")
    @ResponseBody
    public List<Conversation> GroupOnly(@PathVariable int id) {
        List<Conversation> cons = proc.GetConvers(id);
       
        return cons.stream().filter(e -> e.conType == 0).collect(Collectors.toList());
    }
    @GetMapping("{id}/allChat")
    @ResponseBody
    public List<Conversation> FriendOnly(@PathVariable int id) {
        
        List<Conversation> cons = proc.GetConvers(id);
       
        return cons.stream().filter(e -> e.conType == 1).collect(Collectors.toList());
    }
}
