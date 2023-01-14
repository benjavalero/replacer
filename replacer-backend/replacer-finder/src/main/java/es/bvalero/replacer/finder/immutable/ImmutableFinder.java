package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Immutable;
import java.util.regex.MatchResult;

public interface ImmutableFinder extends Finder<Immutable> {
    @Override
    default Immutable convert(MatchResult match, FinderPage page) {
        return convert(match);
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group());
    }
}
