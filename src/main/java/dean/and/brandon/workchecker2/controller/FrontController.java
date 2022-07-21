package dean.and.brandon.workchecker2.controller;

import dean.and.brandon.workchecker2.service.WorkCheckerService;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
        return new ModelAndView("login");
    }

    @PostMapping("/")
    public ModelAndView login(
            HttpServletResponse response,
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {
        ModelAndView modelAndView = new ModelAndView();
        try {
            String sessionId = workCheckerService.getLogin(username, password);
            response.addCookie(new Cookie("username", username));
            response.addCookie(new Cookie("sessionId", sessionId));
            log.info("Login Success : [{}]", username);
            modelAndView.setViewName("loginSuccess");
        } catch (Exception e) {
            log.error("Login Fail : [{}] - {}", username, e.getMessage());
            modelAndView.setViewName("login");
            modelAndView.addObject("loginError", e.getMessage());
        }
        return modelAndView;
    }

    @RequestMapping(value = "/workList", method = RequestMethod.GET)
    public ModelAndView workData(
            @CookieValue(value = "sessionId", required = false) String sessionId,
            @CookieValue(value = "username", required = false) String username,
            @RequestParam(name = "workingMonth", required = false) String workingMonth) {

        ModelAndView modelAndView = new ModelAndView("workList");
        try {
            if (username != null && sessionId != null && !"".equals(username) && !"".equals(sessionId)) {

                long totalWorkingTime = 0;
                long totalMinusTime = 0;
                long totalDiffTime = 0;

                if (workingMonth == null || "".equals(workingMonth)) {
                    workingMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
                }

                List<WorkingInfo> workingInfos = workCheckerService.getWorkData(sessionId, username, workingMonth);
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
                modelAndView.addObject("username", username);
                modelAndView.addObject("workingMonth", workingMonth);
            } else {
                modelAndView.setViewName("login");
            }
        } catch (Exception e) {
            modelAndView.addObject("errorMessage", e.getMessage());
        }
        return modelAndView;
    }

}
