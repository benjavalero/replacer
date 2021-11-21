package es.bvalero.replacer.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The exception to be used in the context of all the application.
 * In general, it is not worth to define sub-exceptions by functionality, but it can be done in some sub-domains.
 * In case of being thrown by Controllers, it is identified as an HTTP error 500.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReplacerException extends Exception {

    public ReplacerException() {
        super();
    }

    public ReplacerException(String message) {
        super(message);
    }

    public ReplacerException(Throwable throwable) {
        super(throwable);
    }

    public ReplacerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
