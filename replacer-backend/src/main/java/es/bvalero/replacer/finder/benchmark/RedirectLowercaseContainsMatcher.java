package es.bvalero.replacer.finder.benchmark;

class RedirectLowercaseContainsMatcher extends RedirectAbstractMatcher {

    @Override
    boolean isRedirect(String text) {
        return text.toLowerCase().contains(REDIRECT_PREFIX);
    }

}
