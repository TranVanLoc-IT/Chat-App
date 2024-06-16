package chat.app.chatapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import chat.app.chatapp.model.MailService.EmailDetails;
import chat.app.chatapp.model.MailService.SendMail;
import chat.app.chatapp.model.auth.HashPassword;
import chat.app.chatapp.model.auth.Verify;
import chat.app.chatapp.model.db.Procedure;
import chat.app.chatapp.model.db.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.text.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import chat.app.chatapp.model.*;
@Controller
public class LoginController {
    @Autowired(required = false)
    private JdbcTemplate connect;
    @Autowired
    private Procedure proc;

    @Autowired
    private SendMail sendMail;
    @GetMapping({"/login","/", "/trang-chu"})
    public String ToLogin() {
        return "Login";
    }

    @GetMapping("/login/reset-password")
    public String GetResetPassword(){
        return "ResetPassword";
    }

    @PostMapping("/login/reset-password")
    public String PostResetPassword(@RequestParam("newpass") String newpass, @RequestParam("email") String email){
        // send mail verify
        connect.update("Update users set password = ? where email = ?", new Object[]{HashPassword.hashPassword(newpass), email});
        return "Login";
    }
    @PostMapping("/login")  
    @ResponseBody
    public ModelAndView LoginRequest(@RequestParam("email") String email, @RequestParam("pass") String pass, HttpServletRequest request, HttpSession session) {
        String query = "SELECT *FROM USERS WHERE email = ?";
        ModelAndView model = new ModelAndView();
        try{
            @SuppressWarnings("deprecation")
            User user = connect.queryForObject(query,new Object[]{email}, (rs, rowNum) -> {
                User u = new User();
                u.setUsername(rs.getString("last_name") + ' '+ rs.getString("middle_name") + ' ' + rs.getString("first_name"));
                u.setEmail(rs.getString("email"));
                u.setId(rs.getInt("id"));
                u.setPassword(rs.getString("password"));
                return u;
            });
            if( user != null ){
                UserSession us = new UserSession();
                us.id = user.getId();
                us.username = user.username;
                List<UserSession> restoreOrInit = (List<UserSession>)session.getAttribute("uid") == null ? new ArrayList<UserSession>() : (List<UserSession>)session.getAttribute("uid");
                restoreOrInit.add(us);
                session.setAttribute("uid", restoreOrInit);
                if (HashPassword.comparePassword(pass, user.password)) {
                        if(!proc.UseCheckVerifyProcedure("CheckVerify", email)){
                            EmailDetails emailDetails = new EmailDetails(); 
                        emailDetails.setRecipient(email);
                        Verify.InitialForVerify(emailDetails, email, request.getRequestURL().toString());
                        sendMail.sendSimpleMail(emailDetails);
                        model.clear();
                        model.setViewName("redirect:/verify-email");
                        model.addObject("content", "Chúng tôi đã gửi email xác thực trong hòm thư Email của bạn!! Vui lòng xác nhận.");
                        return model;
                    }
                        model.clear();
                        model.setViewName("redirect:/trang-chu/" + us.id);
                        model.addObject("user", user);
                    }
                    else{
                        throw new EmptyResultDataAccessException("Not correct password", 0);
                    }
            }
        }catch(EmptyResultDataAccessException ex){
            model.setViewName("Login");
            model.addObject("error", "Sai thông tin hoặc chưa có tài khoản");
        }
        return model;
    }
}
