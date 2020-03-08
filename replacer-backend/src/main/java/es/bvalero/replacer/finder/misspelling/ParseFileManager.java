package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
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

@Slf4j
public abstract class ParseFileManager<T> {
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

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
     * Update the list of false positives or replacements from Wikipedia.
     */
    @Scheduled(fixedDelayString = "${replacer.parse.file.delay}")
    public void update() {
        LOGGER.info("EXECUTE Scheduled daily update of {} set", getLabel());
        try {
            setItems(findItems());
        } catch (WikipediaException e) {
            LOGGER.error("Error updating {} set", getLabel(), e);
        }
    }

    abstract String getLabel();

    private Set<T> findItems() throws WikipediaException {
        LOGGER.info("START Loading {} set from Wikipedia...", getLabel());
        String itemsText = findItemsText();
        Set<T> itemSet = parseItemsText(itemsText);
        LOGGER.info("END Load {} set from Wikipedia. Items found: {}", getLabel(), itemSet.size());
        return itemSet;
    }

    abstract String findItemsText() throws WikipediaException;

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

    abstract @Nullable T parseItemLine(String itemLine);

    abstract String getItemKey(T item);
}
