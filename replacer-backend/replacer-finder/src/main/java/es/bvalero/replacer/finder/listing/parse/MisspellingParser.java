package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.ListingItem;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

@Slf4j
abstract class MisspellingParser<T extends ListingItem> extends ListingParser<T> {

    private static final String CASE_SENSITIVE_VALUE = "cs";

    @Override
    @Nullable
    public T parseItemLine(String itemLine) {
        final String[] tokens = StringUtils.splitPreserveAllTokens(itemLine, '|');
        if (tokens.length >= 3) {
            try {
                return buildMisspelling(tokens);
            } catch (IllegalArgumentException e) {
                LOGGER.warn(e.getMessage());
            }
        } else {
            LOGGER.warn("Bad formatted misspelling: {}", itemLine);
        }

        return null;
    }

    private T buildMisspelling(String[] tokens) {
        final String word = tokens[0].trim();
        final boolean cs = CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim());
        // If the comment contains a pipe then we need to re-join it
        final String comment = StringUtils.join(ArrayUtils.subarray(tokens, 2, tokens.length), '|').trim();
        return buildMisspelling(word, cs, comment);
    }

    abstract T buildMisspelling(String word, boolean cs, String comment);
}
