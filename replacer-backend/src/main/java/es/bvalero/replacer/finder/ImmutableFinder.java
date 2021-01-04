package es.bvalero.replacer.finder;

import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import java.util.List;
import java.util.regex.MatchResult;
import org.apache.commons.collections4.IterableUtils;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;

/**
 * Interface to be implemented by any class returning a collection of immutables.
 * <p>
 * For performance reasons, it is preferred to return them as an iterable.
 */
public interface ImmutableFinder extends Comparable<ImmutableFinder> {
    Iterable<Immutable> find(IndexablePage page);

    @TestOnly
    default List<Immutable> findList(String text) {
        return findList(text, WikipediaLanguage.getDefault());
    }

    @TestOnly
    default List<Immutable> findList(String text, WikipediaLanguage lang) {
        WikipediaPage page = WikipediaPage.builder().content(text).lang(lang).build();
        return IterableUtils.toList(find(page));
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group(), this);
    }

    default ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.NONE;
    }

    default int compareTo(ImmutableFinder finder) {
        return Integer.compare(finder.getPriority().getValue(), this.getPriority().getValue());
    }

    default int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    default void checkMaxLength(Immutable immutable, IndexablePage page, Logger logger) {
        if (immutable.getText().length() > getMaxLength()) {
            logWarning(immutable, page, logger, "Immutable too long");
        }
    }

    default void logWarning(Immutable immutable, IndexablePage page, Logger logger, String message) {
        logger.warn(
            "{}: {} - {} - {} - {} - {}",
            message,
            this.getClass().getSimpleName(),
            immutable.getText(),
            page.getLang(),
            page.getTitle(),
            immutable.getStart()
        );
    }

    default int getContextThreshold() {
        return 50;
    }
}
