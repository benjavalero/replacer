package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.IgnoredReplacement;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Component
public class UppercaseAfterFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = "[!#*|=.]<Z>*(%s)";

    @Autowired
    private MisspellingManager misspellingManager;

    // Misspellings which start with uppercase and are case-sensitive
    @Getter
    private List<String> uppercaseWords = new ArrayList<>();

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private RunAutomaton uppercaseAfterAutomaton;

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.uppercaseAfterAutomaton = buildUppercaseAfterAutomaton((Set<Misspelling>) evt.getNewValue());
    }

    private RunAutomaton buildUppercaseAfterAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("START Build uppercase-after automaton");

        // Load the misspellings
        misspellings.forEach(misspelling -> {
            String word = misspelling.getWord();
            if (misspelling.isCaseSensitive()
                    && FinderUtils.startsWithUpperCase(word)
                    && misspelling.getSuggestions().size() == 1
                    && misspelling.getSuggestions().get(0).getText().equalsIgnoreCase(word)) {
                this.uppercaseWords.add(word);
            }
        });

        String regexAlternations = String.format(REGEX_UPPERCASE_AFTER_PUNCTUATION, StringUtils.join(this.uppercaseWords, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider()));

        LOGGER.info("END Build uppercase-after automaton");
        return automaton;
    }

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, this.uppercaseAfterAutomaton);
    }

    @Override
    public IgnoredReplacement convertMatch(int start, String text) {
        String word = text.substring(1).trim();
        int startPos = start + text.indexOf(word);
        return IgnoredReplacement.of(startPos, word);
    }

}
