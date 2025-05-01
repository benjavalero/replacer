package es.bvalero.replacer.common.exception;

import lombok.experimental.StandardException;

/**
 * The exception to be used in the context of all the application.
 * In general, it is not worth to define sub-exceptions by functionality, but it can be done in some subdomains.
 */
@StandardException
public class ReplacerException extends Exception {}
