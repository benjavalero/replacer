package es.bvalero.replacer.finder.listing;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Abstract class, implementing the Observable pattern, to load periodically properties
 * maintained externally and used by some finders.
 */
@Slf4j
public abstract class ParseFileManager<T> {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Getter(AccessLevel.PROTECTED)
    private SetValuedMap<WikipediaLanguage, T> items = new HashSetValuedHashMap<>();

    public void setItems(SetValuedMap<WikipediaLanguage, T> items) {
        changeSupport.firePropertyChange("name", this.items, items);
        this.items = items;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update periodically a list of items from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void scheduledItemListUpdate() {
        LOGGER.info("Scheduled {} lists update", getLabel());
        setItems(findItems());
    }

    /**
     * The name of the item concept for logging purposes.
     */
    protected abstract String getLabel();

    private SetValuedMap<WikipediaLanguage, T> findItems() {
        SetValuedMap<WikipediaLanguage, T> map = new HashSetValuedHashMap<>();
        for (Map.Entry<WikipediaLanguage, String> entry : findItemsText().entrySet()) {
            Set<T> itemSet = parseItemsText(entry.getValue());
            map.putAll(entry.getKey(), itemSet);
            LOGGER.debug("Found {} items in {} Wikipedia: {}", getLabel(), entry.getKey(), itemSet.size());
        }
        return map;
    }

    /**
     * Retrieve from Wikipedia the text containing the items for all languages.
     */
    private Map<WikipediaLanguage, String> findItemsText() {
        return Arrays
            .stream(WikipediaLanguage.values())
            .collect(Collectors.toMap(Function.identity(), this::findItemsText));
    }

    /**
     * Retrieve from Wikipedia the text containing the items for a specific language.
     */
    private String findItemsText(WikipediaLanguage lang) {
        String text = FinderUtils.STRING_EMPTY;
        try {
            text = findItemsTextInWikipedia(lang);
        } catch (ReplacerException e) {
            LOGGER.error("Error finding {} items in {} Wikipedia", getLabel(), lang, e);
        }
        return text;
    }

    protected abstract String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException;

    /**
     * Parse the text containing the items line by line.
     */
    public Set<T> parseItemsText(String text) {
        Set<T> itemSet = new HashSet<>();

        // We maintain a temporary set of item keys to find soft duplicates
        Set<String> itemKeys = new HashSet<>();

        Stream<String> stream = new BufferedReader(new StringReader(text)).lines();
        // Ignore the lines not corresponding to item lines
        stream
            .filter(this::isItemLineValid)
            .forEach(
                strLine -> {
                    T item = parseItemLine(trimItemLine(strLine));
                    if (item != null) {
                        if (itemKeys.add(getItemKey(item))) {
                            itemSet.add(item);
                        } else {
                            LOGGER.warn("Duplicated item: {}", getItemKey(item));
                        }
                    }
                }
            );

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

    /**
     * Parse a line in the text corresponding to an item.
     */
    protected abstract @Nullable T parseItemLine(String itemLine);

    /**
     * In some cases the key string identifying the item.
     */
    protected abstract String getItemKey(T item);
}
