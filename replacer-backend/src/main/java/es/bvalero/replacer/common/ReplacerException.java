package es.bvalero.replacer.common;

/**
 * Exception to be used in the context of Replacer.
 * It is not worth to define sub-exceptions by functionality.
 */
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
