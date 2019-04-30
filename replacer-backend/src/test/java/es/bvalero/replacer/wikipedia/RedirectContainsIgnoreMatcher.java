package es.bvalero.replacer.wikipedia;

import org.apache.commons.lang3.StringUtils;

class RedirectContainsIgnoreMatcher extends RedirectMatcher {

    @Override
    boolean isRedirect(String text) {
        return StringUtils.containsIgnoreCase(text, REDIRECT_PREFIX);
    }

}
