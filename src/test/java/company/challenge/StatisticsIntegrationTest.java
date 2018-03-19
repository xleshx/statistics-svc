package company.challenge;

import company.challenge.domain.Statistics;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private Clock clock;

    @Autowired
    private ConcurrentHashMap<Long, Statistics> cache;

    @Test
    public void createTransactionFailsOlderThan60() throws Exception {
        long olderBy61Sec = clock.instant().minusSeconds(61).toEpochMilli();
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                    .content("{\"amount\":1000,\"timestamp\":" + olderBy61Sec + "}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void createTransactionCheckStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + clock.instant().toEpochMilli() + "}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void createTransactionCheckByStatistics() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + clock.instant().toEpochMilli() + "}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", greaterThanOrEqualTo(1000.0)))
                .andExpect(jsonPath("$.avg", lessThanOrEqualTo(1000.0)))
                .andExpect(jsonPath("$.max", is(1000.0)))
                .andExpect(jsonPath("$.min", lessThanOrEqualTo(1000.0)))
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testCoupleTransactions() throws Exception {
        cache.clear();
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + clock.instant().minusMillis(100).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":2000,\"timestamp\":" + clock.instant().minusMillis(2000).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(3000.0)))
                .andExpect(jsonPath("$.avg", is(1500.0)))
                .andExpect(jsonPath("$.max", is(2000.0)))
                .andExpect(jsonPath("$.min", is(1000.0)))
                .andExpect(jsonPath("$.count", is(2)));
    }

}