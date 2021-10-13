package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.immutable.Immutable;
import es.bvalero.replacer.finder.immutable.ImmutableCheckedFinder;
import es.bvalero.replacer.finder.immutable.ImmutableFinderPriority;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
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
public class TemplateFinder extends ImmutableCheckedFinder {

    private static final String WILDCARD = "*";
    public static final String START_TEMPLATE = "{{";
    public static final String END_TEMPLATE = "}}";

    @Resource
    private List<String> templateParams;

    // Set with the names of the templates to be ignored as a whole
    private final Set<String> templateNames = new HashSet<>();

    // Set with the partial names of the templates to be ignored as a whole
    private final Set<String> templateNamesPartial = new HashSet<>();

    // Set with the names of the parameters whose values will be ignored no matter the template they are in
    private final Set<String> paramNames = new HashSet<>();

    // Map with the pairs (template name-param) whose values will be ignored
    // Take into account that there might be several param names for the same template name
    private final SetValuedMap<String, String> templateParamPairs = new HashSetValuedHashMap<>();

    @PostConstruct
    public void initTemplateParams() {
        for (String pair : templateParams) {
            String[] tokens = pair.split("\\|");
            if (tokens.length == 2) {
                String name = FinderUtils.toLowerCase(tokens[0].trim());
                String param = FinderUtils.toLowerCase(tokens[1].trim());
                if (WILDCARD.equals(param)) {
                    if (name.endsWith(WILDCARD)) {
                        this.templateNamesPartial.add(name.substring(0, name.length() - 1));
                    } else {
                        this.templateNames.add(name);
                    }
                } else if (WILDCARD.equals(name)) {
                    this.paramNames.add(param);
                } else {
                    this.templateParamPairs.put(name, param);
                }
            }
        }
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

    public List<LinearMatchResult> findAllTemplates(FinderPage page) {
        List<LinearMatchResult> matches = new ArrayList<>(100);

        // Each template found may contain nested templates which are added after
        int start = 0;
        while (start >= 0 && start < page.getContent().length()) {
            // Use a LinkedList as some elements will be prepended
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
                // Template not closed. Not worth keep on searching as the next templates are considered as nested.
                logWarning(text, startTemplate, startTemplate + START_TEMPLATE.length(), page, "Template not closed");
                return -1;
            }
        } else {
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

    private List<Immutable> findImmutables(LinearMatchResult template) {
        String content = template.group();

        // Special case "{{|}}"
        if (!validateSpecialCharacters(content)) {
            return Collections.emptyList();
        }

        // Remove the content of the nested templates
        for (int i = template.groupCount() - 1; i >= 0; i--) {
            content =
                content.substring(0, template.start(i) - template.start()) +
                content.substring(template.end(i) - template.start());
        }

        // There might be links in the values which make the algorithm return some parts of the links as parameters.
        // We could remove them, but it implies a performance penalty, and it isn't worth.
        // Instead, they should be found when finding the nested templates.

        // Remove the start and end of the template
        content = content.substring(START_TEMPLATE.length(), content.length() - END_TEMPLATE.length());

        String[] parameters = content.split("\\|");

        String templateName = parameters[0];
        // Check in case the template name is followed by a colon
        int posColon = templateName.indexOf(':');
        if (posColon >= 0) {
            templateName = templateName.substring(0, posColon);
        }

        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(templateName)) {
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
                value = parameter.substring(posEquals + 1);
            }

            // Always return the parameter
            // To calculate the parameter position we assume the parameters are not repeated in the template
            // As we are removing nested templates, the only way to calculate the position is find the first match.
            // We take into account also the value to find the position, or just the parameter in case there was a
            // nested template that has been removed and thus cannot be found.
            int startParameter =
                template.start() +
                (
                    template.group().contains("|" + parameter)
                        ? template.group().indexOf("|" + parameter)
                        : template.group().indexOf("|" + param)
                ) +
                1;
            if (posEquals >= 0) {
                immutables.add(Immutable.of(startParameter, param));
            } else {
                // Don't take into account parameters with no equals and value (except if they are files)
                // By the way we skip parameters which actually are link aliases
                if (matchesFile(param)) {
                    immutables.add(Immutable.of(startParameter, param));
                }
                continue;
            }

            if (StringUtils.isNotEmpty(value)) {
                // If the value is followed by a reference, comment or similar we ignore it
                int posLessThan = value.indexOf('<');
                if (posLessThan >= 0) {
                    value = value.substring(0, posLessThan);
                }

                // If the param is to be always ignored
                // or the pair template name-param is to be ignored
                // or the value is a file or a domain
                // then we also return the value
                if (
                    paramNames.contains(FinderUtils.toLowerCase(param.trim())) ||
                    templateParamPairs.containsMapping(
                        FinderUtils.toLowerCase(templateName.trim()),
                        FinderUtils.toLowerCase(param.trim())
                    ) ||
                    matchesFile(value)
                ) {
                    int startValue = startParameter + posEquals + 1;
                    immutables.add(Immutable.of(startValue, value));
                }
            }
        }

        return immutables;
    }

    private boolean validateSpecialCharacters(String template) {
        String content = template.substring(2, template.length() - 2);

        // Check that not all the characters are a pipe
        return content.chars().anyMatch(ch -> ch != '|');
    }

    private boolean ignoreCompleteTemplate(String templateName) {
        String template = FinderUtils.toLowerCase(templateName.trim()).replace('_', ' ');
        return (templateNames.contains(template) || templateNamesPartial.stream().anyMatch(template::startsWith));
    }

    private boolean matchesFile(String text) {
        String value = text.trim();
        int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4;
        } else {
            return false;
        }
    }
}
