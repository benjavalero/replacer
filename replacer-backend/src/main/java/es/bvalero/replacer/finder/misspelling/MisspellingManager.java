package es.bvalero.replacer.finder.misspelling;

import es.bvalero.replacer.replacement.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
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
    private ReplacementRepository replacementRepository;

    @Override
    void processRemovedItems(Set<Misspelling> oldItems, Set<Misspelling> newItems) {
        // Find the misspellings removed from the list to remove them from the database
        Set<String> oldWords = oldItems.stream().map(Misspelling::getWord).collect(Collectors.toSet());
        Set<String> newWords = newItems.stream().map(Misspelling::getWord).collect(Collectors.toSet());
        oldWords.removeAll(newWords);
        if (!oldWords.isEmpty()) {
            LOGGER.warn("Deleting from database obsolete misspellings: {}", oldWords);
            replacementRepository.deleteBySubtypeIn(new HashSet<>(oldWords));
        }
    }

    @Override
    String getLabel() {
        return "Misspelling";
    }

    @Override
    String findItemsText() throws WikipediaException {
        return wikipediaService.getMisspellingListPageContent();
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
