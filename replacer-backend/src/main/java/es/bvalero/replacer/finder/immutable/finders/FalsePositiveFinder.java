package es.bvalero.replacer.finder.immutable.finders;

import com.jcabi.aspects.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
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
class FalsePositiveFinder implements ImmutableFinder, PropertyChangeListener {

    @Autowired
    private FalsePositiveLoader falsePositiveLoader;

    private Map<WikipediaLanguage, RunAutomaton> falsePositivesAutomata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        falsePositiveLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives = (SetValuedMap<WikipediaLanguage, FalsePositive>) evt.getNewValue();
        this.falsePositivesAutomata = buildFalsePositivesAutomata(falsePositives);
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildFalsePositivesAutomata(
        SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives
    ) {
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : falsePositives.keySet()) {
            map.put(lang, buildFalsePositivesAutomaton(falsePositives.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildFalsePositivesAutomaton(@Nullable Set<FalsePositive> falsePositives) {
        // Currently, there are about 300 false positives so the best approach is an automaton with all expressions alternated.
        // It gives the best performance with big difference, but it is not perfect though.
        // As we check later if the match is a complete word, we could match an incomplete word
        // that overlaps with the following word which is actually a good match.
        // For instance, in "ratones aún son", the false positive "es aún" is matched but not valid,
        // and it makes that the next one "aún son" is not matched.
        if (falsePositives != null && !falsePositives.isEmpty()) {
            String alternations = String.format(
                "(%s)",
                StringUtils.join(
                    falsePositives.stream().map(FalsePositive::getExpression).collect(Collectors.toList()),
                    "|"
                )
            );
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
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        RunAutomaton automaton = this.falsePositivesAutomata.get(page.getLang());
        return automaton == null ? Collections.emptyList() : AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        // Benchmarks show similar performance with and without validation
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
