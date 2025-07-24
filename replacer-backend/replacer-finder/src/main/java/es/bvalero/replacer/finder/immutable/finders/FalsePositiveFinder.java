package es.bvalero.replacer.finder.immutable.finders;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.FalsePositive;
import es.bvalero.replacer.finder.listing.load.FalsePositiveLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.stereotype.Component;

/**
 * Find known expressions which are (almost) always false positives,
 * e.g. in Spanish `aun así` which hides the potential replacement `aun`
 */
@Slf4j
@Component
class FalsePositiveFinder implements ImmutableFinder, PropertyChangeListener {

    // Dependency injection
    private final FalsePositiveLoader falsePositiveLoader;

    private Map<WikipediaLanguage, AhoCorasickMap<String>> stringMaps = new EnumMap<>(WikipediaLanguage.class);

    FalsePositiveFinder(FalsePositiveLoader falsePositiveLoader) {
        this.falsePositiveLoader = falsePositiveLoader;
    }

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
        this.stringMaps = buildFalsePositiveStringMap(falsePositives);
    }

    private Map<WikipediaLanguage, AhoCorasickMap<String>> buildFalsePositiveStringMap(
        SetValuedMap<WikipediaLanguage, FalsePositive> falsePositives
    ) {
        LOGGER.debug("START Building False Positive string map…");
        final Map<WikipediaLanguage, AhoCorasickMap<String>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : falsePositives.keySet()) {
            final Set<String> falsePositiveExpressions = falsePositives
                .get(lang)
                .stream()
                .map(FalsePositive::getExpression)
                .flatMap(exp -> FinderUtils.expandRegex(exp).stream())
                .collect(Collectors.toSet());
            map.put(lang, new AhoCorasickMap<>(falsePositiveExpressions, falsePositiveExpressions, true));
        }
        LOGGER.debug("END Building False Positive string map");
        return map;
    }

    @Override
    public FinderPriority getPriority() {
        // It should be High for number of matches but it is SO slow that it is better to be the last one
        return FinderPriority.NONE;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are hundreds of false positives.
        // The best approach is an automaton with all the expressions alternated along with some variants of the Aho-Corasick algorithm.
        // Nevertheless, most of them, including the best approach according to benchmarks, capture non-complete overlapping matches.
        // For instance, in "ratones aún son", the false positive "es aún" is matched but not valid,
        // as it is not complete, and it makes the next one "aún son" not to be matched.
        // Therefore, we choose to use the Aho-Corasick algorithm, even if it is slower, to capture complete overlapping matches.
        // To allow simple regular expressions, we "expand" them first.
        final StringMap<String> stringMap = this.stringMaps.get(page.getPageKey().getLang());
        if (stringMap == null) {
            return List.of();
        }
        final ResultMatchListener listener = new ResultMatchListener();
        stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        // Benchmarks show similar performance with and without validation
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }
}
