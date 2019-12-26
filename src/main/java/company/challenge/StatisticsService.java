package company.challenge;

import company.challenge.domain.Statistics;
import company.challenge.domain.Transaction;
import company.challenge.dto.StatisticsDTO;
import company.challenge.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.Instant.ofEpochMilli;
import static java.util.Optional.ofNullable;

@Slf4j
@Service
class StatisticsService {

    @Value("${statistics.windowSize.seconds:60}")
    private int statisticsWindowSize = 60;

    @Value("${statistics.parallelism.threshold:20}")
    private int parallelismThreshold = 20;

    private final Clock clock;

    private final ConcurrentHashMap<Long, Statistics> cache;

    @Autowired
    public StatisticsService(Clock clock, ConcurrentHashMap<Long, Statistics> cache) {
        this.clock = clock;
        this.cache = cache;
    }

    public void save(TransactionDTO transactionDTO) {
        Transaction transaction = new Transaction();
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(transactionDTO.getTimestamp());
        updateCacheData(transaction);
    }

    public StatisticsDTO getStatistics() {
        long windowStartTimestamp = clock.instant()
            .minusSeconds(statisticsWindowSize).toEpochMilli();

        Statistics statistics = ofNullable(
            cache.reduce(parallelismThreshold,
                (k, v) -> filter(k, v, windowStartTimestamp),
                this::calculateStatistics))
            .orElse(new Statistics());

        return toDto(statistics);
    }

    private StatisticsDTO toDto(Statistics statistics) {
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        statisticsDTO.setSum(statistics.getSum());
        statisticsDTO.setAvg(statistics.getAvg());
        statisticsDTO.setMax(statistics.getMax());
        statisticsDTO.setMin(statistics.getMin());
        statisticsDTO.setCount(statistics.getCount());
        return statisticsDTO;
    }

    private Statistics filter(long key, Statistics statistics, long windowStartTimestamp) {
        log.debug("key:window {} : {}: {}", key, windowStartTimestamp, key > windowStartTimestamp);
        return key >= windowStartTimestamp ? statistics : new Statistics();
    }

    private Statistics calculateStatistics(Statistics total, Statistics current) {
        log.debug("current: {}, total: {}", current, total);
        Statistics result = new Statistics();
        result.setSum(total.getSum() + current.getSum());
        result.setCount(total.getCount() + current.getCount());
        result.setMax(Math.max(total.getMax(), current.getMax()));
        result.setMin(Math.min(total.getMin(), current.getMin()));
        double avg = result.getCount() == 0 ? 0 : result.getSum() / result.getCount();
        result.setAvg(avg);
        log.debug("result: {}", result);
        return result;
    }

    private void updateCacheData(Transaction transaction) {
        long timeKey = TimeUtil.alignMillisToSeconds(transaction.getTimestamp());
        log.debug("Updating cache for the key: {}", timeKey);

        cache.compute(timeKey, (k, v) -> updateCacheWithTransaction(v, transaction)); // atomic

        cleanCache();
        printCache(transaction.getTimestamp());
    }

    private Statistics updateCacheWithTransaction(Statistics current, Transaction transaction) {
        current = ofNullable(current).orElseGet(() -> getInitialStatistics(transaction));

        Statistics result = new Statistics();
        result.setSum(current.getSum() + transaction.getAmount());
        result.setCount(current.getCount() + 1); // count this transaction
        result.setMax(Math.max(current.getMax(), transaction.getAmount()));
        result.setMin(Math.min(current.getMin(), transaction.getAmount()));
        result.setAvg(result.getSum() / result.getCount());
        log.debug("New statistic based on transaction {} is {}", transaction, result);
        return result;
    }

    private Statistics getInitialStatistics(Transaction transaction) {
        Statistics result = new Statistics();
        result.setMin(transaction.getAmount());
        result.setMax(transaction.getAmount());
        return result;
    }

    // can be done asynchronously
    private void cleanCache() {
        long windowStartTimestamp = clock.instant()
            .minusSeconds(statisticsWindowSize).toEpochMilli();

        cache.forEachKey(parallelismThreshold,
            k -> {
                if (k < windowStartTimestamp) {
                    cache.remove(k);
                }
            }
        );
    }

    private void printCache(long timestamp) {
        log.debug("time: {}", ofEpochMilli(timestamp).toString());
        if (log.isDebugEnabled()) {
            cache.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> log.debug("{} -> {}", e.getKey(), e.getValue()));
        }
    }

}
