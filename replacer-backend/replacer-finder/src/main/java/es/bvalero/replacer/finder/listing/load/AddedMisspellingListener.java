package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.AddedTypeEvent;
import es.bvalero.replacer.finder.ChangedReplacementType;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class AddedMisspellingListener implements PropertyChangeListener {

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;
    private final ComposedMisspellingLoader composedMisspellingLoader;
    private final ApplicationEventPublisher applicationEventPublisher;

    AddedMisspellingListener(
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
        getAddedMisspellings(oldItems, newItems).forEach(added ->
            applicationEventPublisher.publishEvent(AddedTypeEvent.of(added))
        );
    }

    @VisibleForTesting
    Collection<ChangedReplacementType> getAddedMisspellings(
        SetValuedMap<WikipediaLanguage, StandardMisspelling> oldItems,
        SetValuedMap<WikipediaLanguage, StandardMisspelling> newItems
    ) {
        if (oldItems.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChangedReplacementType> types = new ArrayList<>();
        // Find the misspellings added to the list to index them
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            Set<String> oldWords = oldItems
                .get(lang)
                .stream()
                .map(StandardMisspelling::getWord)
                .collect(Collectors.toUnmodifiableSet());
            Set<String> newWords = newItems
                .get(lang)
                .stream()
                .map(StandardMisspelling::getWord)
                .collect(Collectors.toSet());
            newWords.removeAll(oldWords);
            if (!newWords.isEmpty()) {
                ReplacementKind misspellingType = newItems
                    .get(lang)
                    .stream()
                    .findAny()
                    .map(StandardMisspelling::getReplacementKind)
                    .orElseThrow(IllegalArgumentException::new);
                LOGGER.warn(
                    "Adding to database added misspellings (by indexing): {} - {} - {}",
                    lang,
                    misspellingType,
                    newWords
                );
                newWords
                    .stream()
                    .map(word -> StandardType.of(misspellingType, word))
                    .forEach(type -> types.add(ChangedReplacementType.of(lang, type)));
            }
        }
        return types;
    }
}
