package es.bvalero.replacer.finder.replacement;

import es.bvalero.replacer.common.ReplacerException;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.common.ParseFileManager;
import es.bvalero.replacer.replacement.ReplacementService;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Slf4j
@Service
public class MisspellingManager extends ParseFileManager<Misspelling> {

    // Public as it is used also for some immutables

    private static final String CASE_SENSITIVE_VALUE = "cs";

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementService replacementService;

    @Override
    public void setItems(SetValuedMap<WikipediaLanguage, Misspelling> newItems) {
        SetValuedMap<WikipediaLanguage, Misspelling> oldItems = super.getItems();
        super.setItems(newItems);
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
                LOGGER.warn("Deleting from database obsolete misspellings: {} - {}", lang, oldWords);
                replacementService.deleteToBeReviewedBySubtype(lang, getType(), new HashSet<>(oldWords));
            }
        }
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
        return wikipediaService.getMisspellingListPageContent(lang);
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
                misspelling = Misspelling.of(word, cs, comment);
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
