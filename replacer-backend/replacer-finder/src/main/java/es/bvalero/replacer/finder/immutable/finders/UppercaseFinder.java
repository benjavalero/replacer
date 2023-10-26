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
import es.bvalero.replacer.finder.listing.load.SimpleMisspellingLoader;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.Getter;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component
public class UppercaseFinder implements ImmutableFinder, PropertyChangeListener {

    private static final String CAPTION_SEPARATOR = "|+";

    private static final String PARAGRAPH_START = "\n\n";

    // The pipe is not only used for tables cells, we must check is not a wiki-link!!!
    private static final Set<Character> PUNCTUATIONS = Set.of('=', '#', '*', '>', '.', '!');

    @Autowired
    private SimpleMisspellingLoader simpleMisspellingLoader;

    private static final char[] falseWordChars = { '-' };
    private static final boolean[] wordCharFlags = { false };

    // StringMap with the misspellings which start with uppercase and are case-sensitive
    private Map<WikipediaLanguage, StringMap<String>> uppercaseStringMap = new EnumMap<>(WikipediaLanguage.class);

    @Getter
    private SetValuedMap<WikipediaLanguage, String> uppercaseMap = new HashSetValuedHashMap<>();

    @PostConstruct
    public void init() {
        // Only detect uppercase in simple misspellings
        simpleMisspellingLoader.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        this.uppercaseMap = getUppercaseWords((SetValuedMap<WikipediaLanguage, StandardMisspelling>) evt.getNewValue());
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

    private Map<WikipediaLanguage, StringMap<String>> buildUppercaseStringMap(
        SetValuedMap<WikipediaLanguage, String> uppercaseWords
    ) {
        final Map<WikipediaLanguage, StringMap<String>> map = new EnumMap<>(WikipediaLanguage.class);
        for (WikipediaLanguage lang : uppercaseWords.keySet()) {
            map.put(lang, buildUppercaseAutomaton(uppercaseWords.get(lang)));
        }
        return map;
    }

    @Nullable
    private StringMap<String> buildUppercaseAutomaton(@Nullable Set<String> words) {
        // Currently, there are about 60 uppercase case-sensitive misspellings,
        // so the best approaches are an automaton with all the terms alternated and
        // the Aho-Corasick algorithm. We use the last one giving a better median performance.
        if (words != null && !words.isEmpty()) {
            return new WholeWordLongestMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
        } else {
            return null;
        }
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
}
