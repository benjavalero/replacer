package es.bvalero.replacer.wikipedia.benchmark;

import java.util.regex.Pattern;

class RedirectRegexInsensitiveMatcher extends RedirectAbstractMatcher {

    private static final Pattern PATTERN_REDIRECT = Pattern.compile(REDIRECT_PREFIX, Pattern.CASE_INSENSITIVE);

    @Override
    boolean isRedirect(String text) {
        return PATTERN_REDIRECT.matcher(text).find();
    }

}
