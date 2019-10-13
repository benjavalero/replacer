package es.bvalero.replacer.benchmark;

import org.apache.commons.lang3.StringUtils;

class RedirectContainsIgnoreCaseMatcher extends RedirectAbstractMatcher {

    @Override
    boolean isRedirect(String text) {
        return StringUtils.containsIgnoreCase(text, REDIRECT_PREFIX);
    }

}
