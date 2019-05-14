package es.bvalero.replacer.finder.benchmark;

import es.bvalero.replacer.finder.MatchResult;

import java.util.Locale;
import java.util.Set;

abstract class TemplateAbstractFinder {

    abstract Set<MatchResult> findMatches(String text);

    boolean isLowercase(String word) {
        return word.equals(word.toLowerCase(Locale.forLanguageTag("es")));
    }

    String setFirstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")) + word.substring(1);
    }

    String setFirstUpperCaseClass(String word) {
        return String.format("[%s%s]%s", word.substring(0, 1).toUpperCase(Locale.forLanguageTag("es")), word.substring(0, 1), word.substring(1));
    }

}
