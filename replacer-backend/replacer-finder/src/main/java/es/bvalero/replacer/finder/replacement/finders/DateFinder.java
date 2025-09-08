package es.bvalero.replacer.finder.replacement.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.DOT;
import static es.bvalero.replacer.finder.util.FinderUtils.ENGLISH_LANGUAGE;
import static org.apache.commons.lang3.StringUtils.SPACE;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.StandardType;
import es.bvalero.replacer.finder.Suggestion;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.time.Year;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase.
 */
@Component
class DateFinder implements ReplacementFinder {

    private static final List<Character> YEAR_ALLOWED_CHARS = List.of(DOT);
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
    private static final Map<WikipediaLanguage, RunAutomaton> automata = new EnumMap<>(WikipediaLanguage.class);

    DateFinder(FinderProperties finderProperties) {
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
                    monthsLowerUpperCase.add(FinderUtils.setFirstUpperCase(month));
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
            List<String> prepositionsLowerUpperCase = new ArrayList<>();
            langPrepositions
                .get(lang)
                .forEach(preposition -> {
                    prepositionsLowerUpperCase.add(preposition);
                    prepositionsLowerUpperCase.add(FinderUtils.setFirstUpperCase(preposition));
                });

            // Connectors
            langConnectors.putAll(lang, this.finderProperties.getDateConnectors().get(lang.getCode()));
            List<String> connectorsLowerUpperCase = new ArrayList<>();
            langConnectors
                .get(lang)
                .forEach(connector -> {
                    connectorsLowerUpperCase.add(connector);
                    connectorsLowerUpperCase.add(FinderUtils.setFirstUpperCase(connector));
                });

            // Articles
            langArticles.put(lang, new HashMap<>());
            this.finderProperties.getDateArticles()
                .get(lang.getCode())
                .forEach(dateArticle -> langArticles.get(lang).putAll(buildArticleMap(dateArticle)));

            // Regex
            String regexPrepositions = String.format("(%s)", FinderUtils.joinAlternate(prepositionsLowerUpperCase));
            String regexConnectors = String.format("(%s)", FinderUtils.joinAlternate(connectorsLowerUpperCase));
            String regexMonthsLowerUpperCase = String.format("(%s)", FinderUtils.joinAlternate(monthsLowerUpperCase));
            String regexSpaces = String.format(
                "(%s)+",
                FinderUtils.joinAlternate(FinderUtils.SPACES.stream().map(ReplacerUtils::escapeRegexChars).toList())
            );

            // There is no performance gain by using more generic regex for these numbers
            String regexDay = "([012]?[0-9]|3[01])";
            String regexYear = "[12]\\.?[0-9]{3}";
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

    private Map<String, String> buildArticleMap(FinderProperties.DateArticle dateArticle) {
        final Map<String, String> articleMap = new HashMap<>();
        articleMap.put(dateArticle.getPrep(), dateArticle.getArticle());
        articleMap.put(
            FinderUtils.setFirstUpperCase(dateArticle.getPrep()),
            FinderUtils.setFirstUpperCase(dateArticle.getArticle())
        );
        return articleMap;
    }

    @Override
    public Stream<Replacement> find(FinderPage page) {
        // The performance was better with a linear approach just checking the 12 lang months,
        // but once we also check the English months we use the automaton approach which gives
        // a slightly better performance and is easier to maintain.

        // For the sake of performance and not to duplicate steps and code,
        // it is better to override the main method, and validate and convert at a time.
        return findMatchResults(page).map(match -> convertDate(match, page)).filter(Objects::nonNull);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return AutomatonMatchFinder.find(page.getContent(), automata.get(page.getPageKey().getLang()));
    }

    @Override
    public Replacement convert(MatchResult matchResult, FinderPage page) {
        // We cannot use this same method because we need it to be able to return a null
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Replacement convertDate(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        if (!FinderUtils.isWordCompleteInText(match, page.getContent())) {
            return null;
        }

        final String date = match.group();
        final MatchResult matchWord = FinderUtils.findWordAfter(date, 0, YEAR_ALLOWED_CHARS, false);
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
            throw new IllegalStateException("Unknown date first word: " + firstWord);
        }
    }

    private boolean isRegexDay(String token) {
        return token.length() <= 2 && FinderUtils.isNumeric(token);
    }

    private boolean isRegexConnector(String token, WikipediaLanguage lang) {
        return langConnectors.containsMapping(lang, FinderUtils.setFirstLowerCase(token));
    }

    private boolean isRegexMonth(String month, WikipediaLanguage lang) {
        final String monthLower = FinderUtils.setFirstLowerCase(month);
        return (
            isEnglishMonth(month) ||
            langMonths.containsMapping(lang, monthLower) ||
            (lang == WikipediaLanguage.SPANISH && "setiembre".equals(monthLower))
        );
    }

    private boolean isRegexYear(String token) {
        if (token.length() == 4) {
            return FinderUtils.isNumeric(token);
        } else if (token.length() == 5) {
            return FinderUtils.isDigit(token.charAt(0)) && token.charAt(1) == '.';
        } else {
            throw new IllegalStateException();
        }
    }

    @Nullable
    private Replacement convertLongDate(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String date = match.group();
        boolean isDateValid = true;

        MatchResult matchWord = FinderUtils.findWordAfter(date, 0);
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

        // Prep After or Year
        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
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
    private Replacement convertMonthYear(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        boolean isDateValid = true;

        final String date = match.group();
        MatchResult matchWord = FinderUtils.findWordAfter(date, 0);
        assert matchWord != null;
        final String connector = matchWord.group();

        matchWord = FinderUtils.findWordAfter(date, matchWord.end());
        assert matchWord != null;
        String fixedMonth = matchWord.group();
        if (isFixableMonth(fixedMonth)) {
            fixedMonth = fixMonth(fixedMonth, lang);
            isDateValid = false;
        }

        // Prep After or Year
        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
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
    private Replacement convertMonthDayYear(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String date = match.group();

        MatchResult matchWord = FinderUtils.findWordAfter(date, 0);
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

        // Prep After or Year
        matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
        assert matchWord != null;
        String prepAfter = matchWord.group();
        if (isPreposition(matchWord.group(), lang)) {
            matchWord = FinderUtils.findWordAfter(date, matchWord.end(), YEAR_ALLOWED_CHARS, false);
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
    private Replacement convertYearMonthDay(MatchResult match, FinderPage page) {
        final WikipediaLanguage lang = page.getPageKey().getLang();
        final String date = match.group();

        MatchResult matchWord = FinderUtils.findWordAfter(date, 0, YEAR_ALLOWED_CHARS, false);
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

    @SneakyThrows
    private boolean isFalseYear(String year) {
        // At this point the year string should match an integer
        return Integer.parseInt(year) > CURRENT_YEAR;
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

                final String fixedDateWithoutArticle = article + " " + fixedDate;
                final String fixedDateWithArticle = alternative + " " + fixedDate;
                suggestions.clear();
                suggestions.add(Suggestion.of(fixedDateWithArticle, "con artículo"));
                suggestions.add(Suggestion.of(fixedDateWithoutArticle, "sin artículo"));
            }
        }

        // Add a warning if the date contains an English month
        if (englishMonths.stream().anyMatch(originalDate::contains)) {
            suggestions.add(0, Suggestion.of(text, "no reemplazar si el contexto está en inglés"));
        }

        return Replacement.of(start, text, StandardType.DATE, suggestions, page.getContent());
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
