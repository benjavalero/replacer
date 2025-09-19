package es.bvalero.replacer.finder.cosmetic;

import es.bvalero.replacer.checkwikipedia.CheckWikipediaAction;
import es.bvalero.replacer.finder.Cosmetic;
import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.FinderPage;
import java.util.regex.MatchResult;

public interface CosmeticFinder extends Finder<Cosmetic> {
    @Override
    default Cosmetic convert(MatchResult match, FinderPage page) {
        return new Cosmetic(match.start(), match.group(), getFix(match, page), getCheckWikipediaAction());
    }

    String getFix(MatchResult match, FinderPage page);

    default CheckWikipediaAction getCheckWikipediaAction() {
        return CheckWikipediaAction.NO_ACTION;
    }
}
