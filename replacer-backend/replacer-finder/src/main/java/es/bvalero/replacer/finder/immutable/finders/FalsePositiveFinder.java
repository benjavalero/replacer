package es.bvalero.replacer.finder.immutable.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
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
        final SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives = (SetValuedMap<
                WikipediaLanguage,
                FalsePositive
            >) evt.getNewValue();
        this.falsePositivesAutomata = buildFalsePositivesAutomata(falsePositives);
    }

    private Map<WikipediaLanguage, RunAutomaton> buildFalsePositivesAutomata(
        SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives
    ) {
        final Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : falsePositives.keySet()) {
            map.put(lang, buildFalsePositivesAutomaton(falsePositives.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildFalsePositivesAutomaton(@Nullable Set<FalsePositive> falsePositives) {
        // There are hundreds of false positives. The best approach is, as usual, an automaton.
        // In this case there are two options: (a) all the expressions alternated or (b) the Aho-Corasick algorithm.
        // Option (a) is fast, and allows to capture regular expressions. Nevertheless, it captures non-complete
        // overlapping matches. For instance, in "ratones aún son", the false positive "es aún" is matched but not valid,
        // as it is not complete, and it makes the next one "aún son" not to be matched.
        // On the other hand, option (b) is even 3x faster than (a), and fixes the just mentioned overlapping issue,
        // but it doesn't allow regular expressions.
        // For the moment, we stay with option (a).
        if (falsePositives == null || falsePositives.isEmpty()) {
            return null;
        }
        final List<String> falsePositiveExpressions = falsePositives
            .stream()
            .map(FalsePositive::getExpression)
            .toList();
        final String alternations = FinderUtils.joinAlternate(falsePositiveExpressions);
        return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches but it is SO slow that it is better to be the last one
        return FinderPriority.NONE;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final RunAutomaton automaton = this.falsePositivesAutomata.get(page.getPageKey().getLang());
        return automaton == null ? List.of() : AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        // Benchmarks show similar performance with and without validation
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
