package es.bvalero.replacer.finder.replacement.finders;

import es.bvalero.replacer.finder.util.SimpleMatchResult;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

public record CenturyMatch(int start, String group, @Nullable MatchResult word, CenturyNumber number)
    implements SimpleMatchResult {
    public static CenturyMatch ofNumber(CenturyNumber number) {
        return new CenturyMatch(number.start(), number.group(), null, number);
    }
}
