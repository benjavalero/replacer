package es.bvalero.replacer.finder.listing;

import com.jcabi.aspects.Loggable;
import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.listing.find.ListingFinder;
import es.bvalero.replacer.finder.replacement.ReplacementType;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Slf4j
@Service
public class MisspellingManager extends ListingManager<Misspelling> {

    public static final String PROPERTY_UPPERCASE_WORDS = "uppercaseWords";
    private static final String CASE_SENSITIVE_VALUE = "cs";

    @Setter // For testing
    @Autowired
    protected ListingFinder listingFinder;

    @Autowired
    private ReplacementService replacementService;

    // Derived from the misspelling set to access faster by word
    private Map<WikipediaLanguage, Map<String, Misspelling>> misspellingMap = new EnumMap<>(WikipediaLanguage.class);

    private SetValuedMap<WikipediaLanguage, String> uppercaseWords = new HashSetValuedHashMap<>();

    public Map<String, Misspelling> getMisspellingMap(WikipediaLanguage lang) {
        return this.misspellingMap.get(lang);
    }

    @VisibleForTesting
    public Set<String> getUppercaseWords(WikipediaLanguage lang) {
        return this.uppercaseWords.get(lang);
    }

    @Override
    public void setItems(SetValuedMap<WikipediaLanguage, Misspelling> newItems) {
        SetValuedMap<WikipediaLanguage, Misspelling> oldItems = super.getItems();
        super.setItems(newItems);
        this.processRemovedItems(oldItems, newItems);

        this.misspellingMap = buildMisspellingMaps(newItems);

        SetValuedMap<WikipediaLanguage, String> newUppercaseWords = getUppercaseWords((newItems));
        changeSupport.firePropertyChange(PROPERTY_UPPERCASE_WORDS, this.uppercaseWords, newUppercaseWords);
        this.uppercaseWords = newUppercaseWords;
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
                LOGGER.warn("Deleting from database obsolete misspellings: {} - {}", lang, oldWords);
                replacementService.deleteToBeReviewedBySubtype(lang, getType(), new HashSet<>(oldWords));
            }
        }
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, Map<String, Misspelling>> buildMisspellingMaps(
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings
    ) {
        // Build a map to quick access the misspellings by word
        Map<WikipediaLanguage, Map<String, Misspelling>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildMisspellingMap(misspellings.get(lang)));
        }
        return map;
    }

    private Map<String, Misspelling> buildMisspellingMap(Set<Misspelling> misspellings) {
        // Build a map to quick access the misspellings by word
        Map<String, Misspelling> map = new HashMap<>(misspellings.size());
        misspellings.forEach(
            misspelling -> {
                String word = misspelling.getWord();
                if (misspelling.isCaseSensitive()) {
                    map.put(word, misspelling);
                } else {
                    // If case-insensitive, we add to the map "word" and "Word".
                    map.put(FinderUtils.setFirstLowerCase(word), misspelling);
                    map.put(FinderUtils.setFirstUpperCase(word), misspelling);
                }
            }
        );
        return map;
    }

    private SetValuedMap<WikipediaLanguage, String> getUppercaseWords(
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings
    ) {
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.putAll(lang, getUppercaseWords(misspellings.get(lang)));
        }
        return map;
    }

    /**
     * Find the misspellings which start with uppercase and are case-sensitive
     */
    private Set<String> getUppercaseWords(Set<Misspelling> misspellings) {
        return misspellings
            .stream()
            .filter(this::isUppercaseMisspelling)
            .map(Misspelling::getWord)
            .collect(Collectors.toSet());
    }

    private boolean isUppercaseMisspelling(Misspelling misspelling) {
        String word = misspelling.getWord();
        // Any of the suggestions is the misspelling word in lowercase
        return (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            misspelling
                .getSuggestions()
                .stream()
                .map(Suggestion::getText)
                .anyMatch(text -> text.equals(FinderUtils.toLowerCase(word)))
        );
    }

    @Override
    protected String getLabel() {
        return "Misspelling";
    }

    String getType() {
        return ReplacementType.MISSPELLING_SIMPLE;
    }

    @Override
    protected String findItemsTextInWikipedia(WikipediaLanguage lang) throws ReplacerException {
        return listingFinder.getSimpleMisspellingListing(lang);
    }

    @Override
    @Nullable
    protected Misspelling parseItemLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        if (tokens.length == 3) {
            String word = tokens[0].trim();
            boolean cs = CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim());
            String comment = tokens[2].trim();
            try {
                misspelling = Misspelling.of(getType(), word, cs, comment);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Ignore not valid misspelling: " + e.getMessage());
            }
        } else {
            LOGGER.warn("Bad formatted misspelling: {}", misspellingLine);
        }

        return misspelling;
    }

    @Override
    protected String getItemKey(Misspelling misspelling) {
        return misspelling.getWord();
    }
}
