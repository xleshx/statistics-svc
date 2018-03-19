package company.challenge;

import company.challenge.domain.Statistics;
import company.challenge.dto.StatisticsDTO;
import company.challenge.dto.TransactionDTO;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.time.Instant.ofEpochMilli;
import static java.time.Instant.parse;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.stream.LongStream.rangeClosed;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StatisticsServiceTest {

    private StatisticsService service;

    private ConcurrentHashMap<Long, Statistics> cache;

    private Clock clock;

    private double DELTA = 0.01;

    @Before
    public void setup() {
        clock = Clock.fixed(parse("2018-03-18T12:00:00Z"), UTC);
        cache = new ConcurrentHashMap<>(60);
        service = new StatisticsService(clock, cache);
        ReflectionTestUtils.setField(service, "statisticsWindowSize", 60);
    }

    @Test
    public void testCacheUpdate() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(1000);
        long oneSecOld = clock.instant().minusSeconds(1).toEpochMilli();
        transactionDTO.setTimestamp(oneSecOld);

        // when
        service.save(transactionDTO);

        // then
        long cacheKey = ofEpochMilli(oneSecOld).truncatedTo(SECONDS).toEpochMilli();
        assertTrue(cache.containsKey(cacheKey));

        Statistics statistics = cache.get(cacheKey);
        assertEquals(transactionDTO.getAmount(), statistics.getAvg(), DELTA);
        assertEquals(transactionDTO.getAmount(), statistics.getMin(), DELTA);
        assertEquals(transactionDTO.getAmount(), statistics.getMax(), DELTA);
        assertEquals(1, statistics.getCount());
    }

    @Test
    public void testCacheUpdateSameBucket() {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(1000);
        long oneSecOld = clock.instant().minusSeconds(1).toEpochMilli();
        transactionDTO.setTimestamp(oneSecOld);

        TransactionDTO transactionDTO1 = getTransactionDTO(
                clock.instant().minusMillis(999).toEpochMilli(), 1000);

        // when
        service.save(transactionDTO);
        service.save(transactionDTO1);

        // then
        long cacheKey = ofEpochMilli(oneSecOld).truncatedTo(SECONDS).toEpochMilli();
        assertTrue(cache.containsKey(cacheKey));

        Statistics statistics = cache.get(cacheKey);
        assertEquals(transactionDTO.getAmount() + transactionDTO1.getAmount(),
                statistics.getSum(), DELTA);
        assertEquals(transactionDTO.getAmount(), statistics.getAvg(), DELTA);
        assertEquals(transactionDTO.getAmount(), statistics.getMin(), DELTA);
        assertEquals(transactionDTO.getAmount(), statistics.getMax(), DELTA);
        assertEquals(2, statistics.getCount());
    }

    @Test
    public void testCacheUpdateAll60Buckets() {
        List<TransactionDTO> transactions = rangeClosed(1, 60)
                .map(v -> clock.instant().toEpochMilli() - v * 1000)
                .mapToObj(ts -> getTransactionDTO(ts, 1000))
                .collect(Collectors.toList());

        // when
        transactions.forEach(t -> service.save(t));

        // then
        StatisticsDTO statistics = service.getStatistics();

        assertEquals(60, cache.size());
        assertEquals(60 * 1000, statistics.getSum(), DELTA);
        assertEquals(1000, statistics.getAvg(), DELTA);
        assertEquals(1000, statistics.getMin(), DELTA);
        assertEquals(1000, statistics.getMax(), DELTA);
        assertEquals(60, statistics.getCount());
    }

    @Test
    public void testCacheUpdate10BucketsWithDifferentAmount() {
        List<TransactionDTO> transactions = rangeClosed(1, 10)
                .map(v -> clock.instant().toEpochMilli() - v * 500)
                .mapToObj(ts -> getTransactionDTO(ts, 500))
                .collect(Collectors.toList());

        TransactionDTO transactionMin = getTransactionDTO(
                clock.instant().minusMillis(999).toEpochMilli(), 20);

        TransactionDTO transactionMax = getTransactionDTO(
                clock.instant().minusMillis(998).toEpochMilli(), 2000);

        // when
        transactions.forEach(t -> service.save(t));
        service.save(transactionMin);
        service.save(transactionMax);

        // then
        StatisticsDTO statistics = service.getStatistics();

        assertEquals(5, cache.size());
        assertEquals(7020.0, statistics.getSum(), DELTA);
        assertEquals(585.0, statistics.getAvg(), DELTA);
        assertEquals(20.0, statistics.getMin(), DELTA);
        assertEquals(2000.0, statistics.getMax(), DELTA);
        assertEquals(12, statistics.getCount());
    }

    @Test
    public void testCacheCleanup() {
        cache.put(clock.instant().minusSeconds(90).toEpochMilli(), new Statistics());
        cache.put(clock.instant().minusSeconds(62).toEpochMilli(), new Statistics());
        cache.put(clock.instant().minusSeconds(61).toEpochMilli(), new Statistics());
        TransactionDTO transaction = getTransactionDTO(
                clock.instant().minusMillis(2000).toEpochMilli(), 20);

        assertEquals(3, cache.size());

        service.save(transaction);

        assertEquals(1, cache.size());
        Statistics statistics = cache.get(TimeUtil.alignMillisToSeconds(transaction.getTimestamp()));
        assertEquals(20.0, statistics.getSum(), DELTA);
        assertEquals(20.0, statistics.getAvg(), DELTA);
        assertEquals(20.0, statistics.getMin(), DELTA);
        assertEquals(20.0, statistics.getMax(), DELTA);
        assertEquals(1, statistics.getCount());
    }


    private TransactionDTO getTransactionDTO(long oneSecOld, double amount) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(amount);
        transactionDTO.setTimestamp(oneSecOld);
        return transactionDTO;
    }


}