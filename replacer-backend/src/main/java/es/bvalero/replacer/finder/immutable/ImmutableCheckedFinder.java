package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Value;

/** Decorator to perform some checks in found immutables */
@Slf4j
abstract class ImmutableCheckedFinder implements ImmutableFinder {

    protected static final int CONTEXT_THRESHOLD = 50;

    private boolean showLongImmutables;

    @Value("${replacer.show.long.immutables}")
    public final void setShowLongImmutables(String showLongImmutables) {
        this.showLongImmutables = Boolean.parseBoolean(showLongImmutables);
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        // Trick: use iterable converter with no conversion at all but decoration
        return IterableUtils.transformedIterable(
            ImmutableFinder.super.find(page),
            immutable -> this.check(immutable, page)
        );
    }

    private Immutable check(Immutable immutable, FinderPage page) {
        if (showLongImmutables) {
            this.checkMaxLength(immutable, page);
        }
        return immutable;
    }

    int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    private void checkMaxLength(Immutable immutable, FinderPage page) {
        if (immutable.getText().length() > getMaxLength()) {
            logWarning(immutable, page, "Immutable too long");
        }
    }

    void logWarning(Immutable immutable, FinderPage page, String message) {
        LOGGER.warn(
            "{}: {} - {} - {} - {} - {}",
            message,
            this.getClass().getSimpleName(),
            immutable.getText(),
            page.getLang(),
            page.getTitle(),
            immutable.getStart()
        );
    }
}
