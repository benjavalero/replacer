package es.bvalero.replacer.finder;

import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;

import java.util.Collections;
import java.util.List;

class CustomReplacementFinder implements ReplacementFinder {

    private String replacement;
    private String suggestion;

    CustomReplacementFinder(String replacement, String suggestion) {
        this.replacement = replacement;
        this.suggestion = suggestion;
    }

    @Override
    public List<Replacement> findReplacements(String text) {
        RunAutomaton customAutomaton = buildCustomRegex(this.replacement, this.suggestion);
        return findMatchResults(text, customAutomaton);
    }

    private RunAutomaton buildCustomRegex(String replacement, String suggestion) {
        String regex = FinderUtils.startsWithLowerCase(replacement) && FinderUtils.startsWithLowerCase(suggestion)
                ? FinderUtils.setFirstUpperCaseClass(replacement)
                : replacement;
        return new RunAutomaton(new RegExp(regex).toAutomaton());
    }

    @Override
    public String getType() {
        return ReplacementFinderService.CUSTOM_FINDER_TYPE;
    }

    @Override
    public String getSubtype(String text) {
        return this.replacement;
    }

    @Override
    public List<ReplacementSuggestion> findSuggestions(String text) {
        return Collections.singletonList(ReplacementSuggestion.ofNoComment(
                getNewSuggestion(text, this.replacement, this.suggestion)));
    }

    private String getNewSuggestion(String text, String replacement, String suggestion) {
        if (!text.equalsIgnoreCase(replacement)) {
            throw new IllegalArgumentException(String.format("Text found and replacement don't match: %s - %s",
                    text, replacement));
        } else if (text.equals(replacement)) {
            return suggestion;
        } else {
            return FinderUtils.setFirstUpperCase(suggestion);
        }
    }

}
