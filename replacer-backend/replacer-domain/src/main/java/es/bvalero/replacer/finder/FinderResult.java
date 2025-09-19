package es.bvalero.replacer.finder;

/**
 * Base interface for the finder results: cosmetics, immutables and replacements.
 * It has to be public so the extensions can use the common methods.
 */
public interface FinderResult extends Comparable<FinderResult> {
    int start();
    String text();

    default int end() {
        return start() + text().length();
    }

    @Override
    default int compareTo(FinderResult o) {
        // Compare by start and then by end
        return start() == o.start() ? Integer.compare(end(), o.end()) : Integer.compare(start(), o.start());
    }

    /** Return if a result contains strictly, i.e. not been equal, another result. */
    default boolean containsStrictly(FinderResult r) {
        // We don't want an item to contain itself
        return this.contains(r) && this.compareTo(r) != 0;
    }

    /** Return if a result contains (not strictly, i.e. both can be equal) another result. */
    default boolean contains(FinderResult r) {
        return r.start() >= start() && r.end() <= end();
    }

    default boolean validate(String pageContent) {
        // Validate positions only on tests not to penalize the performance
        if (end() <= pageContent.length() && text().equals(pageContent.substring(start(), end()))) {
            return true;
        } else {
            String msg = String.format(
                "Finder result (%d - %s) doesn't match with page content",
                this.start(),
                this.text()
            );
            throw new IllegalStateException(msg);
        }
    }
}
