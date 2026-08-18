package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import dk.brics.automaton.AutomatonMatcher;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.MisspellingSuggestion;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find words in uppercase which are correct according to the punctuation,
 * e.g. <code>Enero</code> in <code>{{Cite|date=Enero de 2020}}</code>
 * <p />
 * The considered punctuations are:
 * <ul>
 *   <li>After dot</li>
 *   <li>Parameter values</li>
 *   <li>Unordered and ordered list items</li>
 *   <li>After an HTML tag like a reference or a table cell</li>
 *   <li>Wiki-table cells</li>
 *   <li>Starting a paragraph</li>
 *   <li>Starting a header</li>
 * </ul>
 */
@Slf4j
@Component
public class UppercaseFinder implements ImmutableFinder, PropertyChangeListener {

    private static final String CAPTION_SEPARATOR = "|+";
    private static final String PARAGRAPH_START = "\n\n";

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;
    private final ComposedMisspellingLoader composedMisspellingLoader;

    // volatile: references replaced atomically on reload; guarantees visibility across threads
    private volatile SetValuedMap<WikipediaLanguage, String> simpleUppercaseMap = new HashSetValuedHashMap<>();
    private volatile SetValuedMap<WikipediaLanguage, String> composedUppercaseMap = new HashSetValuedHashMap<>();
    // RunAutomaton with the misspellings which start with uppercase and are case-sensitive
    private volatile Map<WikipediaLanguage, RunAutomaton> uppercaseAutomata = new EnumMap<>(WikipediaLanguage.class);

    @Getter
    private volatile SetValuedMap<WikipediaLanguage, String> uppercaseMap = new HashSetValuedHashMap<>();

    public UppercaseFinder(
        SimpleMisspellingLoader simpleMisspellingLoader,
        ComposedMisspellingLoader composedMisspellingLoader
    ) {
        this.simpleMisspellingLoader = simpleMisspellingLoader;
        this.composedMisspellingLoader = composedMisspellingLoader;
    }

    @PostConstruct
    public void init() {
        // Only detect uppercase in simple misspellings
        simpleMisspellingLoader.addPropertyChangeListener(this);
        composedMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case SimpleMisspellingLoader.LABEL_SIMPLE_MISSPELLING -> this.simpleUppercaseMap = getUppercaseWords(
                (SetValuedMap<WikipediaLanguage, StandardMisspelling>) evt.getNewValue()
            );
            case ComposedMisspellingLoader.LABEL_COMPOSED_MISSPELLING -> this.composedUppercaseMap = getUppercaseWords(
                (SetValuedMap<WikipediaLanguage, StandardMisspelling>) evt.getNewValue()
            );
            default -> {
                return;
            }
        }

        final SetValuedMap<WikipediaLanguage, String> mergedUppercaseMap = new HashSetValuedHashMap<>();
        mergedUppercaseMap.putAll(this.simpleUppercaseMap);
        mergedUppercaseMap.putAll(this.composedUppercaseMap);
        this.uppercaseMap = mergedUppercaseMap;

        this.uppercaseAutomata = buildUppercaseAutomata(this.uppercaseMap);
    }

    @VisibleForTesting
    public SetValuedMap<WikipediaLanguage, String> getUppercaseWords(
        SetValuedMap<WikipediaLanguage, StandardMisspelling> misspellings
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
    private Set<String> getUppercaseWords(Set<StandardMisspelling> misspellings) {
        return misspellings
            .stream()
            .filter(this::isUppercaseMisspelling)
            .map(StandardMisspelling::getWord)
            .collect(Collectors.toSet());
    }

    private boolean isUppercaseMisspelling(StandardMisspelling misspelling) {
        final String word = misspelling.getWord();
        // Any of the suggestions is the misspelling word with the first letter in lowercase
        return (
            misspelling.isCaseSensitive() &&
            FinderUtils.startsWithUpperCase(word) &&
            misspelling
                .getSuggestions()
                .stream()
                .map(MisspellingSuggestion::getText)
                .anyMatch(text -> text.equals(FinderUtils.setFirstLowerCase(word)))
        );
    }

    private Map<WikipediaLanguage, RunAutomaton> buildUppercaseAutomata(
        SetValuedMap<WikipediaLanguage, String> uppercaseWords
    ) {
        LOGGER.debug("START Building Uppercase automata…");
        final Map<WikipediaLanguage, RunAutomaton> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : uppercaseWords.keySet()) {
            map.put(lang, buildUppercaseAutomaton(uppercaseWords.get(lang)));
        }
        LOGGER.debug("END Building Uppercase automata");
        return map;
    }

    @Nullable
    private RunAutomaton buildUppercaseAutomaton(@Nullable Set<String> words) {
        // Currently, there are about 60 uppercase case-sensitive misspellings.
        // Benchmarks show that an alternate automaton outperforms Aho-Corasick for this collection size.
        if (words == null || words.isEmpty()) {
            return null;
        }
        return new RunAutomaton(new RegExp(String.format("(%s)", FinderUtils.joinAlternate(words))).toAutomaton());
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final RunAutomaton automaton = this.uppercaseAutomata.get(page.getPageKey().getLang());
        if (automaton == null) {
            LOGGER.warn("No uppercase automaton for lang {} (still loading)", page.getPageKey().getLang());
            return Stream.of();
        }
        return AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String text = page.getContent();
        final int startUpperCase = match.start();

        // Trim the text at the left of the uppercase. Before trimming at the right, we remove the starting link.
        // Note: we keep leftTextEnd (before trim) for isPrecededByParagraphStart, as the \n\n are the signal itself.
        int leftTextEnd = startUpperCase;
        if (endsAtPosition(text, START_LINK, leftTextEnd)) {
            leftTextEnd -= START_LINK.length();
        }
        final int leftTextEndTrimmed = FinderUtils.trimRight(text, 0, leftTextEnd);

        return (
            isPrecededByPunctuation(text, leftTextEndTrimmed) ||
            isPrecededByPipe(text, leftTextEndTrimmed) ||
            isPrecededByCaptionSeparator(text, leftTextEndTrimmed) ||
            isPrecededByParagraphStart(text, leftTextEnd)
        );
    }

    private boolean isPrecededByPunctuation(String text, int end) {
        if (end <= 0) {
            return false;
        }
        final char lastChar = text.charAt(end - 1);
        // The pipe is not only used for tables cells, we must check is not a wiki-link!!!
        return (
            lastChar == '=' ||
            lastChar == '#' ||
            lastChar == '*' ||
            lastChar == '>' ||
            lastChar == '.' ||
            lastChar == '!'
        );
    }

    private boolean isPrecededByPipe(String text, int end) {
        // Check it is preceded by a pipe AND the first char of the line is also a pipe or a header marker
        if (end <= 0 || text.charAt(end - 1) != PIPE) {
            return false;
        }

        final int posNewLine = text.lastIndexOf(NEW_LINE, end - 1);
        final int posFirstLineChar = posNewLine >= 0 ? posNewLine + 1 : 0;
        final char firstChar = text.charAt(posFirstLineChar);
        return firstChar == PIPE || firstChar == '!';
    }

    private boolean isPrecededByCaptionSeparator(String text, int end) {
        return FinderUtils.endsAtPosition(text, CAPTION_SEPARATOR, end);
    }

    private boolean isPrecededByParagraphStart(String text, int end) {
        return FinderUtils.endsAtPosition(text, PARAGRAPH_START, end);
    }

    /**
     * Find the first expression in a text in case it is a known uppercase misspelling.
     * To be used e.g. to find uppercase misspellings at the start of template values or file aliases.
     */
    public @Nullable MatchResult findFirstExpressionUpperCase(FinderPage page, int start) {
        final String text = page.getContent();
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final RunAutomaton automaton = this.uppercaseAutomata.get(lang);
        if (automaton == null) {
            return null;
        }

        // Short-circuit if the first non-space char is not uppercase
        final int startValue = FinderUtils.trimLeft(text, start, text.length());
        if (startValue >= text.length() || !FinderUtils.startsWithUpperCase(String.valueOf(text.charAt(startValue)))) {
            return null;
        }

        final AutomatonMatcher matcher = automaton.newMatcher(text, startValue, text.length());
        if (matcher.find() && matcher.start() == 0) {
            return FinderMatchRange.of(text, startValue, startValue + matcher.end());
        }
        return null;
    }
}
