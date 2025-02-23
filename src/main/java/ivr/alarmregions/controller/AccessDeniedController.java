package ivr.alarmregions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessDeniedController {

    @RequestMapping({"/accessDenied", "/error"})
    public String accessDenied() {
        return "accessDenied";
    }
}
