package es.bvalero.replacer.dump;

class DumpException extends Exception {

    DumpException(String message) {
        super(message);
    }

    DumpException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
