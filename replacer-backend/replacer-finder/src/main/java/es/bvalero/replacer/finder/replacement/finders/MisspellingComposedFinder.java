package es.bvalero.replacer.finder.replacement.finders;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Component
public class MisspellingComposedFinder extends MisspellingFinder implements PropertyChangeListener {

    // Dependency injection
    private final ComposedMisspellingLoader composedMisspellingLoader;

    private Map<WikipediaLanguage, LongestMatchMap<String>> stringMaps = new EnumMap<>(WikipediaLanguage.class);

    public MisspellingComposedFinder(ComposedMisspellingLoader composedMisspellingLoader) {
        this.composedMisspellingLoader = composedMisspellingLoader;
    }

    @PostConstruct
    public void init() {
        composedMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        buildMisspellingMaps((SetValuedMap<WikipediaLanguage, StandardMisspelling>) evt.getNewValue());
        this.stringMaps = buildComposedMisspellingStringMap(
            (SetValuedMap<WikipediaLanguage, ComposedMisspelling>) evt.getNewValue()
        );
    }

    private Map<WikipediaLanguage, LongestMatchMap<String>> buildComposedMisspellingStringMap(
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> misspellings
    ) {
        final Map<WikipediaLanguage, LongestMatchMap<String>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            final Set<ComposedMisspelling> langMisspellings = misspellings.get(lang);
            final Set<String> processedMisspellings = new HashSet<>();
            for (ComposedMisspelling cm : langMisspellings) {
                final String word = cm.getWord();
                if (cm.isCaseSensitive()) {
                    processedMisspellings.add(word);
                } else {
                    processedMisspellings.add(FinderUtils.setFirstUpperCase(word));
                    processedMisspellings.add(FinderUtils.setFirstLowerCase(word));
                }
            }
            map.put(lang, new LongestMatchMap<>(processedMisspellings, processedMisspellings, true));
        }
        return map;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are hundreds of composed misspellings
        // The best approach is an automaton of oll the terms alternated with big difference against the linear approach
        // The Aho-Corasick algorithm can be even better, but it doesn't manage well some composed cases with non-word characters.
        // Finally, we use Aho-Corasick "longest" algorithm not to capture overlapping results,
        // which gives a similar performance to the automaton approach but with less memory consumption.
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
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    public ReplacementKind getType() {
        return ReplacementKind.COMPOSED;
    }
}
