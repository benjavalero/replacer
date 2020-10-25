package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.ReplacerException;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Slf4j
@Service
public class MisspellingManager extends ParseFileManager<Misspelling> {
    private static final String CASE_SENSITIVE_VALUE = "cs";

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementDao replacementDao;

    @Override
    void processRemovedItems(
        SetValuedMap<WikipediaLanguage, Misspelling> oldItems,
        SetValuedMap<WikipediaLanguage, Misspelling> newItems
    ) {
        // Find the misspellings removed from the list to remove them from the database
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            Set<String> oldWords = oldItems.get(lang).stream().map(Misspelling::getWord).collect(Collectors.toSet());
            Set<String> newWords = newItems.get(lang).stream().map(Misspelling::getWord).collect(Collectors.toSet());
            oldWords.removeAll(newWords);
            if (!oldWords.isEmpty()) {
                LOGGER.warn("Deleting from database obsolete misspellings: {}", oldWords);
                replacementDao.deleteByLangAndTypeAndSubtypeInAndReviewerIsNull(
                    lang,
                    MisspellingSimpleFinder.TYPE_MISSPELLING_SIMPLE,
                    new HashSet<>(oldWords)
                );
            }
        }
    }

    @Override
    String getLabel() {
        return "Misspelling";
    }

    @Override
    String findItemsText(WikipediaLanguage lang) {
        String text = FinderUtils.STRING_EMPTY;
        try {
            text = wikipediaService.getMisspellingListPageContent(lang);
        } catch (ReplacerException e) {
            LOGGER.error("Error updating {} {} set", getLabel(), lang, e);
        }
        return text;
    }

    @Override
    Misspelling parseItemLine(String misspellingLine) {
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
    String getItemKey(Misspelling misspelling) {
        return misspelling.getWord();
    }
}
