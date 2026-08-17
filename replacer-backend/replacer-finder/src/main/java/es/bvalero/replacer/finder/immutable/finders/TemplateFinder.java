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
            final List<MatchResult> immutables = new ArrayList<>();
            for (TemplateStructure template : templates) {
                immutables.addAll(findImmutable(template, page));
            }
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

        // Special case "{{!}}" ==> no immutable
        if (!validateSpecialCharacters(text, template)) {
            return List.of();
        }

        // If the whole page is to be ignored the return an immutable of the complete page content
        if (ignoreCompletePage(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, 0, text));
        }
        // If the template is to be ignored as a whole then return an immutable of the complete template
        if (ignoreCompleteTemplate(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, template.start(), template.end()));
        }

        final List<MatchResult> immutables = new ArrayList<>();

        // Add the template name
        immutables.add(FinderMatchRange.of(text, template.startName(), template.name()));

        // Add the immutables from the parameters and/or values
        immutables.addAll(findParameterImmutables(template, page));

        return immutables;
    }

    private boolean validateSpecialCharacters(String text, TemplateStructure template) {
        // Check that not all the characters are an escaped-pipe. See: https://en.wikipedia.org/wiki/Template:!!
        final int start = template.start() + START_TEMPLATE.length();
        final int end = template.end() - END_TEMPLATE.length();
        for (int i = start; i < end; i++) {
            if (text.charAt(i) != '!') {
                return true;
            }
        }
        return false;
    }

    private boolean ignoreCompletePage(String normalizedTemplateName) {
        return this.ignorableTemplates.contains(normalizedTemplateName);
    }

    private boolean ignoreCompleteTemplate(String normalizedTemplateName) {
        if (this.templateNames.contains(normalizedTemplateName)) {
            return true;
        }
        for (String partial : this.templateNamesPartial) {
            if (normalizedTemplateName.startsWith(partial)) {
                return true;
            }
        }
        return false;
    }

    private List<MatchResult> findParameterImmutables(TemplateStructure template, FinderPage page) {
        final List<MatchResult> immutables = new ArrayList<>();
        final String text = page.getContent();
        final int startTemplateContent = template.start() + START_TEMPLATE.length();
        final int endTemplateContent = template.end() - END_TEMPLATE.length();

        int index = text.indexOf(PIPE, startTemplateContent, endTemplateContent);
        while (index >= 0 && index < endTemplateContent) {
            final int startParameter = FinderUtils.trimLeft(text, index + 1, endTemplateContent);
            int posNextPipe = text.indexOf(PIPE, startParameter, endTemplateContent);
            while (posNextPipe >= 0 && template.containsNested(posNextPipe)) {
                posNextPipe = text.indexOf(PIPE, posNextPipe + 1, endTemplateContent);
            }
            final int endValue = posNextPipe >= 0 ? posNextPipe : endTemplateContent;
            final int posEquals = findParameterEquals(text, startParameter, endValue, template);
            final int endParameter = FinderUtils.trimRight(text, startParameter, posEquals);

            int startValue;
            int trimmedEndValue;
            if (posEquals >= 0) {
                // Add the parameter as immutable
                immutables.add(FinderMatchRange.of(text, startParameter, endParameter));
                // Determine value bounds
                startValue = FinderUtils.trimLeft(text, posEquals + 1, endValue);
                trimmedEndValue = trimValue(text, startValue, endValue);
            } else {
                // No '=': the whole parameter is treated as a value
                startValue = startParameter;
                trimmedEndValue = trimValue(text, startValue, endValue);
            }

            if (!FinderUtils.isBlank(text, startValue, trimmedEndValue)) {
                if (
                    isValueImmutable(
                        text,
                        startValue,
                        trimmedEndValue,
                        startParameter,
                        endParameter,
                        template.normalizedName()
                    )
                ) {
                    immutables.add(FinderMatchRange.of(text, startValue, trimmedEndValue));
                } else {
                    addUppercaseImmutable(startValue, page, immutables);
                }
            }

            index = endValue;
        }

        return immutables;
    }

    private int findParameterEquals(String text, int startParameter, int endParameter, TemplateStructure template) {
        final int pos = text.indexOf(EQUALS, startParameter, endParameter);
        if (pos >= 0 && !template.containsNested(pos)) {
            // An '=' preceded by '<' belongs to an HTML tag attribute, not a template parameter
            if (text.indexOf('<', startParameter, pos) < 0) {
                return pos;
            }
        }
        return -1;
    }

    private int trimValue(String text, int start, int end) {
        // If the value is followed by a reference, comment or similar we ignore it
        final int posLessThan = text.indexOf('<', start, end);
        final int startTemplate = text.indexOf(START_TEMPLATE, start, end);
        final int rawEnd;
        if (posLessThan >= 0 && startTemplate >= 0) {
            rawEnd = Math.min(posLessThan, startTemplate);
        } else if (posLessThan >= 0) {
            rawEnd = posLessThan;
        } else if (startTemplate >= 0) {
            rawEnd = startTemplate;
        } else {
            rawEnd = end;
        }
        return FinderUtils.trimRight(text, start, rawEnd);
    }

    private void addUppercaseImmutable(int startValue, FinderPage page, List<MatchResult> immutables) {
        // If the value starts with an uppercase word/expression then we return this word
        final MatchResult uppercase = uppercaseFinder.findFirstExpressionUpperCase(page, startValue);
        if (uppercase != null) {
            immutables.add(uppercase);
        }
    }

    private boolean isValueImmutable(
        String text,
        int startValue,
        int endValue,
        int startParam,
        int endParam,
        String normalizedTemplateName
    ) {
        // If the param is to be always ignored
        // or the pair template name-param is to be ignored
        // or the value is a file or a domain
        // then we also return the value
        if (endParam >= 0) {
            final String trimmedParam = ReplacerUtils.toLowerCase(text.substring(startParam, endParam));
            if (
                this.paramNames.contains(trimmedParam) ||
                this.templateParamPairs.containsMapping(normalizedTemplateName, trimmedParam)
            ) {
                return true;
            }
        }
        return matchesFile(text, startValue, endValue);
    }

    private boolean matchesFile(String text, int start, int end) {
        // start and end are assumed to be already trimmed
        final int posDot = text.lastIndexOf('.', end - 1);
        if (posDot >= start) {
            final int extensionLength = end - (posDot + 1);
            return extensionLength >= 2 && extensionLength <= 4 && FinderUtils.isAscii(text, posDot + 1, end);
        } else {
            return false;
        }
    }
}
