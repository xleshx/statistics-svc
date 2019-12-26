package company.challenge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import company.challenge.domain.Statistics;
import company.challenge.dto.TransactionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.LongStream.rangeClosed;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Clock clock;

    @Autowired
    private ConcurrentHashMap<Long, Statistics> cache;

    @BeforeEach
    public void clearCache(){
        cache.clear();
    }

    @Test
    public void returnEmptyOnFirstStatisticsCall() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/statistics")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum", is(0.0)))
            .andExpect(jsonPath("$.avg", is(0.0)))
            .andExpect(jsonPath("$.max", is(0.0)))
            .andExpect(jsonPath("$.min", is(0.0)))
            .andExpect(jsonPath("$.count", is(0)));
    }

    @Test
    public void createTransactionFailsOlderThan60() throws Exception {
        long olderBy61Sec = clock.instant().minusSeconds(61).toEpochMilli();
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
            .content("{\"amount\":1000,\"timestamp\":" + olderBy61Sec + "}").contentType(APPLICATION_JSON))
            .andExpect(status().isNoContent());
    }

    @Test
    public void createTransactionCheckStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
            .content("{\"amount\":1000,\"timestamp\":" + clock.instant().toEpochMilli() + "}")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    public void createTransactionCheckByStatistics() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
            .content("{\"amount\":1000,\"timestamp\":" + clock.instant().toEpochMilli() + "}")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum", greaterThanOrEqualTo(1000.0)))
            .andExpect(jsonPath("$.avg", lessThanOrEqualTo(1000.0)))
            .andExpect(jsonPath("$.max", is(1000.0)))
            .andExpect(jsonPath("$.min", lessThanOrEqualTo(1000.0)))
            .andExpect(jsonPath("$.count", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testCoupleTransactions() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
            .content("{\"amount\":1000,\"timestamp\":" + clock.instant().minusMillis(100).toEpochMilli() + "}")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
            .content("{\"amount\":2000,\"timestamp\":" + clock.instant().minusMillis(2000).toEpochMilli() + "}")
            .contentType(APPLICATION_JSON))
            .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum", is(3000.0)))
            .andExpect(jsonPath("$.avg", is(1500.0)))
            .andExpect(jsonPath("$.max", is(2000.0)))
            .andExpect(jsonPath("$.min", is(1000.0)))
            .andExpect(jsonPath("$.count", is(2)));
    }

    @Test
    public void loadTest1000() throws Exception {
        List<TransactionDTO> transactions = rangeClosed(1, 1000)
            .map(v -> clock.instant().toEpochMilli() - v)
            .mapToObj(ts -> getTransactionDTO(ts, 1))
            .collect(Collectors.toList());

        List<String> jsonList = transactions.stream()
            .map(this::toJson)
            .collect(Collectors.toList());

        jsonList.parallelStream().forEach(
            json -> {
                try {
                    mvc.perform(MockMvcRequestBuilders.post("/transactions")
                        .content(json)
                        .contentType(APPLICATION_JSON))
                        .andExpect(status().isCreated());

                    mvc.perform(MockMvcRequestBuilders.get("/statistics")
                        .accept(APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.avg", is(1.0)))
                        .andExpect(jsonPath("$.max", is(1.0)))
                        .andExpect(jsonPath("$.min", is(1.0)));
                } catch (Exception ignored) {
                    // ignore for tests
                }
            }
        );

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
            .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sum", is(1000.0)))
            .andExpect(jsonPath("$.avg", is(1.0)))
            .andExpect(jsonPath("$.max", is(1.0)))
            .andExpect(jsonPath("$.min", is(1.0)))
            .andExpect(jsonPath("$.count", is(1000)));
    }

    private TransactionDTO getTransactionDTO(long oneSecOld, double amount) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(amount);
        transactionDTO.setTimestamp(oneSecOld);
        return transactionDTO;
    }

    private String toJson(TransactionDTO transactionDTO) {
        try {
            return objectMapper.writeValueAsString(transactionDTO);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map pojo to json during test"); // ignore for simplicity reasons
        }
    }


}
