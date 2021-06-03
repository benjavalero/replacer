package es.bvalero.replacer.finder.replacement;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.Misspelling;
import es.bvalero.replacer.finder.listing.MisspellingComposedManager;
import es.bvalero.replacer.finder.listing.MisspellingManager;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Component
class MisspellingComposedFinder extends MisspellingFinder {

    @Setter(AccessLevel.PACKAGE) // For testing
    @Autowired
    private MisspellingComposedManager composedManager;

    private Map<WikipediaLanguage, RunAutomaton> automata = new EnumMap<>(WikipediaLanguage.class);

    @Override
    MisspellingManager getMisspellingManager() {
        return composedManager;
    }

    @Override
    void processMisspellingChange(SetValuedMap<WikipediaLanguage, Misspelling> misspellings) {
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            String alternations = String.format(
                "(%s)",
                StringUtils.join(
                    misspellings.get(lang).stream().map(this::processComposedMisspelling).collect(Collectors.toList()),
                    "|"
                )
            );
            map.put(lang, new RunAutomaton(new RegExp(alternations).toAutomaton()));
        }
        this.automata = map;
    }

    private String processComposedMisspelling(Misspelling misspelling) {
        String words = misspelling.getWord();
        return misspelling.isCaseSensitive() ? words : FinderUtils.setFirstUpperCaseClass(words);
    }

    @Override
    public Iterable<Replacement> find(FinderPage page) {
        RunAutomaton automaton = this.automata.get(page.getLang());
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            // We need to perform additional transformations according to the language
            return StreamSupport
                .stream(AutomatonMatchFinder.find(page.getContent(), automaton).spliterator(), false)
                .filter(match -> this.validate(match, page.getContent()))
                .map(this::convert)
                .filter(r -> isExistingWord(r.getText(), page.getLang()))
                .map(r -> r.withSubtype(getSubtype(r.getText(), page.getLang())))
                .map(r -> r.withSuggestions(findSuggestions(r.getText(), page.getLang())))
                .collect(Collectors.toList());
        }
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    @Override
    String getType() {
        return ReplacementType.MISSPELLING_COMPOSED;
    }
}
