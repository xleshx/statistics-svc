package company.challenge.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler {
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public void handleConflict(MethodArgumentNotValidException e) {
        log.debug(e.getMessage());
    }
}
