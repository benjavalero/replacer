package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.article.ArticleService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Service
public class MisspellingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);
    private static final String CASE_SENSITIVE_VALUE = "cs";
    private static final int MISSPELLING_ESTIMATED_COUNT = 20000;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ArticleService articleService;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    // Set of misspellings found in the misspelling list
    private Set<Misspelling> misspellings = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

    @TestOnly
    void setMisspellings(Set<Misspelling> misspellings) {
        changeSupport.firePropertyChange("name", this.misspellings, misspellings);

        // Find the misspellings removed from the list to remove them from the database
        Set<String> oldWords = this.misspellings.stream().map(Misspelling::getWord).collect(Collectors.toSet());
        Set<String> newWords = misspellings.stream().map(Misspelling::getWord).collect(Collectors.toSet());
        oldWords.removeAll(newWords);
        if (!oldWords.isEmpty()) {
            LOGGER.warn("Deleting from database obsolete misspellings: {}", oldWords);
            articleService.deleteReplacementsByTextIn(oldWords);
        }

        this.misspellings = misspellings;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     * It's executed immediately after the tool deployment.
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000)
    public void updateMisspellings() {
        LOGGER.info("Execute scheduled daily update of misspellings");
        try {
            setMisspellings(findWikipediaMisspellings());
        } catch (WikipediaException e) {
            LOGGER.error("Error updating misspelling list", e);
        }
    }

    private Set<Misspelling> findWikipediaMisspellings() throws WikipediaException {
        LOGGER.info("Start loading misspellings from Wikipedia");
        String misspellingListText = wikipediaService.getMisspellingListPageContent();
        Set<Misspelling> misspellingSet = parseMisspellingListText(misspellingListText);
        LOGGER.info("Finish loading misspellings from Wikipedia: {} items", misspellingSet.size());
        return misspellingSet;
    }

    Set<Misspelling> parseMisspellingListText(String misspellingListText) {
        Set<Misspelling> misspellingSet = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

        // We maintain a temporary set of words to find soft duplicates (only the word)
        Set<String> words = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

        Stream<String> stream = new BufferedReader(new StringReader(misspellingListText)).lines();
        // Ignore the lines not corresponding to misspelling lines
        stream.filter(line -> line.startsWith(" ") && StringUtils.isNotBlank(line)).forEach(strLine -> {
            Misspelling misspelling = parseMisspellingLine(strLine);
            if (misspelling != null) {
                if (words.add(misspelling.getWord())) {
                    misspellingSet.add(misspelling);
                } else {
                    LOGGER.warn("Duplicated misspelling: {}", misspelling.getWord());
                }
            }
        });

        return misspellingSet;
    }

    private @Nullable Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling;

        String[] tokens = misspellingLine.split("\\|");
        if (tokens.length == 3) {
            String word = tokens[0].trim();
            String comment = tokens[2].trim();
            misspelling = Misspelling.builder()
                    .setWord(word)
                    .setCaseSensitive(CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim()))
                    .setComment(comment)
                    .build();
        } else {
            LOGGER.warn("Bad formatted misspelling: {}", misspellingLine);
            return null;
        }

        return misspelling;
    }

}
