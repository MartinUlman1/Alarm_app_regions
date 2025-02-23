
package ivr.alarmregions.controller;

import alapp.service.LogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class LogController {

    @Autowired
    private LogService logService;

    @GetMapping("/log/view")
    public void viewLog(@RequestParam String page, HttpServletResponse response) throws IOException {
        String logContent = logService.getLogContent(page);
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().write(logContent);
    }
}