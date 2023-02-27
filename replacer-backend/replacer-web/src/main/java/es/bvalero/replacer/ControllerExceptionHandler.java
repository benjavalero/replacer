package es.bvalero.replacer;

import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.user.AuthenticationException;
import es.bvalero.replacer.wikipedia.WikipediaConflictException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { IllegalArgumentException.class })
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(e, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = { AuthenticationException.class })
    protected ResponseEntity<Object> handleAuthenticationException() {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = { ForbiddenException.class })
    protected ResponseEntity<Object> handleForbiddenException() {
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = { WikipediaException.class })
    protected ResponseEntity<Object> handleWikipediaException(WikipediaException e) {
        if (e instanceof WikipediaConflictException) {
            LOGGER.info(e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } else if (e.getMessage() != null && e.getMessage().contains("mwoauth-invalid-authorization")) {
            LOGGER.warn("Authentication error saving page content: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } else {
            LOGGER.error("Error saving page content", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleOtherException(Exception e) {
        return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
