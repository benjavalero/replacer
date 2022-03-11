package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.common.domain.CheckWikipediaAction;
import es.bvalero.replacer.common.domain.Cosmetic;
import es.bvalero.replacer.finder.FinderPage;

import java.util.regex.MatchResult;

public interface CosmeticCheckedFinder extends CosmeticFinder {

    @Override
    default Cosmetic convert(MatchResult match, FinderPage page) {
        return Cosmetic.builder()
            .start(match.start())
            .text(match.group())
            .fix(getFix(match, page))
            .checkWikipediaAction(getCheckWikipediaAction())
            .build();
    }

    CheckWikipediaAction getCheckWikipediaAction();
}
