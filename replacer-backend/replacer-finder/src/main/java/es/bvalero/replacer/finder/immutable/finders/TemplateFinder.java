package es.bvalero.replacer.finder.immutable.finders;

import static es.bvalero.replacer.finder.util.FinderUtils.*;

import es.bvalero.replacer.FinderProperties;
import es.bvalero.replacer.common.util.ReplacerUtils;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.FinderPriority;
import es.bvalero.replacer.finder.immutable.ImmutableFinder;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchCollectionFinder;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
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
public class TemplateFinder implements ImmutableFinder {

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

    public TemplateFinder(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
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
        return LinearMatchCollectionFinder.find(page, this::findImmutables);
    }

    private SequencedCollection<MatchResult> findImmutables(FinderPage page, int start) {
        // findNextStructure returns nested structures first, root structure last
        while (start >= 0 && start < page.getContent().length()) {
            final SequencedCollection<TemplateStructure> templates = TemplateStructureFinder.findNext(page, start);
            if (templates.isEmpty()) {
                break;
            }
            final List<MatchResult> immutables = templates
                .stream()
                .flatMap(template -> findImmutable(template, page).stream())
                .toList();
            if (!immutables.isEmpty()) {
                return immutables;
            }
            // If no immutables found, search from the end of the root structure (last element)
            start = templates.getLast().end();
        }
        return Collections.emptyList();
    }

    private List<MatchResult> findImmutable(TemplateStructure template, FinderPage page) {
        final String text = page.getContent();

        // If the whole page is to be ignored the return an immutable of the complete page content
        if (ignoreCompletePage(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, 0, text.length()));
        }
        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, template.start(), template.end()));
        }

        final List<MatchResult> immutables = new ArrayList<>();

        // Add the template name
        final int startName = template.start() + START_TEMPLATE.length();
        immutables.add(FinderMatchRange.of(text, startName, template.name()));

        // Add the immutables from the parameters and/or values
        immutables.addAll(findParameterImmutables(template, page));

        return immutables;
    }

    private boolean ignoreCompletePage(String normalizedTemplateName) {
        return this.ignorableTemplates.contains(normalizedTemplateName);
    }

    private boolean ignoreCompleteTemplate(String normalizedTemplateName) {
        return (
            this.templateNames.contains(normalizedTemplateName) ||
            this.templateNamesPartial.stream().anyMatch(normalizedTemplateName::startsWith)
        );
    }

    private List<MatchResult> findParameterImmutables(TemplateStructure template, FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>();

        final String text = page.getContent();
        final int startTemplateContent = template.start() + START_TEMPLATE.length();
        final int endTemplateContent = template.end() - END_TEMPLATE.length();

        // Let's iterate over the template parameters (if any)
        int index = text.indexOf(PIPE, startTemplateContent, endTemplateContent);
        while (index >= 0 && index < endTemplateContent) {
            final int startParameterPipe = text.indexOf(PIPE, index, endTemplateContent); // Including the pipe
            if (template.containsNested(startParameterPipe)) {
                // The template has no pipe and the pipe belongs to a nested template
                index = startParameterPipe + 1;
                continue;
            }
            final int startParameter = startParameterPipe + 1;
            final int endParameter = findEndParameter(text, startParameter, endTemplateContent, template);

            final int startValueEquals = findParameterEquals(text, startParameter, endParameter, template);

            int startValue;
            int endValue;
            if (startValueEquals >= 0) {
                startValue = startValueEquals + 1;
                endValue = trimValue(text, startValue, endParameter);
            } else {
                // Parameters with no equals symbol are considered as values
                startValue = startParameter;
                endValue = trimValue(text, startValue, endParameter);
            }

            // Always return template parameters as immutables
            if (startValueEquals >= 0) {
                immutables.add(FinderMatchRange.of(text, startParameter, startValueEquals));
            }

            if (!FinderUtils.isBlank(text, startValue, endValue)) {
                final String value = text.substring(startValue, endValue);
                if (
                    isValueImmutable(
                        text,
                        startValue,
                        endValue,
                        startParameter,
                        startValueEquals,
                        template.normalizedName()
                    )
                ) {
                    immutables.add(FinderMatchRange.of(text, startValue, value));
                } else {
                    addUppercaseImmutable(startValue, page, immutables);
                }
            }

            index = endParameter;
        }

        return immutables;
    }

    private int findEndParameter(String text, int startParameter, int endTemplateContent, TemplateStructure template) {
        int endParameter = text.indexOf(PIPE, startParameter, endTemplateContent);
        while (endParameter >= 0 && template.containsNested(endParameter)) {
            endParameter = text.indexOf(PIPE, endParameter + 1, endTemplateContent);
        }
        if (endParameter < 0) {
            // We have reached the end of the template
            endParameter = endTemplateContent;
        }
        return endParameter;
    }

    private int findParameterEquals(String text, int startParameter, int endParameter, TemplateStructure template) {
        int startValueEquals = text.indexOf(EQUALS, startParameter, endParameter); // Including the equals
        if (startValueEquals >= 0 && template.containsNested(startValueEquals)) {
            // The equals symbol belongs to a nested template, so we ignore it.
            startValueEquals = -1;
        } else if (startValueEquals >= 0 && !isValidParameterEquals(text, startParameter, startValueEquals)) {
            // The equals symbol is not the parameter one, so we ignore it.
            startValueEquals = -1;
        }
        return startValueEquals;
    }

    private void addUppercaseImmutable(int startValue, FinderPage page, List<MatchResult> immutables) {
        // If the value starts with an uppercase word/expression then we return this word
        uppercaseFinder.findFirstExpressionUpperCase(page, startValue).ifPresent(immutables::add);
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

    private int trimValue(String text, int start, int end) {
        // If the value is followed by a reference, comment or similar we ignore it
        int posLessThan = text.indexOf('<', start, end);
        int posStartTemplate = text.indexOf(START_TEMPLATE, start, end);
        if (posLessThan >= 0 || posStartTemplate >= 0) {
            if (posLessThan < 0) {
                posLessThan = Integer.MAX_VALUE;
            }
            if (posStartTemplate < 0) {
                posStartTemplate = Integer.MAX_VALUE;
            }
            return Math.min(posLessThan, posStartTemplate);
        } else {
            return end;
        }
    }

    private boolean isValueImmutable(
        String text,
        int startValue,
        int endValue,
        int startKey,
        int endKey,
        String normalizedTemplateName
    ) {
        // If the param is to be always ignored
        // or the pair template name-param is to be ignored
        // or the value is a file or a domain
        // then we also return the value
        if (endKey >= 0) {
            final int trimStart = FinderUtils.trimLeft(text, startKey, endKey);
            final int trimEnd = FinderUtils.trimRight(text, trimStart, endKey);
            final String trimmedKey = ReplacerUtils.toLowerCase(text.substring(trimStart, trimEnd));
            if (
                this.paramNames.contains(trimmedKey) ||
                this.templateParamPairs.containsMapping(normalizedTemplateName, trimmedKey)
            ) {
                return true;
            }
        }
        return matchesFile(text, startValue, endValue);
    }

    private boolean matchesFile(String text, int start, int end) {
        final int trimStart = FinderUtils.trimLeft(text, start, end);
        final int trimEnd = FinderUtils.trimRight(text, trimStart, end);
        final int posDot = text.lastIndexOf('.', trimEnd - 1);
        if (posDot >= trimStart) {
            final int extensionStart = posDot + 1;
            final int extensionEnd = trimEnd;
            final int extensionLength = extensionEnd - extensionStart;
            return (
                extensionLength >= 2 && extensionLength <= 4 && FinderUtils.isAscii(text, extensionStart, extensionEnd)
            );
        } else {
            return false;
        }
    }
}
