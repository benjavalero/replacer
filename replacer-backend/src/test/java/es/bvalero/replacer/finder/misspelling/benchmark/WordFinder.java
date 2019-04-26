package es.bvalero.replacer.finder.misspelling.benchmark;

import java.util.Set;

abstract class WordFinder {

    abstract Set<WordMatch> findWords(String text);

    boolean isWordCompleteInText(WordMatch word, String text) {
        return word.getStart() == 0 || word.getEnd() == text.length() ||
                (!Character.isLetterOrDigit(text.charAt(word.getStart() - 1))
                        && !Character.isLetterOrDigit(text.charAt(word.getEnd())));
    }

}
