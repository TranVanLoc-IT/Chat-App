package chat.app.chatapp.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ch.qos.logback.core.model.Model;
import jakarta.servlet.http.HttpSession;
@Controller
public class VerifyMailController {
    
    @Autowired(required = false)
    private JdbcTemplate connect;
    @GetMapping({"/register/verify-email-result", "/login/verify-email-result"})
    public String ToAuthentication(@RequestParam(name = "code", required = true) String code, HttpSession session) {
        // passed
        String addUserVerifyCode = "insert into user_verification values (?, ?, ?)";
        System.out.println(session.getAttribute(session.getAttribute("username").toString())+code);
        int result = connect.update(addUserVerifyCode, session.getAttribute(session.getAttribute("username").toString()), code, LocalDate.now());
        if(result != 0)
        {
            return "Home";
        }
        return "Register";
    }
    @GetMapping("/verify-email")
    @ResponseBody
    public ModelAndView Verify(@RequestParam(name = "content") String content) {
        ModelAndView model = new ModelAndView();
        model.addObject("content", content);
        model.setViewName("Verify");
        return model;
    }
}
