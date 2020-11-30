package es.bvalero.replacer.finder.immutables;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find complete templates, even with nested templates, e.g. `{{Cite|A cite}}`
 */
@Component
public class TemplateFinder implements ImmutableFinder {
    // The nested regex takes more but it is worth as it captures completely the templates with inner templates
    // A linear optimization, instead of regex, is too complex and it is not worth for the moment
    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_TEMPLATE = "\\{\\{[^}]+}}";

    @org.intellij.lang.annotations.RegExp
    private static final String REGEX_NESTED = "\\{\\{<Zs>*(%s)(<Zs>|<Cc>)*[|:](%s|[^}])+}}";

    private RunAutomaton automatonTemplate;

    @Resource
    private List<String> templateNames;

    @PostConstruct
    public void initAutomaton() {
        automatonTemplate =
            new RunAutomaton(
                new RegExp(
                    String.format(REGEX_NESTED, StringUtils.join(toUpperCase(templateNames), '|'), REGEX_TEMPLATE)
                )
                .toAutomaton(new DatatypesAutomatonProvider())
            );
    }

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.MEDIUM;
    }

    @Override
    public int getMaxLength() {
        return 5000;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new RegexIterable<>(text, automatonTemplate, this::convert);
    }

    private List<String> toUpperCase(List<String> names) {
        return names.stream().map(this::toUpperCase).collect(Collectors.toList());
    }

    private String toUpperCase(String word) {
        return FinderUtils.startsWithLowerCase(word) ? FinderUtils.setFirstUpperCaseClass(word) : word;
    }
}
