package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Value;

/** Decorator to perform some checks in found immutables */
@Slf4j
public abstract class ImmutableCheckedFinder implements ImmutableFinder {

    private static final String SUFFIX_FINDER_CLASS = "Finder";
    private static final int CONTEXT_THRESHOLD = 50;

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

    protected int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    private void checkMaxLength(Immutable immutable, FinderPage page) {
        if (immutable.getText().length() > getMaxLength()) {
            String message = String.format("%s too long", getImmutableType());
            logWarning(immutable.getText(), page, message);
        }
    }

    private String getImmutableType() {
        String className = this.getClass().getSimpleName();
        return className.substring(className.length() - SUFFIX_FINDER_CLASS.length());
    }

    protected void logWarning(String pageContent, int start, int end, FinderPage page, String message) {
        String immutableText = FinderUtils.getContextAroundWord(pageContent, start, end, CONTEXT_THRESHOLD);
        logWarning(immutableText, page, message);
    }

    private void logWarning(String immutableText, FinderPage page, String message) {
        LOGGER.warn("{}: {} - {} - {}", message, immutableText, page.getLang(), page.getTitle());
    }
}
