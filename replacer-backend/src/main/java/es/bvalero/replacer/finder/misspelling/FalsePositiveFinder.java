package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find known expressions which are (almost) always false positives,
 * e.g. in Spanish `aun así` which hides the potential replacement `aun`
 */
@Slf4j
@Component
public class FalsePositiveFinder implements ImmutableFinder, PropertyChangeListener {
    @Autowired
    private FalsePositiveManager falsePositiveManager;

    private Map<WikipediaLanguage, RunAutomaton> falsePositivesAutomata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        falsePositiveManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        SetValuedMap<WikipediaLanguage, String> falsePositives = (SetValuedMap<WikipediaLanguage, String>) evt.getNewValue();
        this.falsePositivesAutomata = buildFalsePositivesAutomata(falsePositives);
    }

    private Map<WikipediaLanguage, RunAutomaton> buildFalsePositivesAutomata(
        SetValuedMap<WikipediaLanguage, String> falsePositives
    ) {
        LOGGER.info("START Build false positive automata");
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : falsePositives.keySet()) {
            map.put(lang, buildFalsePositivesAutomaton(falsePositives.get(lang)));
        }
        LOGGER.info("END Build false positive automata");
        return map;
    }

    private RunAutomaton buildFalsePositivesAutomaton(Set<String> falsePositives) {
        if (falsePositives != null && !falsePositives.isEmpty()) {
            String alternations = String.format(
                "(%s)",
                StringUtils.join(
                    falsePositives.stream().map(this::processFalsePositive).collect(Collectors.toList()),
                    "|"
                )
            );
            return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        } else {
            return null;
        }
    }

    private String processFalsePositive(String falsePositive) {
        return FinderUtils.startsWithLowerCase(falsePositive)
            ? FinderUtils.setFirstUpperCaseClass(falsePositive)
            : falsePositive;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        RunAutomaton automaton = this.falsePositivesAutomata.get(lang);
        return automaton == null
            ? Collections.emptyList()
            : new RegexIterable<>(text, automaton, this::convert, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult match, String text) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), text);
    }
}
