package es.bvalero.replacer.finder;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import es.bvalero.replacer.finder2.RegexIterable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find complete templates, even with nested templates, e. g. `{{Cite|A cite}}`
 */
@Component
class TemplateFinder implements ImmutableFinder {
    // The nested regex takes more but it is worth as it captures completely the templates with inner templates
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+?}}";
    private static final String REGEX_NESTED_TEMPLATE = "\\{\\{ *(%s)[ |\n]*[|:](%s|[^}])+?}}";
    private static final List<String> TEMPLATE_NAMES = Arrays.asList(
        "ORDENAR",
        "DEFAULTSORT",
        "NF",
        "TA",
        "commonscat",
        "coord",
        "cit[ae] ?<L>*",
        "quote",
        "cquote",
        "caja de cita",
        "traducido (de|ref)"
    );
    private static final RunAutomaton AUTOMATON_TEMPLATE;

    static {
        Set<String> wordsToJoin = new HashSet<>();
        for (String word : TEMPLATE_NAMES) {
            if (FinderUtils.startsWithLowerCase(word)) {
                wordsToJoin.add(FinderUtils.setFirstUpperCaseClass(word));
            } else {
                wordsToJoin.add(word);
            }
        }
        AUTOMATON_TEMPLATE =
            new RunAutomaton(
                new RegExp(String.format(REGEX_NESTED_TEMPLATE, StringUtils.join(wordsToJoin, "|"), REGEX_TEMPLATE))
                .toAutomaton(new DatatypesAutomatonProvider())
            );
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, AUTOMATON_TEMPLATE, this::convert, this::isValid);
    }
}
