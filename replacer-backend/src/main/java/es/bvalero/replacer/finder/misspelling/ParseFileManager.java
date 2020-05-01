package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Abstract class, implementing the Observable pattern, to load periodically properties maintained externally
 * and used by some finders.
 */
@Slf4j
public abstract class ParseFileManager<T> {
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    // Set of misspellings found in the misspelling list
    private Set<T> items = new HashSet<>();

    void setItems(Set<T> items) {
        changeSupport.firePropertyChange("name", this.items, items);

        processRemovedItems(this.items, items);

        this.items = items;
    }

    abstract void processRemovedItems(Set<T> oldItems, Set<T> newItems);

    void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update periodically a list of items from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void update() {
        LOGGER.info("EXECUTE Scheduled daily update of {} set", getLabel());
        try {
            setItems(findItems());
        } catch (ReplacerException e) {
            LOGGER.error("Error updating {} set", getLabel(), e);
        }
    }

    /**
     * The name of the item concept for logging purposes.
     */
    abstract String getLabel();

    private Set<T> findItems() throws ReplacerException {
        LOGGER.info("START Loading {} set from Wikipedia...", getLabel());
        String itemsText = findItemsText();
        Set<T> itemSet = parseItemsText(itemsText);
        LOGGER.info("END Load {} set from Wikipedia. Items found: {}", getLabel(), itemSet.size());
        return itemSet;
    }

    /**
     * Retrieve from Wikipedia the text containing the items.
     */
    abstract String findItemsText() throws ReplacerException;

    /**
     * Parse the text containing the items line by line.
     */
    Set<T> parseItemsText(String text) {
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
    abstract @Nullable T parseItemLine(String itemLine);

    /**
     * In some cases the key string identifying the item.
     */
    abstract String getItemKey(T item);
}
