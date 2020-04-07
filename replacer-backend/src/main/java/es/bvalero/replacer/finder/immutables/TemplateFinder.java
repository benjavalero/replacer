package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;

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
public class TemplateFinder implements ImmutableFinder {
    // The nested regex takes more but it is worth as it captures completely the templates with inner templates
    // A linear optimization, instead of regex, is too complex and it is not worth for the moment
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
        "galería de imágenes",
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
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        // There may be really long cites
        return 100000;
    }

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<>(text, AUTOMATON_TEMPLATE, this::convert);
    }
}
