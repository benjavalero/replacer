package es.bvalero.replacer.finder.benchmark.redirect;

class RedirectLowercaseContainsMatcher extends RedirectAbstractMatcher {

    @Override
    boolean isRedirect(String text) {
        return text.toLowerCase().contains(REDIRECT_PREFIX);
    }
}
