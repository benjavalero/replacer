package es.bvalero.replacer.wikipedia.benchmark;

abstract class RedirectAbstractMatcher {

    static final String REDIRECT_PREFIX = "#redirec";

    abstract boolean isRedirect(String text);

}
