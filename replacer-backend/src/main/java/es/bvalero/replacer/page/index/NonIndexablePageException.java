package es.bvalero.replacer.page.index;

import es.bvalero.replacer.common.exception.ReplacerException;

class NonIndexablePageException extends ReplacerException {

    NonIndexablePageException(String message) {
        super(message);
    }
}
