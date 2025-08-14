package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.RemovedTypeEvent;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ObsoleteMisspellingListener implements PropertyChangeListener {

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;
    private final ComposedMisspellingLoader composedMisspellingLoader;
    private final ApplicationEventPublisher applicationEventPublisher;

    ObsoleteMisspellingListener(
        SimpleMisspellingLoader simpleMisspellingLoader,
        ComposedMisspellingLoader composedMisspellingLoader,
        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.simpleMisspellingLoader = simpleMisspellingLoader;
        this.composedMisspellingLoader = composedMisspellingLoader;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
        composedMisspellingLoader.addPropertyChangeListener(this);
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
        getObsoleteMisspellings(oldItems, newItems)
            .entries()
            .forEach(obsolete ->
                applicationEventPublisher.publishEvent(RemovedTypeEvent.of(obsolete.getKey(), obsolete.getValue()))
            );
    }

    @VisibleForTesting
    SetValuedMap<WikipediaLanguage, StandardType> getObsoleteMisspellings(
        SetValuedMap<WikipediaLanguage, StandardMisspelling> oldItems,
        SetValuedMap<WikipediaLanguage, StandardMisspelling> newItems
    ) {
        SetValuedMap<WikipediaLanguage, StandardType> types = new HashSetValuedHashMap<>();
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
                    .forEach(type -> types.put(lang, type));
            }
        }
        return types;
    }
}
