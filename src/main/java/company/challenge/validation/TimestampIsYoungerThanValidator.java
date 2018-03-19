package company.challenge.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;

public class TimestampIsYoungerThanValidator implements ConstraintValidator<TimestampIsYoungerThan, Long> {

    private Long threshold;

    @Override
    public void initialize(TimestampIsYoungerThan annotation) {
        this.threshold = annotation.value();
    }

    // implementation has to be changed in order to inject Clock impl using LocalValidatorFactoryBean
    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        long thresholdTs = Instant.now().minusMillis(threshold).toEpochMilli();
        return value > thresholdTs;
    }
}
