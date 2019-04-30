package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.ReplacementFinder;
import es.bvalero.replacer.persistence.ReplacementType;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ProperNounFinder extends ReplacementFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ProperNounFinder.class);

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_IN_LINK = "\\[\\[(%s)\\|";

    @Autowired
    private MisspellingManager misspellingManager;

    // Regex with the misspellings which start with uppercase in links, e. g. [[Hispano|hispanidad]]
    private RunAutomaton uppercaseLinkAutomaton;

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        @SuppressWarnings("unchecked")
        Set<Misspelling> newMisspellings = (Set<Misspelling>) evt.getNewValue();
        buildMisspellingRelatedFields(newMisspellings);
    }

    void buildMisspellingRelatedFields(Set<Misspelling> newMisspellings) {
        this.uppercaseLinkAutomaton = buildUppercaseLinkAutomaton(newMisspellings);
    }

    private RunAutomaton buildUppercaseLinkAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("Start building uppercaseLink automaton...");

        // Build an automaton with the misspellings starting with uppercase
        List<String> alternations = new ArrayList<>(misspellings.size());
        for (Misspelling misspelling : misspellings) {
            if (misspelling.isCaseSensitive() && startsWithUpperCase(misspelling.getWord())) {
                alternations.add(misspelling.getWord());
            }
        }
        String regexAlternations = String.format(REGEX_UPPERCASE_IN_LINK, StringUtils.join(alternations, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("End building uppercaseLink automaton");
        return automaton;
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        // Lowercase nouns that start with uppercase because in the first part of links
        // We don't need the extra letters captured for the separator
        for (ArticleReplacement match : findReplacements(text, this.uppercaseLinkAutomaton, ReplacementType.IGNORED)) {
            matches.add(match
                    .withStart(match.getStart() + 2)
                    .withText(match.getText().substring(2, match.getText().length() - 1)));
        }

        return matches;
    }

}
