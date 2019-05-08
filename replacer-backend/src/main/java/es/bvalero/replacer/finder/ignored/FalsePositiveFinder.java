package es.bvalero.replacer.finder.ignored;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class FalsePositiveFinder extends ReplacementFinder implements IgnoredReplacementFinder {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);

    @Autowired
    private WikipediaService wikipediaService;

    private RunAutomaton falsePositivesAutomaton;

    /**
     * Update daily the list of false positives
     */
    @Scheduled(fixedDelay = 3600 * 24 * 1000)
    void updateFalsePositives() {
        LOGGER.info("Scheduled false positive update...");
        try {
            this.falsePositivesAutomaton = buildFalsePositivesAutomaton();
        } catch (WikipediaException e) {
            LOGGER.error("Error loading false positive list from Wikipedia", e);
        }
    }

    private RunAutomaton buildFalsePositivesAutomaton() throws WikipediaException {
        List<String> falsePositivesList = loadFalsePositives();
        String alternations = StringUtils.collectionToDelimitedString(falsePositivesList, "|");
        RegExp r = new RegExp(alternations);
        return new RunAutomaton(r.toAutomaton(new DatatypesAutomatonProvider()));
    }

    List<String> loadFalsePositives() throws WikipediaException {
        LOGGER.info("Start loading false positive list from Wikipedia...");
        String falsePositivesListText = wikipediaService.getFalsePositiveListPageContent();
        List<String> falsePositivesList = parseFalsePositivesListText(falsePositivesListText);
        LOGGER.info("End parsing false positive list from Wikipedia: {} items", falsePositivesList.size());
        return falsePositivesList;
    }

    private List<String> parseFalsePositivesListText(String falsePositivesListText) {
        List<String> falsePositivesList = new ArrayList<>(1000);

        Stream<String> stream = new BufferedReader(new StringReader(falsePositivesListText)).lines();
        stream.forEach(strLine -> {
            if (strLine.startsWith(" ")) {
                String trim = strLine.trim();
                // Skip empty and commented lines
                if (!StringUtils.isEmpty(trim) && !trim.startsWith("#")) {
                    falsePositivesList.add(trim);
                }
            }
        });
        return falsePositivesList;
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        return findReplacements(text, falsePositivesAutomaton, ReplacementType.IGNORED);
    }

}
