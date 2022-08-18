package es.bvalero.replacer.common.domain;

import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public interface ComparableReplacement {
    ReplacementType getType();
    int getStart();
    String getContext();

    /* If two replacements have the same position or context they will be considered equivalent but NOT EQUAL */
    default boolean isSame(ComparableReplacement that) {
        return (
            getType().equals(that.getType()) &&
            (isSameStart(getStart(), that.getStart()) || isSameContext(getContext(), that.getContext()))
        );
    }

    private boolean isSameStart(int start1, int start2) {
        // 0 is the default start for legacy replacements
        if (start1 != 0 || start2 != 0) {
            return start1 == start2;
        } else {
            return false;
        }
    }

    private boolean isSameContext(String context1, String context2) {
        // An empty string is the default context for legacy replacements
        if (StringUtils.isNotBlank(context1) || StringUtils.isNotBlank(context2)) {
            return Objects.equals(context1, context2);
        } else {
            return false;
        }
    }
}
