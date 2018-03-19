package company.challenge;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeUtil {
    public static long alignMillisToSeconds(long ts){
        return Instant.ofEpochMilli(ts).truncatedTo(ChronoUnit.SECONDS).toEpochMilli();
    }
    
}
