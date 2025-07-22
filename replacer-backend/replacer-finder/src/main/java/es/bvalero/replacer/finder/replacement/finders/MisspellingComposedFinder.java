package es.bvalero.replacer.finder.replacement.finders;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Slf4j
@Component
public class MisspellingComposedFinder extends MisspellingFinder implements PropertyChangeListener {

    // Dependency injection
    private final ComposedMisspellingLoader composedMisspellingLoader;

    private Map<WikipediaLanguage, AhoCorasickMap<String>> stringMaps = new EnumMap<>(WikipediaLanguage.class);

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

    private Map<WikipediaLanguage, AhoCorasickMap<String>> buildComposedMisspellingStringMap(
        SetValuedMap<WikipediaLanguage, ComposedMisspelling> misspellings
    ) {
        LOGGER.debug("START Building Composed Misspelling string map…");
        final Map<WikipediaLanguage, AhoCorasickMap<String>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            final Set<String> misspellingTerms = misspellings
                .get(lang)
                .stream()
                .flatMap(cm -> cm.getTerms().stream())
                .collect(Collectors.toSet());
            map.put(lang, new AhoCorasickMap<>(misspellingTerms, misspellingTerms, true));
        }
        LOGGER.debug("END Building Composed Misspelling string map");
        return map;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // There are hundreds of composed misspellings.
        // The best approach is to loop over all the misspellings and find them in the text.
        // Nevertheless, the Aho-Corasick algorithm can be even better.
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
