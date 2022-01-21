package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.replacement.RemoveObsoleteReplacementType;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class ObsoleteMisspellingListener implements PropertyChangeListener {

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Autowired
    private RemoveObsoleteReplacementType removeObsoleteReplacementType;

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
        composedMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        // This should work for both types of misspellings
        SetValuedMap<WikipediaLanguage, Misspelling> oldItems = (SetValuedMap<WikipediaLanguage, Misspelling>) evt.getOldValue();
        SetValuedMap<WikipediaLanguage, Misspelling> newItems = (SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue();
        this.processRemovedItems(oldItems, newItems);
    }

    private void processRemovedItems(
        SetValuedMap<WikipediaLanguage, Misspelling> oldItems,
        SetValuedMap<WikipediaLanguage, Misspelling> newItems
    ) {
        // Find the misspellings removed from the list to remove them from the database
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            Set<Misspelling> oldMisspellings = new HashSet<>(oldItems.get(lang));
            Set<Misspelling> newMisspellings = new HashSet<>(newItems.get(lang));
            oldMisspellings.removeAll(newMisspellings);
            if (!oldMisspellings.isEmpty()) {
                // The kind of the misspellings can be different even if the source is the same
                Set<String> oldWords = oldMisspellings
                    .stream()
                    .map(Misspelling::getWord)
                    .collect(Collectors.toUnmodifiableSet());
                LOGGER.warn("Deleting from database obsolete misspellings: {} - {}", lang, oldWords);
                Collection<ReplacementType> types = oldMisspellings
                    .stream()
                    .map(Misspelling::getReplacementType)
                    .collect(Collectors.toUnmodifiableSet());
                removeObsoleteReplacementType.removeObsoleteReplacementTypes(lang, types);
            }
        }
    }
}
