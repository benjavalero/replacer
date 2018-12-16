package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaFacade;
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
import java.util.Collections;
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
    private IWikipediaFacade wikipediaFacade;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    // Set of misspellings found in the misspelling list
    private Set<Misspelling> misspellings = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

    static Set<Misspelling> parseMisspellingListText(String misspellingListText) {
        Set<Misspelling> misspellingSet = new HashSet<>(MISSPELLING_ESTIMATED_COUNT);

        Stream<String> stream = new BufferedReader(new StringReader(misspellingListText)).lines();
        stream.forEach(strLine -> {
            Misspelling misspelling = parseMisspellingLine(strLine);
            // Add the misspelling and check if it is duplicated
            if (misspelling != null && !misspellingSet.add(misspelling)) {
                LOGGER.warn("Duplicated misspelling term: {}", misspelling.getWord());
            }
        });

        return misspellingSet;
    }

    private static Misspelling parseMisspellingLine(String misspellingLine) {
        Misspelling misspelling = null;

        String[] tokens = misspellingLine.split("\\|");
        // Ignore the lines not corresponding to misspelling lines
        if (!misspellingLine.isEmpty() && misspellingLine.startsWith(" ")) {
            if (tokens.length == 3) {
                misspelling = Misspelling.builder()
                        .setWord(tokens[0].trim())
                        .setCaseSensitive(CASE_SENSITIVE_VALUE.equalsIgnoreCase(tokens[1].trim()))
                        .setComment(tokens[2].trim())
                        .build();
            } else {
                LOGGER.warn("Bad formatted misspelling line: {}", misspellingLine);
            }
        }

        return misspelling;
    }

    /**
     * @return If the first letter of the word is uppercase
     */
    public static boolean startsWithUpperCase(CharSequence word) {
        return Character.isUpperCase(word.charAt(0));
    }

    public synchronized Set<Misspelling> getMisspellings() {
        if (misspellings.isEmpty()) { // For the first time
            setMisspellings(findWikipediaMisspellings());
        }
        return misspellings;
    }

    public synchronized void setMisspellings(Set<Misspelling> misspellings) {
        changeSupport.firePropertyChange("name", this.misspellings, misspellings);
        this.misspellings = misspellings;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update daily the list of misspellings from Wikipedia.
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000, initialDelay = 3600 * 24 * 1000)
    void updateMisspellings() {
        Set<Misspelling> newMisspellings = findWikipediaMisspellings();
        if (!newMisspellings.isEmpty()) {
            setMisspellings(newMisspellings);
        }
    }

    Set<Misspelling> findWikipediaMisspellings() {
        try {
            LOGGER.info("Start loading misspelling list from Wikipedia...");
            String misspellingListText = wikipediaFacade.getArticleContent(WikipediaFacade.MISSPELLING_LIST_ARTICLE);
            Set<Misspelling> misspellingSet = parseMisspellingListText(misspellingListText);
            LOGGER.info("End parsing misspelling list from Wikipedia: {} items", misspellingSet.size());
            return misspellingSet;
        } catch (WikipediaException e) {
            LOGGER.error("Error loading misspellings list from Wikipedia");
            return Collections.emptySet();
        }
    }

}
