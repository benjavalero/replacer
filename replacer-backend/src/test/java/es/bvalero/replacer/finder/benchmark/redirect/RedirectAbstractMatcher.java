package es.bvalero.replacer.finder.benchmark.redirect;

abstract class RedirectAbstractMatcher {

    static final String REDIRECT_PREFIX = "#redirec";

    abstract boolean isRedirect(String text);
}
