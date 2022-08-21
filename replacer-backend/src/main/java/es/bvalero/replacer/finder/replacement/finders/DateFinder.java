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

    static final String SUBTYPE_DOT_YEAR = "Año con punto";
    static final String SUBTYPE_INCOMPLETE = "Fecha incompleta";
    static final String SUBTYPE_LEADING_ZERO = "Día con cero";
    static final String SUBTYPE_UPPERCASE = "Mes en mayúscula";
    static final String SUBTYPE_UNORDERED = "Fecha desordenada";

    @Resource
    private Map<String, String> monthNames;

    @Resource
    private Map<String, String> dateConnectors;

    @Resource
    private Map<String, String> yearPrepositions;

    private final SetValuedMap<WikipediaLanguage, String> langMonths = new HashSetValuedHashMap<>();
    private final SetValuedMap<WikipediaLanguage, String> langConnectors = new HashSetValuedHashMap<>();
    private final ListValuedMap<WikipediaLanguage, String> langPrepositions = new ArrayListValuedHashMap<>();

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

            // The regex consists in 5 tokens for the 4 date formats:
            // 1) Day + Prep? + Month + Prep? + Year
            // 2) Connector + Ø + Month + Prep? + Year
            // 3) Month + Ø + Day + Prep? + Year
            // 4) Year + Prep? + Month + Ø + Day

            final String regexConnectors = StringUtils.join(this.langConnectors.get(lang), "|");
            final String regexMonths = StringUtils.join(this.langMonths.get(lang), "|");

            // Add the comma as a preposition and a space before the prepositions
            final List<String> prepositions =
                this.langPrepositions.get(lang).stream().map(p -> " " + p).collect(Collectors.toList());
            prepositions.add(",");
            final String regexPrepositions = String.format("(%s)?", StringUtils.join(prepositions, "|"));

            @RegExp
            final String regexDay = "(0?[1-9]|[12]<N>|3[01])";
            @RegExp
            final String regexYear = "[12]\\.?<N>{3}";

            final String regex1 = String.format("(%s|%s|%s|%s)", regexYear, regexDay, regexConnectors, regexMonths);
            final String regex3 = String.format("(%s|%s)", regexDay, regexMonths);
            final String regex5 = String.format("(%s|%s)", regexYear, regexDay);

            final String regexDate = String.format(
                "%s%s %s%s %s",
                regex1,
                regexPrepositions,
                regex3,
                regexPrepositions,
                regex5
            );
            final RunAutomaton dateAutomaton = new RunAutomaton(
                new dk.brics.automaton.RegExp(regexDate).toAutomaton(new DatatypesAutomatonProvider())
            );
            dateAutomata.put(lang, dateAutomaton);
        }
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
        return ReplacementFinder.super.validate(match, page) && validateDate(match.group(), page.getId().getLang());
    }

    private boolean validateDate(String date, WikipediaLanguage lang) {
        List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));

        // Remove the prepositions
        if (isPreposition(tokens.get(1), lang)) {
            tokens.remove(1);
        }
        if (isPreposition(tokens.get(2), lang)) {
            tokens.remove(2);
        }
        if (tokens.size() != 3) {
            return false;
        }

        final String token1 = tokens.get(0);
        final String token3 = tokens.get(1);
        final String token5 = tokens.get(2);
        if (isDay(token1)) {
            // Long date: DMY
            return isMonth(token3, lang) && isYear(token5) && !isValidLongDate(date, lang);
        } else if (isConnector(token1, lang)) {
            // Connector + MY
            return isMonth(token3, lang) && isYear(token5) && !isValidMonthYear(date, lang);
        } else if (isMonth(token1, lang)) {
            final String normalizedToken3 = token3.replace(",", "");
            // Unordered MDY
            return isDay(normalizedToken3) && isYear(token5);
        } else if (isYear(token1)) {
            // Unordered YMD
            return isMonth(token3, lang) && isDay(token5);
        } else {
            return false;
        }
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

    private boolean isValidLongDate(String date, WikipediaLanguage lang) {
        // We can assume at this point that the core tokens are day-month-year
        final String[] tokens = date.split(" ");
        return (
            tokens.length == 5 &&
            !tokens[0].startsWith("0") &&
            isPrepositionDefault(tokens[1], lang) &&
            FinderUtils.startsWithLowerCase(tokens[2]) &&
            isPreposition(tokens[3], lang) &&
            tokens[4].length() == 4
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
    public Replacement convert(MatchResult matcher, WikipediaPage page) {
        final WikipediaLanguage lang = page.getId().getLang();
        final String[] tokens = matcher.group().split(" ");
        final String token1 = tokens[0];
        if (isDay(token1)) {
            // Long date: DMY
            return convertLongDate(matcher, lang);
        } else if (isConnector(token1, lang)) {
            // Connector + MY
            return convertMonthYear(matcher, lang);
        } else if (isMonth(token1, lang)) {
            // Unordered MDY
            return convertMonthDayYear(matcher, lang);
        } else if (isYear(token1)) {
            // Unordered YMD
            return convertYearMonthDay(matcher, lang);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private Replacement convertLongDate(MatchResult match, WikipediaLanguage lang) {
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));
        String subtype = null;

        // Fix year with dot
        final String year = tokens.get(tokens.size() - 1);
        if (year.contains(".")) {
            final String fixedYear = fixYearWithDot(year);
            tokens.set(tokens.size() - 1, fixedYear);
            subtype = SUBTYPE_DOT_YEAR;
        }

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        if (isMonth(tokens.get(1), lang)) {
            tokens.add(1, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        } else if (!isPrepositionDefault(tokens.get(1), lang)) {
            tokens.set(1, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        }

        if (isYear(tokens.get(3))) {
            tokens.add(3, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        } else if (!isPreposition(tokens.get(3), lang)) {
            tokens.set(3, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix leading zero
        final String day = tokens.get(0);
        if (day.startsWith("0")) {
            final String fixedDay = fixLeadingZero(day);
            tokens.set(0, fixedDay);
            subtype = SUBTYPE_LEADING_ZERO;
        }

        // Fix uppercase
        final String month = tokens.get(2);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(2, fixedMonth);
            subtype = SUBTYPE_UPPERCASE;
        }

        // Fix september
        tokens.set(2, fixSeptember(tokens.get(2), lang));

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        final String fixedDate = StringUtils.join(tokens, " ");
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, subtype))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private Replacement convertMonthYear(MatchResult match, WikipediaLanguage lang) {
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));
        String subtype = null;

        // Fix year with dot
        final String year = tokens.get(tokens.size() - 1);
        if (year.contains(".")) {
            final String fixedYear = fixYearWithDot(year);
            tokens.set(tokens.size() - 1, fixedYear);
            subtype = SUBTYPE_DOT_YEAR;
        }

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        if (isYear(tokens.get(2))) {
            tokens.add(2, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        } else if (!isPreposition(tokens.get(2), lang)) {
            tokens.set(2, defaultPreposition);
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix uppercase
        final String month = tokens.get(1);
        if (FinderUtils.startsWithUpperCase(month)) {
            final String fixedMonth = FinderUtils.setFirstLowerCase(month);
            tokens.set(1, fixedMonth);
            subtype = SUBTYPE_UPPERCASE;
        }

        // Fix september
        tokens.set(1, fixSeptember(tokens.get(1), lang));

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        final String fixedDate = StringUtils.join(tokens, " ");
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, subtype))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private Replacement convertMonthDayYear(MatchResult match, WikipediaLanguage lang) {
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
        if (isDay(tokens.get(1).replace(",", ""))) {
            tokens.add(1, defaultPreposition);
        } else if (!isPrepositionDefault(tokens.get(1), lang)) {
            tokens.set(1, defaultPreposition);
        }

        if (isYear(tokens.get(3))) {
            tokens.add(3, defaultPreposition);
        } else if (!isPreposition(tokens.get(3), lang)) {
            tokens.set(3, defaultPreposition);
        }

        // Fix leading zero
        String day = tokens.get(2);
        if (day.startsWith("0")) {
            day = fixLeadingZero(day);
        }
        if (day.endsWith(",")) {
            day = day.substring(0, day.length() - 1);
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
        final String swap = tokens.get(0);
        tokens.set(0, tokens.get(2));
        tokens.set(2, swap);

        final String fixedDate = StringUtils.join(tokens, " ");
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, SUBTYPE_UNORDERED))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private Replacement convertYearMonthDay(MatchResult match, WikipediaLanguage lang) {
        final String date = match.group();
        final List<String> tokens = Arrays.stream(date.split(" ")).collect(Collectors.toCollection(LinkedList::new));

        // Fix year with dot
        String year = tokens.get(0);
        if (year.contains(".")) {
            year = fixYearWithDot(year);
        }
        if (year.endsWith(",")) {
            year = year.substring(0, year.length() - 1);
        }
        tokens.set(0, year);

        // Add/fix missing prepositions
        final String defaultPreposition = getPrepositionDefault(lang);
        if (isMonth(tokens.get(1), lang)) {
            tokens.add(1, defaultPreposition);
        } else if (!isPrepositionDefault(tokens.get(1), lang)) {
            tokens.set(1, defaultPreposition);
        }

        if (isDay(tokens.get(3))) {
            tokens.add(3, defaultPreposition);
        } else if (!isPreposition(tokens.get(3), lang)) {
            tokens.set(3, defaultPreposition);
        }

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
        final String swap = tokens.get(0);
        tokens.set(0, tokens.get(4));
        tokens.set(4, swap);

        final String fixedDate = StringUtils.join(tokens, " ");
        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, SUBTYPE_UNORDERED))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
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

    private List<Suggestion> findSuggestions(String date) {
        return Collections.singletonList(Suggestion.ofNoComment(date));
    }
}
