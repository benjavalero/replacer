package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.exception.ReplacerException;

public class PageNotProcessableException extends ReplacerException {

    PageNotProcessableException(String message) {
        super(message);
    }
}
