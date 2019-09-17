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

    RunAutomaton getAutomaton() {
        return AUTOMATON_WORD;
    }

}
