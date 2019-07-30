package es.bvalero.replacer.misspelling;

import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
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
 * Manages the cache for the false positive list in order to reduce the calls to Wikipedia.
 */
@Slf4j
@Service
public class FalsePositiveManager {

    private static final int FALSE_POSITIVE_ESTIMATED_COUNT = 1000;

    @Autowired
    private WikipediaService wikipediaService;

    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    // Set of misspellings found in the misspelling list
    private Set<String> falsePositives = new HashSet<>(FALSE_POSITIVE_ESTIMATED_COUNT);

    private void setFalsePositives(Set<String> falsePositives) {
        changeSupport.firePropertyChange("name", this.falsePositives, falsePositives);
        this.falsePositives = falsePositives;
    }

    void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Update daily the list of false positives from Wikipedia.
     * It's executed immediately after the tool deployment.
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000)
    public void updateFalsePositives() {
        LOGGER.info("EXECUTE Scheduled daily update of false positives");
        try {
            setFalsePositives(findWikipediaFalsePositives());
        } catch (WikipediaException e) {
            LOGGER.error("Error updating false positive list", e);
        }
    }

    private Set<String> findWikipediaFalsePositives() throws WikipediaException {
        LOGGER.info("START Load false positives from Wikipedia");
        String falsePositiveListText = wikipediaService.getFalsePositiveListPageContent();
        Set<String> falsePositiveSet = parseFalsePositiveListText(falsePositiveListText);
        LOGGER.info("END Load false positives from Wikipedia. Size: {}", falsePositiveSet.size());
        return falsePositiveSet;
    }

    private Set<String> parseFalsePositiveListText(String falsePositivesListText) {
        Set<String> falsePositivesList = new HashSet<>(FALSE_POSITIVE_ESTIMATED_COUNT);

        Stream<String> stream = new BufferedReader(new StringReader(falsePositivesListText)).lines();
        stream.forEach(strLine -> {
            if (strLine.startsWith(" ")) {
                String trim = strLine.trim();
                // Skip empty and commented lines
                if (!org.springframework.util.StringUtils.isEmpty(trim) && !trim.startsWith("#")) {
                    falsePositivesList.add(trim);
                }
            }
        });
        return falsePositivesList;
    }

}
