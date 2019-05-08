package es.bvalero.replacer.wikipedia.benchmark;

class RedirectLowercaseContainsMatcher extends RedirectAbstractMatcher {

    @Override
    boolean isRedirect(String text) {
        return text.toLowerCase().contains(REDIRECT_PREFIX);
    }

}
