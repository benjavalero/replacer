package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.ListingItem;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.VisibleForTesting;

// Use an abstract class to include the logic to fire events on item changes
@Slf4j
abstract class ListingLoader<T extends ListingItem> {

    @VisibleForTesting
    public static final String PROPERTY_ITEMS = "items";

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Getter(AccessLevel.PROTECTED)
    private SetValuedMap<WikipediaLanguage, T> items = new HashSetValuedHashMap<>();

    @VisibleForTesting
    public final void setItems(SetValuedMap<WikipediaLanguage, T> items) {
        this.changeSupport.firePropertyChange(PROPERTY_ITEMS, this.items, items);
        this.items = items;
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    public final void load() {
        LOGGER.debug("Load {} listings...", getLabel());
        try {
            setItems(findItemsForAllLanguages());
        } catch (ReplacerException e) {
            LOGGER.error("Error finding {} items in Wikipedia", getLabel(), e);
        }
    }

    abstract String getLabel(); // For tracing purposes

    private SetValuedMap<WikipediaLanguage, T> findItemsForAllLanguages() throws ReplacerException {
        SetValuedMap<WikipediaLanguage, T> map = new HashSetValuedHashMap<>();
        for (Map.Entry<WikipediaLanguage, String> entry : findListingsForAllLanguages().entrySet()) {
            WikipediaLanguage lang = entry.getKey();
            Set<T> listingItems = parseListing(entry.getValue());
            map.putAll(lang, listingItems);
            LOGGER.debug("Found {} items in {} Wikipedia: {}", getLabel(), lang, listingItems.size());
        }
        return map;
    }

    private Map<WikipediaLanguage, String> findListingsForAllLanguages() throws ReplacerException {
        Map<WikipediaLanguage, String> map = new HashMap<>();
        for (WikipediaLanguage wikipediaLanguage : WikipediaLanguage.values()) {
            map.put(wikipediaLanguage, findListingContentByLang(wikipediaLanguage));
        }
        return map;
    }

    private String findListingContentByLang(WikipediaLanguage lang) throws ReplacerException {
        LOGGER.debug("Find {} listings in {} Wikipedia...", getLabel(), lang);
        return findListingByLang(lang);
    }

    abstract String findListingByLang(WikipediaLanguage lang) throws ReplacerException;

    abstract Set<T> parseListing(String listingContent);
}
