package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.regex.MatchResult;

public interface CosmeticFinder extends Finder<Cosmetic> {
    @Override
    default Cosmetic convert(MatchResult match, FinderPage page) {
        return Cosmetic
            .builder()
            .start(match.start())
            .text(match.group())
            .fix(getFix(match, page))
            .checkWikipediaAction(getCheckWikipediaAction())
            .build();
    }

    String getFix(MatchResult match, FinderPage page);

    default CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.NO_ACTION;
    }
}
