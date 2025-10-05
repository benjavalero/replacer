package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
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

    private static final char COLON = ':';
    private static final char EQUALS = '=';

    // Dependency injection
    private final FinderProperties finderProperties;
    private final UppercaseFinder uppercaseFinder;

    // Set with the names of the templates making the whole page to be ignored
    private final Set<String> ignorableTemplates = new HashSet<>();

    // Set with the names of the templates to be ignored as a whole
    private final Set<String> templateNames = new HashSet<>();

    // Set with the partial names of the templates to be ignored as a whole
    private final Set<String> templateNamesPartial = new HashSet<>();

    // Set with the names of the parameters whose values will be ignored no matter the template they are in
    private final Set<String> paramNames = new HashSet<>();

    // Map with the pairs (template name-param) whose values will be ignored
    // Take into account that there might be several param names for the same template name
    private final SetValuedMap<String, String> templateParamPairs = new HashSetValuedHashMap<>();

    TemplateFinder(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
        this.finderProperties = finderProperties;
        this.uppercaseFinder = uppercaseFinder;
    }

    @PostConstruct
    public void initTemplateParams() {
        this.ignorableTemplates.addAll(this.finderProperties.getIgnorableTemplates()); // Caching

        for (FinderProperties.TemplateParam templateParam : this.finderProperties.getTemplateParams()) {
            assert templateParam.getTemplate() != null || templateParam.getParam() != null;
            if (templateParam.getParam() == null) {
                String name = ReplacerUtils.toLowerCase(templateParam.getTemplate());
                if (templateParam.isPartial()) {
                    this.templateNamesPartial.add(name);
                } else {
                    this.templateNames.add(name);
                }
            } else if (templateParam.getTemplate() == null) {
                this.paramNames.add(ReplacerUtils.toLowerCase(templateParam.getParam()));
            } else {
                this.templateParamPairs.put(
                        ReplacerUtils.toLowerCase(templateParam.getTemplate()),
                        ReplacerUtils.toLowerCase(templateParam.getParam())
                    );
            }
        }
    }

    @Override
    public FinderPriority getPriority() {
        return FinderPriority.VERY_HIGH;
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>(100);
        for (FinderMatchResult template : FinderUtils.findAllStructures(
            page,
            START_TEMPLATE,
            END_TEMPLATE,
            this::isNotFakeTemplate
        )) {
            immutables.addAll(findImmutables(template, page));
        }
        return immutables.stream();
    }

    private boolean isNotFakeTemplate(String text, int templateStart) {
        // There are some cases where curly braces inside a LaTeX formula may be confused with template start
        // We want to avoid the warning in these cases
        // This method is only called in case the template is not closed
        final char nextChar = text.charAt(templateStart + START_TEMPLATE.length());
        return FinderUtils.isWordChar(nextChar);
    }

    private List<MatchResult> findImmutables(FinderMatchResult template, FinderPage page) {
        // Let's check first the easiest cases
        final String templateContent = getTemplateContent(template.group());

        // Special case "{{!}}" ==> no immutable
        if (!validateSpecialCharacters(templateContent)) {
            return List.of();
        }

        final String templateName = findTemplateName(templateContent);
        final String normalizedTemplateName = normalizeTemplateName(templateName);

        // If the whole page is to be ignored the return an immutable of the complete page content
        if (ignoreCompletePage(normalizedTemplateName)) {
            return List.of(FinderMatchResult.of(0, page.getContent()));
        }
        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(normalizedTemplateName)) {
            return List.of(template);
        }

        final List<MatchResult> immutables = new ArrayList<>();

        // Add the template name
        immutables.add(FinderMatchResult.of(template.start() + START_TEMPLATE.length(), templateName));

        // Add the immutables from the parameters and/or values
        immutables.addAll(findParameterImmutables(template, templateName, templateContent, page));

        return immutables;
    }

    private String getTemplateContent(String template) {
        return template.substring(START_TEMPLATE.length(), template.length() - END_TEMPLATE.length());
    }

    private boolean validateSpecialCharacters(String templateContent) {
        // Check that not all the characters are an escaped-pipe. See: https://en.wikipedia.org/wiki/Template:!!
        for (int i = 0; i < templateContent.length(); i++) {
            if (templateContent.charAt(i) != '!') {
                return true;
            }
        }
        return false;
    }

    private String findTemplateName(String templateContent) {
        final int startPipe = templateContent.indexOf(PIPE);
        final String templateTitle = startPipe >= 0 ? templateContent.substring(0, startPipe) : templateContent;
        final int startColon = templateTitle.indexOf(COLON);
        return startColon >= 0 ? templateTitle.substring(0, startColon) : templateTitle;
    }

    private String normalizeTemplateName(String templateName) {
        return ReplacerUtils.toLowerCase(templateName.trim()).replace('_', ' ');
    }

    private boolean ignoreCompletePage(String templateName) {
        return this.ignorableTemplates.contains(templateName);
    }

    private boolean ignoreCompleteTemplate(String templateName) {
        return (
            this.templateNames.contains(templateName) ||
            this.templateNamesPartial.stream().anyMatch(templateName::startsWith)
        );
    }

    // TODO: Reduce cyclomatic complexity
    private List<MatchResult> findParameterImmutables(
        FinderMatchResult template,
        String templateName,
        String templateContent,
        FinderPage page
    ) {
        final List<MatchResult> immutables = new ArrayList<>();

        final WikipediaLanguage lang = page.getPageKey().getLang();
        final int startTemplateContent = template.start() + START_TEMPLATE.length();

        // Let's iterate over the template parameters (if any)
        // Note: we iterate over the template content, so we have to take this into account for the match position.
        int index = templateContent.indexOf(PIPE);
        while (index >= 0 && index < templateContent.length()) {
            final int startParameterPipe = templateContent.indexOf(PIPE, index); // Including the pipe
            if (template.containsNested(startTemplateContent + startParameterPipe)) {
                // The template has no pipe and the pipe belongs to a nested template
                index = startParameterPipe + 1;
                continue;
            }
            final int startParameter = startParameterPipe + 1;
            int endParameter = templateContent.indexOf(PIPE, startParameter);
            while (template.containsNested(startTemplateContent + endParameter)) {
                endParameter = templateContent.indexOf(PIPE, endParameter + 1);
            }
            if (endParameter < 0) {
                // We have reached the end of the template
                endParameter = templateContent.length();
            }

            int startValueEquals = templateContent.indexOf(EQUALS, startParameter); // Including the equals
            if (startValueEquals >= endParameter) {
                // The equals symbol belongs to the next parameter
                startValueEquals = -1;
            } else if (template.containsNested(startTemplateContent + startValueEquals)) {
                // The equals symbol belongs to a nested template, so we ignore it.
                startValueEquals = -1;
            } else if (!isValidParameterEquals(templateContent, startParameter, startValueEquals)) {
                // The equals symbol is not the parameter one, so we ignore it.
                startValueEquals = -1;
            }

            String parameter = null;
            int startValue;
            String value;
            if (startValueEquals >= 0) {
                parameter = templateContent.substring(startParameter, startValueEquals);
                startValue = startValueEquals + 1;
                value = templateContent.substring(startValue, endParameter);
            } else {
                // Parameters with no equals symbol are considered as values
                startValue = startParameter;
                value = templateContent.substring(startParameter, endParameter);
            }

            // Always return template parameters as immutables
            if (parameter != null) {
                immutables.add(FinderMatchResult.of(startTemplateContent + startParameter, parameter));
            }

            if (StringUtils.isNotBlank(value)) {
                final String trimmedValue = trimValue(value);
                if (isValueImmutable(trimmedValue, parameter, templateName)) {
                    immutables.add(FinderMatchResult.of(startTemplateContent + startValue, trimmedValue));
                } else {
                    // If the value starts with an uppercase word/expression then we return this word
                    final Optional<MatchResult> firstExpressionUpperCase = uppercaseFinder.findFirstExpressionUpperCase(
                        value,
                        lang
                    );
                    firstExpressionUpperCase.ifPresent(uppercase ->
                        immutables.add(
                            FinderMatchResult.of(
                                startTemplateContent + startValue + uppercase.start(),
                                uppercase.group()
                            )
                        )
                    );
                }
            }

            index = endParameter;
        }

        return immutables;
    }

    private boolean isValidParameterEquals(String text, int startParameter, int startEquals) {
        for (int i = startParameter; i < startEquals; i++) {
            if (isForbiddenChar(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isForbiddenChar(char ch) {
        // We want to avoid the '=' in references to be mistaken by the one of the parameter
        return ch == '<';
    }

    private String trimValue(String value) {
        // If the value is followed by a reference, comment or similar we ignore it
        int posLessThan = value.indexOf('<');
        int posStartTemplate = value.indexOf(START_TEMPLATE);
        if (posLessThan >= 0 || posStartTemplate >= 0) {
            if (posLessThan < 0) {
                posLessThan = Integer.MAX_VALUE;
            }
            if (posStartTemplate < 0) {
                posStartTemplate = Integer.MAX_VALUE;
            }
            return value.substring(0, Math.min(posLessThan, posStartTemplate));
        } else {
            return value;
        }
    }

    private boolean isValueImmutable(String value, @Nullable String key, String templateName) {
        // If the param is to be always ignored
        // or the pair template name-param is to be ignored
        // or the value is a file or a domain
        // then we also return the value
        final String trimmedKey = key == null ? EMPTY : ReplacerUtils.toLowerCase(key.trim());
        return (
            this.paramNames.contains(trimmedKey) ||
            this.templateParamPairs.containsMapping(ReplacerUtils.toLowerCase(templateName.trim()), trimmedKey) ||
            matchesFile(value)
        );
    }

    private boolean matchesFile(String text) {
        final String value = text.trim();
        final int dot = value.lastIndexOf('.');
        if (dot >= 0) {
            final String extension = value.substring(dot + 1);
            return extension.length() >= 2 && extension.length() <= 4 && FinderUtils.isAscii(extension);
        } else {
            return false;
        }
    }
}
