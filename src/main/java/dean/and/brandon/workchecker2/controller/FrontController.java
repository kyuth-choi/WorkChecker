package dean.and.brandon.workchecker2.controller;

import dean.and.brandon.workchecker2.service.WorkCheckerService;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FrontController {

    private final WorkCheckerService workCheckerService;

    @GetMapping("/")
    public ModelAndView loginForm(
            HttpServletResponse response
    ) {
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return new ModelAndView("login");
    }

    @PostMapping("/")
    public ModelAndView login(
            HttpServletResponse response,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {
        try {
            String sessionId = workCheckerService.getLogin(username, password);
            log.info("Login Success : {}", username);
            response.addCookie(new Cookie("sessionId", sessionId));
            response.addCookie(new Cookie("username", username));
            ModelAndView modelAndView = new ModelAndView("loginSuccess");
            modelAndView.addObject("username", username);
            return modelAndView;
        } catch (Exception e) {
            log.info("Login Fail : {}", username);
            ModelAndView modelAndView = new ModelAndView("login");
            modelAndView.addObject("loginError", e.getMessage());
            return modelAndView;
        }
    }

    @RequestMapping(value = "/workList", method = RequestMethod.GET)
    public ModelAndView workData(
            @CookieValue(value = "sessionId", required = false) Cookie sessionInfo,
            @RequestParam(name = "workingMonth", required = false) String workingMonth) {

        if (sessionInfo == null || "".equals(sessionInfo.getValue())) {
            return new ModelAndView("login");
        } else {
            String sessionId = sessionInfo.getValue();
            long totalWorkingTime = 0;
            long totalMinusTime = 0;
            long totalDiffTime = 0;

            ModelAndView modelAndView = new ModelAndView("workList");

            if (workingMonth == null || "".equals(workingMonth)) {
                workingMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            }

            List<WorkingInfo> workingInfos = workCheckerService.getWorkData(sessionId, workingMonth);
            if (workingInfos != null) {
                for (WorkingInfo wi : workingInfos) {
                    totalWorkingTime += wi.getDiffTime();
                    totalDiffTime += wi.getOriginTime();
                    totalMinusTime += wi.getMinusTime() + wi.getAddTime();
                }
                modelAndView.addObject("workingInfos", workingInfos);
                modelAndView.addObject("totalWorkingTime", totalWorkingTime);
                modelAndView.addObject("totalMinusTime", totalMinusTime);
                modelAndView.addObject("totalDiffTime", totalDiffTime);
            } else {
                modelAndView.addObject("errorMessage", "그룹웨어 서버오류 나중에 다시 조회하세요.");
            }
            modelAndView.addObject("workingMonth", workingMonth);

            return modelAndView;
        }
    }

}
