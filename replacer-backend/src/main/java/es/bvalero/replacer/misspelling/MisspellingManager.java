package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
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
import java.util.stream.Stream;

/**
 * Manages the cache for the misspelling list in order to reduce the calls to Wikipedia.
 */
@Service
public class MisspellingManager {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingManager.class);
    private static final String CASE_SENSITIVE_VALUE = "cs";
    private static final int MISSPELLING_ESTIMATED_COUNT = 20000;

    @Autowired
    private WikipediaService wikipediaService;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    // Set of misspellings found in the misspelling list
    private Set<Misspelling> misspellings = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

    private void setMisspellings(Set<Misspelling> misspellings) {
        changeSupport.firePropertyChange("name", this.misspellings, misspellings);
        this.misspellings = misspellings;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     * It's executed immediately after the tool deployment.
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        LOGGER.info("Scheduled misspellings update...");
        try {
            setMisspellings(findWikipediaMisspellings());
        } catch (WikipediaException e) {
            LOGGER.error("Error loading misspellings list from Wikipedia", e);
        }
    }

    // We make this method public to be used by the word benchmark
    public Set<Misspelling> findWikipediaMisspellings() throws WikipediaException {
        LOGGER.info("Start loading misspelling list from Wikipedia...");
        String misspellingListText = wikipediaService.getMisspellingListPageContent();
        Set<Misspelling> misspellingSet = parseMisspellingListText(misspellingListText);
        LOGGER.info("End parsing misspelling list from Wikipedia: {} items", misspellingSet.size());
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
                    LOGGER.warn("Duplicated misspelling term: {}", misspelling.getWord());
                }
            }
        });

        return misspellingSet;
    }

    private Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        if (tokens.length == 3) {
            misspelling = Misspelling.builder()
                    .setWord(tokens[0].trim())
                    .setCaseSensitive(CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim()))
                    .setComment(tokens[2].trim())
                    .build();
        } else {
            LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
        }

        return misspelling;
    }

}
