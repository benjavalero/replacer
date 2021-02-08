package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.common.Finder;
import java.util.regex.MatchResult;

interface ImmutableFinder extends Finder<Immutable>, Comparable<ImmutableFinder> {
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
