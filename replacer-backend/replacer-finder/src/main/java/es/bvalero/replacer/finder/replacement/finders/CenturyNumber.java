package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.findWordAfter;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.util.SimpleMatchResult;
import java.util.Objects;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

public record CenturyNumber(int start, String group, int arabic, @Nullable String era) implements SimpleMatchResult {
    /**
     * Get the Roman numeral representation of this century number.
     * This method is only used in conversion methods and does not affect performance
     * as it uses a pre-computed map.
     */
    public String roman() {
        return CenturyFinder.ARABIC_TO_ROMAN.get(String.valueOf(arabic));
    }

    public CenturyNumber withEra(String text, MatchResult era) {
        return new CenturyNumber(this.start, text.substring(this.start, era.end()), this.arabic, era.group());
    }

    private boolean isEraBefore() {
        return "a".equals(getEraLetter());
    }

    String getEraLetter() {
        return era == null ? EMPTY : ReplacerUtils.toLowerCase(String.valueOf(era.charAt(0)));
    }

    public boolean isGreaterThan(CenturyNumber m) {
        if (this.isEraBefore()) {
            return m.arabic() > this.arabic();
        } else if (m.isEraBefore()) {
            return true;
        } else {
            return this.arabic() > m.arabic();
        }
    }

    /**
     * Get the original number as it appears in the text (without era).
     * This method is only used in cosmetic actions and does not affect performance.
     */
    public String getOriginalNumber() {
        if (era == null) {
            return group;
        } else {
            return Objects.requireNonNull(findWordAfter(group, 0)).group();
        }
    }
}
