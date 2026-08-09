package es.bvalero.replacer.finder.replacement.finders;

import com.roklenarcic.util.strings.AhoCorasickMap;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.ReplacementKind;
import es.bvalero.replacer.finder.listing.ComposedMisspelling;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

    // volatile: reference replaced atomically on reload; guarantees visibility across threads
    private volatile Map<WikipediaLanguage, AhoCorasickMap<String>> stringMaps = new EnumMap<>(WikipediaLanguage.class);

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
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        // There are thousands of composed misspellings, many containing non-word characters (spaces, hyphens, etc.).
        // This prevents using the faster Aho-Corasick "whole word" variant, which only works with pure word patterns.
        // The standard Aho-Corasick algorithm gives the best performance here.
        final StringMap<String> stringMap = this.stringMaps.get(page.getPageKey().getLang());
        if (stringMap == null) {
            LOGGER.warn("No composed misspelling map for lang {} (still loading)", page.getPageKey().getLang());
            return Stream.of();
        }
        final ResultMatchListener listener = new ResultMatchListener();
        stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return FinderUtils.isWordCompleteInText(match, page.getContent());
    }

    @Override
    public ReplacementKind getType() {
        return ReplacementKind.COMPOSED;
    }
}
