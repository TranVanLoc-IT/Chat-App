package chat.app.chatapp.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import chat.app.chatapp.model.MailService.EmailDetails;
import chat.app.chatapp.model.MailService.SendMail;
import chat.app.chatapp.model.auth.HashPassword;
import chat.app.chatapp.model.auth.Verify;
import chat.app.chatapp.model.db.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import javax.swing.text.View;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import chat.app.chatapp.model.*;
@Controller
public class RegisterController {
    @Autowired(required = false)
    private JdbcTemplate connect;

    @Autowired
    private SendMail sendMail;
    @GetMapping("/register")
    public String ToRegister(HttpSession session) {
        if (session.getAttribute("uid") != null)
            return "redirect:/login";
        return "Register";
    }
    @PostMapping("/register")
    public ModelAndView RegisterRequest(@ModelAttribute(value = "user") User user,HttpServletRequest request, HttpSession session) {
        // handle
        String[] nameComponents = HandleData.HandleNameField(user.username);
        user.password = HashPassword.hashPassword(user.password); // password hash
        EmailDetails emailDetails = new EmailDetails(); 
        emailDetails.setRecipient(user.email);
        int userId = new Random().nextInt(10000 - 100 + 1) + 100;
        session.setAttribute("uid", userId);
        // what view want to return ??
        ModelAndView model = new ModelAndView();
        model.setViewName("redirect:/login");
        //
        try{
            try {
                @SuppressWarnings("deprecation")
                User userExisting = connect.queryForObject("select *from users where email = ?", new Object[]{user.email},  (rs, rowNum) -> {
                    User u = new User();
                    return u;
                });
                session.removeAttribute("uid");
                model.addObject("error", "Đã có tài khoản Email này!!");
                return model;
            } catch (Exception e) {
                // error: insert vietnamese, please  check datatype is nvarchar
                 String query = "insert into users(id, phone, last_name, middle_name, first_name, email, password, created_at) values(?,?,?,?,?,?,?,?)";
                connect.execute(query, (PreparedStatement ps) -> {
                            ps.setInt(1, userId);
                            ps.setString(2, user.phone);
                            ps.setString(3, nameComponents[0]);
                            ps.setString(4, nameComponents[1]);
                            ps.setString(5, nameComponents[2]);
                            ps.setString(6, user.email);
                            ps.setString(7, user.password);
                            ps.setString(8, LocalDate.now().toString());
                            return ps.executeUpdate();
                        });
                Verify.InitialForVerify(emailDetails, user.username, request.getRequestURL().toString());
                sendMail.sendSimpleMail(emailDetails);
                model.clear();
                model.setViewName("redirect:/verify-email");
                model.addObject("content", "Chúng tôi đã gửi email xác thực trong hòm thư Email của bạn!! Vui lòng xác nhận.");
            }
        }catch(Exception e){
            System.out.println("Loi" + e.getMessage());
        }
        return model;
    }
}
