package es.bvalero.replacer.misspelling;

import java.util.Set;

abstract class WordFinder {

    abstract Set<WordMatch> findWords(String text);

    boolean isWordCompleteInText(WordMatch word, String text) {
        return word.getStart() == 0 || word.getEnd() == text.length() ||
                (!Character.isLetter(text.charAt(word.getStart() - 1))
                        && !Character.isLetter(text.charAt(word.getEnd())));
    }

    boolean isWordFollowedByUppercase(WordMatch word, String text) {
        return word.getEnd() + 1 < text.length()
                && !Character.isLetter(text.charAt(word.getEnd()))
                && Character.isUpperCase(text.charAt(word.getEnd() + 1));
    }

}
