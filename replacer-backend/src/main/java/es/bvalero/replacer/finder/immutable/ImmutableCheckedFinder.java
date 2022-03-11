package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Value;

/** Decorator to perform some checks in found immutables */
public abstract class ImmutableCheckedFinder implements ImmutableFinder {

    private static final String SUFFIX_FINDER_CLASS = "Finder";

    private boolean showImmutableWarning;

    @Value("${replacer.show.immutable.warning}")
    public final void setShowImmutableWarning(String showImmutableWarning) {
        this.showImmutableWarning = Boolean.parseBoolean(showImmutableWarning);
    }

    // NOTE this will not be applied if overridden by an Immutable Finder
    @Override
    public Iterable<Immutable> find(FinderPage page) {
        if (showImmutableWarning) {
            // Trick: use iterable converter with no conversion at all but decoration
            return IterableUtils.transformedIterable(
                ImmutableFinder.super.find(page),
                immutable -> this.check(immutable, page)
            );
        } else {
            return ImmutableFinder.super.find(page);
        }
    }

    private Immutable check(Immutable immutable, FinderPage page) {
        this.checkMaxLength(immutable, page);
        return immutable;
    }

    protected int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    private void checkMaxLength(Immutable immutable, FinderPage page) {
        if (immutable.getText().length() > getMaxLength()) {
            final String message = String.format("%s too long", getImmutableType());
            logImmutableCheck(page, immutable, message);
        }
    }

    private String getImmutableType() {
        final String className = this.getClass().getSimpleName();
        return className.substring(className.length() - SUFFIX_FINDER_CLASS.length());
    }

    private void logImmutableCheck(FinderPage page, Immutable immutable, String message) {
        logImmutableCheck(page, immutable.getStart(), immutable.getEnd(), message);
    }

    protected void logImmutableCheck(FinderPage page, int start, int end, String message) {
        if (showImmutableWarning) {
            FinderUtils.logFinderResult(page, start, end, message);
        }
    }
}
