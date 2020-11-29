package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.FinderUtils;
import es.bvalero.replacer.finder.RegexIterable;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Component
public class MisspellingComposedFinder extends MisspellingFinder {
    public static final String TYPE_MISSPELLING_COMPOSED = "Compuestos";

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
    public Iterable<Replacement> find(String text, WikipediaLanguage lang) {
        RunAutomaton automaton = this.automata.get(lang);
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            // We need to perform additional transformations according to the language
            return StreamSupport
                .stream(
                    new RegexIterable<>(text, automaton, this::convertMatch, this::isValidMatch).spliterator(),
                    false
                )
                .filter(r -> isExistingWord(r.getText(), lang))
                .map(r -> r.withSubtype(getSubtype(r.getText(), lang)))
                .map(r -> r.withSuggestions(findSuggestions(r.getText(), lang)))
                .collect(Collectors.toList());
        }
    }

    @Override
    String getType() {
        return TYPE_MISSPELLING_COMPOSED;
    }
}
