package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import org.apache.commons.lang3.StringUtils;

class IgnorableTemplateContainsIgnoreCaseFinder extends IgnorableTemplateAbstractFinder {

    @Override
    boolean isRedirect(String text) {
        return StringUtils.containsIgnoreCase(text, REDIRECT_PREFIX);
    }
}
