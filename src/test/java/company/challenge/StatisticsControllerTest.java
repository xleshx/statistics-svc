package company.challenge;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Instant;
import java.util.Date;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
public class StatisticsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    TransactionRepository repo;

    @Test
    public void createTransactionCheckStatus() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + new Date().getTime() + "}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void createTransactionCheckByStatistics() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + new Date().getTime() + "}").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", greaterThanOrEqualTo(1000)))
                .andExpect(jsonPath("$.avg", lessThanOrEqualTo(1000.0)))
                .andExpect(jsonPath("$.max", is(1000)))
                .andExpect(jsonPath("$.min", lessThanOrEqualTo(1000)))
                .andExpect(jsonPath("$.count", greaterThanOrEqualTo(1)));
    }

    @Test
    public void testCoupleTransactions() throws Exception {
        Instant now = Instant.now();

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + now.minusMillis(100).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":2000,\"timestamp\":" + now.minusMillis(2000).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(3000)))
                .andExpect(jsonPath("$.avg", is(1500.0)))
                .andExpect(jsonPath("$.max", is(2000)))
                .andExpect(jsonPath("$.min", is(1000)))
                .andExpect(jsonPath("$.count", is(2)));
    }

    @Test
    public void testIgnoreEarliest() throws Exception {
        Instant now = Instant.now();
        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + now.minusMillis(100).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":2000,\"timestamp\":" + now.minusMillis(2000).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.post("/transactions")
                .content("{\"amount\":1000,\"timestamp\":" + now.minusMillis(10).toEpochMilli() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        mvc.perform(MockMvcRequestBuilders.get("/statistics")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sum", is(3000)))
                .andExpect(jsonPath("$.avg", is(1500.0)))
                .andExpect(jsonPath("$.max", is(2000)))
                .andExpect(jsonPath("$.min", is(1000)))
                .andExpect(jsonPath("$.count", is(2)));
    }

}