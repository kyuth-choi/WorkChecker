package dean.and.brandon.workchecker2.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dean.and.brandon.workchecker2.vo.WorkingInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class WorkCheckerService {

    public String getLogin(String username, String password) throws Exception {
        String sessionId = getJSessionId(username);
        if (!sessionId.equals("")) {
            String returnData1 = setLogin(sessionId, username, password);
            if (!"success".equals(returnData1)) {
                log.info("[ERROR] 로그인 실패 : {}", returnData1);
                throw new Exception("[ERROR] 로그인 실패 : " + returnData1);
            }
        }
        return sessionId;
    }

    public List<WorkingInfo> getWorkData(String sessionId, String workingMonth) {
        String returnData2 = getWorkTime(sessionId, workingMonth);
        if ("".equals(returnData2)) {
            return null;
        }
        JsonObject convertedObject = new Gson().fromJson(returnData2, JsonObject.class);

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
                    annualMap.put(carDate, carType);
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
                if (carEtime != 0) {
                    endDate = Instant.ofEpochMilli(carEtime).atZone(ZoneId.systemDefault()).toLocalDateTime();
                    if (annualMap.containsKey(carDate) && annualMap.get(carDate).equals("오후반차")) {
                        if (endDate.toLocalTime().isAfter(LocalTime.of(13, 0, 0))) {
                            endDate = LocalDateTime.of(endDate.toLocalDate(), LocalTime.of(13, 0, 0));
                        }
                    }
                }
                long diffTime = 0;
                long addTime = 0L;
                if (startDate != null && endDate != null) {
                    if (annualMap.containsKey(carDate)) {
                        if (annualMap.get(carDate).contains("반차")) {
                            diffTime = ChronoUnit.MINUTES.between(startDate, endDate);
                            addTime = 240L;
                        } else { //쿠폰 사용
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
                            if (annualMap.get(carDate).equals("2시간사용")) {
                                addTime = 120L;
                            } else if (annualMap.get(carDate).equals("1시간사용")) {
                                addTime = 60L;
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
                wi.setAddTime(addTime);
                wi.setDiffTime(diffTime);

                wi.setOriginTime(480 - addTime);
                if (carEtime == 0 && LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")).equals(carDate)) {
                    wi.setMinusTime(0L);
                } else {
                    wi.setMinusTime(diffTime - 480);
                }

                String note = wi.getAnnualType() == null ? "" : wi.getAnnualType() + (wi.getAddTime() > 0L ? " (-" + wi.getAddTime() + ")" : "");
                wi.setNote(note);
                workingInfos.add(wi);
            }
        }

        //날짜순 sorting
        workingInfos.sort((o1, o2) -> {
            long comp1 = Long.parseLong(o1.getCarDate());
            long comp2 = Long.parseLong(o2.getCarDate());
            if (comp1 < comp2) {
                return -1;
            } else {
                return 1;
            }
        });

        return workingInfos;
    }

    public static String getJSessionId(String username) {
        String sessionId = "";
        try {
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
            String endpointUrl = "http://gwintra.lunasoft.co.kr/loginForm.do";
            if (username.toUpperCase().startsWith("G")) {
                endpointUrl = "http://gwintra.cellook.kr/loginForm.do";
            }
            URL url = new URL(endpointUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.getContent();
            List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
            for (HttpCookie cookie : cookies) {
                String cookieValue = cookie.toString();
                if (cookieValue.contains("JSESSIONID")) {
                    sessionId = cookieValue;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return sessionId;
    }

    public static String setLogin(String sessionId, String username, String password) {
        // return data
        String returnData = "";
        // 아이디/비밀번호
        String paramData = "j_password=" + password + "&j_gwIdCheck=&j_username=" + username + "&password=" + password;
        // http 통신 요청 후 응답 받은 데이터를 담기 위한 변수
        BufferedReader br = null;
        try {
            String endpointUrl = "http://gwintra.lunasoft.co.kr/login.do";
            if (username.toUpperCase().startsWith("G")) {
                endpointUrl = "http://gwintra.cellook.kr/login.do";
            }
            //URL 설정
            URL url = new URL(endpointUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // http 요청 응답 코드 확인 실시
            // post method
            con.setRequestMethod("POST");
            // set timeout
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);
            // set header
            con.addRequestProperty("Connection", "keep-alive");
            con.addRequestProperty("Cache-Control", "max-age=0");
            con.addRequestProperty("Upgrade-Insecure-Requests", "1");
            con.addRequestProperty("Origin", "http://gwintra.lunasoft.co.kr");
            con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            con.addRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            con.addRequestProperty("Referer", "http://gwintra.lunasoft.co.kr/loginForm.do");
            con.addRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            con.addRequestProperty("Cookie", "LOG_LEVEL=1; IKEP_LOGIN_SAVEID=; " + sessionId);
            // use OutputStream
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte[] request_data = paramData.getBytes(StandardCharsets.UTF_8);
                os.write(request_data);
            } catch (Exception e) {
                log.error("", e);
            }

            String responseCode = String.valueOf(con.getResponseCode());
            log.info("[setLogin][CODE:" + responseCode + "]");
            if (con.getResponseCode() != 200) {
                returnData = "그룹웨어 응답 오류";
            } else {
                // http 요청 후 응답 받은 데이터를 버퍼에 쌓는다
                br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String responseData;
                while ((responseData = br.readLine()) != null) {
                    sb.append(responseData); //StringBuffer에 응답받은 데이터 순차적으로 저장 실시
                }
                // 메소드 호출 완료 시 반환하는 변수에 버퍼 데이터 삽입 실시

                if (sb.toString().contains("로그아웃")) {
                    returnData = "success";
                } else {
                    returnData = "아이디, 패스워드를 확인하세요.";
                }
            }
        } catch (Exception e) {
            log.error("", e);
            if (e instanceof SocketTimeoutException) {
                returnData = "그룹웨어 응답 지연 \n(08:30~ 09:30 트래픽 과다로 응답지연 가능성 높습니다.)";
            } else {
                returnData = "그룹웨어 서버 오류";
            }
        } finally {
            // http 요청 및 응답 완료 후 BufferedReader를 닫아줍니다
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return returnData;
    }

    public static String getWorkTime(String sessionId, String workingMonth) {
        // return data
        String returnData = "";
        // 조회 할 월
        String paramData = "sDate=" + workingMonth;
        // http 통신 요청 후 응답 받은 데이터를 담기 위한 변수
        String responseData = "";
        BufferedReader br = null;
        StringBuffer sb = null;
        try {
            String endpointUrl = "http://gwintra.lunasoft.co.kr/ehr/attend/userAttendCalendar.do";
            //URL 설정
            URL url = new URL(endpointUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            // post method
            con.setRequestMethod("POST");
            // set timeout
            con.setConnectTimeout(5000);
            con.setReadTimeout(10000);
            // set header
            con.addRequestProperty("Connection", "keep-alive");
            con.addRequestProperty("Accept", "*/*");
            con.addRequestProperty("X-Requested-With", "XMLHttpRequest");
            con.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");
            con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            con.addRequestProperty("Origin", "http://gwintra.lunasoft.co.kr");
            con.addRequestProperty("Referer", "http://gwintra.lunasoft.co.kr/ehr/attend/userAttendCalendar.do");
            con.addRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            con.addRequestProperty("Cookie", "LOG_LEVEL=1; IKEP_LOGIN_SAVEID=; " + sessionId);
            // use OutputStream
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                byte request_data[] = paramData.getBytes("UTF-8");
                os.write(request_data);
            } catch (Exception e) {
                log.error("", e);
            }
            // http 요청 후 응답 받은 데이터를 버퍼에 쌓는다
            br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            sb = new StringBuffer();
            while ((responseData = br.readLine()) != null) {
                sb.append(responseData); //StringBuffer에 응답받은 데이터 순차적으로 저장 실시
            }
            // 메소드 호출 완료 시 반환하는 변수에 버퍼 데이터 삽입 실시
            returnData = sb.toString();
            // http 요청 응답 코드 확인 실시
            String responseCode = String.valueOf(con.getResponseCode());
            log.info("[getWorkTime][CODE:" + responseCode + "]");
        } catch (Exception e) {
            log.error("", e);
        } finally {
            //http 요청 및 응답 완료 후 BufferedReader를 닫아줍니다
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return returnData;
    }
}
