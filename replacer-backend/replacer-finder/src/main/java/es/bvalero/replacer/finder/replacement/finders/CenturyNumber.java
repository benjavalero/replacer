package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.findWordAfter;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.util.SimpleMatchResult;
import java.util.Objects;
import org.springframework.lang.Nullable;

public record CenturyNumber(int start, String group, String roman, int arabic, @Nullable String era)
    implements SimpleMatchResult {
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

    public String getOriginalNumber() {
        if (era == null) {
            return group;
        } else {
            return Objects.requireNonNull(findWordAfter(group, 0)).group();
        }
    }
}
