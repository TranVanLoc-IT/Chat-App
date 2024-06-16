package chat.app.chatapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import chat.app.chatapp.model.db.*;
import chat.app.chatapp.model.message.Model.Conversation;
import chat.app.chatapp.model.UserSession;
import chat.app.chatapp.model.Profile.Profile;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
    @Autowired(required = false)
    private JdbcTemplate connect;

    @Autowired
    private Procedure proc;

    @GetMapping("{id}/dang-xuat")
    public String Logout(@PathVariable int id, HttpSession session) {
        if (session.getAttribute("uid") == null)
            return "redirect:/login";
        try {
            List<UserSession> restoreOrInit = (List<UserSession>) session.getAttribute("uid");
            if (restoreOrInit != null) {
                for (UserSession user : restoreOrInit) {
                    if (user.id == id) {
                        restoreOrInit.remove(user);
                    }
                }
                session.setAttribute("uid", restoreOrInit);
            }
        } catch (Exception e) {
            // TODO: handle exception
            return "redirect:/login";
        }
        return "redirect:/login";
    }
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam("fileType") String fileType, 
                                                 @RequestParam("fileName") String fileName, 
                                                 @RequestParam("fileData") String fileData) throws Exception {
        try {
            MediaType mediaType;
            try {
                mediaType = MediaType.parseMediaType(fileType+"/plain");
            } catch (InvalidMimeTypeException e) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .body(new InputStreamResource(new ByteArrayInputStream(Base64.getDecoder().decode(fileData))));
        } catch (Exception e) {
            throw new Exception("Error downloading file", e);
        }
    }
    @GetMapping({ "/trang-chu/{id}" })
    @ResponseBody
    public ModelAndView Home(@PathVariable("id") int id, HttpSession session) {
        if (session.getAttribute("uid") == null) {
            return new ModelAndView("redirect:/login");
        }
        List<UserSession> uss = (List<UserSession>) session.getAttribute("uid");
        if (!UserSession.hasUserSessionWithId(uss, id)) {
            return new ModelAndView("redirect:/login");
        }
        session.setAttribute("id", id);
        
        List<Conversation> users = proc.GetConvers(id);
        
        List<User> friends = proc.GetFriends(id);
        ModelAndView model = new ModelAndView();
        model.setViewName("Home");
        session.setAttribute("friends", friends);
        session.setAttribute("convers", users);

        return model;
    }

    @GetMapping("{id}/trang-chu/ket-ban")
    @ResponseBody
    public ModelAndView LoadFriendRecommend(@PathVariable int id) {
        List<User> users = connect.query("\r\n" + //
                "\tselect *from users where id not in \r\n" + //
                "\t(select users_id from conversations, participants where creator_id = " + id
                + " and conversations.id = Participants.conversation_id)",
                new RowMapper<User>() {
                    @Override
                    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
                        // TODO Auto-generated method stub
                        User u = new User();
                        u.setUsername(rs.getString("last_name") + ' ' + rs.getString("middle_name") + ' '
                                + rs.getString("first_name"));
                        u.setId(rs.getInt("id"));
                        return u;
                    }
        });
        ModelAndView model = new ModelAndView();
        model.setViewName("FriendsRecommend");
        model.addObject("friends", users);
        return model;
    }

    @PostMapping("{id}/trang-chu/ket-ban")
    public String AddFriendsRecommend(@PathVariable int id,
            @RequestParam(name = "frid", required = true) int frId,
            @RequestParam(name = "name", required = true) String title) {
        boolean add = proc.UseAddFriendProcedure(id, frId, title);
        
        return "redirect:/trang-chu/" + id;
    }

    @GetMapping("{id}/tai-khoan")
    public ModelAndView GetProfile(@PathVariable int id, HttpSession session) {
        session.setAttribute("id", id);
        Profile pr = connect.queryForObject("select *from users where id = ?", new RowMapper<Profile>() {
            @Override
            public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
                Profile p = new Profile();
                p.fullName = rs.getString("last_name") + ' ' + rs.getString("middle_name") + ' '
                        + rs.getString("first_name");
                p.dateSet = rs.getString("created_at");
                p.emailAddress = rs.getString("email");
                p.phoneNumber = rs.getString("phone");
                return p;
            }
        }, new Object[] { id });
        @SuppressWarnings("deprecation")
        int totalFriends = ((List<User>)session.getAttribute("friends")).size();
        @SuppressWarnings("deprecation")
        int totalGroups = (Integer) connect.queryForObject(
        "select count(*) from conversations as c, participants as p where c.id = p.conversation_id and conversationType = 0 and p.users_id = ?", new Object[] { id }, Integer.class);
        ModelAndView model = new ModelAndView();
        model.setViewName("Profile");
        model.addObject("profile", pr);
        model.addObject("totalFriends", totalFriends);
        
        model.addObject("totalGroups", totalGroups);

        return model;
    }

}
