package es.bvalero.replacer.replacement;

import es.bvalero.replacer.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ObsoleteMisspellingListener implements PropertyChangeListener {

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @Autowired
    private ReplacementService replacementService;

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
                ReplacementType misspellingType = oldItems
                    .get(lang)
                    .stream()
                    .findAny()
                    .map(ReplacementType::ofMisspellingType)
                    .orElseThrow(IllegalArgumentException::new);
                LOGGER.warn(
                    "Deleting from database obsolete misspellings: {} - {} - {}",
                    lang,
                    misspellingType,
                    oldWords
                );
                replacementService.deleteToBeReviewedBySubtype(lang, misspellingType.getLabel(), oldWords);
            }
        }
    }
}
