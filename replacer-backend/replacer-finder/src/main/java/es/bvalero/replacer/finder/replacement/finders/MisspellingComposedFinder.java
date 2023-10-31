package es.bvalero.replacer.finder.replacement.finders;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
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

    private Map<WikipediaLanguage, RunAutomaton> automata = new EnumMap<>(WikipediaLanguage.class);

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
        this.automata =
            buildComposedMisspellingAutomata((SetValuedMap<WikipediaLanguage, ComposedMisspelling>) evt.getNewValue());
    }

    private Map<WikipediaLanguage, RunAutomaton> buildComposedMisspellingAutomata(
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> misspellings
    ) {
        final Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            final Set<ComposedMisspelling> langMisspellings = misspellings.get(lang);
            final Set<String> processedMisspellings = new HashSet<>();
            for (ComposedMisspelling cm : langMisspellings) {
                final String word = toRegex(cm.getWord());
                if (cm.isCaseSensitive()) {
                    processedMisspellings.add(word);
                } else {
                    processedMisspellings.add(FinderUtils.setFirstUpperCase(word));
                    processedMisspellings.add(FinderUtils.setFirstLowerCase(word));
                }
            }
            final String alternations = FinderUtils.joinAlternate(processedMisspellings);
            map.put(lang, new RunAutomaton(new RegExp(alternations).toAutomaton()));
        }
        return map;
    }

    private String toRegex(String word) {
        return word.replace("[", "\\[").replace("]", "\\]").replace(".", "\\.");
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are hundreds of composed misspellings
        // The best approach is an automaton of oll the terms alternated with big difference against the linear approach
        // The Aho-Corasick can be even better, but it doesn't manage well some composed cases with non-word characters.
        final RunAutomaton automaton = this.automata.get(page.getPageKey().getLang());
        return automaton == null ? List.of() : AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match.start(), match.group(), page.getContent());
    }

    @Override
    ReplacementKind getType() {
        return ReplacementKind.COMPOSED;
    }
}
