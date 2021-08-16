package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.finder.Finder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.regex.MatchResult;

public interface ReplacementFinder extends Finder<Replacement> {
    @Override
    default boolean validate(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
