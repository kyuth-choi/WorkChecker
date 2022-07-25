package dean.and.brandon.workchecker2.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WorkingInfo {

    String carDiff;
    String carDate;
    String startDate;
    String endDate;
    String annualType;
    String note;

    long diffTime;
    long originTime;
    long minusTime;

}
