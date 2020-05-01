package es.bvalero.replacer.finder.misspelling;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find words in uppercase which are correct according to the punctuation,
 * e.g. `Enero` in `{{Cite|date=Enero de 2020}}`
 */
// We make this implementation public to be used by the finder benchmarks
@Slf4j
@Component
public class UppercaseAfterFinder implements ImmutableFinder, PropertyChangeListener {
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = "[!#*|=.]<Z>*(%s)";

    @Autowired
    private MisspellingManager misspellingManager;

    // Misspellings which start with uppercase and are case-sensitive
    @Getter
    private List<String> uppercaseWords = new ArrayList<>();

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private RunAutomaton uppercaseAfterAutomaton;

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.uppercaseAfterAutomaton = buildUppercaseAfterAutomaton((Set<Misspelling>) evt.getNewValue());
    }

    private RunAutomaton buildUppercaseAfterAutomaton(Set<Misspelling> misspellings) {
        LOGGER.info("START Build uppercase-after automaton");

        // Load the misspellings
        misspellings
            .stream()
            .filter(this::isUppercaseMisspelling)
            .map(Misspelling::getWord)
            .forEach(word -> this.uppercaseWords.add(word));

        String regexAlternations = String.format(
            REGEX_UPPERCASE_AFTER_PUNCTUATION,
            StringUtils.join(this.uppercaseWords, "|")
        );
        RunAutomaton automaton = new RunAutomaton(
            new RegExp(regexAlternations).toAutomaton(new DatatypesAutomatonProvider())
        );

        LOGGER.info("END Build uppercase-after automaton");
        return automaton;
    }

    private boolean isUppercaseMisspelling(Misspelling misspelling) {
        String word = misspelling.getWord();
        // Any of the suggestions is the misspelling word in lowercase
        return (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            misspelling
                .getSuggestions()
                .stream()
                .map(Suggestion::getText)
                .anyMatch(text -> text.equals(FinderUtils.toLowerCase(word)))
        );
    }

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new RegexIterable<>(text, this.uppercaseAfterAutomaton, this::convert);
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        String word = text.substring(1).trim();
        int startPos = match.start() + text.indexOf(word);
        return Immutable.of(startPos, word, this);
    }
}
