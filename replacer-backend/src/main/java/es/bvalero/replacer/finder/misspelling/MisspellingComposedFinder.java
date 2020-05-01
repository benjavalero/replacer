package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import java.util.Set;
import java.util.stream.Collectors;
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

    private RunAutomaton automaton;

    @Override
    MisspellingManager getMisspellingManager() {
        return composedManager;
    }

    @Override
    void processMisspellingChange(Set<Misspelling> misspellings) {
        String alternations = String.format(
            "(%s)",
            StringUtils.join(misspellings.stream().map(Misspelling::getWord).collect(Collectors.toList()), "|")
        );
        this.automaton = new RunAutomaton(new RegExp(alternations).toAutomaton());
    }

    @Override
    RunAutomaton getAutomaton() {
        return this.automaton;
    }

    @Override
    String getType() {
        return TYPE_MISSPELLING_COMPOSED;
    }
}
