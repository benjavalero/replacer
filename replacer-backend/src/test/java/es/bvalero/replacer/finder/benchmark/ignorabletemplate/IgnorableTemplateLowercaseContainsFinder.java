package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

class IgnorableTemplateLowercaseContainsFinder extends IgnorableTemplateAbstractFinder {

    @Override
    boolean isRedirect(String text) {
        return text.toLowerCase().contains(REDIRECT_PREFIX);
    }
}
