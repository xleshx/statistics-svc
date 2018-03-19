package company.challenge;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

import static java.time.Instant.parse;
import static java.time.ZoneOffset.UTC;

@Configuration
public class FixedClockConfiguration {

    @Bean
    public Clock getSystemClock() {
        return Clock.fixed(parse("2018-03-18T12:00:00Z"), UTC);
    }
}
