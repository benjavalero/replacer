package es.bvalero.replacer.finder.misspelling;

import com.jcabi.aspects.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find words in uppercase which are correct according to the punctuation,
 * e.g. `Enero` in `{{Cite|date=Enero de 2020}}`
 */
@Component
public class UppercaseAfterFinder implements ImmutableFinder, PropertyChangeListener {
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = "[!#*|=.]<Zs>*(%s)";

    @Autowired
    private MisspellingManager misspellingManager;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private Map<WikipediaLanguage, RunAutomaton> uppercaseAfterAutomata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        misspellingManager.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings = (SetValuedMap<WikipediaLanguage, Misspelling>) evt.getNewValue();
        this.uppercaseAfterAutomata = buildUppercaseAfterAutomata(misspellings);
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildUppercaseAfterAutomata(
        SetValuedMap<WikipediaLanguage, Misspelling> misspellings
    ) {
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.put(lang, buildUppercaseAfterAutomaton(misspellings.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildUppercaseAfterAutomaton(@Nullable Set<Misspelling> misspellings) {
        if (misspellings != null) {
            Set<String> words = getUppercaseWords(misspellings);

            if (!words.isEmpty()) {
                String alternations = String.format(REGEX_UPPERCASE_AFTER_PUNCTUATION, StringUtils.join(words, "|"));
                return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
            }
        }
        return null;
    }

    /**
     * Find the misspellings which start with uppercase and are case-sensitive
     */
    public Set<String> getUppercaseWords(Set<Misspelling> misspellings) {
        return misspellings
            .stream()
            .filter(this::isUppercaseMisspelling)
            .map(Misspelling::getWord)
            .collect(Collectors.toSet());
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
        RunAutomaton automaton = this.uppercaseAfterAutomata.get(lang);
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            return new RegexIterable<>(text, automaton, this::convert);
        }
    }

    @Override
    public Immutable convert(MatchResult match) {
        String text = match.group();
        String word = text.substring(1).trim();
        int startPos = match.start() + text.indexOf(word);
        return Immutable.of(startPos, word, this);
    }
}
