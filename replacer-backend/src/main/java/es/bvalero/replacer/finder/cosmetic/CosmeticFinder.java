package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.regex.MatchResult;

public interface CosmeticFinder extends Finder<Cosmetic> {
    @Override
    default Cosmetic convert(MatchResult match, FinderPage page) {
        return convert(match);
    }

    default Cosmetic convert(MatchResult match) {
        return Cosmetic.builder().start(match.start()).text(match.group()).fix(getFix(match)).build();
    }

    String getFix(MatchResult match);
}
