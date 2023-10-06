package dean.and.brandon.workchecker2.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin(origins="*")
@Controller
@Slf4j
public class WorkCheckerController implements ErrorController {

    @GetMapping("/error")
    public String redirectRoot() {
        return "index.html";
    }

}
