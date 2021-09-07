package es.bvalero.replacer.finder.benchmark.ignorabletemplate;

import java.util.regex.Pattern;

class IgnorableTemplateRegexInsensitiveFinder extends IgnorableTemplateAbstractFinder {

    private static final Pattern PATTERN_REDIRECT = Pattern.compile(REDIRECT_PREFIX, Pattern.CASE_INSENSITIVE);

    @Override
    boolean isRedirect(String text) {
        return PATTERN_REDIRECT.matcher(text).find();
    }
}
