package es.bvalero.replacer.finder.cosmetic.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.cosmetic.CosmeticFinder;
import es.bvalero.replacer.finder.util.RegexMatchFinder;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/** Links with cursive (or bold) around the alias, e.g. `[[Teléfono móvil|''smartphone'']] ==> ''[[Teléfono móvil|smartphone]]` */
@Component
class LinkCursiveFinder implements CosmeticFinder {

    @RegExp
    private static final String REGEX_LINK_CURSIVE = "\\[\\[([^|\\[\\]']+\\|)?('{2,3})([^|\\[\\]']+)\\2]]";

    private static final Pattern PATTERN_LINK_CURSIVE = Pattern.compile(REGEX_LINK_CURSIVE);

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        return RegexMatchFinder.find(page.getContent(), PATTERN_LINK_CURSIVE);
    }

    @Override
    public String getFix(MatchResult match, FinderPage page) {
        String linkTitle = Objects.requireNonNullElse(match.group(1), StringUtils.EMPTY);
        String aliasQuotes = match.group(2);
        String linkAlias = match.group(3);
        return String.format("%s[[%s%s]]%s", aliasQuotes, linkTitle, linkAlias, aliasQuotes);
    }
}
