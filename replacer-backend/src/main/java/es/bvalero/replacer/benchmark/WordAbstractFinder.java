package es.bvalero.replacer.benchmark;

import es.bvalero.replacer.finder.IgnoredReplacement;

import java.util.Set;

abstract class WordAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

    boolean isWordCompleteInText(int start, String word, String text) {
        int end = start + word.length();
        return start == 0 || end == text.length()
                || (!Character.isLetter(text.charAt(start - 1)) && !Character.isLetter(text.charAt(end)));
    }

}
