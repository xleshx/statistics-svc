package company.challenge.domain;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class Transaction {
    @Id
    @GeneratedValue
    Long id;
    Long amount;
    Long timestamp;
}
