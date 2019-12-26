package company.challenge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ClockConfiguration {

    @Bean("clock")
    public Clock getSystemClock() {
        return Clock.systemUTC();
    }
}
