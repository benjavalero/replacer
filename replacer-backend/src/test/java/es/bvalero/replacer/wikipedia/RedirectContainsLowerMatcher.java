package es.bvalero.replacer.wikipedia;

class RedirectContainsLowerMatcher extends RedirectMatcher {

    @Override
    boolean isRedirect(String text) {
        return text.toLowerCase().contains(REDIRECT_PREFIX);
    }

}
