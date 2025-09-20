package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.util.ReplacerUtils;

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
        if (end() <= pageContent.length() && ReplacerUtils.containsAtPosition(pageContent, text(), start())) {
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
