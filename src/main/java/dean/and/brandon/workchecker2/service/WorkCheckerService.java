package dean.and.brandon.workchecker2.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkCheckerService {

    private final RestTemplate restTemplate;

    public String getLogin(String username, String password) throws Exception {
        String sessionId;
        try {
            sessionId = getJSessionId(username);
            if (!sessionId.equals("")) {
                String returnData = setLogin(sessionId, username, password);
                if (!"success".equals(returnData)) {
                    throw new Exception(returnData);
                }
            } else {
                throw new Exception("그룹웨어 서버 오류");
            }
        } catch (ResourceAccessException e) {
            log.error("그룹웨어 서버 오류", e);
            if (e.getRootCause() instanceof SocketTimeoutException) {
                throw new Exception("그룹웨어 응답 지연 <br/>(08:30~ 10:30 트래픽 과다로 응답지연있습니다.)");
            }
            throw new Exception("그룹웨어 서버 오류");
        }

        return sessionId;
    }

    public List<WorkingInfo> getWorkData(String sessionId, String username, String workingMonth) throws Exception {

        try {
            String returnData = getWorkTime(sessionId, username, workingMonth);

            JsonObject convertedObject = new Gson().fromJson(returnData, JsonObject.class);

            JsonArray dataArray = convertedObject.getAsJsonArray("data");
            JsonArray data2Array = convertedObject.getAsJsonArray("data2");

            HashMap<String, String> annualMap = new HashMap<>();
            List<WorkingInfo> workingInfos = new ArrayList<>();

            for (int i = 0; i < data2Array.size(); ++i) {
                JsonObject jsonObject = data2Array.get(i).getAsJsonObject();
                if (jsonObject != null) {
                    String carType = jsonObject.get("carType").isJsonNull() ? "" : jsonObject.get("carType").getAsString();
                    String carDate = jsonObject.get("carDate").isJsonNull() ? "" : jsonObject.get("carDate").getAsString().replace("-", "");

                    if (carType.equals("연차")) {
                        WorkingInfo wi = new WorkingInfo();
                        wi.setCarDate(carDate);
                        wi.setStartDate(carType);
                        wi.setEndDate(carType);
                        wi.setDiffTime(0);
                        wi.setOriginTime(0);
                        wi.setMinusTime(0L);
                        workingInfos.add(wi);
                    } else {
                        //복합 사용 처리
                        if (annualMap.containsKey(carDate)) {
                            annualMap.put(carDate, annualMap.get(carDate) + " + " + carType);
                        } else {
                            annualMap.put(carDate, carType);
                        }
                    }
                }
            }

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd (E) HH:mm:ss", Locale.KOREA);

            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject jsonObject = dataArray.get(i).getAsJsonObject();
                if (jsonObject != null) {
                    String carDate = jsonObject.get("carDate").isJsonNull() ? "" : jsonObject.get("carDate").getAsString();
                    long carStime = jsonObject.get("carStime").isJsonNull() ? 0 : jsonObject.get("carStime").getAsLong();
                    long carEtime = jsonObject.get("carEtime").isJsonNull() ? 0 : jsonObject.get("carEtime").getAsLong();
                    LocalDateTime startDate = null;
                    LocalDateTime endDate = null;
                    // 출근 시간 -> 8시 이전 출근시 8시 고정, 오전 반차 후 14시 이전 출근시 14시 출근 고정
                    if (carStime != 0) {
                        startDate = Instant.ofEpochMilli(carStime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        if (startDate.toLocalTime().isBefore(LocalTime.of(8, 0, 0))) {
                            startDate = LocalDateTime.of(startDate.toLocalDate(), LocalTime.of(8, 0, 0));
                        }
                        if (annualMap.containsKey(carDate) && annualMap.get(carDate).equals("오전반차")) {
                            if (startDate.toLocalTime().isBefore(LocalTime.of(14, 0, 0))) {
                                startDate = LocalDateTime.of(startDate.toLocalDate(), LocalTime.of(14, 0, 0));
                            }
                        }
                    }
                    // 퇴근시간 -> 오후반차 사용시 13시 퇴근 고정
                    if (carEtime != 0) {
                        endDate = Instant.ofEpochMilli(carEtime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        if (annualMap.containsKey(carDate) && annualMap.get(carDate).equals("오후반차")) {
                            if (endDate.toLocalTime().isAfter(LocalTime.of(13, 0, 0))) {
                                endDate = LocalDateTime.of(endDate.toLocalDate(), LocalTime.of(13, 0, 0));
                            }
                        }
                    }
                    long diffTime = 0;

                    //비고 내용 추가 및 하루 근무 시간 빼기
                    long originTime = 480;
                    String note = "";
                    if (annualMap.containsKey(carDate)) {
                        if (annualMap.get(carDate).contains("반차")) {
                            originTime -= 240L;
                        }
                        if (annualMap.get(carDate).contains("2시간사용")) {
                            originTime -= 120;
                        }
                        if (annualMap.get(carDate).contains("1시간사용")) {
                            originTime -= 60L;
                        }
                        note = annualMap.get(carDate) + " (-" + (480 - originTime) + ")";
                    }

                    // 근무 시간 계산시 점심시간 전 퇴근 및 점심시간 후 출근하는 경우 점심시간 빼기 제외 및 근무시간 계산
                    if (startDate != null && endDate != null) {
                        if (annualMap.containsKey(carDate)) {
                            if (annualMap.get(carDate).contains("반차")) {
                                diffTime = ChronoUnit.MINUTES.between(startDate, endDate);
                            } else {
                                //점심시간 60분
                                if (annualMap.get(carDate).equals("2시간사용") && startDate.toLocalTime().isAfter(LocalTime.of(12, 59, 59))) {
                                    startDate = LocalDateTime.of(startDate.toLocalDate(), LocalTime.of(14, 0, 0));
                                    diffTime = ChronoUnit.MINUTES.between(startDate, endDate);
                                } else if (endDate.toLocalTime().isBefore(LocalTime.of(14, 0, 0))) {
                                    endDate = LocalDateTime.of(endDate.toLocalDate(), LocalTime.of(13, 0, 0));
                                    diffTime = ChronoUnit.MINUTES.between(startDate, endDate);
                                } else {
                                    diffTime = ChronoUnit.MINUTES.between(startDate, endDate) - 60;
                                }
                            }
                        } else {
                            diffTime = ChronoUnit.MINUTES.between(startDate, endDate) - 60;
                        }
                    }

                    WorkingInfo wi = new WorkingInfo();
                    wi.setCarDate(carDate);
                    wi.setStartDate(startDate != null ? startDate.format(dateTimeFormatter) : "");
                    wi.setEndDate(endDate != null ? endDate.format(dateTimeFormatter) : "");
                    wi.setAnnualType(annualMap.get(carDate));
                    wi.setDiffTime(diffTime);
                    wi.setOriginTime(originTime);

                    //당일 퇴근 전인 경우 정상근무시간-하루근무시간 = 0 으로 세팅
                    if (carEtime == 0 && LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).equals(carDate)) {
                        wi.setMinusTime(0L);
                    } else {
                        wi.setMinusTime(diffTime - originTime);
                    }

                    wi.setNote(note);
                    workingInfos.add(wi);
                }
            }

            //날짜순 sorting

            workingInfos.sort(Comparator.comparing(WorkingInfo::getCarDate));
            return workingInfos;
        } catch (ResourceAccessException e) {
            log.error("", e);
            if (e.getRootCause() instanceof SocketTimeoutException) {
                throw new Exception("그룹웨어 응답 지연 \n(08:30~ 10:30 트래픽 과다로 응답지연 가능성 높습니다.)");
            }
            throw new Exception("그룹웨어 서버 오류");
        }
    }

    public String getJSessionId(String username) {
        String sessionId = "";
        String endpointUrl = "http://gwintra.lunasoft.co.kr/loginForm.do";
        if (username.toUpperCase().startsWith("G") || username.toLowerCase().contains("cellook.kr")) {
            endpointUrl = "http://gwintra.cellook.kr/loginForm.do";
        }

        ResponseEntity<String> response = restTemplate.getForEntity(endpointUrl, String.class);

        List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (cookies != null) {
            for (String cookie : cookies) {
                for (String cookieValue : cookie.replace(";", "").split(" ")) {
                    if (cookieValue.contains("JSESSIONID")) {
                        sessionId = cookieValue;
                        break;
                    }
                }
            }
        }
        return sessionId;
    }

    public String setLogin(String sessionId, String username, String password) {

        String result;
        // 아이디/비밀번호
        String paramData = "j_password=" + password + "&j_gwIdCheck=&j_username=" + username + "&password=" + password;
        // http 통신 요청 후 응답 받은 데이터를 담기 위한 변수
        String endpointUrl = "http://gwintra.lunasoft.co.kr/login.do";
        if (username.toUpperCase().startsWith("G") || username.toLowerCase().contains("cellook.kr")) {  //그룹웨어 계정이 메일인 경우가 있음.
            endpointUrl = "http://gwintra.cellook.kr/login.do";
        }

        RequestEntity<String> requestEntity = RequestEntity
                .post(URI.create(endpointUrl)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("Cookie", sessionId)
                .body(paramData);

        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        String location = response.getHeaders().getLocation() == null ? "" : response.getHeaders().getLocation().toString();
        if (location.contains("portal/main/portalMain.do")) {
            result = "success";
        } else {
            result = "아이디, 패스워드를 확인하세요.";
        }
        //URL 설정
        return result;
    }

    public String getWorkTime(String sessionId, String username, String workingMonth) {
        // return data
        // 조회 할 월
        String endpointUrl = "http://gwintra.lunasoft.co.kr/ehr/attend/userAttendCalendar.do?sDate=" + workingMonth;
        if (username.toUpperCase().startsWith("G") || username.toLowerCase().contains("cellook.kr")) {
            endpointUrl = "http://gwintra.cellook.kr/ehr/attend/userAttendCalendar.do?sDate=" + workingMonth;
        }

        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(endpointUrl)).header("Cookie", sessionId).build();
        ResponseEntity<String> result = restTemplate.exchange(requestEntity, String.class);

        return result.getBody();
    }

}
