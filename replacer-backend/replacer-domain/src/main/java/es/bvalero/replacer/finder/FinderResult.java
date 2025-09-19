package es.bvalero.replacer.finder;

/**
 * Base interface for the finder results: cosmetics, immutables and replacements.
 * It has to be public so the extensions can use the common methods.
 */
public interface FinderResult extends Comparable<FinderResult> {
    int getStart();
    String getText();

    default int getEnd() {
        return getStart() + getText().length();
    }

    @Override
    default int compareTo(FinderResult o) {
        // Compare by start and then by end
        return getStart() == o.getStart()
            ? Integer.compare(getEnd(), o.getEnd())
            : Integer.compare(getStart(), o.getStart());
    }

    /** Return if a result contains strictly, i.e. not been equal, another result. */
    default boolean containsStrictly(FinderResult r) {
        // We don't want an item to contain itself
        return this.contains(r) && this.compareTo(r) != 0;
    }

    /** Return if a result contains (not strictly, i.e. both can be equal) another result. */
    default boolean contains(FinderResult r) {
        return r.getStart() >= getStart() && r.getEnd() <= getEnd();
    }

    default boolean validate(String pageContent) {
        // Validate positions only on tests not to penalize the performance
        if (getEnd() <= pageContent.length() && getText().equals(pageContent.substring(getStart(), getEnd()))) {
            return true;
        } else {
            String msg = String.format(
                "Finder result (%d - %s) doesn't match with page content",
                this.getStart(),
                this.getText()
            );
            throw new IllegalStateException(msg);
        }
    }
}
