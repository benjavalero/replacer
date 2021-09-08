package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.regex.MatchResult;

public interface ImmutableFinder extends Finder<Immutable>, Comparable<ImmutableFinder> {
    @Override
    default Immutable convert(MatchResult match, FinderPage page) {
        return convert(match);
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group());
    }

    default ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.NONE;
    }

    default int compareTo(ImmutableFinder finder) {
        return Integer.compare(finder.getPriority().getValue(), this.getPriority().getValue());
    }
}
