package es.bvalero.replacer.finder.immutable.finders;

import com.github.rozidan.springboot.logger.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
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
        final SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives = (SetValuedMap<WikipediaLanguage, FalsePositive>) evt.getNewValue();
        this.falsePositivesAutomata = buildFalsePositivesAutomata(falsePositives);
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
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
        // Currently, there are about 300 false positives so the best approach is an automaton with all expressions alternated.
        // It gives the best performance with big difference, but it is not perfect though.
        // As we check later if the match is a complete word, we could match an incomplete word
        // that overlaps with the following word which is actually a good match.
        // For instance, in "ratones aún son", the false positive "es aún" is matched but not valid,
        // and it makes that the next one "aún son" is not matched.
        if (falsePositives != null && !falsePositives.isEmpty()) {
            final List<String> falsePositiveExpressions = falsePositives
                .stream()
                .map(FalsePositive::getExpression)
                .collect(Collectors.toUnmodifiableList());
            final String alternations = FinderUtils.joinAlternate(falsePositiveExpressions);
            return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        } else {
            return null;
        }
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches but it is SO slow that it is better to be the last one
        return FinderPriority.NONE;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        final RunAutomaton automaton = this.falsePositivesAutomata.get(page.getId().getLang());
        return automaton == null ? Collections.emptyList() : AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        // Benchmarks show similar performance with and without validation
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
