package es.bvalero.replacer.finder.misspelling;

import com.jcabi.aspects.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find known expressions which are (almost) always false positives,
 * e.g. in Spanish `aun así` which hides the potential replacement `aun`
 */
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

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildFalsePositivesAutomata(
        SetValuedMap<WikipediaLanguage, String> falsePositives
    ) {
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : falsePositives.keySet()) {
            map.put(lang, buildFalsePositivesAutomaton(falsePositives.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildFalsePositivesAutomaton(@Nullable Set<String> falsePositives) {
        // Currently there are about 300 false positives so the best approach is a simple alternation
        // It gives the best performance with big difference but it is not perfect though
        // As we check later if the match is a complete word, we could match an incomplete word
        // that overlaps with the following word which is actually a good match.
        // For instance, in "ratones aún son", the false positive "es aún" is matched but not valid,
        // and it makes that the next one "aún son" is not matched.
        if (falsePositives != null && !falsePositives.isEmpty()) {
            String alternations = String.format("(%s)", StringUtils.join(falsePositives, "|"));
            return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        } else {
            return null;
        }
    }

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.HIGH;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        RunAutomaton automaton = this.falsePositivesAutomata.get(page.getLang());
        return automaton == null
            ? Collections.emptyList()
            // Benchmarks show similar performance with and without validation
            : new RegexIterable<>(page, automaton, this::convert, this::isValidMatch);
    }

    private boolean isValidMatch(MatchResult match, IndexablePage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
