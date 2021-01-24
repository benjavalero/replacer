package es.bvalero.replacer.authentication;

class AuthenticationException extends Exception {

    private static final long serialVersionUID = -6299065674942122771L;

    AuthenticationException() {
        super();
    }

    AuthenticationException(Throwable th) {
        super(th);
    }
}
