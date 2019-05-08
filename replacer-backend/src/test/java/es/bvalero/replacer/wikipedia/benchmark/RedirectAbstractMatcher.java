package es.bvalero.replacer.wikipedia.benchmark;

abstract class RedirectAbstractMatcher {

    final static String REDIRECT_PREFIX = "#redirec";

    abstract boolean isRedirect(String text);

}
