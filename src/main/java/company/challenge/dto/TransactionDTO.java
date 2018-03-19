package company.challenge.dto;

import company.challenge.validation.TimestampIsYoungerThan;
import lombok.Data;

@Data
public class TransactionDTO {
       double amount;

       @TimestampIsYoungerThan(value = 60 * 1000, message = "Transaction timestamp is older than expected")
       long timestamp;
}
