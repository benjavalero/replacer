package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.common.domain.Immutable;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.Finder;
import java.util.regex.MatchResult;

public interface ImmutableFinder extends Finder<Immutable> {
    @Override
    default Immutable convert(MatchResult match, WikipediaPage page) {
        return convert(match);
    }

    default Immutable convert(MatchResult match) {
        return Immutable.of(match.start(), match.group());
    }
}
