package company.challenge;

import company.challenge.domain.Statistics;
import company.challenge.domain.Transaction;
import company.challenge.dto.StatisticsDTO;
import company.challenge.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
class TransactionalService {

    // last 60 seconds
    private long statisticsWindowSize = 60 * 1000L;

    @Autowired
    private TransactionRepository repo;

    private ConcurrentSkipListMap<Long, Statistics> cache = new ConcurrentSkipListMap<>();

    private static <V> V logAndProxy(V v, String s) {
        log.debug(s);
        return v;
    }

    public StatisticsDTO getStatistics() {
        long windowStartTimestamp = Instant.now().minusMillis(statisticsWindowSize).toEpochMilli();


        // change to use DB for ceiling entry calc
        Statistics statistics = ofNullable(cache.ceilingEntry(windowStartTimestamp))
                .map(v -> logAndProxy(v, "Found in cache")) //TODOO
                .map(Map.Entry::getValue).orElse(getStatistics(windowStartTimestamp));

        StatisticsDTO statisticsDTO = new StatisticsDTO();
        statisticsDTO.setSum(statistics.getSum());
        statisticsDTO.setAvg(statistics.getAvg());
        statisticsDTO.setMax(statistics.getMax());
        statisticsDTO.setMin(statistics.getMin());
        statisticsDTO.setCount(statistics.getCount());

        return statisticsDTO;
    }

    private Statistics getStatistics(long windowStartTimestamp) {
        Object[] stats = repo.getStatistics(windowStartTimestamp).iterator().next();
        Statistics statistics = new Statistics();
        ofNullable(stats[0]).filter(Long.class::isInstance).map(Long.class::cast)
                .ifPresent(statistics::setSum);
        ofNullable(stats[1]).filter(Double.class::isInstance).map(Double.class::cast)
                .ifPresent(statistics::setAvg);
        ofNullable(stats[2]).filter(Long.class::isInstance).map(Long.class::cast)
                .ifPresent(statistics::setMax);
        ofNullable(stats[3]).filter(Long.class::isInstance).map(Long.class::cast)
                .ifPresent(statistics::setMin);
        ofNullable(stats[4]).filter(Long.class::isInstance).map(Long.class::cast)
                .ifPresent(statistics::setCount);
        return statistics;
    }

    public void save(TransactionDTO transactionDTO) {
        // exit fast if older than now - 60s
        Transaction toSave = new Transaction();
        toSave.setAmount(transactionDTO.getAmount());
        toSave.setTimestamp(transactionDTO.getTimestamp());
        repo.save(toSave);
        // find all timestamps greater than current and greater than now - 60s
        // recalculate each
        // update the cache
        // drop all the records older than now - 60 in DB
        // drop all the records older than now - 60 in cache
        cache.put(transactionDTO.getTimestamp(), getStatistics(transactionDTO.getTimestamp()));
    }

}
