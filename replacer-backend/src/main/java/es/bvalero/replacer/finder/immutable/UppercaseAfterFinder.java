package es.bvalero.replacer.finder.immutable;

import com.jcabi.aspects.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.replacement.Misspelling;
import es.bvalero.replacer.finder.replacement.MisspellingManager;
import es.bvalero.replacer.finder.replacement.Suggestion;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
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
 *
 * The considered punctuations are: `!`, `#`, `*`, `|`, `=` and `.`
 */
@Component
class UppercaseAfterFinder implements ImmutableFinder, PropertyChangeListener {

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
        // There are hundreds of only uppercase words so the best approach is a simple alternation
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
    Set<String> getUppercaseWords(Set<Misspelling> misspellings) {
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
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        RunAutomaton automaton = this.uppercaseAfterAutomata.get(page.getLang());
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            // Benchmarks show similar performance with and without validation
            return AutomatonMatchFinder.find(page.getContent(), automaton);
        }
    }

    @Override
    public Immutable convert(MatchResult match) {
        String word = match.group().substring(1).trim();
        int startPos = match.start() + match.group().indexOf(word);
        return Immutable.of(startPos, word);
    }

    @Override
    public boolean validate(MatchResult match, String text) {
        String word = match.group().substring(1).trim();
        int startPos = match.start() + match.group().indexOf(word);
        return FinderUtils.isWordCompleteInText(startPos, word, text);
    }
}
