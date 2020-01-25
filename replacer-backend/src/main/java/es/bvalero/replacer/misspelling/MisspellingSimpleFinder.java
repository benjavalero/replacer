package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find misspellings with only word, e. g. `habia` in Spanish
 */
@Component
class MisspellingSimpleFinder extends MisspellingFinder {
    private static final RunAutomaton AUTOMATON_WORD = new RunAutomaton(
        new RegExp("(<L>|[-'])+").toAutomaton(new DatatypesAutomatonProvider())
    );
    private static final String TYPE_MISSPELLING_SIMPLE = "Ortografía";

    @Autowired
    private MisspellingManager misspellingManager;

    @Override
    MisspellingManager getMisspellingManager() {
        return misspellingManager;
    }

    @Override
    void processMisspellingChange(Set<Misspelling> misspellings) {
        // Do nothing
    }

    @Override
    RunAutomaton getAutomaton() {
        return AUTOMATON_WORD;
    }

    @Override
    String getType() {
        return TYPE_MISSPELLING_SIMPLE;
    }
}
