package company.challenge.dto;

import lombok.Data;

@Data
public class StatisticsDTO {
    long sum;
    double avg;
    long max;
    long min;
    long count;
}
