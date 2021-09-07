package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

abstract class IgnorableTemplateAbstractFinder {

    static final String REDIRECT_PREFIX = "#redirec";

    abstract boolean isRedirect(String text);
}
