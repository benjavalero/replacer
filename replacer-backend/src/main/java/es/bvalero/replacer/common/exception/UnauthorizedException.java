package es.bvalero.replacer.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception for operations not allowed according to the user roles or rights */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends Exception {

    public UnauthorizedException() {
        super();
    }
}
