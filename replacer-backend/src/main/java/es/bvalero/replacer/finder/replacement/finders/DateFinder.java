package es.bvalero.replacer.finder.replacement.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase.
 */
@Component
public class DateFinder implements ReplacementFinder {

    @Resource
    private Map<String, String> monthNames;

    @Resource
    private Map<String, String> dateConnectors;

    @Resource
    private Map<String, String> yearPrepositions;

    @Resource
    private Map<String, String> dateArticles;

    private final SetValuedMap<WikipediaLanguage, String> langMonths = new HashSetValuedHashMap<>();
    private final SetValuedMap<WikipediaLanguage, String> langConnectors = new HashSetValuedHashMap<>();
    private final ListValuedMap<WikipediaLanguage, String> langPrepositions = new ArrayListValuedHashMap<>();
    private final Map<WikipediaLanguage, Map<String, String>> langArticles = new EnumMap<>(WikipediaLanguage.class);

    private final Map<WikipediaLanguage, RunAutomaton> dateAutomata = new EnumMap<>(WikipediaLanguage.class);

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
            Arrays
                .stream(monthNames.get(lang.getCode()).split(","))
                .forEach(month -> {
                    this.langMonths.put(lang, month);
                    this.langMonths.put(lang, FinderUtils.setFirstUpperCase(month));
                });

            Arrays
                .stream(dateConnectors.get(lang.getCode()).split(","))
                .forEach(connector -> {
                    this.langConnectors.put(lang, connector);
                    this.langConnectors.put(lang, FinderUtils.setFirstUpperCase(connector));
                });

            Arrays
                .stream(yearPrepositions.get(lang.getCode()).split(","))
                .forEach(preposition -> {
                    this.langPrepositions.put(lang, preposition);
                    this.langPrepositions.put(lang, FinderUtils.setFirstUpperCase(preposition));
                });

            // Date formats:
            // 1) Day + Prep? + Month + Prep? + Year
            // 2) Connector + Ø + Month + Prep? + Year
            // 3) Month + Ø + Day + Prep? + Year
            // 4) Year + Comma? + Month + Ø + Day

            final String regexConnectors = String.format("(%s)", StringUtils.join(this.langConnectors.get(lang), "|"));
            final String regexMonths = String.format("(%s)", StringUtils.join(this.langMonths.get(lang), "|"));
            final String regexPrepositions = String.format(
                "( (%s))?",
                StringUtils.join(this.langPrepositions.get(lang), "|")
            );

            @RegExp
            final String regexDay = "(0?[1-9]|[12]<N>|3[01])";
            @RegExp
            final String regexYear = "[12]\\.?<N>{3}";
            @RegExp
            final String regexComma = ",?";

            final String regex1 = String.format(
                "%s%s %s%s %s",
                regexDay,
                regexPrepositions,
                regexMonths,
                regexPrepositions,
                regexYear
            );
            final String regex2 = String.format(
                "%s %s%s %s",
                regexConnectors,
                regexMonths,
                regexPrepositions,
                regexYear
            );
            final String regex3 = String.format(
                "%s %s(%s|%s) %s",
                regexMonths,
                regexDay,
                regexComma,
                regexPrepositions,
                regexYear
            );
            final String regex4 = String.format("%s%s %s %s", regexYear, regexComma, regexMonths, regexDay);

            final String regexDate = String.format("(%s|%s|%s|%s)", regex1, regex2, regex3, regex4);
            final RunAutomaton dateAutomaton = new RunAutomaton(
                new dk.brics.automaton.RegExp(regexDate).toAutomaton(new DatatypesAutomatonProvider())
            );
            dateAutomata.put(lang, dateAutomaton);

            // Finally configure the date articles
            if (dateArticles.containsKey(lang.getCode())) {
                this.langArticles.put(lang, new HashMap<>());
                Arrays
                    .stream(dateArticles.get(lang.getCode()).split(","))
                    .forEach(pair -> this.langArticles.get(lang).putAll(buildArticleMap(pair)));
            }
        }
    }

    private Map<String, String> buildArticleMap(String pair) {
        final Map<String, String> articleMap = new HashMap<>();
        final String[] tokens = pair.split("-");
        articleMap.put(tokens[0], tokens[1]);
        articleMap.put(FinderUtils.setFirstUpperCase(tokens[0]), FinderUtils.setFirstUpperCase(tokens[1]));
        return articleMap;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        final RunAutomaton dateAutomaton = dateAutomata.get(page.getId().getLang());
        return dateAutomaton != null
            ? AutomatonMatchFinder.find(page.getContent(), dateAutomaton)
            : Collections.emptyList();
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return ReplacementFinder.super.validate(match, page) && validateDate(match, page);
    }

    private boolean validateDate(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        final String token1 = Arrays.stream(date.split(" ")).findFirst().orElseThrow(IllegalArgumentException::new);
        if (isDay(token1)) {
            // Long date: DMY
            return !isValidLongDate(match, page);
        } else if (isConnector(token1, lang)) {
            // Connector + MY
            return !isValidMonthYear(date, lang);
        }
        return true;
    }

    private boolean isDay(String token) {
        return token.length() <= 2 && startsWithNumber(token);
    }

    private boolean isYear(String token) {
        return token.length() > 2 && startsWithNumber(token);
    }

    private boolean startsWithNumber(String token) {
        return Character.isDigit(token.charAt(0));
    }

    private boolean isMonth(String token, WikipediaLanguage lang) {
        return this.langMonths.containsMapping(lang, token);
    }

    private boolean isConnector(String token, WikipediaLanguage lang) {
        return this.langConnectors.containsMapping(lang, token);
    }

    private boolean isPreposition(String token, WikipediaLanguage lang) {
        return this.langPrepositions.containsMapping(lang, token);
    }

    private boolean isPrepositionDefault(String token, WikipediaLanguage lang) {
        return token.equals(getPrepositionDefault(lang));
    }

    private String getPrepositionDefault(WikipediaLanguage lang) {
        return this.langPrepositions.get(lang).get(0);
    }

    private boolean isValidLongDate(MatchResult match, WikipediaPage page) {
        // We can assume at this point that the core tokens are day-month-year
        final WikipediaLanguage lang = page.getId().getLang();
        final String[] tokens = match.group().split(" ");
        return (
            tokens.length == 5 &&
            !tokens[0].startsWith("0") &&
            isPrepositionDefault(tokens[1], lang) &&
            FinderUtils.startsWithLowerCase(tokens[2]) &&
            isPreposition(tokens[3], lang) &&
            tokens[4].length() == 4 &&
            !isPrecededByArticleToBeFixed(page, match.start())
        );
    }

    private boolean isValidMonthYear(String date, WikipediaLanguage lang) {
        // We can assume at this point that the core tokens are connector-month-year
        final String[] tokens = date.split(" ");
        return (
            tokens.length == 4 &&
            FinderUtils.startsWithLowerCase(tokens[1]) &&
            isPreposition(tokens[2], lang) &&
            tokens[3].length() == 4
        );
    }

    @Override
    public Replacement convert(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String[] tokens = match.group().split(" ");
        final String token1 = tokens[0];
        if (isDay(token1)) {
            // Long date: DMY
            return convertLongDate(match, page);
        } else if (isConnector(token1, lang)) {
            // Connector + MY
            return convertMonthYear(match, page);
        } else if (isMonth(token1, lang)) {
            // Unordered MDY
            return convertMonthDayYear(match, page);
        } else if (isYear(token1)) {
            // Unordered YMD
            return convertYearMonthDay(match, page);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Replacement convertLongDate(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));
        ReplacementType type = ReplacementType.DATE_INCOMPLETE; // Default value

        // Fix year with dot
        final String year = tokens.get(tokens.size() - 1);
        if (year.contains(".")) {
            final String fixedYear = fixYearWithDot(year);
            tokens.set(tokens.size() - 1, fixedYear);
            type = ReplacementType.DATE_DOT_YEAR;
        }

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        if (!isPreposition(tokens.get(1), lang)) {
            tokens.add(1, defaultPreposition);
            type = ReplacementType.DATE_INCOMPLETE;
        } else if (!isPrepositionDefault(tokens.get(1), lang)) {
            tokens.set(1, defaultPreposition);
            type = ReplacementType.DATE_INCOMPLETE;
        }

        if (!isPreposition(tokens.get(3), lang)) {
            tokens.add(3, defaultPreposition);
            type = ReplacementType.DATE_INCOMPLETE;
        }

        // Fix leading zero
        final String day = tokens.get(0);
        if (day.startsWith("0")) {
            final String fixedDay = fixLeadingZero(day);
            tokens.set(0, fixedDay);
            type = ReplacementType.DATE_LEADING_ZERO;
        }

        // Fix uppercase
        final String month = tokens.get(2);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(2, fixedMonth);
            type = ReplacementType.DATE_UPPERCASE;
        }

        // Fix september
        tokens.set(2, fixSeptember(tokens.get(2), lang));

        final String fixedDate = StringUtils.join(tokens, " ");
        return buildDateReplacement(page, type, match.start(), date, fixedDate);
    }

    private Replacement convertMonthYear(MatchResult match, WikipediaPage page) {
        final String date = match.group();
        final WikipediaLanguage lang = page.getId().getLang();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));
        ReplacementType type = ReplacementType.DATE_INCOMPLETE; // Default value

        // Fix year with dot
        final String year = tokens.get(tokens.size() - 1);
        if (year.contains(".")) {
            final String fixedYear = fixYearWithDot(year);
            tokens.set(tokens.size() - 1, fixedYear);
            type = ReplacementType.DATE_DOT_YEAR;
        }

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        if (!isPreposition(tokens.get(2), lang)) {
            tokens.add(2, defaultPreposition);
            type = ReplacementType.DATE_INCOMPLETE;
        }

        // Fix uppercase
        final String month = tokens.get(1);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(1, fixedMonth);
            type = ReplacementType.DATE_UPPERCASE;
        }

        // Fix september
        tokens.set(1, fixSeptember(tokens.get(1), lang));

        final String fixedDate = StringUtils.join(tokens, " ");
        return buildDateReplacement(page, type, match.start(), date, fixedDate);
    }

    private Replacement convertMonthDayYear(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));

        // Fix year with dot
        final String year = tokens.get(tokens.size() - 1);
        if (year.contains(".")) {
            final String fixedYear = fixYearWithDot(year);
            tokens.set(tokens.size() - 1, fixedYear);
        }

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        tokens.add(1, defaultPreposition);

        if (!isPreposition(tokens.get(3), lang)) {
            tokens.add(3, defaultPreposition);
        }

        // Fix leading zero
        // Note: in this date format the day might be followed by a comma
        String day = removeTrailingComma(tokens.get(2));
        if (day.startsWith("0")) {
            day = fixLeadingZero(day);
        }
        tokens.set(2, day);

        // Fix uppercase
        final String month = tokens.get(0);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(0, fixedMonth);
        }

        // Fix september
        tokens.set(0, fixSeptember(tokens.get(0), lang));

        // Reorder the tokens
        Collections.swap(tokens, 0, 2);

        final String fixedDate = StringUtils.join(tokens, " ");
        return buildDateReplacement(page, ReplacementType.DATE_UNORDERED, match.start(), date, fixedDate);
    }

    private Replacement convertYearMonthDay(MatchResult match, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));

        // Fix year with dot
        // Note: in this date format the day might be followed by a comma
        String year = removeTrailingComma(tokens.get(0));
        if (year.contains(".")) {
            year = fixYearWithDot(year);
        }
        tokens.set(0, year);

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        tokens.add(1, defaultPreposition);
        tokens.add(3, defaultPreposition);

        // Fix leading zero
        final String day = tokens.get(4);
        if (day.startsWith("0")) {
            final String fixedDay = fixLeadingZero(day);
            tokens.set(4, fixedDay);
        }

        // Fix uppercase
        final String month = tokens.get(2);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(2, fixedMonth);
        }

        // Fix september
        tokens.set(2, fixSeptember(tokens.get(2), lang));

        // Reorder the tokens
        Collections.swap(tokens, 0, 4);

        final String fixedDate = StringUtils.join(tokens, " ");
        return buildDateReplacement(page, ReplacementType.DATE_UNORDERED, match.start(), date, fixedDate);
    }

    private String fixYearWithDot(String year) {
        // We assume the year has a dot in the expected place
        return year.charAt(0) + year.substring(2);
    }

    private String fixLeadingZero(String day) {
        // We assume the day starts with 0
        return day.substring(1);
    }

    private String fixSeptember(String month, WikipediaLanguage lang) {
        return WikipediaLanguage.SPANISH.equals(lang) && "setiembre".equals(month) ? "septiembre" : month;
    }

    private String removeTrailingComma(String token) {
        return token.endsWith(",") ? token.substring(0, token.length() - 1) : token;
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

                final String fixedDateWithoutArticle = String.format("%s %s", article, fixedDate);
                final String fixedDateWithArticle = String.format("%s %s", alternative, fixedDate);
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
        final String preceding = FinderUtils.findWordBefore(page.getContent(), start);
        final WikipediaLanguage lang = page.getId().getLang();
        if (preceding != null) {
            final String precedingAlternative = this.langArticles.get(lang).get(preceding);
            if (precedingAlternative != null) {
                return List.of(preceding, precedingAlternative);
            }
        }
        return Collections.emptyList();
    }

    private boolean isPrecededByArticleToBeFixed(WikipediaPage page, int start) {
        return !getFixArticle(page, start).isEmpty();
    }
}
