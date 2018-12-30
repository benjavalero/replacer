package es.bvalero.replacer.dump;

class DumpException extends Exception {

    private static final long serialVersionUID = 7156918867787498430L;

    DumpException(String message) {
        super(message);
    }

    DumpException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
