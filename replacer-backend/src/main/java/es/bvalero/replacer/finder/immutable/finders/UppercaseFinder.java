package es.bvalero.replacer.finder.immutable.finders;

import com.github.rozidan.springboot.logger.Loggable;
import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.listing.MisspellingSuggestion;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find words in uppercase which are correct according to the punctuation,
 * e.g. `Enero` in `{{Cite|date=Enero de 2020}}`
 *
 * The considered punctuations are:
 * - After dot
 * - Parameter values
 * - Unordered and ordered list items
 * - After an HTML tag like a reference or a table cell
 * - Wiki-table cells
 * - Starting a paragraph
 * - Starting a header
 */
@Component
public class UppercaseFinder implements ImmutableFinder, PropertyChangeListener {

    @org.intellij.lang.annotations.RegExp
    private static final String CAPTION_SEPARATOR = "\\|\\+";

    private static final String TIMELINE_TEXT = "text:";

    private static final String PARAGRAPH_START = "\n\n";

    // The pipe is not only used for tables cells, we must check is not a wiki-link!!!
    @org.intellij.lang.annotations.RegExp
    private static final String CLASS_PUNCTUATION = "[=#*>.!|]";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_UPPERCASE_PUNCTUATION = String.format(
        "(%s|%s|%s|%s)<Zs>*(\\[\\[)?(%%s)(]])?",
        CLASS_PUNCTUATION,
        CAPTION_SEPARATOR,
        TIMELINE_TEXT,
        PARAGRAPH_START
    );

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    // Regex with the misspellings which start with uppercase and are case-sensitive
    // and starting with a special character which justifies the uppercase
    private Map<WikipediaLanguage, RunAutomaton> uppercaseAutomata = new EnumMap<>(WikipediaLanguage.class);

    @Getter
    private SetValuedMap<WikipediaLanguage, String> uppercaseMap = new HashSetValuedHashMap<>();

    @PostConstruct
    public void init() {
        simpleMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.uppercaseMap = getUppercaseWords((SetValuedMap<WikipediaLanguage, SimpleMisspelling>) evt.getNewValue());
        this.uppercaseAutomata = buildUppercaseAutomata(this.uppercaseMap);
    }

    @VisibleForTesting
    public SetValuedMap<WikipediaLanguage, String> getUppercaseWords(
        SetValuedMap<WikipediaLanguage, SimpleMisspelling> misspellings
    ) {
        final SetValuedMap<WikipediaLanguage, String> map = new HashSetValuedHashMap<>();
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
        final String word = misspelling.getWord();
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

    @Loggable(value = LogLevel.DEBUG, skipArgs = true, skipResult = true)
    private Map<WikipediaLanguage, RunAutomaton> buildUppercaseAutomata(
        SetValuedMap<WikipediaLanguage, String> uppercaseWords
    ) {
        final Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : uppercaseWords.keySet()) {
            map.put(lang, buildUppercaseAutomaton(uppercaseWords.get(lang)));
        }
        return map;
    }

    @Nullable
    private RunAutomaton buildUppercaseAutomaton(@Nullable Set<String> words) {
        // Currently, there are about 60 uppercase case-sensitive misspellings,
        // so the best approach is an automaton of oll the terms alternated.
        if (words != null && !words.isEmpty()) {
            final String alternations = String.format(REGEX_UPPERCASE_PUNCTUATION, StringUtils.join(words, "|"));
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
        final RunAutomaton automaton = this.uppercaseAutomata.get(page.getLang());
        if (automaton == null) {
            return Collections.emptyList();
        } else {
            // Benchmarks show similar performance with and without validation
            return AutomatonMatchFinder.find(page.getContent(), automaton);
        }
    }

    @Override
    public Immutable convert(MatchResult match) {
        return findUppercaseWord(match);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final Immutable uppercaseWord = findUppercaseWord(match);
        return (
            FinderUtils.isWordCompleteInText(uppercaseWord.getStart(), uppercaseWord.getText(), page.getContent()) &&
            validatePipe(page.getContent(), uppercaseWord.getStart())
        );
    }

    private Immutable findUppercaseWord(MatchResult match) {
        final String text = match.group();
        final int posUppercase = findFirstUppercase(text);
        if (posUppercase < 0) {
            throw new IllegalArgumentException("Wrong match with no uppercase letter: " + text);
        }

        String word = text.substring(posUppercase).trim();
        if (word.endsWith("]]")) {
            word = word.substring(0, word.length() - 2);
        }
        final int startPos = match.start() + posUppercase;
        return Immutable.of(startPos, word);
    }

    private int findFirstUppercase(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isUpperCase(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean validatePipe(String text, int matchPosition) {
        if (findPreviousNonSpaceChar(text, matchPosition) == '|') {
            return findFirstLineChar(text, matchPosition) == '|';
        }
        return true;
    }

    private char findPreviousNonSpaceChar(String text, int start) {
        for (int i = start - 1; i >= 0; i--) {
            final char ch = text.charAt(i);
            if (!Character.isSpaceChar(ch)) {
                return ch;
            }
        }
        return text.charAt(0);
    }

    private char findFirstLineChar(String text, int start) {
        for (int i = start - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                return text.charAt(i + 1);
            }
        }
        return text.charAt(0);
    }
}
