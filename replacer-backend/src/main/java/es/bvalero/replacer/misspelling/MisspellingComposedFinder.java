package es.bvalero.replacer.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
class MisspellingComposedFinder extends MisspellingFinder {

    @Autowired
    private MisspellingComposedManager composedManager;

    private RunAutomaton automaton;

    @Override
    MisspellingManager getMisspellingManager() {
        return composedManager;
    }

    @Override
    void processMisspellingChange(Set<Misspelling> misspellings) {
        String alternations = String.format("(%s)",
                StringUtils.join(misspellings.stream().map(Misspelling::getWord).collect(Collectors.toList()), "|"));
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    RunAutomaton getAutomaton() {
        return this.automaton;
    }

}
