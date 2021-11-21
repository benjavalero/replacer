package es.bvalero.replacer.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Exception for operations in Controllers not allowed according to the user roles or rights */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends ReplacerException {}
