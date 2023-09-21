package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.ObsoleteReplacementType;
import es.bvalero.replacer.finder.ObsoleteReplacementTypeObservable;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ObsoleteMisspellingListener implements PropertyChangeListener, ObsoleteReplacementTypeObservable {

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
        composedMisspellingLoader.addPropertyChangeListener(this);
    }

    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        this.changeSupport.addPropertyChangeListener(listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        // This should work for both types of misspellings
        SetValuedMap<WikipediaLanguage, StandardMisspelling> oldItems = (SetValuedMap<
                WikipediaLanguage,
                StandardMisspelling
            >) evt.getOldValue();
        SetValuedMap<WikipediaLanguage, StandardMisspelling> newItems = (SetValuedMap<
                WikipediaLanguage,
                StandardMisspelling
            >) evt.getNewValue();
        this.changeSupport.firePropertyChange(
                "types",
                Collections.emptyList(),
                getObsoleteMisspellings(oldItems, newItems)
            );
    }

    @VisibleForTesting
    Collection<ObsoleteReplacementType> getObsoleteMisspellings(
        SetValuedMap<WikipediaLanguage, StandardMisspelling> oldItems,
        SetValuedMap<WikipediaLanguage, StandardMisspelling> newItems
    ) {
        List<ObsoleteReplacementType> types = new ArrayList<>();
        // Find the misspellings removed from the list to remove them from the database
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            Set<String> oldWords = oldItems
                .get(lang)
                .stream()
                .map(StandardMisspelling::getWord)
                .collect(Collectors.toSet());
            Set<String> newWords = newItems
                .get(lang)
                .stream()
                .map(StandardMisspelling::getWord)
                .collect(Collectors.toUnmodifiableSet());
            oldWords.removeAll(newWords);
            if (!oldWords.isEmpty()) {
                ReplacementKind misspellingType = oldItems
                    .get(lang)
                    .stream()
                    .findAny()
                    .map(StandardMisspelling::getReplacementKind)
                    .orElseThrow(IllegalArgumentException::new);
                LOGGER.warn(
                    "Deleting from database obsolete misspellings: {} - {} - {}",
                    lang,
                    misspellingType,
                    oldWords
                );
                oldWords
                    .stream()
                    .map(word -> StandardType.of(misspellingType, word))
                    .forEach(type -> types.add(ObsoleteReplacementType.of(lang, type)));
            }
        }
        return types;
    }
}
