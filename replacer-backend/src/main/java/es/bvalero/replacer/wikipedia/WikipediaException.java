package es.bvalero.replacer.wikipedia;

public class WikipediaException extends Exception {

    private static final long serialVersionUID = 2871760157267387056L;

    public WikipediaException() {
    }

    WikipediaException(String message) {
        super(message);
    }

    WikipediaException(Throwable throwable) {
        super(throwable);
    }

}
