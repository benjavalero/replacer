package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.TemplateUtils.END_TEMPLATE;
import static es.bvalero.replacer.finder.util.TemplateUtils.START_TEMPLATE;

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
import org.springframework.lang.Nullable;
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
    private static final char PIPE = '|';
    private static final char COLON = ':';

    @Resource
    private Set<String> ignorableTemplates;

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
            final String[] tokens = StringUtils.split(pair, '|');
            if (tokens.length == 2) {
                final String name = FinderUtils.toLowerCase(tokens[0].trim());
                final String param = FinderUtils.toLowerCase(tokens[1].trim());
                if (WILDCARD.equals(param)) {
                    if (name.endsWith(WILDCARD)) {
                        this.templateNamesPartial.add(StringUtils.chop(name));
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
            immutables.addAll(findImmutables(page, template));
        }
        return immutables;
    }

    private List<MatchResult> findImmutables(WikipediaPage page, LinearMatchResult template) {
        // There might be links in the values which make the algorithm return some parts of the links as parameters.
        // We could remove them, but it implies a performance penalty, and it isn't worth.
        // Instead, they should be found when finding the nested templates.
        final WikipediaLanguage lang = page.getId().getLang();

        // Remove the content of the nested templates
        // Remove the start and end of the template
        final String templateContent = getTemplateContent(template.getTextWithoutNested());

        // Special case "{{|}}"
        if (!validateSpecialCharacters(templateContent)) {
            return Collections.emptyList();
        }

        final String[] parameters = StringUtils.split(templateContent, PIPE);

        final String templateName = findTemplateName(parameters[0]);
        final String normalizedTemplateName = normalizeTemplateName(templateName);
        // If the whole page is to be ignored the return an immutable of the complete page content
        if (ignoreCompletePage(normalizedTemplateName)) {
            return Collections.singletonList(LinearMatchResult.of(0, page.getContent()));
        }
        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(normalizedTemplateName)) {
            return Collections.singletonList(template);
        }

        final List<MatchResult> immutables = new ArrayList<>();

        // Add the template name
        immutables.add(LinearMatchResult.of(template.start() + START_TEMPLATE.length(), templateName));

        // Process the rest of parameters
        // Not sure why but when refactoring the loop content the performance decreases
        for (int i = 1; i < parameters.length; i++) {
            final String parameter = parameters[i];

            final int posEquals = parameter.indexOf('=');
            final String key = findParameterKey(parameter, posEquals);
            String value = findParameterValue(parameter, posEquals);

            // Always return the parameter
            // To calculate the parameter position we assume the parameters are not repeated in the template
            // As we are removing nested templates, the only way to calculate the position is find the first match.
            // We take into account also the value to find the position, or just the parameter in case there was a
            // nested template that has been removed and thus cannot be found.
            final int startParameter = findStartParameter(template, parameter, key);
            if (posEquals >= 0) {
                immutables.add(LinearMatchResult.of(startParameter, key));
            } else {
                // Don't take into account parameters with no equals and value (except if they are files or uppercase)
                // By the way we skip parameters which actually are link aliases
                if (matchesFile(key)) {
                    immutables.add(LinearMatchResult.of(startParameter, key));
                } else {
                    final String firstWordUpperCase = findFirstWordUpperCase(key, lang);
                    if (firstWordUpperCase != null) {
                        immutables.add(LinearMatchResult.of(startParameter, firstWordUpperCase));
                    }
                }
                continue;
            }

            if (StringUtils.isNotEmpty(value)) {
                final int startValue = startParameter + posEquals + 1;
                value = trimValue(value);

                if (isValueImmutable(value, key, templateName)) {
                    immutables.add(LinearMatchResult.of(startValue, value));
                } else {
                    // If the value starts with an uppercase word then we return this word
                    final String firstWordUpperCase = findFirstWordUpperCase(value, lang);
                    if (firstWordUpperCase != null) {
                        immutables.add(LinearMatchResult.of(startValue, firstWordUpperCase));
                    }
                }
            }
        }

        return immutables;
    }

    private String getTemplateContent(String template) {
        return template.substring(START_TEMPLATE.length(), template.length() - END_TEMPLATE.length());
    }

    private boolean validateSpecialCharacters(String templateContent) {
        // Check that not all the characters are a pipe
        for (int i = 0; i < templateContent.length(); i++) {
            if (templateContent.charAt(i) != PIPE) {
                return true;
            }
        }
        return false;
    }

    private String findTemplateName(String parameter) {
        String templateName = parameter;
        // Check in case the template name is followed by a colon
        final int posColon = templateName.indexOf(COLON);
        if (posColon >= 0) {
            templateName = templateName.substring(0, posColon);
        }
        return templateName;
    }

    private String normalizeTemplateName(String templateName) {
        return FinderUtils.toLowerCase(templateName.trim()).replace('_', ' ');
    }

    private boolean ignoreCompletePage(String templateName) {
        return ignorableTemplates.contains(templateName);
    }

    private boolean ignoreCompleteTemplate(String templateName) {
        return (
            templateNames.contains(templateName) || templateNamesPartial.stream().anyMatch(templateName::startsWith)
        );
    }

    private String findParameterKey(String parameter, int posEquals) {
        return posEquals >= 0 ? parameter.substring(0, posEquals) : parameter;
    }

    @Nullable
    private String findParameterValue(String parameter, int posEquals) {
        return posEquals >= 0 ? parameter.substring(posEquals + 1) : null;
    }

    private int findStartParameter(LinearMatchResult template, String parameter, String key) {
        return (
            template.start() +
            (
                template.group().contains(PIPE + parameter)
                    ? template.group().indexOf(PIPE + parameter)
                    : template.group().indexOf(PIPE + key)
            ) +
            1
        );
    }

    private boolean matchesFile(String text) {
        // There are cases of files followed by the {{!}} template
        // so they are not detected here as we are removing first all nested templates
        final String value = text.trim();
        final int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            final String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4 && StringUtils.isAlpha(extension);
        } else {
            return false;
        }
    }

    @Nullable
    private String findFirstWordUpperCase(String text, WikipediaLanguage lang) {
        final String firstWord = FinderUtils.findFirstWord(text);
        if (firstWord != null && matchesUppercase(lang, firstWord)) {
            return firstWord;
        } else {
            return null;
        }
    }

    private boolean matchesUppercase(WikipediaLanguage lang, String text) {
        return uppercaseFinder.getUppercaseMap().containsMapping(lang, text);
    }

    private String trimValue(String value) {
        // If the value is followed by a reference, comment or similar we ignore it
        final int posLessThan = value.indexOf('<');
        return posLessThan >= 0 ? value.substring(0, posLessThan) : value;
    }

    private boolean isValueImmutable(String value, String key, String templateName) {
        // If the param is to be always ignored
        // or the pair template name-param is to be ignored
        // or the value is a file or a domain
        // then we also return the value
        return (
            paramNames.contains(FinderUtils.toLowerCase(key.trim())) ||
            templateParamPairs.containsMapping(
                FinderUtils.toLowerCase(templateName.trim()),
                FinderUtils.toLowerCase(key.trim())
            ) ||
            matchesFile(value)
        );
    }
}
