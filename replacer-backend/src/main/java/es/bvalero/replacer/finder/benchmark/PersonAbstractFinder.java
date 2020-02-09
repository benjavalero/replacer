package es.bvalero.replacer.finder.benchmark;

import java.util.Set;

abstract class PersonAbstractFinder {

    abstract Set<IgnoredReplacement> findMatches(String text);

    boolean isWordFollowedByUppercase(int start, String word, String text) {
        int end = start + word.length();
        return end + 1 < text.length()
                && !Character.isLetter(text.charAt(end))
                && Character.isUpperCase(text.charAt(end + 1));
    }

}
