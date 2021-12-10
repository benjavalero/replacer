package es.bvalero.replacer.finder.replacement.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.ReplacementKind;
import es.bvalero.replacer.common.domain.ReplacementType;
import es.bvalero.replacer.common.domain.Suggestion;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.replacement.Replacement;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.intellij.lang.annotations.RegExp;
import org.springframework.stereotype.Component;

/**
 * Find dates to be corrected, e.g. with the month in uppercase
 */
@Component
public class DateFinder implements ReplacementFinder {

    static final String SUBTYPE_DOT_YEAR = "Año con punto";
    static final String SUBTYPE_INCOMPLETE = "Fecha incompleta";
    static final String SUBTYPE_LEADING_ZERO = "Día con cero";
    static final String SUBTYPE_UPPERCASE = "Mes en mayúscula";

    @RegExp
    private static final String REGEX_DATE = "(%s|(3[01]|[012]?<N>)( [Dd]e)?) (%s) ((%s) )?[12]\\.?<N>{3}";

    @Resource
    private Map<String, String> monthNames;

    @Resource
    private Map<String, String> dateConnectors;

    @Resource
    private Map<String, String> yearPrepositions;

    private final SetValuedMap<WikipediaLanguage, String> langPrepositions = new HashSetValuedHashMap<>();

    private final Map<WikipediaLanguage, RunAutomaton> dateAutomata = new EnumMap<>(WikipediaLanguage.class);

    @PostConstruct
    public void init() {
        for (WikipediaLanguage lang : WikipediaLanguage.values()) {
            if (!monthNames.containsKey(lang.getCode()) || !dateConnectors.containsKey(lang.getCode())) {
                // Skip this language as it has no configuration for date finder
                continue;
            }

            final List<String> monthsLowerCase = Arrays.asList(monthNames.get(lang.getCode()).split(","));
            final List<String> connectors = Arrays.asList(dateConnectors.get(lang.getCode()).split(","));
            final List<String> monthsUpperCaseClass = monthsLowerCase
                .stream()
                .map(FinderUtils::setFirstUpperCaseClass)
                .collect(Collectors.toUnmodifiableList());
            final List<String> connectorsUpperCaseClass = connectors
                .stream()
                .map(FinderUtils::setFirstUpperCaseClass)
                .collect(Collectors.toUnmodifiableList());
            this.langPrepositions.putAll(lang, Arrays.asList(yearPrepositions.get(lang.getCode()).split(",")));
            final List<String> prepositionsUpperCaseClass =
                this.langPrepositions.get(lang)
                    .stream()
                    .map(FinderUtils::setFirstUpperCaseClass)
                    .collect(Collectors.toUnmodifiableList());
            final RunAutomaton dateAutomaton = new RunAutomaton(
                new dk.brics.automaton.RegExp(
                    String.format(
                        REGEX_DATE,
                        StringUtils.join(connectorsUpperCaseClass, "|"),
                        StringUtils.join(monthsUpperCaseClass, "|"),
                        StringUtils.join(prepositionsUpperCaseClass, "|")
                    )
                )
                    .toAutomaton(new DatatypesAutomatonProvider())
            );
            dateAutomata.put(lang, dateAutomaton);
        }
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        RunAutomaton dateAutomaton = dateAutomata.get(page.getLang());
        return dateAutomaton != null
            ? AutomatonMatchFinder.find(page.getContent(), dateAutomaton)
            : Collections.emptyList();
    }

    @Override
    public boolean validate(MatchResult match, FinderPage page) {
        return ReplacementFinder.super.validate(match, page) && !isValidDate(match.group());
    }

    private boolean isValidDate(String date) {
        return startsWithNumber(date) ? isValidLongDate(date) : isValidMonthYear(date);
    }

    private boolean isValidLongDate(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        return (
            tokens.size() == 5 &&
            FinderUtils.toLowerCase(date).equals(date) &&
            !date.startsWith("0") &&
            tokens.get(4).length() == 4
        );
    }

    private boolean isValidMonthYear(String date) {
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        return (
            tokens.size() == 4 &&
            FinderUtils.toLowerCase(date.substring(1)).equals(date.substring(1)) &&
            tokens.get(3).length() == 4
        );
    }

    @Override
    public Replacement convert(MatchResult matcher, FinderPage page) {
        WikipediaLanguage lang = page.getLang();
        return startsWithNumber(matcher.group()) ? convertLongDate(matcher, lang) : convertMonthYear(matcher, lang);
    }

    private boolean startsWithNumber(String text) {
        return Character.isDigit(text.charAt(0));
    }

    private Replacement convertLongDate(MatchResult match, WikipediaLanguage lang) {
        String date = match.group();
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        String subtype = null;

        // Fix year with dot
        String year = tokens.get(tokens.size() - 1);
        String fixedYear = fixYearWithDot(year);
        if (!fixedYear.equals(year)) {
            subtype = SUBTYPE_DOT_YEAR;
            tokens.set(tokens.size() - 1, fixedYear);
        }

        // Add missing prepositions
        if (isNotPreposition(tokens.get(1), lang)) {
            tokens.add(1, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }
        if (isNotPreposition(tokens.get(3), lang)) {
            tokens.add(3, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix leading zero
        String day = tokens.get(0);
        String fixedDay = fixLeadingZero(day);
        if (!fixedDay.equals(day)) {
            subtype = SUBTYPE_LEADING_ZERO;
            tokens.set(0, fixedDay);
        }

        // Fix uppercase
        String fixedDate = StringUtils.join(tokens, " ");
        String lowerDate = FinderUtils.toLowerCase(fixedDate);
        if (!lowerDate.equals(fixedDate)) {
            subtype = SUBTYPE_UPPERCASE;
            fixedDate = lowerDate;
        }

        // Fix September
        fixedDate = fixSeptember(fixedDate, lang);

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, subtype))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private Replacement convertMonthYear(MatchResult match, WikipediaLanguage lang) {
        String date = match.group();
        List<String> tokens = new LinkedList<>(Arrays.asList(date.split(" ")));
        String subtype = null;

        // Fix year with dot
        String year = tokens.get(tokens.size() - 1);
        String fixedYear = fixYearWithDot(year);
        if (!fixedYear.equals(year)) {
            subtype = SUBTYPE_DOT_YEAR;
            tokens.set(tokens.size() - 1, fixedYear);
        }

        // Add missing prepositions
        if (isNotPreposition(tokens.get(2), lang)) {
            tokens.add(2, "de");
            subtype = SUBTYPE_INCOMPLETE;
        }

        // Fix uppercase
        String monthYear = StringUtils.join(tokens.subList(1, tokens.size()), " ");
        String lowerMonthYear = FinderUtils.toLowerCase(monthYear);
        if (!lowerMonthYear.equals(monthYear)) {
            subtype = SUBTYPE_UPPERCASE;
            monthYear = lowerMonthYear;
        }

        // Fix September
        monthYear = fixSeptember(monthYear, lang);

        String fixedDate = String.format("%s %s", tokens.get(0), monthYear);

        if (subtype == null) {
            throw new IllegalArgumentException(String.format("Not valid date to convert: %s", date));
        }

        return Replacement
            .builder()
            .type(ReplacementType.of(ReplacementKind.DATE, subtype))
            .start(match.start())
            .text(date)
            .suggestions(findSuggestions(fixedDate))
            .build();
    }

    private String fixYearWithDot(String year) {
        return year.charAt(1) == '.' ? year.charAt(0) + year.substring(2) : year;
    }

    private boolean isNotPreposition(String word, WikipediaLanguage lang) {
        return !this.langPrepositions.get(lang).contains(FinderUtils.toLowerCase(word));
    }

    private String fixLeadingZero(String day) {
        return day.startsWith("0") ? day.substring(1) : day;
    }

    private String fixSeptember(String date, WikipediaLanguage lang) {
        return WikipediaLanguage.SPANISH.equals(lang) ? date.replace("setiembre", "septiembre") : date;
    }

    private List<Suggestion> findSuggestions(String date) {
        return Collections.singletonList(Suggestion.ofNoComment(date));
    }
}
