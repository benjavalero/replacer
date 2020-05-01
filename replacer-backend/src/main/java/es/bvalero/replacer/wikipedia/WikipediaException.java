package es.bvalero.replacer.wikipedia;

import es.bvalero.replacer.ReplacerException;

/**
 * @deprecated To be removed and replaced completely by @ReplacerException.
 */
@Deprecated(forRemoval = true)
public class WikipediaException extends ReplacerException {

    private static final long serialVersionUID = 2871760157267387056L;

    public WikipediaException() {
    }

    WikipediaException(String message) {
        super(message);
    }

    WikipediaException(Throwable throwable) {
        super(throwable);
    }

    WikipediaException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
