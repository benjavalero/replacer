package es.bvalero.replacer.finder.immutable.finders;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchResult;
import es.bvalero.replacer.finder.util.TemplateUtils;
import java.util.*;
import java.util.regex.MatchResult;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Find template-related immutables:
 * <ul>
 *     <li>Template names, e.g. `Bandera` in `{{Bandera|España}}`</li>
 *     <li>Some complete templates, even with nested templates, e.g. `{{Cite|A cite}}`</li>
 *     <li>Template parameters, e.g. `param` in `{{Template|param=value}}`. For some specific parameters,
 *     we include in the result also the value, which is usually a taxonomy, a Commons category, etc.
 *     We also include the value if it seems like a file or a domain.</li>
 * </ul>
 */
@Component
class TemplateFinder implements ImmutableFinder {

    private static final String WILDCARD = "*";

    @Resource
    private List<String> templateParams;

    @Autowired
    private UppercaseFinder uppercaseFinder;

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
            final String[] tokens = pair.split("\\|");
            if (tokens.length == 2) {
                final String name = FinderUtils.toLowerCase(tokens[0].trim());
                final String param = FinderUtils.toLowerCase(tokens[1].trim());
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
    public FinderPriority getPriority() {
        return FinderPriority.VERY_HIGH;
    }

    @Override
    public Iterable<MatchResult> findMatchResults(WikipediaPage page) {
        final List<MatchResult> immutables = new ArrayList<>(100);
        for (LinearMatchResult template : TemplateUtils.findAllTemplates(page)) {
            immutables.addAll(findImmutables(page.getId().getLang(), template));
        }
        return immutables;
    }

    private List<MatchResult> findImmutables(WikipediaLanguage lang, LinearMatchResult template) {
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
        content =
            content.substring(
                TemplateUtils.START_TEMPLATE.length(),
                content.length() - TemplateUtils.END_TEMPLATE.length()
            );

        final String[] parameters = content.split("\\|");

        String templateName = parameters[0];
        // Check in case the template name is followed by a colon
        final int posColon = templateName.indexOf(':');
        if (posColon >= 0) {
            templateName = templateName.substring(0, posColon);
        }

        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(templateName)) {
            return Collections.singletonList(template);
        }

        final List<MatchResult> immutables = new ArrayList<>();

        // Add the template name
        immutables.add(LinearMatchResult.of(template.start() + TemplateUtils.START_TEMPLATE.length(), templateName));

        // Process the rest of parameters
        for (int i = 1; i < parameters.length; i++) {
            final String parameter = parameters[i];
            String param = parameter;
            String value = null;
            final int posEquals = parameter.indexOf('=');
            if (posEquals >= 0) {
                param = parameter.substring(0, posEquals);
                value = parameter.substring(posEquals + 1);
            }

            // Always return the parameter
            // To calculate the parameter position we assume the parameters are not repeated in the template
            // As we are removing nested templates, the only way to calculate the position is find the first match.
            // We take into account also the value to find the position, or just the parameter in case there was a
            // nested template that has been removed and thus cannot be found.
            final int startParameter =
                template.start() +
                (
                    template.group().contains("|" + parameter)
                        ? template.group().indexOf("|" + parameter)
                        : template.group().indexOf("|" + param)
                ) +
                1;
            if (posEquals >= 0) {
                immutables.add(LinearMatchResult.of(startParameter, param));
            } else {
                // Don't take into account parameters with no equals and value (except if they are files or uppercase)
                // By the way we skip parameters which actually are link aliases
                if (matchesFile(param)) {
                    immutables.add(LinearMatchResult.of(startParameter, param));
                } else {
                    final String firstWord = FinderUtils.getFirstWord(param);
                    if (matchesUppercase(lang, firstWord)) {
                        immutables.add(LinearMatchResult.of(startParameter, firstWord));
                    }
                }
                continue;
            }

            if (StringUtils.isNotEmpty(value)) {
                // If the value is followed by a reference, comment or similar we ignore it
                final int posLessThan = value.indexOf('<');
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
                    final int startValue = startParameter + posEquals + 1;
                    immutables.add(LinearMatchResult.of(startValue, value));
                } else {
                    // If the value starts with an uppercase word then we return this word
                    final String firstWord = FinderUtils.getFirstWord(value);
                    if (matchesUppercase(lang, firstWord)) {
                        final int startValue = startParameter + posEquals + 1;
                        immutables.add(LinearMatchResult.of(startValue, firstWord));
                    }
                }
            }
        }

        return immutables;
    }

    private boolean validateSpecialCharacters(String template) {
        final String content = template.substring(2, template.length() - 2);

        // Check that not all the characters are a pipe
        return content.chars().anyMatch(ch -> ch != '|');
    }

    private boolean ignoreCompleteTemplate(String templateName) {
        final String template = FinderUtils.toLowerCase(templateName.trim()).replace('_', ' ');
        return (templateNames.contains(template) || templateNamesPartial.stream().anyMatch(template::startsWith));
    }

    private boolean matchesFile(String text) {
        // There are cases of files followed by the {{!}} template
        // so they are not detected here as we are removing first all nested templates
        final String value = text.trim();
        final int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            final String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4;
        } else {
            return false;
        }
    }

    private boolean matchesUppercase(WikipediaLanguage lang, String text) {
        return uppercaseFinder.getUppercaseMap().containsMapping(lang, text);
    }
}
