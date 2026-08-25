package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import jakarta.annotation.PostConstruct;
import java.time.Year;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase.
 * Uses Aho-Corasick to find month names in a single pass,
 * then inspects the context before/after each match to identify the date pattern.
 */
@Component
public class DateFinder implements ReplacementFinder {

    private final int CURRENT_YEAR = Year.now(ZoneId.systemDefault()).getValue();

    // Dependency injection
    private final FinderProperties finderProperties;

    private static final ListValuedMap<WikipediaLanguage, String> langMonths = new ArrayListValuedHashMap<>();
    private static final List<String> englishMonths = new ArrayList<>();
    private static final ListValuedMap<WikipediaLanguage, String> langPrepositions = new ArrayListValuedHashMap<>();
    private static final SetValuedMap<WikipediaLanguage, String> langConnectors = new HashSetValuedHashMap<>();
    private static final Map<WikipediaLanguage, Map<String, String>> langArticles = new EnumMap<>(
        WikipediaLanguage.class
    );
    private static final Map<WikipediaLanguage, StringMap<String>> monthMatchers = new EnumMap<>(
        WikipediaLanguage.class
    );

    public DateFinder(FinderProperties finderProperties) {
        this.finderProperties = finderProperties;
    }

    @PostConstruct
    public void init() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            // Months: the ones for the language, in lower and uppercase, and the English ones.
            // Plus the special case of "setiembre" in Spanish
            langMonths.putAll(lang, this.finderProperties.getMonthNames().get(lang.getCode()));
            List<String> monthsLowerUpperCase = new ArrayList<>();
            langMonths
                .get(lang)
                .forEach(month -> {
                    monthsLowerUpperCase.add(month);
                    monthsLowerUpperCase.add(ReplacerUtils.setFirstUpperCase(month));
                });
            englishMonths.addAll(this.finderProperties.getMonthNames().get(ENGLISH_LANGUAGE));
            monthsLowerUpperCase.addAll(englishMonths);
            if (lang == WikipediaLanguage.SPANISH) {
                // Trick only for Spanish months
                monthsLowerUpperCase.add("setiembre");
                monthsLowerUpperCase.add("Setiembre");
            }

            // Prepositions
            langPrepositions.putAll(lang, this.finderProperties.getYearPrepositions().get(lang.getCode()));

            // Connectors
            langConnectors.putAll(lang, this.finderProperties.getDateConnectors().get(lang.getCode()));

            // Articles
            langArticles.put(lang, new HashMap<>());
            this.finderProperties.getDateArticles()
                .get(lang.getCode())
                .forEach(dateArticle -> langArticles.get(lang).putAll(buildArticleMap(dateArticle)));

            // Aho-Corasick matcher for month names (whole-word matching)
            // WholeWordMatchMap guarantees word-boundary: no need to validate completeness afterwards
            // (e.g. "abril" inside "abriles" won't be matched)
            monthMatchers.put(lang, new WholeWordMatchMap<>(monthsLowerUpperCase, monthsLowerUpperCase, true));
        }
    }

    private Map<String, String> buildArticleMap(FinderProperties.DateArticle dateArticle) {
        final Map<String, String> articleMap = new HashMap<>();
        articleMap.put(dateArticle.getPrep(), dateArticle.getArticle());
        articleMap.put(
            ReplacerUtils.setFirstUpperCase(dateArticle.getPrep()),
            ReplacerUtils.setFirstUpperCase(dateArticle.getArticle())
        );
        return articleMap;
    }

    @Override
    public Stream<Replacement> find(FinderPage page) {
        // Override find because convertDate may return null (date is valid and needs no fix)
        return findMatchResults(page)
            .map(match -> convertDate(match, page))
            .filter(Objects::nonNull);
    }

    @Override
    public Stream<Replacement> findWithNoSuggestions(FinderPage page) {
        return find(page).map(Replacement::withNoSuggestions);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String text = page.getContent();

        // Phase 1: Find all months in the text using Aho-Corasick
        final ResultMatchListener listener = new ResultMatchListener();
        monthMatchers.get(lang).match(text, listener);

        // Phase 2: For each month, inspect context to build a complete date MatchResult
        return listener
            .getMatches()
            .map(monthMatch -> buildDateMatch(monthMatch, page))
            .filter(Objects::nonNull);
    }

    /**
     * From a month match, look before and after to determine if it's part of a date pattern.
     * If so, return a MatchResult covering the full date. Otherwise return null.
     */
    @Nullable
    private MatchResult buildDateMatch(MatchResult monthMatch, FinderPage page) {
        final String text = page.getContent();
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final int startMonth = monthMatch.start();
        final int endMonth = monthMatch.end();

        // Look before the month (DOT allowed to capture years like "2.013")
        MatchResult wordBefore = FinderUtils.findWordBefore(text, startMonth, DOT);

        if (wordBefore != null) {
            if (isValidDay(text, wordBefore)) {
                // Pattern 1: day + month (no prep before)
                if (FinderUtils.isEmptyBlankOrSpaceAlias(text, wordBefore.end(), startMonth)) {
                    // The part after the month ([prep] + year) will be verified in the build method
                    return buildLongDateMatch(page, wordBefore, null, monthMatch);
                }
                return null;
            } else if (isPreposition(wordBefore.group(), lang)) {
                // Could be: day + prep + month → long date
                if (FinderUtils.isEmptyBlankOrSpaceAlias(text, wordBefore.end(), startMonth)) {
                    MatchResult dayBefore = findDayBefore(text, wordBefore);
                    if (dayBefore != null) {
                        // The part after the month ([prep] + year) will be verified in the build method
                        return buildLongDateMatch(page, dayBefore, wordBefore, monthMatch);
                    }
                }
                // Not a recognized pattern
                return null;
            } else if (isRegexConnector(wordBefore.group(), lang)) {
                // Pattern 2: connector + month + [prep] + year (month-year)
                if (FinderUtils.isEmptyBlankOrSpaceAlias(text, wordBefore.end(), startMonth)) {
                    MatchResult monthYearMatch = buildMonthYearMatch(page, wordBefore, monthMatch);
                    if (monthYearMatch != null) {
                        return monthYearMatch;
                    }
                }
                // If month-year fails, fall through to try month-day-year below
            } else if (isValidYear(text, wordBefore)) {
                // Pattern 4: year [,] + month + day (year-month-day)
                // Allow an optional comma between year and month
                int afterYear = wordBefore.end();
                if (afterYear < startMonth && text.charAt(afterYear) == ',') {
                    afterYear++;
                }
                if (FinderUtils.isEmptyBlankOrSpaceAlias(text, afterYear, startMonth)) {
                    // The part after the month (day) will be verified in the build method
                    return buildYearMonthDayMatch(page, wordBefore, monthMatch);
                }
                return null;
            }
        }

        // Look after the month (month is start of date: MonthDayYear)
        MatchResult dayAfter = findDayAfter(text, endMonth);
        if (dayAfter != null) {
            // Pattern 3: month + day + [prep] + year (month-day-year)
            return buildMonthDayYearMatch(page, monthMatch, dayAfter);
        }

        // No recognized pattern
        return null;
    }

    /*
     * Nested groups in the date MatchResult (unified for all patterns):
     * group(1) = connector (may be empty, only for month-year pattern)
     * group(2) = day
     * group(3) = preposition before month (may be empty)
     * group(4) = month
     * group(5) = preposition after month (may be empty)
     * group(6) = year
     */

    // Build a MatchResult for long date: day [prep] month [prep|,] year
    @Nullable
    private MatchResult buildLongDateMatch(
        FinderPage page,
        MatchResult dayMatch,
        @Nullable MatchResult prepBeforeMatch,
        MatchResult monthMatch
    ) {
        final String text = page.getContent();
        MatchResult yearAfter = findYearAfter(page, monthMatch.end());
        if (yearAfter == null) {
            return null;
        }
        FinderMatchRange result = FinderMatchRange.ofNested(text, dayMatch.start(), yearAfter.end());
        result.addGroup(FinderMatchRange.ofEmpty(text, dayMatch.start())); // group(1) = connector (empty)
        result.addGroup(dayMatch); // group(2) = day
        result.addGroup(prepBeforeMatch != null ? prepBeforeMatch : FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(3) = prep before
        result.addGroup(monthMatch); // group(4) = month
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(1)); // group(5) = prep after
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(2)); // group(6) = year
        return result;
    }

    // Build a MatchResult for month-year: connector + month + [prep] + year
    @Nullable
    private MatchResult buildMonthYearMatch(FinderPage page, MatchResult connectorMatch, MatchResult monthMatch) {
        final String text = page.getContent();
        MatchResult yearAfter = findYearAfter(page, monthMatch.end());
        if (yearAfter == null) {
            return null;
        }
        FinderMatchRange result = FinderMatchRange.ofNested(text, connectorMatch.start(), yearAfter.end());
        result.addGroup(connectorMatch); // group(1) = connector
        result.addGroup(FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(2) = day (empty)
        result.addGroup(FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(3) = prep before (empty)
        result.addGroup(monthMatch); // group(4) = month
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(1)); // group(5) = prep after
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(2)); // group(6) = year
        return result;
    }

    // Build a MatchResult for year-month-day: year [,] month day
    @Nullable
    private MatchResult buildYearMonthDayMatch(FinderPage page, MatchResult yearMatch, MatchResult monthMatch) {
        final String text = page.getContent();
        MatchResult dayMatch = findDayAfter(text, monthMatch.end());
        if (dayMatch == null) {
            return null;
        }
        FinderMatchRange result = FinderMatchRange.ofNested(text, yearMatch.start(), dayMatch.end());
        result.addGroup(FinderMatchRange.ofEmpty(text, yearMatch.start())); // group(1) = connector (empty)
        result.addGroup(dayMatch); // group(2) = day
        result.addGroup(FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(3) = prep before (empty)
        result.addGroup(monthMatch); // group(4) = month
        result.addGroup(FinderMatchRange.ofEmpty(text, yearMatch.start())); // group(5) = prep after (empty)
        result.addGroup(yearMatch); // group(6) = year
        return result;
    }

    // Build a MatchResult for month-day-year: month day [prep] year
    @Nullable
    private MatchResult buildMonthDayYearMatch(FinderPage page, MatchResult monthMatch, MatchResult dayMatch) {
        final String text = page.getContent();
        MatchResult yearAfter = findYearAfter(page, dayMatch.end());
        if (yearAfter == null) {
            return null;
        }
        FinderMatchRange result = FinderMatchRange.ofNested(text, monthMatch.start(), yearAfter.end());
        result.addGroup(FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(1) = connector (empty)
        result.addGroup(dayMatch); // group(2) = day
        result.addGroup(FinderMatchRange.ofEmpty(text, monthMatch.start())); // group(3) = prep before (empty)
        result.addGroup(monthMatch); // group(4) = month
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(1)); // group(5) = prep after
        result.addGroup(((FinderMatchRange) yearAfter).getGroup(2)); // group(6) = year
        return result;
    }

    /**
     * Find [prep] + year after the given position.
     * Returns a MatchResult covering the full range (from prep or year start to year end),
     * with nested groups: group(1) = preposition (may be empty), group(2) = year.
     * Returns null if no valid year found.
     */
    @Nullable
    private MatchResult findYearAfter(FinderPage page, int start) {
        final String text = page.getContent();
        final WikipediaLanguage lang = page.getPageKey().getLang();
        MatchResult wordAfter = FinderUtils.findWordAfter(text, start, DOT);
        if (wordAfter == null) {
            return null;
        }

        // Allow an optional comma between the previous token and what follows
        int afterStart = start;
        if (afterStart < wordAfter.start() && text.charAt(afterStart) == ',') {
            afterStart++;
        }
        if (!FinderUtils.isEmptyBlankOrSpaceAlias(text, afterStart, wordAfter.start())) {
            return null;
        }

        if (isPreposition(wordAfter.group(), lang)) {
            MatchResult yearMatch = findYear(text, wordAfter.end());
            if (yearMatch == null) {
                return null;
            }
            if (!FinderUtils.isEmptyBlankOrSpaceAlias(text, wordAfter.end(), yearMatch.start())) {
                return null;
            }
            FinderMatchRange result = FinderMatchRange.ofNested(text, wordAfter.start(), yearMatch.end());
            result.addGroup(wordAfter); // group(1) = preposition
            result.addGroup(yearMatch); // group(2) = year
            return result;
        }

        // Try as year directly
        MatchResult yearMatch = trimTrailingDot(text, wordAfter);
        if (isValidYear(text, yearMatch)) {
            FinderMatchRange result = FinderMatchRange.ofNested(text, yearMatch.start(), yearMatch.end());
            result.addGroup(FinderMatchRange.ofEmpty(text, yearMatch.start())); // group(1) = empty preposition
            result.addGroup(yearMatch); // group(2) = year
            return result;
        }
        return null;
    }

    /** Find a year after the given position, trimming trailing dot if needed. */
    @Nullable
    private MatchResult findYear(String text, int start) {
        MatchResult match = FinderUtils.findWordAfter(text, start, DOT);
        if (match == null) {
            return null;
        }
        match = trimTrailingDot(text, match);
        return isValidYear(text, match) ? match : null;
    }

    /** If the match ends with a dot that doesn't belong to a year pattern (e.g. "1999."), trim it. */
    private MatchResult trimTrailingDot(String text, MatchResult match) {
        if (match.end() > match.start() && text.charAt(match.end() - 1) == '.') {
            int len = match.end() - match.start();
            // A valid year with dot is like "2.019" (dot at position 1). Any other trailing dot is spurious.
            if (len != 5 || text.charAt(match.start() + 1) != '.') {
                return FinderMatchRange.of(text, match.start(), match.end() - 1);
            }
        }
        return match;
    }

    // Conversion phase

    @Override
    public Replacement convertWithNoSuggestions(MatchResult matchResult, FinderPage page) {
        // We cannot use this same method because we need it to be able to return a null
        throw new UnsupportedOperationException();
    }

    @Override
    public Replacement convert(MatchResult matchResult, FinderPage page) {
        // We cannot use this same method because we need it to be able to return a null
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Replacement convertDate(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        assert FinderUtils.isWordCompleteInText(match, page.getContent());

        final String date = match.group();
        boolean isDateValid = true;

        // Extract components from nested groups
        final String connector = match.group(1); // may be empty
        String fixedDay = match.group(2); // may be empty (month-year pattern)
        final String prepBefore = match.group(3); // may be empty
        String fixedMonth = match.group(4);
        String prepAfter = match.group(5); // may be empty
        String fixedYear = match.group(6);

        // Validate and fix day
        if (!fixedDay.isEmpty()) {
            if (isFixableDay(fixedDay)) {
                fixedDay = fixDay(fixedDay);
                isDateValid = false;
            }
        }

        // Validate prep before month
        if (prepBefore.isEmpty() && !fixedDay.isEmpty() && connector.isEmpty()) {
            // Long date without preposition before month (e.g. "17 Agosto 2019")
            isDateValid = false;
        }

        // Validate and fix month
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
            isDateValid = false;
        }

        // Validate and fix prep after month
        if (prepAfter.isEmpty() && !connector.isEmpty()) {
            // Month-year without preposition (e.g. "Desde Agosto 2019")
            prepAfter = getPrepositionDefault(lang);
            isDateValid = false;
        } else if (prepAfter.isEmpty() && !fixedDay.isEmpty() && fixedYear.isEmpty()) {
            // This shouldn't happen with current patterns
            prepAfter = getPrepositionDefault(lang);
            isDateValid = false;
        } else if (prepAfter.isEmpty()) {
            prepAfter = getPrepositionDefault(lang);
            isDateValid = false;
        }

        // Validate and fix year
        if (isFixableYear(fixedYear)) {
            fixedYear = fixYear(fixedYear);
            isDateValid = false;
        }

        // If everything is already valid, nothing to fix
        if (isDateValid) {
            return null;
        }

        // Cosmetic fixes
        fixedMonth = fixSeptember(fixedMonth, lang);
        prepAfter = fixPrepositionAfter(prepAfter, lang);

        // Build the fixed date depending on the pattern
        final String fixedDate;
        final String defaultPrep = getPrepositionDefault(lang);
        if (!connector.isEmpty() && fixedDay.isEmpty()) {
            // Month-year: connector + month + prep + year
            fixedDate = connector + SPACE + fixedMonth + SPACE + prepAfter + SPACE + fixedYear;
        } else {
            // All other patterns produce: day + prep + month + prep + year
            fixedDate = fixedDay + SPACE + defaultPrep + SPACE + fixedMonth + SPACE + prepAfter + SPACE + fixedYear;
        }

        return buildDateReplacement(page, match.start(), date, fixedDate);
    }

    private boolean isValidDay(String text, MatchResult match) {
        int len = match.end() - match.start();
        if (len < 1 || len > 2 || !FinderUtils.isNumber(text, match.start(), match.end())) {
            return false;
        }
        // Reject day "0" (but allow "07" which will be fixed later)
        return !(len == 1 && text.charAt(match.start()) == '0');
    }

    /** Find a day (1-2 digit number) after the given position, separated by space, or null if not found. */
    @Nullable
    private MatchResult findDayAfter(String text, int start) {
        MatchResult wordAfter = FinderUtils.findWordAfter(text, start);
        if (
            wordAfter != null &&
            isValidDay(text, wordAfter) &&
            FinderUtils.isEmptyBlankOrSpaceAlias(text, start, wordAfter.start())
        ) {
            return wordAfter;
        }
        return null;
    }

    /**
     * Find a day before a preposition, separated by space.
     * Returns the day match, or null if not found.
     */
    @Nullable
    private MatchResult findDayBefore(String text, MatchResult prepMatch) {
        MatchResult wordBefore = FinderUtils.findWordBefore(text, prepMatch.start());
        if (
            wordBefore != null &&
            isValidDay(text, wordBefore) &&
            FinderUtils.isEmptyBlankOrSpaceAlias(text, wordBefore.end(), prepMatch.start())
        ) {
            return wordBefore;
        }
        return null;
    }

    private boolean isRegexConnector(String token, WikipediaLanguage lang) {
        return langConnectors.containsMapping(lang, FinderUtils.setFirstLowerCase(token));
    }

    private boolean isValidYear(String text, MatchResult match) {
        int len = match.end() - match.start();
        int start = match.start();
        boolean validFormat;
        if (len == 4) {
            validFormat = FinderUtils.isNumber(text, start, match.end());
        } else if (len == 5) {
            validFormat =
                FinderUtils.isAsciiDigit(text.charAt(start)) &&
                text.charAt(start + 1) == '.' &&
                FinderUtils.isNumber(text, start + 2, match.end());
        } else {
            return false;
        }
        if (!validFormat) {
            return false;
        }
        // Reject years in the future
        int year;
        if (len == 4) {
            year = Integer.parseInt(match.group());
        } else {
            // Format "X.YYY" → concatenate without the dot and parse
            year = Integer.parseInt(text.substring(start, start + 1) + text.substring(start + 2, match.end()));
        }
        return year <= CURRENT_YEAR;
    }

    private boolean isFixableDay(String day) {
        return day.length() == 2 && day.charAt(0) == '0';
    }

    private String fixDay(String day) {
        return day.substring(1);
    }

    private boolean isPreposition(String token, WikipediaLanguage lang) {
        return langPrepositions.containsMapping(lang, FinderUtils.setFirstLowerCase(token));
    }

    private boolean isFixableMonth(String month) {
        return FinderUtils.startsWithUpperCase(month);
    }

    private String fixMonth(String month, WikipediaLanguage lang) {
        if (isEnglishMonth(month)) {
            return fixEnglishMonth(month, lang);
        } else {
            return FinderUtils.setFirstLowerCase(month);
        }
    }

    private boolean isEnglishMonth(String month) {
        return englishMonths.contains(month);
    }

    private String fixEnglishMonth(String month, WikipediaLanguage lang) {
        return langMonths.get(lang).get(englishMonths.indexOf(month));
    }

    private String fixSeptember(String month, WikipediaLanguage lang) {
        // The September fix is a trick only for Spanish months
        return lang == WikipediaLanguage.SPANISH && "setiembre".equals(month) ? "septiembre" : month;
    }

    private boolean isFixableYear(String year) {
        return year.length() == 5;
    }

    private String fixYear(String year) {
        return year.charAt(0) + year.substring(2);
    }

    private String getPrepositionDefault(WikipediaLanguage lang) {
        return langPrepositions.get(lang).getFirst();
    }

    private String fixPrepositionAfter(String prepAfter, WikipediaLanguage lang) {
        return lang != WikipediaLanguage.SPANISH
            ? FinderUtils.setFirstLowerCase(prepAfter)
            : getPrepositionDefault(lang);
    }

    private Replacement buildDateReplacement(
        FinderPage page,
        int originalStart,
        String originalDate,
        String fixedDate
    ) {
        // Default before fixing article (if applicable)
        int start = originalStart;
        String text = originalDate;
        final List<Suggestion> suggestions = new ArrayList<>();
        // Default suggestion
        suggestions.add(Suggestion.ofNoComment(fixedDate));

        // Fix article (but for dates starting with connector)
        if (FinderUtils.startsWithNumber(fixedDate)) {
            final List<String> articlePair = getFixArticle(page, start);
            if (!articlePair.isEmpty()) {
                final String article = articlePair.getFirst();
                final String alternative = articlePair.get(1);
                start -= (article.length() + 1); // + 1 because of the space between
                text = page.getContent().substring(start, start + article.length() + 1 + originalDate.length());

                final String fixedDateWithoutArticle = article + SPACE + fixedDate;
                final String fixedDateWithArticle = alternative + SPACE + fixedDate;
                suggestions.clear();
                suggestions.add(Suggestion.of(fixedDateWithArticle, "con artículo"));
                suggestions.add(Suggestion.of(fixedDateWithoutArticle, "sin artículo"));
            }
        }

        // Add a warning if the date contains an English month
        if (englishMonths.stream().anyMatch(originalDate::contains)) {
            suggestions.add(0, Suggestion.of(text, "no reemplazar si el contexto está en inglés"));
        }

        return Replacement.of(start, text, StandardType.DATE, suggestions);
    }

    // Return the appropriate article for the date
    private List<String> getFixArticle(FinderPage page, int start) {
        final String text = page.getContent();
        final MatchResult preceding = FinderUtils.findWordBefore(text, start);
        final WikipediaLanguage lang = page.getPageKey().getLang();
        // Check there is a space between the article and the date
        if (preceding != null && SPACE.equals(text.substring(preceding.end(), start))) {
            final String precedingArticle = preceding.group();
            final String precedingAlternative = langArticles.get(lang).get(precedingArticle);
            if (precedingAlternative != null) {
                return List.of(precedingArticle, precedingAlternative);
            }
        }
        return List.of();
    }
}
