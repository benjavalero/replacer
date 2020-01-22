package es.bvalero.replacer.finder2;

import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Replacement;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface to be implemented by any class returning a collection of replacements.
 */
public interface ReplacementFinder extends RegexFinder<Replacement> {
    public Stream<Replacement> find(String text);

    default List<Replacement> findList(String text) {
        return find(text).collect(Collectors.toList());
    }

    @Override
    default boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
