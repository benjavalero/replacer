package es.bvalero.replacer.finder.immutable;

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
        // Trick: use iterable converter with no conversion at all but decoration
        return IterableUtils.transformedIterable(
            ImmutableFinder.super.find(page),
            immutable -> this.check(immutable, page)
        );
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
            String message = String.format("%s too long", getImmutableType());
            logImmutableCheck(getImmutableSnippet(immutable, page), message);
        }
    }

    private String getImmutableSnippet(Immutable immutable, FinderPage page) {
        return getImmutableSnippet(immutable.getStart(), immutable.getEnd(), page);
    }

    protected String getImmutableSnippet(int start, int end, FinderPage page) {
        return FinderUtils.getPageSnippet(start, end, page);
    }

    private String getImmutableType() {
        String className = this.getClass().getSimpleName();
        return className.substring(className.length() - SUFFIX_FINDER_CLASS.length());
    }

    protected void logImmutableCheck(String snippet, String message) {
        if (showImmutableWarning) {
            FinderUtils.logFinderResult(snippet, message);
        }
    }
}
