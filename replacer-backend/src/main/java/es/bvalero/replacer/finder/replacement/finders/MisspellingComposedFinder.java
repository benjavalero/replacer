package es.bvalero.replacer.finder.replacement.finders;

import com.github.rozidan.springboot.logger.Loggable;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Setter;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Component
public class MisspellingComposedFinder extends MisspellingFinder implements PropertyChangeListener {

    @Setter(onMethod_ = @TestOnly)
    @Autowired
    private ComposedMisspellingLoader composedMisspellingLoader;

    private Map<WikipediaLanguage, RunAutomaton> automata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        composedMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.buildMisspellingMaps((SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue());
        this.automata =
            buildComposedMisspellingAutomata((SetValuedMap<WikipediaLanguage, ComposedMisspelling>) evt.getNewValue());
    }

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildComposedMisspellingAutomata(
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> misspellings
    ) {
        final Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            final String alternations = String.format(
                "(%s)",
                StringUtils.join(
                    misspellings.get(lang).stream().map(this::processComposedMisspelling).collect(Collectors.toList()),
                    "|"
                )
            );
            map.put(lang, new RunAutomaton(new RegExp(alternations).toAutomaton()));
        }
        return map;
    }

    private String processComposedMisspelling(ComposedMisspelling misspelling) {
        final String words = misspelling.getWord();
        return misspelling.isCaseSensitive() ? words : FinderUtils.setFirstUpperCaseClass(words);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // With more than 400 composed misspellings
        // the best approach is an automaton of oll the terms alternated
        final RunAutomaton automaton = this.automata.get(page.getId().getLang());
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            return AutomatonMatchFinder.find(page.getContent(), automaton);
        }
    }

    @Override
    ReplacementKind getType() {
        return ReplacementKind.COMPOSED;
    }
}
