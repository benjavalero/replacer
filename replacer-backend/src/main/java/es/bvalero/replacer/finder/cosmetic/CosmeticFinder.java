package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.common.domain.Cosmetic;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.regex.MatchResult;

public interface CosmeticFinder extends Finder<Cosmetic> {
    @Override
    default Cosmetic convert(MatchResult match, WikipediaPage page) {
        return Cosmetic.builder().start(match.start()).text(match.group()).fix(getFix(match, page)).build();
    }

    String getFix(MatchResult match, WikipediaPage page);
}
