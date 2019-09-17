package es.bvalero.replacer.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
class ComposedFinder extends MisspellingFinder {

    @Override
    RunAutomaton getAutomaton() {
        String alternations = String.format("(%s)", StringUtils.join(getMisspellingMap().keySet(), "|"));
        return new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

}
