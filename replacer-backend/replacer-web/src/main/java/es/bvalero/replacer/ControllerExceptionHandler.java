package es.bvalero.replacer;

import es.bvalero.replacer.common.security.ForbiddenException;
import es.bvalero.replacer.common.util.WebUtils;
import es.bvalero.replacer.page.save.WikipediaConflictException;
import es.bvalero.replacer.user.AuthorizationException;
import es.bvalero.replacer.wikipedia.WikipediaException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

    @ExceptionHandler(value = { AuthorizationException.class })
    protected ResponseEntity<Object> handleAuthorizationException(AuthorizationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .header(HttpHeaders.SET_COOKIE, WebUtils.buildAccessTokenEmptyCookie().toString())
            .body(e);
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
            LOGGER.warn("Authorization error saving page content: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.SET_COOKIE, WebUtils.buildAccessTokenEmptyCookie().toString())
                .body(e);
        } else {
            LOGGER.error("Error saving page content", e);
            return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleOtherException(Exception e) {
        LOGGER.warn("Handle unmanaged exception", e);
        return new ResponseEntity<>(e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
