package es.bvalero.replacer.finder.listing.load;

import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.replacement.type.ReplacementTypeSaveApi;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class ObsoleteMisspellingListener implements PropertyChangeListener {

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;
    private final ComposedMisspellingLoader composedMisspellingLoader;
    private final ReplacementTypeSaveApi replacementTypeSaveApi;

    ObsoleteMisspellingListener(
        SimpleMisspellingLoader simpleMisspellingLoader,
        ComposedMisspellingLoader composedMisspellingLoader,
        ReplacementTypeSaveApi replacementTypeSaveApi
    ) {
        this.simpleMisspellingLoader = simpleMisspellingLoader;
        this.composedMisspellingLoader = composedMisspellingLoader;
        this.replacementTypeSaveApi = replacementTypeSaveApi;
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
        getObsoleteMisspellings(oldItems, newItems).forEach(
            obsolete -> replacementTypeSaveApi.remove(obsolete.getLang(), obsolete.getType())
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
