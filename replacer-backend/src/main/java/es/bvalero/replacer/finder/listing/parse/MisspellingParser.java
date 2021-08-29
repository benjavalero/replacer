package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.Misspelling;
import lombok.extern.slf4j.Slf4j;
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
        T misspelling = null;

        String[] tokens = itemLine.split("\\|");
        if (tokens.length == 3) {
            String word = tokens[0].trim();
            boolean cs = CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim());
            String comment = tokens[2].trim();
            try {
                misspelling = buildMisspelling(word, cs, comment);
            } catch (IllegalArgumentException e) {
                LogHolder.LOGGER.warn("Ignore not valid misspelling: " + e.getMessage());
            }
        } else {
            LogHolder.LOGGER.warn("Bad formatted misspelling: {}", itemLine);
        }

        return misspelling;
    }

    T buildMisspelling(String word, boolean cs, String comment);
}