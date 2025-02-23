package ivr.alarmregions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultController extends AbstractController {

    @RequestMapping({"/", "/index"})
    public String index() {
        return "index";
    }
}
