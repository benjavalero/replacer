package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
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
public class UppercaseAfterFinder extends ReplacementFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(UppercaseAfterFinder.class);

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = "[!#*|=.]<Z>*(%s)";

    @Autowired
    private MisspellingManager misspellingManager;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private RunAutomaton uppercaseAfterAutomaton;

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
        this.uppercaseAfterAutomaton = buildUppercaseAfterAutomaton(newMisspellings);
    }

    private RunAutomaton buildUppercaseAfterAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("Start building uppercaseAfter automaton...");

        // Load the misspellings
        List<String> alternations = new ArrayList<>();
        misspellings.forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()
                    && startsWithUpperCase(word)
                    && misspelling.getSuggestions().size() == 1
                    && misspelling.getSuggestions().get(0).equals(word.toLowerCase())) {
                alternations.add(word);
            }
        });

        String regexAlternations = String.format(REGEX_UPPERCASE_AFTER_PUNCTUATION, StringUtils.join(alternations, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("End building uppercaseAfter automaton");
        return automaton;
    }

    @Override
    public List<ArticleReplacement> findIgnoredReplacements(String text) {
        List<ArticleReplacement> matches = new ArrayList<>(100);

        AutomatonMatcher m = this.uppercaseAfterAutomaton.newMatcher(text);
        while (m.find()) {
            String word = m.group().substring(1).trim();
            int start = m.start() + m.group().indexOf(word);
            matches.add(ArticleReplacement.builder()
                    .setStart(start)
                    .setText(word)
                    .setType(ReplacementType.IGNORED)
                    .build());
        }

        return matches;
    }

}
