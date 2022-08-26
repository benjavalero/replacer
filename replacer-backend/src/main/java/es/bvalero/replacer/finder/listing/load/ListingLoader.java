package es.bvalero.replacer.finder.listing.load;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.exception.ReplacerException;
import es.bvalero.replacer.finder.listing.ListingItem;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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
        changeSupport.firePropertyChange(PROPERTY_ITEMS, this.items, items);
        this.items = items;
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public final void load() {
        LOGGER.debug("Load {} listings...", getLabel());
        setItems(findItemsForAllLanguages());
    }

    abstract String getLabel(); // For tracing purposes

    private SetValuedMap<WikipediaLanguage, T> findItemsForAllLanguages() {
        SetValuedMap<WikipediaLanguage, T> map = new HashSetValuedHashMap<>();
        for (Map.Entry<WikipediaLanguage, String> entry : findListingsForAllLanguages().entrySet()) {
            WikipediaLanguage lang = entry.getKey();
            Set<T> listingItems = parseListing(entry.getValue());
            map.putAll(lang, listingItems);
            LOGGER.debug("Found {} items in {} Wikipedia: {}", getLabel(), lang, listingItems.size());
        }
        return map;
    }

    private Map<WikipediaLanguage, String> findListingsForAllLanguages() {
        return Arrays
            .stream(WikipediaLanguage.values())
            .collect(Collectors.toMap(Function.identity(), this::findListingContentByLang));
    }

    private String findListingContentByLang(WikipediaLanguage lang) {
        try {
            LOGGER.debug("Find {} listings in {} Wikipedia...", getLabel(), lang);
            return findListingByLang(lang);
        } catch (ReplacerException e) {
            LOGGER.error("Error finding {} items in {} Wikipedia", getLabel(), lang, e);
            return EMPTY;
        }
    }

    abstract String findListingByLang(WikipediaLanguage lang) throws ReplacerException;

    abstract Set<T> parseListing(String listingContent);
}
