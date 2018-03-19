package company.challenge.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class Statistics {
    double sum;
    double avg;
    double max;
    double min;
    long count;
}
