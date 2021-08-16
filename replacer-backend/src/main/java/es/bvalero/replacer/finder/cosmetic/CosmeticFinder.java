package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.finder.Finder;
import java.util.regex.MatchResult;

public interface CosmeticFinder extends Finder<Cosmetic> {
    default Cosmetic convert(MatchResult match) {
        return Cosmetic.builder().start(match.start()).text(match.group()).fix(getFix(match)).finder(this).build();
    }

    String getFix(MatchResult match);
}
