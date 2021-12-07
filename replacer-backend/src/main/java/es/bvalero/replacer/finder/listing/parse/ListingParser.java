package es.bvalero.replacer.finder.listing.parse;

import es.bvalero.replacer.finder.listing.ListingItem;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

interface ListingParser<T extends ListingItem> {
    @Slf4j
    final class LogHolder {
        // Trick to be able to log in interfaces
    }

    /** Parse the listing text containing the items line by line */
    default Set<T> parseListing(String listing) {
        Set<T> itemSet = new HashSet<>();

        // We maintain a temporary set of item keys to find soft duplicates
        Set<String> itemKeys = new HashSet<>();

        Stream<String> stream = new BufferedReader(new StringReader(listing)).lines();
        // Ignore the lines not corresponding to item lines
        stream
            .filter(this::isItemLineValid)
            .forEach(strLine -> {
                T item = parseItemLine(trimItemLine(strLine));
                if (item != null) {
                    if (itemKeys.add(item.getKey())) {
                        itemSet.add(item);
                    } else {
                        LogHolder.LOGGER.warn("Duplicated item: {}", item.getKey());
                    }
                }
            });

        return itemSet;
    }

    private boolean isItemLineValid(String itemLine) {
        return itemLine.startsWith(" ") && StringUtils.isNotBlank(itemLine) && !itemLine.trim().startsWith("#");
    }

    private String trimItemLine(String itemLine) {
        return removeTrailingComment(itemLine).trim();
    }

    private String removeTrailingComment(String line) {
        int idx = line.indexOf('#');
        return idx != -1 ? line.substring(0, idx) : line;
    }

    /** Parse a line in the text corresponding to an item */
    @Nullable
    T parseItemLine(String itemLine);
}
