package es.bvalero.replacer.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be used in the context of Replacer.
 * It is not worth to define sub-exceptions by functionality.
 * In case of throwing in Controllers is identified as an error 500.
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