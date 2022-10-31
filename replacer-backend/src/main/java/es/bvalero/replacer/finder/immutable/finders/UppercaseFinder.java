package es.bvalero.replacer.finder.immutable.finders;

import com.github.rozidan.springboot.logger.Loggable;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.listing.MisspellingSuggestion;
import es.bvalero.replacer.finder.listing.SimpleMisspelling;
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
 * <br />
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

    private static final String START_LINK = "[[";

    private static final String CAPTION_SEPARATOR = "|+";

    private static final String TIMELINE_TEXT = "text:";

    private static final String PARAGRAPH_START = "\n\n";

    // The pipe is not only used for tables cells, we must check is not a wiki-link!!!
    private static final Set<Character> PUNCTUATIONS = Set.of('=', '#', '*', '>', '.', '!');
    private static final String PIPE = "|";
    private static final char NEW_LINE = '\n';

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
            final String alternations = String.format("(%s)", FinderUtils.joinAlternate(words));
            return new RunAutomaton(new RegExp(alternations).toAutomaton());
        } else {
            return null;
        }
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.MEDIUM;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        final RunAutomaton automaton = this.uppercaseAutomata.get(page.getId().getLang());
        // Benchmarks show similar performance with and without validation
        return automaton == null ? Collections.emptyList() : AutomatonMatchFinder.find(page.getContent(), automaton);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        final String text = page.getContent();
        final int startUpperCase = match.start();
        final String leftTextNotTrimmed = text.substring(0, startUpperCase);
        final String leftText = StringUtils.strip(StringUtils.removeEnd(leftTextNotTrimmed, START_LINK));

        return (
            isPrecededByPunctuation(leftText) ||
            isPrecededByPipe(leftText) ||
            isPrecededByCaptionSeparator(leftText) ||
            isPrecededByTimeLineText(leftText) ||
            isPrecededByParagraphStart(leftTextNotTrimmed)
        );
    }

    private boolean isPrecededByPunctuation(String leftText) {
        if (leftText.length() == 0) {
            return false;
        } else {
            final char lastChar = leftText.charAt(leftText.length() - 1);
            return PUNCTUATIONS.contains(lastChar);
        }
    }

    private boolean isPrecededByPipe(String leftText) {
        final boolean isPrecededByPipe = leftText.endsWith(PIPE);
        // Check the first char of the line is also a pipe
        return isPrecededByPipe && PIPE.equals(String.valueOf(findFirstLineChar(leftText)));
    }

    private boolean isPrecededByCaptionSeparator(String leftText) {
        return leftText.endsWith(CAPTION_SEPARATOR);
    }

    private boolean isPrecededByTimeLineText(String leftText) {
        return leftText.endsWith(TIMELINE_TEXT);
    }

    private boolean isPrecededByParagraphStart(String leftText) {
        return leftText.endsWith(PARAGRAPH_START);
    }

    private char findFirstLineChar(String text) {
        final int newLinePos = text.lastIndexOf(NEW_LINE);
        return newLinePos >= 0 ? text.charAt(newLinePos + 1) : 0;
    }
}
