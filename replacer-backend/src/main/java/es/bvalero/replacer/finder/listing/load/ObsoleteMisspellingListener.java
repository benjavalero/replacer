package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class ObsoleteMisspellingListener implements PropertyChangeListener {

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

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
            Set<String> oldWords = oldItems.get(lang).stream().map(Misspelling::getWord).collect(Collectors.toSet());
            Set<String> newWords = newItems.get(lang).stream().map(Misspelling::getWord).collect(Collectors.toSet());
            oldWords.removeAll(newWords);
            if (!oldWords.isEmpty()) {
                ReplacementKind misspellingType = oldItems
                    .get(lang)
                    .stream()
                    .findAny()
                    .map(ReplacementKind::ofMisspellingType)
                    .orElseThrow(IllegalArgumentException::new);
                LOGGER.warn(
                    "Deleting from database obsolete misspellings: {} - {} - {}",
                    lang,
                    misspellingType,
                    oldWords
                );
                processObsoleteReplacementTypes(lang, misspellingType, oldWords);
            }
        }
    }

    protected abstract void processObsoleteReplacementTypes(
        WikipediaLanguage lang,
        ReplacementKind type,
        Collection<String> subtypes
    );
}
