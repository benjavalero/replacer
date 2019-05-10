package es.bvalero.replacer.authentication;

public class AuthenticationException extends Exception {

    private static final long serialVersionUID = -6299065674942122771L;

    AuthenticationException(Throwable throwable) {
        super(throwable);
    }

    AuthenticationException(String message) {
        super(message);
    }

}
