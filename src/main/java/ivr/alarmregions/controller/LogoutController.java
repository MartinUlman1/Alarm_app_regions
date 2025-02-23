package ivr.alarmregions.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LogoutController {

    @RequestMapping("/logout")
    public void logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
