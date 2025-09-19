package es.bvalero.replacer.finder;

/**
 * An <strong>immutable</strong> is a section in the page contents to be left untouched,
 * for instance a literal quote, so any replacement found within it must be ignored
 * and not offered to the user for revision.
 */
public record Immutable(int start, String text) implements FinderResult {
    public static Immutable of(int start, String text) {
        return new Immutable(start, text);
    }
}
