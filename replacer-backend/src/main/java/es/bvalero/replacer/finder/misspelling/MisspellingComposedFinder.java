package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with more than one word, e.g. `aún así` in Spanish
 */
@Component
public class MisspellingComposedFinder extends MisspellingFinder {
    private static final String TYPE_MISSPELLING_COMPOSED = "Compuestos";

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
                    misspellings.get(lang).stream().map(Misspelling::getWord).collect(Collectors.toList()),
                    "|"
                )
            );
            map.put(lang, new RunAutomaton(new RegExp(alternations).toAutomaton()));
        }
        this.automata = map;
    }

    @Override
    RunAutomaton getAutomaton(WikipediaLanguage lang) {
        return this.automata.get(lang);
    }

    @Override
    String getType() {
        return TYPE_MISSPELLING_COMPOSED;
    }
}
