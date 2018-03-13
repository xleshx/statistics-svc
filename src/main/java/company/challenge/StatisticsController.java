package company.challenge;

import company.challenge.dto.StatisticsDTO;
import company.challenge.dto.TransactionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RestController
public class StatisticsController {

    @Autowired
    TransactionalService service;

    @RequestMapping(method = GET, path = "/statistics", produces = APPLICATION_JSON_VALUE)
    public StatisticsDTO get() {
        log.debug("Getting statistics");
        return service.getStatistics();
    }

    @RequestMapping(method = POST, path = "/transactions", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void createTransaction(@RequestBody TransactionDTO transactionDTO) {
        // Service layer can be introduced at this point, not introduced for simplicity reasons
        service.save(transactionDTO);
    }

}
