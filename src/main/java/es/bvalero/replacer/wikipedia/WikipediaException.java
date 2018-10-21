package es.bvalero.replacer.wikipedia;

public class WikipediaException extends Exception {

    public WikipediaException() {
    }

    WikipediaException(String message) {
        super(message);
    }

    WikipediaException(Throwable throwable) {
        super(throwable);
    }

}
