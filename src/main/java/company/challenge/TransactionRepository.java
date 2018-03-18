package company.challenge;

import company.challenge.domain.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query("SELECT sum(t.amount), avg(t.amount), max(t.amount), min(t.amount), count(t.amount) " +
            "FROM Transaction t WHERE t.timestamp >= :windowStartTimestamp")
    List<Object[]> getStatistics(@Param("windowStartTimestamp") long windowStartTimestamp);

    Transaction findDistinctFirstByTimestampAfter(Long timestamp);

    Set<Transaction> findAllByTimestampBefore(Long timestamp);

    Transaction removeTransactionsByTimestampIsBefore(Long timestamp);
}
