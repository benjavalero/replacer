package es.bvalero.replacer.wikipedia;

abstract class RedirectMatcher {

    final static String REDIRECT_PREFIX = "#redirec";

    abstract boolean isRedirect(String text);

}
