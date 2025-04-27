package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordLongestMatchMap;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.MisspellingSuggestion;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.StandardMisspelling;
import es.bvalero.replacer.finder.listing.load.ComposedMisspellingLoader;
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import jakarta.annotation.PostConstruct;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.Nullable;
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
    // The pipe is not only used for tables cells, we must check is not a wiki-link!!!
    private static final Set<Character> PUNCTUATIONS = Set.of('=', '#', '*', '>', '.', '!');
    private static final char[] falseWordChars = { '-' };
    private static final boolean[] wordCharFlags = { false };

    // Dependency injection
    private final SimpleMisspellingLoader simpleMisspellingLoader;
    private final ComposedMisspellingLoader composedMisspellingLoader;

    // Uppercase words for simple and composed misspellings
    private SetValuedMap<WikipediaLanguage, String> simpleUppercaseMap = new HashSetValuedHashMap<>();
    private SetValuedMap<WikipediaLanguage, String> composedUppercaseMap = new HashSetValuedHashMap<>();
    // StringMap with the misspellings which start with uppercase and are case-sensitive
    private Map<WikipediaLanguage, StringMap<String>> uppercaseStringMap = new EnumMap<>(WikipediaLanguage.class);

    @Getter
    private SetValuedMap<WikipediaLanguage, String> uppercaseMap = new HashSetValuedHashMap<>();

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

        this.uppercaseStringMap = buildUppercaseStringMap(this.uppercaseMap);
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

    private Map<WikipediaLanguage, StringMap<String>> buildUppercaseStringMap(
        SetValuedMap<WikipediaLanguage, String> uppercaseWords
    ) {
        LOGGER.debug("START Building Uppercase string map…");
        final Map<WikipediaLanguage, StringMap<String>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : uppercaseWords.keySet()) {
            map.put(lang, buildUppercaseStringMap(uppercaseWords.get(lang)));
        }
        LOGGER.debug("END Building Uppercase string map");
        return map;
    }

    @Nullable
    private StringMap<String> buildUppercaseStringMap(@Nullable Set<String> words) {
        // Currently, there are about 60 uppercase case-sensitive misspellings,
        // so the best approaches are an automaton with all the terms alternated and
        // the Aho-Corasick algorithm. We use the last one giving a better median performance.
        if (words == null || words.isEmpty()) {
            return null;
        }
        return new WholeWordLongestMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final StringMap<String> stringMap = this.uppercaseStringMap.get(page.getPageKey().getLang());
        final ResultMatchListener listener = new ResultMatchListener();
        stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        final String text = page.getContent();
        final int startUpperCase = match.start();
        final String leftTextNotTrimmed = text.substring(0, startUpperCase);
        final String leftText = StringUtils.strip(StringUtils.removeEnd(leftTextNotTrimmed, START_LINK));

        return (
            isPrecededByPunctuation(leftText) ||
            isPrecededByPipe(leftText) ||
            isPrecededByCaptionSeparator(leftText) ||
            isPrecededByParagraphStart(leftTextNotTrimmed)
        );
    }

    private boolean isPrecededByPunctuation(String leftText) {
        if (leftText.isEmpty()) {
            return false;
        } else {
            final char lastChar = leftText.charAt(leftText.length() - 1);
            return PUNCTUATIONS.contains(lastChar);
        }
    }

    private boolean isPrecededByPipe(String leftText) {
        final boolean isPrecededByPipe = leftText.endsWith(String.valueOf(PIPE));
        // Check the first char of the line is also a pipe
        return isPrecededByPipe && findFirstLineChar(leftText) == PIPE;
    }

    private boolean isPrecededByCaptionSeparator(String leftText) {
        return leftText.endsWith(CAPTION_SEPARATOR);
    }

    private boolean isPrecededByParagraphStart(String leftText) {
        return leftText.endsWith(PARAGRAPH_START);
    }

    private char findFirstLineChar(String text) {
        final int newLinePos = text.lastIndexOf(NEW_LINE);
        return newLinePos >= 0 ? text.charAt(newLinePos + 1) : 0;
    }

    /**
     * Find the first expression in a text in case it is a known uppercase misspelling.
     * To be used e.g. to find uppercase misspellings at the start of template values or file aliases.
     */
    public Optional<MatchResult> findFirstExpressionUpperCase(String text, WikipediaLanguage lang) {
        // Find the start of the potential expression as there could be other characters like a whitespace
        final MatchResult firstWord = FinderUtils.findWordAfter(text, 0);
        if (firstWord == null) {
            return Optional.empty();
        }

        // To improve the performance, first we try with the simple misspellings with an exact match.
        if (this.getUppercaseMap().containsMapping(lang, firstWord.group())) {
            return Optional.of(firstWord);
        }

        // Then, we try with the composed misspellings, checking if the parameter value starts with an uppercase misspelling.
        final int valueStart = firstWord.start();
        final String valueText = text.substring(valueStart);
        return this.getUppercaseMap()
            .get(lang)
            .stream()
            .filter(valueText::startsWith)
            .findAny()
            .map(s -> FinderMatchResult.of(valueStart, s));
    }
}
