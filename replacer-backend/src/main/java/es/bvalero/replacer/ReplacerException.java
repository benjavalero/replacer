package es.bvalero.replacer;

public class ReplacerException extends Exception {

    private static final long serialVersionUID = -7659839124955029365L;

    public ReplacerException(String message) {
        super(message);
    }

    public ReplacerException(String message, Throwable throwable) {
        super(message, throwable);
    }

}