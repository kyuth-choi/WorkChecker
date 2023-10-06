package dean.and.brandon.workchecker2.vo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class ResponseInfo {
    public ResponseInfo() {
    }

    public ResponseInfo(int responseCode, String responseMsg) {
        this.responseCode = responseCode;
        this.responseMsg = responseMsg;
    }

    private int responseCode = 0;
    private String responseMsg = "success";
    private Object data;

}
