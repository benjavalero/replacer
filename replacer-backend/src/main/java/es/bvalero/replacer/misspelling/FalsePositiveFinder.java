package es.bvalero.replacer.misspelling;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.IgnoredReplacementFinder;
import es.bvalero.replacer.finder.MatchResult;
import es.bvalero.replacer.finder.ReplacementFinder;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Find misspelling replacements in a given text.
 * Based in the WordAlternateAutomatonFinder winner in the benchmarks.
 */
@Component
public class FalsePositiveFinder extends ReplacementFinder implements IgnoredReplacementFinder, PropertyChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(FalsePositiveFinder.class);

    @Autowired
    private FalsePositiveManager falsePositiveManager;

    private Set<String> falsePositives = new HashSet<>();

    private RunAutomaton falsePositivesAutomaton;

    @TestOnly
    public Set<String> getFalsePositives() {
        return falsePositives;
    }

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
        LOGGER.info("Build false positive automaton");
        String alternations = String.format("(%s)", StringUtils.join(falsePositives, "|"));
        RunAutomaton automaton = new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        LOGGER.info("Built false positive automaton");
        return automaton;
    }

    @Override
    public List<MatchResult> findIgnoredReplacements(String text) {
        List<MatchResult> matches = new ArrayList<>();
        AutomatonMatcher m = this.falsePositivesAutomaton.newMatcher(text);
        while (m.find()) {
            if (isWordCompleteInText(m.start(), m.group(), text)) {
                matches.add(new MatchResult(m.start(), m.group()));
            }
        }
        return matches;
    }

}
