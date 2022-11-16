package es.bvalero.replacer.finder.replacement.finders;

import static org.apache.commons.lang3.StringUtils.SPACE;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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

    private static final List<Character> YEAR_DOT = List.of('.');
    private static final int CURRENT_YEAR = LocalDate.now().getYear();

    private static final List<String> ENGLISH_MONTHS = List.of(
        "january",
        "february",
        "march",
        "april",
        "may",
        "june",
        "july",
        "august",
        "september",
        "october",
        "november",
        "december"
    );

    @Resource
    private Map<String, String> monthNames;

    @Resource
    private Map<String, String> yearPrepositions;

    @Resource
    private Map<String, String> dateConnectors;

    @Resource
    private Map<String, String> dateArticles;

    private static final ListValuedMap<WikipediaLanguage, String> langMonths = new ArrayListValuedHashMap<>();
    private static final ListValuedMap<WikipediaLanguage, String> langPrepositions = new ArrayListValuedHashMap<>();
    private static final SetValuedMap<WikipediaLanguage, String> langConnectors = new HashSetValuedHashMap<>();
    private static final Map<WikipediaLanguage, Map<String, String>> langArticles = new EnumMap<>(
        WikipediaLanguage.class
    );

    private static final Map<WikipediaLanguage, RunAutomaton> automata = new HashMap<>();

    @PostConstruct
    public void init() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            // Months: the ones for the language and the English ones, all in lower and uppercase.
            // Plus the special case of "setiembre" in Spanish
            langMonths.putAll(lang, FinderUtils.splitList(monthNames.get(lang.getCode())));
            List<String> monthsLowerCase = new ArrayList<>();
            monthsLowerCase.addAll(langMonths.get(lang));
            monthsLowerCase.addAll(ENGLISH_MONTHS);
            if (WikipediaLanguage.SPANISH.equals(lang)) {
                // Trick only for Spanish months
                monthsLowerCase.add("setiembre");
            }
            List<String> monthsLowerUpperCase = new ArrayList<>();
            monthsLowerCase.forEach(month -> {
                monthsLowerUpperCase.add(month);
                monthsLowerUpperCase.add(FinderUtils.setFirstUpperCase(month));
            });

            // Prepositions
            langPrepositions.putAll(lang, FinderUtils.splitList(yearPrepositions.get(lang.getCode())));
            List<String> prepositionsLowerUpperCase = new ArrayList<>();
            langPrepositions
                .get(lang)
                .forEach(preposition -> {
                    prepositionsLowerUpperCase.add(preposition);
                    prepositionsLowerUpperCase.add(FinderUtils.setFirstUpperCase(preposition));
                });

            // Connectors
            langConnectors.putAll(lang, FinderUtils.splitList(dateConnectors.get(lang.getCode())));
            List<String> connectorsLowerUpperCase = new ArrayList<>();
            langConnectors
                .get(lang)
                .forEach(connector -> {
                    connectorsLowerUpperCase.add(connector);
                    connectorsLowerUpperCase.add(FinderUtils.setFirstUpperCase(connector));
                });

            // Articles
            langArticles.put(lang, new HashMap<>());
            FinderUtils
                .splitListAsStream(dateArticles.get(lang.getCode()))
                .forEach(pair -> langArticles.get(lang).putAll(buildArticleMap(pair)));

            // Regex
            String regexPrepositions = String.format("(%s)", FinderUtils.joinAlternate(prepositionsLowerUpperCase));
            String regexConnectors = String.format("(%s)", FinderUtils.joinAlternate(connectorsLowerUpperCase));
            String regexMonthsLowerUpperCase = String.format("(%s)", FinderUtils.joinAlternate(monthsLowerUpperCase));
            String regexSpaces = String.format(
                "(%s)+",
                FinderUtils.joinAlternate(
                    FinderUtils.SPACES
                        .stream()
                        .map(s -> s.replace("{", "\\{").replace("}", "\\}"))
                        .collect(Collectors.toUnmodifiableList())
                )
            );

            String regexDay = "([012]?<N>|3[01])";
            String regexYear = "[12]\\.?<N>{3}";
            String regexLongDate = String.format(
                "%s%s(%s%s)?%s,?%s(%s%s)?%s",
                regexDay,
                regexSpaces,
                regexPrepositions,
                regexSpaces,
                regexMonthsLowerUpperCase,
                regexSpaces,
                regexPrepositions,
                regexSpaces,
                regexYear
            );
            String regexMonthYear = String.format(
                "%s%s%s,?%s(%s%s)?%s",
                regexConnectors,
                regexSpaces,
                regexMonthsLowerUpperCase,
                regexSpaces,
                regexPrepositions,
                regexSpaces,
                regexYear
            );
            String regexMonthDayYear = String.format(
                "%s%s%s,?%s(%s%s)?%s",
                regexMonthsLowerUpperCase,
                regexSpaces,
                regexDay,
                regexSpaces,
                regexPrepositions,
                regexSpaces,
                regexYear
            );
            String regexYearMonthDay = String.format(
                "%s,?%s%s%s%s",
                regexYear,
                regexSpaces,
                regexMonthsLowerUpperCase,
                regexSpaces,
                regexDay
            );

            String regex = String.format(
                "(%s|%s|%s|%s)",
                regexLongDate,
                regexMonthYear,
                regexMonthDayYear,
                regexYearMonthDay
            );

            automata.put(lang, new RunAutomaton(new RegExp(regex).toAutomaton(new DatatypesAutomatonProvider())));
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
        // The performance was better with a linear approach just checking the 12 lang months,
        // but once we also check the English months we use the automaton approach which gives
        // a slightly better performance and is easier to maintain.

        // For the sake of performance and not to duplicate steps and code,
        // it is better to override the main method, and validate and convert at a time.
        final List<Replacement> results = new ArrayList<>(100);
        for (MatchResult match : findMatchResults(page)) {
            CollectionUtils.addIgnoreNull(results, convertDate(match, page));
        }
        return results;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automata.get(page.getId().getLang()));
    }

    @Override
    public Replacement convert(MatchResult matchResult, WikipediaPage page) {
        // We cannot use this same method because we need it to be able to return a null
        throw new IllegalCallerException();
    }

    @Nullable
    public Replacement convertDate(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        if (!FinderUtils.isWordCompleteInText(match.start(), date, page.getContent())) {
            return null;
        }

        final LinearMatchResult matchWord = FinderUtils.findWordAfter(date, 0, YEAR_DOT);
        assert matchWord != null;
        final String firstWord = matchWord.group();
        if (isRegexDay(firstWord)) {
            return convertLongDate(match, page);
        }
        if (isRegexConnector(firstWord, lang)) {
            return convertMonthYear(match, page);
        }
        if (isRegexMonth(firstWord, lang)) {
            return convertMonthDayYear(match, page);
        }
        if (isRegexYear(firstWord)) {
            return convertYearMonthDay(match, page);
        } else {
            return null;
        }
    }

    private boolean isRegexDay(String token) {
        return StringUtils.isNumeric(token) && token.length() <= 2;
    }

    private boolean isRegexConnector(String token, WikipediaLanguage lang) {
        return langConnectors.containsMapping(lang, FinderUtils.setFirstLowerCase(token));
    }

    private boolean isRegexMonth(String month, WikipediaLanguage lang) {
        final String monthLower = FinderUtils.setFirstLowerCase(month);
        return (
            isEnglishMonth(month) ||
            langMonths.containsMapping(lang, monthLower) ||
            (WikipediaLanguage.SPANISH.equals(lang) && "setiembre".equals(monthLower))
        );
    }

    private boolean isRegexYear(String token) {
        if (token.length() == 4) {
            return StringUtils.isNumeric(token);
        } else if (token.length() == 5) {
            return Character.isDigit(token.charAt(0)) && token.charAt(1) == '.';
        } else {
            return false;
        }
    }

    @Nullable
    private Replacement convertLongDate(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        boolean isDateValid = true;

        LinearMatchResult matchWord = FinderUtils.findWordAfter(date, 0);
        assert matchWord != null;
        String fixedDay = matchWord.group();
        if (isFixableDay(fixedDay)) {
            fixedDay = fixDay(fixedDay);
            isDateValid = false;
        }
        if (isFalseDay(fixedDay)) {
            return null;
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end()); // Prep Before or Month
        assert matchWord != null;
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end());
            assert matchWord != null;
        } else {
            isDateValid = false;
        }
        String fixedMonth = matchWord.group();
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
            isDateValid = false;
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT); // Prep After or Year
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT);
            assert matchWord != null;
        } else {
            prepAfter = getPrepositionDefault(lang);
            isDateValid = false;
        }
        String fixedYear = matchWord.group();
        if (isFixableYear(fixedYear)) {
            fixedYear = fixYear(fixedYear);
            isDateValid = false;
        }
        if (isFalseYear(fixedYear)) {
            return null;
        }

        // Validation
        if (isDateValid) {
            return null;
        }

        // Cosmetic fixes in case we actually fix the date
        fixedMonth = fixSeptember(fixedMonth, lang);
        prepAfter = fixPrepositionAfter(prepAfter, lang);

        final String prepBefore = getPrepositionDefault(lang);
        final String fixedDate = String.format(
            "%s %s %s %s %s",
            fixedDay,
            prepBefore,
            fixedMonth,
            prepAfter,
            fixedYear
        );

        return buildDateReplacement(page, match.start(), date, fixedDate);
    }

    @Nullable
    private Replacement convertMonthYear(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        boolean isDateValid = true;

        final String date = match.group();
        LinearMatchResult matchWord = FinderUtils.findWordAfter(date, 0);
        assert matchWord != null;
        final String connector = matchWord.group();

        matchWord = FinderUtils.findWordAfter(date, matchWord.end());
        assert matchWord != null;
        String fixedMonth = matchWord.group();
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
            isDateValid = false;
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT); // Prep After or Year
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT);
            assert matchWord != null;
        } else {
            prepAfter = getPrepositionDefault(lang);
            isDateValid = false;
        }
        String fixedYear = matchWord.group();
        if (isFixableYear(fixedYear)) {
            fixedYear = fixYear(fixedYear);
            isDateValid = false;
        }
        if (isFalseYear(fixedYear)) {
            return null;
        }

        // Validation
        if (isDateValid) {
            return null;
        }

        // Cosmetic fixes in case we actually fix the date
        fixedMonth = fixSeptember(fixedMonth, lang);
        prepAfter = fixPrepositionAfter(prepAfter, lang);

        final String fixedDate = String.format("%s %s %s %s", connector, fixedMonth, prepAfter, fixedYear);

        return buildDateReplacement(page, match.start(), date, fixedDate);
    }

    @Nullable
    private Replacement convertMonthDayYear(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();

        LinearMatchResult matchWord = FinderUtils.findWordAfter(date, 0);
        assert matchWord != null;
        String fixedMonth = matchWord.group();
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end());
        assert matchWord != null;
        String fixedDay = matchWord.group();
        if (isFixableDay(fixedDay)) {
            fixedDay = fixDay(fixedDay);
        }
        if (isFalseDay(fixedDay)) {
            return null;
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT); // Prep After or Year
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_DOT);
            assert matchWord != null;
        } else {
            prepAfter = getPrepositionDefault(lang);
        }
        String fixedYear = matchWord.group();
        if (isFixableYear(fixedYear)) {
            fixedYear = fixYear(fixedYear);
        }
        if (isFalseYear(fixedYear)) {
            return null;
        }

        // Cosmetic fixes in case we actually fix the date
        fixedMonth = fixSeptember(fixedMonth, lang);
        prepAfter = fixPrepositionAfter(prepAfter, lang);

        final String prepBefore = getPrepositionDefault(lang);
        final String fixedDate = String.format(
            "%s %s %s %s %s",
            fixedDay,
            prepBefore,
            fixedMonth,
            prepAfter,
            fixedYear
        );

        return buildDateReplacement(page, match.start(), date, fixedDate);
    }

    @Nullable
    private Replacement convertYearMonthDay(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();

        LinearMatchResult matchWord = FinderUtils.findWordAfter(date, 0, YEAR_DOT);
        assert matchWord != null;
        String fixedYear = matchWord.group();
        if (isFixableYear(fixedYear)) {
            fixedYear = fixYear(fixedYear);
        }
        if (isFalseYear(fixedYear)) {
            return null;
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end());
        assert matchWord != null;
        String fixedMonth = matchWord.group();
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
        }

        matchWord = FinderUtils.findWordAfter(date, matchWord.end());
        assert matchWord != null;
        String fixedDay = matchWord.group();
        if (isFixableDay(fixedDay)) {
            fixedDay = fixDay(fixedDay);
        }
        if (isFalseDay(fixedDay)) {
            return null;
        }

        // Cosmetic fixes in case we actually fix the date
        fixedMonth = fixSeptember(fixedMonth, lang);

        final String preposition = getPrepositionDefault(lang);
        final String fixedDate = String.format(
            "%s %s %s %s %s",
            fixedDay,
            preposition,
            fixedMonth,
            preposition,
            fixedYear
        );

        return buildDateReplacement(page, match.start(), date, fixedDate);
    }

    private boolean isFixableDay(String day) {
        return day.length() == 2 && day.charAt(0) == '0';
    }

    private String fixDay(String day) {
        return day.substring(1);
    }

    private boolean isFalseDay(String day) {
        return "0".equals(day);
    }

    private boolean isPreposition(String token, WikipediaLanguage lang) {
        return langPrepositions.containsMapping(lang, FinderUtils.setFirstLowerCase(token));
    }

    private boolean isFixableMonth(String month) {
        return FinderUtils.startsWithUpperCase(month) || isEnglishMonth(month);
    }

    private String fixMonth(String month, WikipediaLanguage lang) {
        String fixedMonth = FinderUtils.setFirstLowerCase(month);
        if (isEnglishMonth(fixedMonth)) {
            fixedMonth = fixEnglishMonth(fixedMonth, lang);
        }
        return fixedMonth;
    }

    private boolean isEnglishMonth(String month) {
        return ENGLISH_MONTHS.contains(FinderUtils.setFirstLowerCase(month));
    }

    private String fixEnglishMonth(String month, WikipediaLanguage lang) {
        return langMonths.get(lang).get(ENGLISH_MONTHS.indexOf(month));
    }

    private String fixSeptember(String month, WikipediaLanguage lang) {
        // The September fix is a trick only for Spanish months
        return WikipediaLanguage.SPANISH.equals(lang) && "setiembre".equals(month) ? "septiembre" : month;
    }

    private boolean isFixableYear(String year) {
        return year.length() == 5;
    }

    private String fixYear(String year) {
        return year.charAt(0) + year.substring(2);
    }

    private boolean isFalseYear(String year) {
        try {
            return Integer.parseInt(year) > CURRENT_YEAR;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private String getPrepositionDefault(WikipediaLanguage lang) {
        return langPrepositions.get(lang).get(0);
    }

    private String fixPrepositionAfter(String prepAfter, WikipediaLanguage lang) {
        return WikipediaLanguage.SPANISH.equals(lang)
            ? getPrepositionDefault(lang)
            : FinderUtils.setFirstLowerCase(prepAfter);
    }

    private Replacement buildDateReplacement(
        WikipediaPage page,
        int originalStart,
        String originalDate,
        String fixedDate
    ) {
        // Default before fixing article (if applicable)
        int start = originalStart;
        String text = originalDate;
        final List<Suggestion> suggestions = new LinkedList<>();
        // Default suggestion
        suggestions.add(Suggestion.ofNoComment(fixedDate));

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
                suggestions.clear();
                suggestions.add(Suggestion.of(fixedDateWithArticle, "con artículo"));
                suggestions.add(Suggestion.of(fixedDateWithoutArticle, "sin artículo"));
            }
        }

        // Add a warning if the date contains an English month
        if (ENGLISH_MONTHS.stream().anyMatch(m -> FinderUtils.toLowerCase(originalDate).contains(m))) {
            suggestions.add(0, Suggestion.of(text, "no reemplazar si el contexto está en inglés"));
        }

        return Replacement
            .builder()
            .type(ReplacementType.DATE)
            .start(start)
            .text(text)
            .suggestions(suggestions)
            .build();
    }

    // Return the appropriate article for the date
    private List<String> getFixArticle(WikipediaPage page, int start) {
        final String text = page.getContent();
        final LinearMatchResult preceding = FinderUtils.findWordBefore(text, start);
        final WikipediaLanguage lang = page.getId().getLang();
        // Check there is a space between the article and the date
        if (preceding != null && SPACE.equals(text.substring(preceding.end(), start))) {
            final String precedingArticle = preceding.group();
            final String precedingAlternative = langArticles.get(lang).get(precedingArticle);
            if (precedingAlternative != null) {
                return List.of(precedingArticle, precedingAlternative);
            }
        }
        return Collections.emptyList();
    }
}
