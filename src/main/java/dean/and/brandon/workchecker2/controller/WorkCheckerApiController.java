package dean.and.brandon.workchecker2.controller;

import com.google.gson.JsonSyntaxException;
import com.opencsv.CSVWriter;
import dean.and.brandon.workchecker2.service.WorkCheckerService;
import dean.and.brandon.workchecker2.vo.ResponseInfo;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor
public class WorkCheckerApiController {

    private final WorkCheckerService workCheckerService;

    @PostMapping("/login")
    public ResponseInfo login(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "password") String password
    ) {
        try {
            String sessionId = workCheckerService.getLogin(username, password);
            log.info("Login Success : [{}]", username);
            HashMap<String, String> resultMap = new HashMap<>();
            resultMap.put("username", username);
            resultMap.put("sessionId", sessionId);

            ResponseInfo responseInfo = new ResponseInfo(0, "success");
            responseInfo.setData(resultMap);

            return responseInfo;
        } catch (Exception e) {
            log.error("Login Fail : [{}] - {}", username, e.getMessage());
            return new ResponseInfo(-1, e.getMessage());
        }
    }

    @RequestMapping(value = "/workList", method = RequestMethod.POST)
    public ResponseInfo workData(
            @RequestParam(value = "sessionId") String sessionId,
            @RequestParam(value = "username") String username,
            @RequestParam(name = "workingMonth", required = false) String workingMonth) {

        ResponseInfo responseInfo = new ResponseInfo();
        HashMap<String, Object> resultMap = new HashMap<>();
        responseInfo.setData(resultMap);
        try {

            if (username != null && sessionId != null && !"".equals(username) && !"".equals(sessionId)) {
                long totalWorkingTime = 0;
                long totalMinusTime = 0;
                long totalDiffTime = 0;

                if (workingMonth == null || "".equals(workingMonth)) {
                    workingMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
                }
                log.info("Retrieve Working Data : [{}] - [{}]", username, workingMonth);
                List<WorkingInfo> workingInfos = workCheckerService.getWorkData(sessionId, username, workingMonth);
                if (workingInfos != null) {
                    for (WorkingInfo wi : workingInfos) {
                        totalWorkingTime += wi.getDiffTime();
                        totalDiffTime += wi.getOriginTime();
                        totalMinusTime += wi.getMinusTime();
                        if (wi.getCarDate().equals(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))) && "".equals(wi.getEndDate())) {
                            resultMap.put("realTimeCalc", true);
                        }
                    }
                    resultMap.put("workingInfos", workingInfos);
                    resultMap.put("totalWorkingTime", totalWorkingTime);
                    resultMap.put("totalMinusTime", totalMinusTime);
                    resultMap.put("totalDiffTime", totalDiffTime);

                } else {
                    resultMap.put("errorMessage", "그룹웨어 서버오류 나중에 다시 조회하세요.");
                }
                resultMap.put("username", username);
                resultMap.put("workingMonth", workingMonth);
            } else {
                return new ResponseInfo(-1, "다시 로그인해주세요");
            }
        } catch (Exception e) {
            log.error("", e);
            if (e instanceof JsonSyntaxException) {
                return new ResponseInfo(-1, "다시 로그인해주세요");
            }
            resultMap.put("errorMessage", e.getMessage());
        }
        return responseInfo;
    }

    @RequestMapping(value = "/excelDown", method = RequestMethod.GET)
    public String workChecker(
            HttpServletResponse response,
            @CookieValue(value = "username", required = false) String username,
            @CookieValue(value = "sessionId", required = false) String sessionId,
            @RequestParam(name = "workingMonth") String workingMonth
    ) {
        try {
            if (username != null && sessionId != null && !"".equals(username) && !"".equals(sessionId)) {
                List<WorkingInfo> workingInfos = workCheckerService.getWorkData(sessionId, username, workingMonth);
                if (workingInfos == null) {
                    response.sendRedirect("/");
                    return "fail";
                }
                response.setHeader("Content-Disposition", "attachment;filename=" + workingMonth + "_" + username + "_working.csv");
                response.setContentType("text/csv");
                CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), "EUC-KR"));
                // header
                String[] headerRecord = {"근무일자", "출근시각", "퇴근시각", "정상근무시간", "하루근무시간", "정상근무시간-하루근무시간", "비고"};
                csvWriter.writeNext(headerRecord);
                // body
                long totalWorkingTime = 0;
                long totalMinusTime = 0L;
                long totalDiffTime = 0L;

                for (WorkingInfo wi : workingInfos) {
                    csvWriter.writeNext(new String[]{wi.getCarDate(), wi.getStartDate(), wi.getEndDate(), String.valueOf(wi.getDiffTime()), String.valueOf(wi.getOriginTime()), String.valueOf(wi.getMinusTime()), wi.getNote()});
                    totalWorkingTime += wi.getDiffTime();
                    totalDiffTime += wi.getOriginTime();
                    totalMinusTime += wi.getMinusTime();
                }
                // footer
                csvWriter.writeNext(new String[]{"", "", "합계", String.valueOf(totalWorkingTime), String.valueOf(totalDiffTime), String.valueOf(totalMinusTime)});
                csvWriter.close();
            }
        } catch (Exception e) {
            log.error("", e);
            try {
                response.sendRedirect("/");
                return "fail";
            } catch (IOException ignored) {
            }
        }

        return "success";
    }

}
