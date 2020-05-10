package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.apache.commons.collections4.SetValuedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with only word, e.g. `habia` in Spanish
 */
@Component
public class MisspellingSimpleFinder extends MisspellingFinder {
    private static final RunAutomaton AUTOMATON_WORD = new RunAutomaton(
        new RegExp("(<L>|[-'])+").toAutomaton(new DatatypesAutomatonProvider())
    );
    static final String TYPE_MISSPELLING_SIMPLE = "Ortografía";

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    MisspellingManager getMisspellingManager() {
        return misspellingManager;
    }

    @Override
    void processMisspellingChange(SetValuedMap<WikipediaLanguage, Misspelling> misspellings) {
        // Do nothing
    }

    @Override
    RunAutomaton getAutomaton(WikipediaLanguage lang) {
        return AUTOMATON_WORD;
    }

    @Override
    String getType() {
        return TYPE_MISSPELLING_SIMPLE;
    }
}
