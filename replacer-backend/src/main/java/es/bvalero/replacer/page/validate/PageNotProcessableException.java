package es.bvalero.replacer.page.validate;

import es.bvalero.replacer.common.exception.ReplacerException;

class PageNotProcessableException extends ReplacerException {

    PageNotProcessableException(String message) {
        super(message);
    }
}
