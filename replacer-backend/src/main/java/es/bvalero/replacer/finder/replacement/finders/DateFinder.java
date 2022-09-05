package es.bvalero.replacer.finder.replacement.finders;

import static org.apache.commons.lang3.StringUtils.SPACE;

import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import lombok.Data;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase.
 */
@Component
public class DateFinder implements ReplacementFinder {

    private static final char YEAR_DOT = '.';
    private static final int CURRENT_YEAR = LocalDate.now().getYear();

    @Resource
    private Map<String, String> monthNames;

    @Resource
    private Map<String, String> dateConnectors;

    @Resource
    private Map<String, String> yearPrepositions;

    @Resource
    private Map<String, String> dateArticles;

    private static final SetValuedMap<WikipediaLanguage, String> langMonths = new HashSetValuedHashMap<>();
    private static final SetValuedMap<WikipediaLanguage, String> langMonthsLowerCase = new HashSetValuedHashMap<>();
    private static final SetValuedMap<WikipediaLanguage, String> langConnectors = new HashSetValuedHashMap<>();
    private static final ListValuedMap<WikipediaLanguage, String> langPrepositions = new ArrayListValuedHashMap<>();
    private static final Map<WikipediaLanguage, Map<String, String>> langArticles = new EnumMap<>(
        WikipediaLanguage.class
    );

    @PostConstruct
    public void init() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            if (
                !monthNames.containsKey(lang.getCode()) ||
                !dateConnectors.containsKey(lang.getCode()) ||
                !yearPrepositions.containsKey(lang.getCode())
            ) {
                // Skip this language as it has no configuration for date finder
                continue;
            }

            // We assume the months, connectors and prepositions in the configuration are in lowercase
            FinderUtils
                .splitListAsStream(monthNames.get(lang.getCode()))
                .forEach(month -> {
                    langMonths.put(lang, month);
                    langMonths.put(lang, FinderUtils.setFirstUpperCase(month));
                    langMonthsLowerCase.put(lang, month);
                });

            FinderUtils
                .splitListAsStream(dateConnectors.get(lang.getCode()))
                .forEach(connector -> {
                    langConnectors.put(lang, connector);
                    langConnectors.put(lang, FinderUtils.setFirstUpperCase(connector));
                });

            FinderUtils
                .splitListAsStream(yearPrepositions.get(lang.getCode()))
                .forEach(preposition -> {
                    langPrepositions.put(lang, preposition);
                    langPrepositions.put(lang, FinderUtils.setFirstUpperCase(preposition));
                });

            // Finally configure the date articles
            if (dateArticles.containsKey(lang.getCode())) {
                langArticles.put(lang, new HashMap<>());
                FinderUtils
                    .splitListAsStream(dateArticles.get(lang.getCode()))
                    .forEach(pair -> langArticles.get(lang).putAll(buildArticleMap(pair)));
            }
        }
    }

    private Map<String, String> buildArticleMap(String pair) {
        final Map<String, String> articleMap = new HashMap<>();
        final String[] tokens = StringUtils.split(pair, "-");
        articleMap.put(tokens[0], tokens[1]);
        articleMap.put(FinderUtils.setFirstUpperCase(tokens[0]), FinderUtils.setFirstUpperCase(tokens[1]));
        return articleMap;
    }

    @Override
    public Iterable<Replacement> find(WikipediaPage page) {
        final Iterable<MatchResult> months = findAllMonths(page);
        final Iterable<Replacement> replacements = IterableUtils.transformedIterable(
            months,
            m -> convertMonth(m, page)
        );
        return IterableUtils.filteredIterable(replacements, Objects::nonNull);
    }

    @SuppressWarnings("unchecked")
    private Iterable<MatchResult> findAllMonths(WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        return IterableUtils.chainedIterable(
            langMonthsLowerCase
                .get(lang)
                .stream()
                .map(MonthFinder::new)
                .map(finder -> finder.find(page))
                .toArray(Iterable[]::new)
        );
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    @Nullable
    private Replacement convertMonth(MatchResult match, WikipediaPage page) {
        // We use a specific method to convert the result as it might return a null replacement
        // if the month found is not a date one or the date is not to be fixed

        // Date formats:
        // 1) Day + Prep? + Month + Prep? + Year (long date)
        // 2) Connector + Month + Prep? + Year (month-year)
        // 3) Month + Day + Prep? + Year (month-day-year)
        // 4) Year,? + Month + Day (year-month-day)
        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();

        // We allow the dot to find years with a dot as a separator
        Set<Character> allowedChars = Set.of(YEAR_DOT);
        final int startMonth = match.start();
        final LinearMatchResult matchBefore = FinderUtils.findWordBefore(text, startMonth, allowedChars);
        if (matchBefore == null) {
            return convertMonthDayYear(match, page);
        } else {
            final String spaceBefore = text.substring(matchBefore.end(), startMonth);
            if (isNotSpace(spaceBefore)) {
                return convertMonthDayYear(match, page);
            }
            final String wordBefore = matchBefore.group();
            if (isDay(wordBefore)) {
                return convertLongDate(match, matchBefore, page);
            } else if (isYear(wordBefore)) {
                return convertYearMonthDay(match, matchBefore, page);
            } else if (isConnector(wordBefore, lang)) {
                return convertMonthYear(match, matchBefore, page);
            } else if (isPreposition(wordBefore, lang)) {
                return convertLongDate(match, matchBefore, page);
            } else {
                return convertMonthDayYear(match, page);
            }
        }
    }

    @Nullable
    private Replacement convertLongDate(MatchResult matchMonth, MatchResult matchBefore, WikipediaPage page) {
        // 1) Day + Prep? + Month + Prep? + Year

        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();
        final TokenizedDate tokenizedDate = new TokenizedDate(matchMonth);
        tokenizedDate.setType(1);

        // Find the day in case the word before is a preposition
        final String wordBefore = matchBefore.group();
        final int startBefore = matchBefore.start();
        if (isPreposition(wordBefore, lang)) {
            tokenizedDate.setPrepBefore(wordBefore);

            final LinearMatchResult matchBefore2 = FinderUtils.findWordBefore(text, startBefore);
            if (matchBefore2 == null) {
                return null;
            } else {
                final String spaceBefore2 = text.substring(matchBefore2.end(), startBefore);
                if (isNotSpace(spaceBefore2)) {
                    return null;
                }
                final String wordBefore2 = matchBefore2.group();
                if (isDay(wordBefore2)) {
                    tokenizedDate.setDay(wordBefore2);
                    tokenizedDate.setStart(matchBefore2.start());
                } else {
                    return null;
                }
            }
        } else {
            tokenizedDate.setDay(wordBefore);
            tokenizedDate.setStart(startBefore);
        }

        if (!findPrepositionAfterAndYear(tokenizedDate, page, matchMonth)) {
            return null;
        }

        final String date = text.substring(tokenizedDate.getStart(), tokenizedDate.getEnd());
        return findDateReplacement(date, tokenizedDate, page);
    }

    @Nullable
    private Replacement convertMonthYear(MatchResult matchMonth, MatchResult matchBefore, WikipediaPage page) {
        // 2) Connector + Month + Prep? + Year (month-year)

        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();
        final TokenizedDate tokenizedDate = new TokenizedDate(matchMonth);
        tokenizedDate.setType(2);

        final String wordBefore = matchBefore.group();
        if (isConnector(wordBefore, lang)) {
            tokenizedDate.setConnector(wordBefore);
            tokenizedDate.setStart(matchBefore.start());
        } else {
            return null;
        }

        if (!findPrepositionAfterAndYear(tokenizedDate, page, matchMonth)) {
            // Special case: the date after the connector is unordered of type month-day-year
            final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, matchMonth.end());
            if (matchAfter != null && isDay(matchAfter.group())) {
                return convertMonthDayYear(matchMonth, page);
            } else {
                return null;
            }
        }

        final String date = text.substring(tokenizedDate.getStart(), tokenizedDate.getEnd());
        return findDateReplacement(date, tokenizedDate, page);
    }

    private boolean isNotSpace(String str) {
        // Allow commas before
        final String space = StringUtils.removeStart(str, ",");
        return !FinderUtils.isSpace(space);
    }

    private boolean findPrepositionAfterAndYear(
        TokenizedDate tokenizedDate,
        WikipediaPage page,
        MatchResult matchMonth
    ) {
        // Impure function!! Modify the tokenized date passed as a parameter.
        final WikipediaLanguage lang = page.getId().getLang();
        final String text = page.getContent();

        // Find the year with a possible preposition in the middle
        // We allow the dot to find years with a dot as a separator
        Set<Character> allowedChars = Set.of(YEAR_DOT);
        int endMonth = matchMonth.end();
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, endMonth, allowedChars);
        if (matchAfter == null) {
            return false;
        } else {
            final String spaceAfter = text.substring(endMonth, matchAfter.start());
            if (isNotSpace(spaceAfter)) {
                return false;
            }
            final String wordAfter = matchAfter.group();
            final int endAfter = matchAfter.end();
            if (isPreposition(wordAfter, lang)) {
                tokenizedDate.setPrepAfter(wordAfter);

                final LinearMatchResult matchAfter2 = FinderUtils.findWordAfter(text, endAfter, allowedChars);
                if (matchAfter2 == null) {
                    return false;
                } else {
                    final String spaceAfter2 = text.substring(endAfter, matchAfter2.start());
                    if (isNotSpace(spaceAfter2)) {
                        return false;
                    }
                    final String wordAfter2 = matchAfter2.group();
                    if (isYear(wordAfter2)) {
                        tokenizedDate.setYear(wordAfter2);
                        tokenizedDate.setEnd(matchAfter2.end());
                    } else {
                        return false;
                    }
                }
            } else if (isYear(wordAfter)) {
                tokenizedDate.setYear(wordAfter);
                tokenizedDate.setEnd(endAfter);
            } else {
                return false;
            }
        }
        return true;
    }

    @Nullable
    private Replacement convertMonthDayYear(MatchResult matchMonth, WikipediaPage page) {
        // 3) Month + Day + Prep? + Year

        final String text = page.getContent();
        final TokenizedDate tokenizedDate = new TokenizedDate(matchMonth);
        tokenizedDate.setType(3);

        final int endMonth = matchMonth.end();
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, endMonth);
        if (matchAfter == null) {
            return null;
        } else {
            final String spaceAfter = text.substring(endMonth, matchAfter.start());
            if (isNotSpace(spaceAfter)) {
                return null;
            }
            final String wordAfter = matchAfter.group();
            final int endAfter = matchAfter.end();
            if (isDay(wordAfter)) {
                tokenizedDate.setDay(wordAfter);
                tokenizedDate.setEnd(endAfter);
            } else {
                return null;
            }
        }

        if (!findPrepositionAfterAndYear(tokenizedDate, page, matchAfter)) {
            return null;
        }

        final String date = text.substring(tokenizedDate.getStart(), tokenizedDate.getEnd());
        return findDateReplacement(date, tokenizedDate, page);
    }

    @Nullable
    private Replacement convertYearMonthDay(MatchResult matchMonth, MatchResult matchBefore, WikipediaPage page) {
        // 4) Year,? + Month + Day

        final String text = page.getContent();
        final TokenizedDate tokenizedDate = new TokenizedDate(matchMonth);
        tokenizedDate.setType(4);

        tokenizedDate.setYear(matchBefore.group());
        tokenizedDate.setStart(matchBefore.start());

        final int endMonth = matchMonth.end();
        final LinearMatchResult matchAfter = FinderUtils.findWordAfter(text, endMonth);
        if (matchAfter == null) {
            return null;
        } else {
            final String spaceAfter = text.substring(endMonth, matchAfter.start());
            if (isNotSpace(spaceAfter)) {
                return null;
            }
            final String wordAfter = matchAfter.group();
            if (isDay(wordAfter)) {
                tokenizedDate.setDay(wordAfter);
                tokenizedDate.setEnd(matchAfter.end());
            } else {
                return null;
            }
        }

        final String date = text.substring(tokenizedDate.getStart(), tokenizedDate.getEnd());
        return findDateReplacement(date, tokenizedDate, page);
    }

    @Nullable
    private Replacement findDateReplacement(String dateText, TokenizedDate date, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();

        // The last checks are the replacement types we want as a result
        ReplacementType type = null;

        // Preposition Before
        String prepBefore = date.getPrepBefore();
        if (date.getType() != 2 && !isValidPrepositionBefore(prepBefore, lang)) {
            prepBefore = getPrepositionDefault(lang);
            type = ReplacementType.DATE_INCOMPLETE;
        }

        // Preposition After
        String prepAfter = date.getPrepAfter();
        if (!isValidPrepositionAfter(prepAfter)) {
            prepAfter = getPrepositionDefault(lang);
            type = ReplacementType.DATE_INCOMPLETE;
        }

        // Year
        String year = date.getYear();
        if (!isValidYear(year)) {
            year = fixYearWithDot(year);
            type = ReplacementType.DATE_DOT_YEAR;
        }

        // Day
        String day = date.getDay();
        if (date.getType() != 2 && !isValidDay(day)) {
            day = fixLeadingZero(day);
            type = ReplacementType.DATE_LEADING_ZERO;
        }

        // Month
        String month = date.getMonth();
        if (!isValidMonth(month)) {
            month = fixUpperCaseMonth(month);
            type = ReplacementType.DATE_UPPERCASE;
        }

        if (date.getType() == 3 || date.getType() == 4) {
            type = ReplacementType.DATE_UNORDERED;
        } else if (type == null) {
            // Date not to be fixed
            return null;
        }

        // Cosmetic fixes in case we actually fix the date
        month = fixSeptember(month, lang);
        prepAfter = fixPrepositionAfter(prepAfter, lang);

        final String fixedDate;
        if (date.getType() == 2) {
            fixedDate = date.getConnector() + SPACE + month + SPACE + prepAfter + SPACE + year;
        } else {
            fixedDate = day + SPACE + prepBefore + SPACE + month + SPACE + prepAfter + SPACE + year;
        }

        return buildDateReplacement(page, type, date.getStart(), dateText, fixedDate);
    }

    private boolean isDay(String token) {
        // It could be ended with a comma in case of month-day-year
        return (
            StringUtils.isNotEmpty(token) &&
            token.length() <= 2 &&
            StringUtils.isNumeric(token) &&
            Integer.parseInt(token) <= 31 &&
            Integer.parseInt(token) > 0
        );
    }

    private boolean isPreposition(String token, WikipediaLanguage lang) {
        return langPrepositions.containsMapping(lang, token);
    }

    private boolean isConnector(String token, WikipediaLanguage lang) {
        return langConnectors.containsMapping(lang, token);
    }

    private boolean isYear(String token) {
        // It could be ended with a comma in case of year-month-day
        final String year;
        if (token.length() == 4) {
            year = token;
        } else if (token.length() == 5) {
            if (token.charAt(1) != YEAR_DOT) {
                return false;
            }
            year = StringUtils.remove(token, YEAR_DOT);
        } else {
            return false;
        }
        return StringUtils.isNumeric(year) && Integer.parseInt(year) <= CURRENT_YEAR;
    }

    private boolean isValidDay(String day) {
        return !day.startsWith("0");
    }

    private String fixLeadingZero(String day) {
        return day.substring(1);
    }

    private boolean isValidPrepositionBefore(@Nullable String prepBefore, WikipediaLanguage lang) {
        return getPrepositionDefault(lang).equals(prepBefore);
    }

    private String getPrepositionDefault(WikipediaLanguage lang) {
        return langPrepositions.get(lang).get(0);
    }

    private boolean isValidMonth(String month) {
        return FinderUtils.startsWithLowerCase(month);
    }

    private String fixUpperCaseMonth(String month) {
        return FinderUtils.setFirstLowerCase(month);
    }

    private String fixSeptember(String month, WikipediaLanguage lang) {
        return WikipediaLanguage.SPANISH.equals(lang) && "setiembre".equals(month) ? "septiembre" : month;
    }

    private boolean isValidPrepositionAfter(@Nullable String prepAfter) {
        return StringUtils.isAllLowerCase(prepAfter);
    }

    private String fixPrepositionAfter(String prepAfter, WikipediaLanguage lang) {
        return WikipediaLanguage.SPANISH.equals(lang) ? getPrepositionDefault(lang) : prepAfter;
    }

    private boolean isValidYear(String year) {
        return year.length() == 4;
    }

    private String fixYearWithDot(String year) {
        // We assume the year has a dot in the expected place
        return year.charAt(0) + year.substring(2);
    }

    private Replacement buildDateReplacement(
        WikipediaPage page,
        ReplacementType type,
        int originalStart,
        String originalDate,
        String fixedDate
    ) {
        // Default before fixing article (if applicable)
        int start = originalStart;
        String text = originalDate;
        List<Suggestion> suggestions = Collections.singletonList(Suggestion.ofNoComment(fixedDate));

        // Fix article (but for dates starting with connector)
        if (FinderUtils.startsWithNumber(fixedDate)) {
            final List<String> articlePair = getFixArticle(page, start);
            if (!articlePair.isEmpty()) {
                final String article = articlePair.get(0);
                final String alternative = articlePair.get(1);
                start -= (article.length() + 1); // + 1 because of the space between
                text = page.getContent().substring(start, start + article.length() + 1 + originalDate.length());

                final String fixedDateWithoutArticle = article + " " + fixedDate;
                final String fixedDateWithArticle = alternative + " " + fixedDate;
                suggestions =
                    List.of(
                        Suggestion.of(fixedDateWithArticle, "con artículo"),
                        Suggestion.of(fixedDateWithoutArticle, "sin artículo")
                    );
            }
        }

        return Replacement.builder().type(type).start(start).text(text).suggestions(suggestions).build();
    }

    // Return the appropriate article for the date
    private List<String> getFixArticle(WikipediaPage page, int start) {
        final LinearMatchResult preceding = FinderUtils.findWordBefore(page.getContent(), start);
        final WikipediaLanguage lang = page.getId().getLang();
        if (preceding != null) {
            final String precedingArticle = preceding.group();
            final String precedingAlternative = langArticles.get(lang).get(precedingArticle);
            if (precedingAlternative != null) {
                return List.of(precedingArticle, precedingAlternative);
            }
        }
        return Collections.emptyList();
    }

    private static class MonthFinder {

        private final String monthSearch;

        MonthFinder(String month) {
            this.monthSearch = month.substring(1);
        }

        public Iterable<MatchResult> find(WikipediaPage page) {
            return IterableUtils.toList(LinearMatchFinder.find(page, this::findMonth));
        }

        private int findMonth(WikipediaPage page, int start, List<MatchResult> matches) {
            final WikipediaLanguage lang = page.getId().getLang();
            final String text = page.getContent();
            final int startMonth = findStartMonth(text, start);
            if (startMonth >= 0) {
                final String month = text.charAt(startMonth) + monthSearch;
                if (isMonth(month, lang)) {
                    matches.add(LinearMatchResult.of(startMonth, month));
                }
                return startMonth + month.length();
            } else {
                return -1;
            }
        }

        private int findStartMonth(String text, int start) {
            final int startMonthSearch = text.indexOf(monthSearch, start);
            return startMonthSearch >= 1 ? startMonthSearch - 1 : -1;
        }

        private boolean isMonth(String token, WikipediaLanguage lang) {
            return langMonths.containsMapping(lang, token);
        }
    }

    @Data
    private static class TokenizedDate {

        int type;
        int start;
        int end;
        String connector;
        String day;
        String prepBefore;
        String month;
        String prepAfter;
        String year;

        TokenizedDate(MatchResult matchMonth) {
            this.start = matchMonth.start();
            this.end = matchMonth.end();
            this.month = matchMonth.group();
        }
    }
}
