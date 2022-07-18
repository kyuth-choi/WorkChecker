package dean.and.brandon.workchecker2.controller;

import com.opencsv.CSVWriter;
import dean.and.brandon.workchecker2.service.WorkCheckerService;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WorkCheckerApiController {

    private final WorkCheckerService workCheckerService;

    @RequestMapping(value = "/excelDown", method = RequestMethod.GET)
    public String workChecker(
            HttpServletResponse response,
            @CookieValue(value = "sessionId", required = false) Cookie sessionInfo,
            @RequestParam(name = "workingMonth") String workingMonth
    ) {
        try {
            List<WorkingInfo> workingInfos = workCheckerService.getWorkData(sessionInfo.getValue(), workingMonth);
            if (workingInfos == null) {
                response.sendRedirect("/");
            }
            response.setHeader("Content-Disposition", "attachment;filename=" + workingMonth + "_working.csv");
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
                csvWriter.writeNext(new String[]{wi.getCarDate(), wi.getStartDate(), wi.getEndDate(), String.valueOf(wi.getDiffTime()), String.valueOf(wi.getOriginTime()), String.valueOf(wi.getMinusTime() + wi.getAddTime()), wi.getNote()});
                totalWorkingTime += wi.getDiffTime();
                totalDiffTime += wi.getOriginTime();
                totalMinusTime += wi.getMinusTime() + wi.getAddTime();
            }
            // footer
            csvWriter.writeNext(new String[]{"", "", "합계", String.valueOf(totalWorkingTime), String.valueOf(totalDiffTime), String.valueOf(totalMinusTime)});
            csvWriter.close();
        } catch (Exception e) {
            log.error("", e);
            try {
                response.sendRedirect("/");
            } catch (IOException ignored) {
            }
        }

        return "success";
    }

}
