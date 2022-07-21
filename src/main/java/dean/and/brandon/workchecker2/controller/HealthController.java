package dean.and.brandon.workchecker2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health")
@RestController
public class HealthController {

    @GetMapping("/checker")
    public String health (){
        return "success";
    }
}
