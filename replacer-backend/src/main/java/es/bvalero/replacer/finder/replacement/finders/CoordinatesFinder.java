package es.bvalero.replacer.finder.replacement.finders;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.common.domain.*;
import es.bvalero.replacer.finder.replacement.ReplacementFinder;
import es.bvalero.replacer.finder.util.AutomatonMatchFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.Arrays;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find coordinates with wrong degree, minute or second symbols.
 */
@Component
public class CoordinatesFinder implements ReplacementFinder {

    private static final String DEGREE = "\u00b0"; // °
    private static final char MASCULINE_ORDINAL = '\u00ba'; // º
    private static final String PRIME = "\u2032"; // ′
    private static final char APOSTROPHE = '\'';
    private static final char SINGLE_QUOTE = '\u2019'; // ’
    private static final char ACUTE_ACCENT = '\u00b4'; // ´
    private static final String DOUBLE_PRIME = "\u2033"; // ″
    private static final String DOUBLE_QUOTE = "\\\"";

    private static final String REGEX_DEGREE = String.format("[%s%s]", DEGREE, MASCULINE_ORDINAL);
    private static final String REGEX_SPACE = String.format(
        "(%s)?",
        StringUtils.join(FinderUtils.SPACES, "|").replace("{", "\\{")
    );
    private static final String REGEX_PRIME = String.format(
        "[%s%s%s%s]",
        PRIME,
        APOSTROPHE,
        SINGLE_QUOTE,
        ACUTE_ACCENT
    );
    private static final String REGEX_DOUBLE_PRIME = String.format(
        "([%s%s]|[%s%s%s%s]{2})",
        DOUBLE_PRIME,
        DOUBLE_QUOTE,
        PRIME,
        APOSTROPHE,
        SINGLE_QUOTE,
        ACUTE_ACCENT
    );

    private static final String REGEX_DEGREE_NUMBER = "1?<N>{1,2}";
    private static final String REGEX_MINUTE_NUMBER = "[0-5]?<N>";

    private static final String REGEX_COORDINATES = String.format(
        "%s%s%s%s%s%s%s%s",
        REGEX_DEGREE_NUMBER,
        REGEX_DEGREE,
        REGEX_SPACE,
        REGEX_MINUTE_NUMBER,
        REGEX_PRIME,
        REGEX_SPACE,
        REGEX_MINUTE_NUMBER,
        REGEX_DOUBLE_PRIME
    );
    private static final RunAutomaton AUTOMATON_COORDINATES = new RunAutomaton(
        new dk.brics.automaton.RegExp(REGEX_COORDINATES).toAutomaton(new DatatypesAutomatonProvider())
    );

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        return AutomatonMatchFinder.find(page.getContent(), AUTOMATON_COORDINATES);
    }

    @Override
    public boolean validate(MatchResult match, WikipediaPage page) {
        return ReplacementFinder.super.validate(match, page) && !isValidCoordinates(match.group());
    }

    private boolean isValidCoordinates(String coordinates) {
        // Let's just check the symbols splitting by numbers
        final List<String> tokens = Arrays
            .stream(coordinates.split("\\d"))
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toUnmodifiableList());
        return (
            tokens.size() == 3 &&
            tokens.get(0).startsWith(DEGREE) &&
            tokens.get(1).startsWith(PRIME) &&
            tokens.get(2).startsWith(DOUBLE_PRIME)
        );
    }

    @Override
    public Replacement convert(MatchResult match, WikipediaPage page) {
        final String coordinates = match.group();
        // Let's just find the numbers
        final List<String> tokens = Arrays
            .stream(coordinates.split("\\D"))
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.toUnmodifiableList());

        String fixedCoordinates = String.format(
            "%s%s%s%s%s%s",
            tokens.get(0),
            DEGREE,
            tokens.get(1),
            PRIME,
            tokens.get(2),
            DOUBLE_PRIME
        );

        return Replacement
            .builder()
            .type(ReplacementType.COORDINATES)
            .start(match.start())
            .text(coordinates)
            .suggestions(List.of(Suggestion.ofNoComment(fixedCoordinates)))
            .build();
    }
}
