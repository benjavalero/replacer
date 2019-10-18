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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Find misspelling replacements in a given text.
 * Based in the WordAlternateAutomatonFinder winner in the benchmarks.
 */
// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Component
public class FalsePositiveFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    @Getter
    private Set<String> falsePositives = new HashSet<>();

    private RunAutomaton falsePositivesAutomaton;

    @PostConstruct
    public void init() {
        falsePositiveManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.falsePositives = (Set<String>) evt.getNewValue();
        this.falsePositivesAutomaton = buildFalsePositivesAutomaton(this.falsePositives);
    }

    private RunAutomaton buildFalsePositivesAutomaton(Set<String> falsePositives) {
        LOGGER.info("START Build false positive automaton");
        String alternations = String.format("(%s)", StringUtils.join(falsePositives, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        LOGGER.info("END Build false positive automaton");
        return automaton;
    }

    @Override
    public List<IgnoredReplacement> findIgnoredReplacements(String text) {
        return findMatchResults(text, this.falsePositivesAutomaton);
    }

    @Override
    public boolean isValidMatch(int start, String matchedText, String fullText) {
        return FinderUtils.isWordCompleteInText(start, matchedText, fullText);
    }

}
