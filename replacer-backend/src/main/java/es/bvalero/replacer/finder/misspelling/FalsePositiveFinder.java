package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find known expressions which are (almost) always false positives,
 * e. g. in Spanish `aun así` which hides the potential replacement `aun`
 */
// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Component
public class FalsePositiveFinder implements ImmutableFinder, PropertyChangeListener {
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
        String alternations = String.format(
            "(%s)",
            StringUtils.join(falsePositives.stream().map(this::processFalsePositive).collect(Collectors.toList()), "|")
        );
        RunAutomaton automaton = new RunAutomaton(
            new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider())
        );
        LOGGER.info("END Build false positive automaton");
        return automaton;
    }

    private String processFalsePositive(String falsePositive) {
        return FinderUtils.startsWithLowerCase(falsePositive)
            ? FinderUtils.setFirstUpperCaseClass(falsePositive)
            : falsePositive;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new RegexIterable<>(text, this.falsePositivesAutomaton, this::convert, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
