package es.bvalero.replacer.finder.immutable;

import com.jcabi.aspects.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.listing.*;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
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
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
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
public class UppercaseAfterFinder implements ImmutableFinder, PropertyChangeListener {

    @org.intellij.lang.annotations.RegExp
    private static final String CELL_SEPARATOR = "\\|\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String FIRST_CELL_SEPARATOR = "\n\\|";

    @org.intellij.lang.annotations.RegExp
    private static final String CAPTION_SEPARATOR = "\\|\\+";

    // Escaping is necessary for automaton
    private static final String CELL_HTML_TAG = "\\<td\\>";

    @org.intellij.lang.annotations.RegExp
    private static final String CLASS_PUNCTUATION = "[=#*.!]";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_AFTER_PUNCTUATION = String.format(
        "(%s|%s|%s|%s|%s)<Zs>*(%%s)",
        CLASS_PUNCTUATION,
        FIRST_CELL_SEPARATOR,
        CELL_SEPARATOR,
        CAPTION_SEPARATOR,
        CELL_HTML_TAG
    );

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private Map<WikipediaLanguage, RunAutomaton> uppercaseAfterAutomata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        SetValuedMap<WikipediaLanguage, String> uppercaseWords = getUppercaseWords(
            (SetValuedMap<WikipediaLanguage, SimpleMisspelling>) evt.getNewValue()
        );
        this.uppercaseAfterAutomata = buildUppercaseAfterAutomata(uppercaseWords);
    }

    @VisibleForTesting
    public SetValuedMap<WikipediaLanguage, String> getUppercaseWords(
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> misspellings
    ) {
        SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
        for (WikipediaLanguage lang : misspellings.keySet()) {
            map.putAll(lang, getUppercaseWords(misspellings.get(lang)));
        }
        return map;
    }

    /**
     * Find the misspellings which start with uppercase and are case-sensitive
     */
    private Set<String> getUppercaseWords(Set<SimpleMisspelling> misspellings) {
        return misspellings
            .stream()
            .filter(this::isUppercaseMisspelling)
            .map(SimpleMisspelling::getWord)
            .collect(Collectors.toSet());
    }

    private boolean isUppercaseMisspelling(SimpleMisspelling misspelling) {
        String word = misspelling.getWord();
        // Any of the suggestions is the misspelling word in lowercase
        return (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            misspelling
                .getSuggestions()
                .stream()
                .map(MisspellingSuggestion::getText)
                .anyMatch(text -> text.equals(FinderUtils.toLowerCase(word)))
        );
    }

    @Loggable(value = Loggable.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildUppercaseAfterAutomata(
        SetValuedMap<WikipediaLanguage, String> uppercaseWords
    ) {
        Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : uppercaseWords.keySet()) {
            map.put(lang, buildUppercaseAfterAutomaton(uppercaseWords.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildUppercaseAfterAutomaton(@Nullable Set<String> words) {
        // There are hundreds of only uppercase words so the best approach is a simple alternation
        if (words != null && !words.isEmpty()) {
            String alternations = String.format(REGEX_UPPERCASE_AFTER_PUNCTUATION, StringUtils.join(words, "|"));
            return new RunAutomaton(new RegExp(alternations).toAutomaton(new DatatypesAutomatonProvider()));
        } else {
            return null;
        }
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
        // Find the first uppercase letter
        String text = match.group();
        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                String word = text.substring(i).trim();
                int startPos = match.start() + i;
                return Immutable.of(startPos, word);
            }
        }
        throw new IllegalArgumentException("Wrong match with no uppercase letter");
    }

    @Override
    public boolean validate(MatchResult match, String text) {
        String word = match.group().substring(1).trim();
        int startPos = match.start() + match.group().indexOf(word);
        return FinderUtils.isWordCompleteInText(startPos, word, text);
    }
}
