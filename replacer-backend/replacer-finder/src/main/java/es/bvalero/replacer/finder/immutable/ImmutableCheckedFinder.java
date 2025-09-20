package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.stream.Stream;
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
    public Stream<Immutable> find(FinderPage page) {
        if (this.showImmutableWarning) {
            // Trick: use peek with no conversion at all but decoration
            return ImmutableFinder.super.find(page).peek(immutable -> check(immutable, page));
        } else {
            return ImmutableFinder.super.find(page);
        }
    }

    private void check(Immutable immutable, FinderPage page) {
        this.checkMaxLength(immutable, page);
    }

    protected int getMaxLength() {
        return Integer.MAX_VALUE;
    }

    private void checkMaxLength(Immutable immutable, FinderPage page) {
        if (immutable.text().length() > getMaxLength()) {
            final String message = getImmutableType() + " too long";
            logImmutableCheck(page, immutable, message);
        }
    }

    private String getImmutableType() {
        final String className = getClass().getSimpleName();
        return className.substring(className.length() - SUFFIX_FINDER_CLASS.length());
    }

    private void logImmutableCheck(FinderPage page, Immutable immutable, String message) {
        logImmutableCheck(page, immutable.start(), immutable.end(), message);
    }

    public void logImmutableCheck(FinderPage page, int start, int end, String message) {
        FinderUtils.logFinderResult(page, start, end, message);
    }
}
