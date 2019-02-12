package es.bvalero.replacer.authentication;

public class AuthenticationException extends Exception {

    AuthenticationException(String message) {
        super(message);
    }

    AuthenticationException(Throwable throwable) {
        super(throwable);
    }

}
