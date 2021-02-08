package es.bvalero.replacer.finder.benchmark.redirect;

import org.apache.commons.lang3.StringUtils;

class RedirectContainsIgnoreCaseMatcher extends RedirectAbstractMatcher {

    @Override
    boolean isRedirect(String text) {
        return StringUtils.containsIgnoreCase(text, REDIRECT_PREFIX);
    }
}
