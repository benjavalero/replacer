package es.bvalero.replacer.finder.immutable;

import dk.brics.automaton.DatatypesAutomatonProvider;
import dk.brics.automaton.RegExp;
import dk.brics.automaton.RunAutomaton;
import es.bvalero.replacer.finder.common.FinderPage;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find template-related immutables:
 * <ul>
 *     <li>Find template names, e.g. `Bandera` in `{{Bandera|España}}`</li>
 *     <li>Find some complete templates, even with nested templates, e.g. `{{Cite|A cite}}`.
 *     The list of template names is configured in `template-names.xml`.</li>
 *     <li>Find template parameters, e.g. `param` in `{{Template|param=value}}`.
 *     For some specific parameters (see `template-param.xml`), we include in the result also the value,
 *     which is usually a taxonomy, a Commons category, etc.
 *     Finally, we include also the value if it seems like a file or a domain.</li>
 * </ul>
 */
@Component
class CompleteTemplateFinder extends ImmutableCheckedFinder {

    private static final String START_TEMPLATE = "{{";
    private static final String END_TEMPLATE = "}}";

    @Resource
    private List<String> templateNames;

    @Resource
    private Set<String> paramNames;

    @Resource
    private Set<String> paramValues;

    private RunAutomaton automatonTemplateNames;

    @PostConstruct
    public void initAutomaton() {
        automatonTemplateNames =
            new RunAutomaton(
                new RegExp(String.format("(%s)", StringUtils.join(toUpperCase(templateNames), '|')))
                .toAutomaton(new DatatypesAutomatonProvider())
            );
    }

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.VERY_HIGH;
    }

    @Override
    public Iterable<Immutable> find(FinderPage page) {
        List<Immutable> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : findAllTemplates(page)) {
            immutables.addAll(findImmutables(template));
        }
        return immutables;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        // We are overriding the more general find method
        throw new IllegalCallerException();
    }

    @VisibleForTesting
    List<LinearMatchResult> findAllTemplates(FinderPage page) {
        List<LinearMatchResult> matches = new ArrayList<>(100);

        // Each template found may contain nested templates which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            List<LinearMatchResult> subMatches = new LinkedList<>();
            start = findTemplate(page, start, subMatches);
            matches.addAll(subMatches);
        }

        return matches;
    }

    private int findTemplate(FinderPage page, int start, List<LinearMatchResult> matches) {
        String text = page.getContent();
        int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            LinearMatchResult completeMatch = findNestedTemplate(text, startTemplate, matches);
            if (completeMatch != null) {
                matches.add(0, completeMatch);
                return completeMatch.end();
            } else {
                // Template not closed. Not worth keep on searching.
                Immutable immutable = Immutable.of(
                    startTemplate,
                    FinderUtils.getContextAroundWord(text, startTemplate, startTemplate, CONTEXT_THRESHOLD)
                );
                logWarning(immutable, page, "Template not closed");
                return -1;
            }
        } else {
            // No more templates
            return -1;
        }
    }

    private int findStartTemplate(String text, int start) {
        return text.indexOf(START_TEMPLATE, start);
    }

    private int findEndTemplate(String text, int start) {
        return text.indexOf(END_TEMPLATE, start);
    }

    /* Find the immutable of the template. It also finds nested templates and adds them to the given list. */
    @Nullable
    private LinearMatchResult findNestedTemplate(String text, int startTemplate, List<LinearMatchResult> matches) {
        List<LinearMatchResult> nestedMatches = new ArrayList<>();
        int start = startTemplate;
        if (text.startsWith(START_TEMPLATE, start)) {
            start += START_TEMPLATE.length();
        }
        while (true) {
            int end = findEndTemplate(text, start);
            if (end < 0) {
                return null;
            }

            int startNested = findStartTemplate(text, start);
            if (startNested >= 0 && startNested < end) {
                // Nested
                // Find the end of the nested which can be the found end or forward in case of more nesting levels
                LinearMatchResult nestedMatch = findNestedTemplate(text, startNested, matches);
                if (nestedMatch == null) {
                    return null;
                }

                matches.add(0, nestedMatch);
                nestedMatches.add(nestedMatch);

                // Prepare to find the next nested
                start = nestedMatch.end();
            } else {
                LinearMatchResult completeMatch = LinearMatchResult.of(
                    startTemplate,
                    text.substring(startTemplate, end + END_TEMPLATE.length())
                );
                completeMatch.addGroups(nestedMatches);
                return completeMatch;
            }
        }
    }

    private List<String> toUpperCase(List<String> names) {
        return names.stream().map(this::toUpperCase).collect(Collectors.toList());
    }

    private String toUpperCase(String word) {
        return FinderUtils.startsWithLowerCase(word) ? FinderUtils.setFirstUpperCaseClass(word) : word;
    }

    private List<Immutable> findImmutables(LinearMatchResult template) {
        String content = template.group();

        // Special case "{{|}}"
        if ("{{|}}".equals(content)) {
            return Collections.emptyList();
        }

        // Remove the content of the nested templates
        for (int i = template.groupCount() - 1; i >= 0; i--) {
            content =
                content.substring(0, template.start(i) - template.start()) +
                content.substring(template.end(i) - template.start());
        }

        // Remove the start end end of the template
        content = content.substring(START_TEMPLATE.length(), content.length() - END_TEMPLATE.length());

        String[] parameters = content.split("\\|");

        String templateName = parameters[0];
        // Check in case the template name is followed by a colon
        int posColon = templateName.indexOf(':');
        if (posColon >= 0) {
            templateName = templateName.substring(0, posColon);
        }

        // If the template name is in the list we instead return an immutable of the complete template
        if (automatonTemplateNames.run(templateName.trim())) {
            return Collections.singletonList(this.convert(template));
        }

        List<Immutable> immutables = new ArrayList<>();

        // Add the template name
        immutables.add(Immutable.of(template.start() + START_TEMPLATE.length(), templateName));

        // Process the rest of parameters
        for (int i = 1; i < parameters.length; i++) {
            String parameter = parameters[i];
            String param = parameter;
            String value = null;
            int posEquals = parameter.indexOf('=');
            if (posEquals >= 0) {
                param = parameter.substring(0, posEquals);
                value = parameter.substring(posEquals + 1).trim();
            }

            // Always return the parameter
            int startParameter = template.start() + template.group().indexOf("|" + param) + 1;
            immutables.add(Immutable.of(startParameter, param));

            if (StringUtils.isNotEmpty(value)) {
                // If the value is followed by a reference, comment or similar we ignore it
                int posLessThan = value.indexOf('<');
                if (posLessThan >= 0) {
                    value = value.substring(0, posLessThan).trim();
                }

                // If the param is in the list
                // or the value is in the list
                // or the value is a file or a domain
                // then we also return the value
                if (
                    paramNames.contains(FinderUtils.toLowerCase(param.trim())) ||
                    paramValues.contains(FinderUtils.toLowerCase(value)) ||
                    matchesFile(value)
                ) {
                    int startValue = startParameter + posEquals + 1;
                    immutables.add(Immutable.of(startValue, value));
                }
            }
        }

        return immutables;
    }

    private boolean matchesFile(String value) {
        int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4;
        } else {
            return false;
        }
    }
}
