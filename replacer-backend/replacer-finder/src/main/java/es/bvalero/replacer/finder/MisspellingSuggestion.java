package es.bvalero.replacer.finder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class MisspellingSuggestion {

    @NonNull
    String text;

    @Nullable
    String comment;

    static MisspellingSuggestion of(String text, @Nullable String comment) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank suggestion text");
        }
        return new MisspellingSuggestion(text, StringUtils.isBlank(comment) ? null : comment);
    }

    static MisspellingSuggestion ofNoComment(String text) {
        return of(text, null);
    }
}
