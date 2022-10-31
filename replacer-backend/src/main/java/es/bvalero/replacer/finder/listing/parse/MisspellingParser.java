package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.Misspelling;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

interface MisspellingParser<T extends Misspelling> extends ListingParser<T> {
    String CASE_SENSITIVE_VALUE = "cs";

    @Slf4j
    final class LogHolder {
        // Trick to be able to log in interfaces
    }

    @Override
    @Nullable
    default T parseItemLine(String itemLine) {
        final String[] tokens = StringUtils.splitPreserveAllTokens(itemLine, '|');
        if (tokens.length >= 3) {
            try {
                return buildMisspelling(tokens);
            } catch (IllegalArgumentException e) {
                LogHolder.LOGGER.warn("Ignore not valid misspelling: " + e.getMessage());
            }
        } else {
            LogHolder.LOGGER.warn("Bad formatted misspelling: {}", itemLine);
        }

        return null;
    }

    default T buildMisspelling(String[] tokens) {
        final String word = tokens[0].trim();
        final boolean cs = CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim());
        // If the comment contains a pipe then we need to re-join it
        final String comment = StringUtils.join(ArrayUtils.subarray(tokens, 2, tokens.length), '|').trim();
        return buildMisspelling(word, cs, comment);
    }

    T buildMisspelling(String word, boolean cs, String comment);
}
