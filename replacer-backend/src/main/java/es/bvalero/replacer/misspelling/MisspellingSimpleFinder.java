package es.bvalero.replacer.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Find misspelling replacements in a given text.
 * Based in the WordAutomatonAllFinder winner in the benchmarks.
 */
@Slf4j
@Component
class MisspellingSimpleFinder extends MisspellingFinder {

    private static final RunAutomaton AUTOMATON_WORD = new RunAutomaton(new RegExp("(<L>|[-'])+")
            .toAutomaton(new DatatypesAutomatonProvider()));
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
    public String getType() {
        return TYPE_MISSPELLING_SIMPLE;
    }

}
