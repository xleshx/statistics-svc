package company.challenge.domain;


import lombok.Data;

@Data
public class Transaction {
    double amount;
    long timestamp;
}
