package company.challenge;

import company.challenge.domain.Statistics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class App {
    @Value("${statistics.windowSize.seconds}")
    private int statisticsWindowSize;

    @Bean
    public ConcurrentHashMap<Long, Statistics> getCache() {
        return new ConcurrentHashMap<>(statisticsWindowSize);
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
