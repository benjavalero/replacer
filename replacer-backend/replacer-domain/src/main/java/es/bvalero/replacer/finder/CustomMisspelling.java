package es.bvalero.replacer.finder;

public class CustomMisspelling extends Misspelling {

    private CustomMisspelling(String word, boolean caseSensitive, String comment) {
        super(word, caseSensitive, comment);
    }

    public static CustomMisspelling of(String word, boolean caseSensitive, String comment) {
        return new CustomMisspelling(word, caseSensitive, comment);
    }
}
