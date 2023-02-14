package es.bvalero.replacer;

import es.bvalero.replacer.common.exception.ForbiddenException;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.user.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

    @ExceptionHandler(value = { ReplacerException.class })
    protected ResponseEntity<Object> handleReplacerException(ReplacerException e) {
        return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
