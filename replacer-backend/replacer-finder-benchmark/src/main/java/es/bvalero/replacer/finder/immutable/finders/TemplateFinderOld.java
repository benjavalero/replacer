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
import java.util.*;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.apache.commons.collections4.SetValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

/** Snapshot of {@link TemplateFinder} as it was at the time the benchmark was introduced. */
public class TemplateFinderOld implements ImmutableFinder {

    private static final char EQUALS = '=';

    private final UppercaseFinder uppercaseFinder;

    private final Set<String> ignorableTemplates = new HashSet<>();
    private final Set<String> templateNames = new HashSet<>();
    private final Set<String> templateNamesPartial = new HashSet<>();
    private final Set<String> paramNames = new HashSet<>();
    private final SetValuedMap<String, String> templateParamPairs = new HashSetValuedHashMap<>();

    public TemplateFinderOld(FinderProperties finderProperties, UppercaseFinder uppercaseFinder) {
        this.uppercaseFinder = uppercaseFinder;

        this.ignorableTemplates.addAll(finderProperties.getIgnorableTemplates());

        for (FinderProperties.TemplateParam templateParam : finderProperties.getTemplateParams()) {
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
        while (start >= 0 && start < page.getContent().length()) {
            final SequencedCollection<TemplateStructure> templates = TemplateStructureFinderOld.findNext(page, start);
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
            start = templates.getLast().end();
        }
        return Collections.emptyList();
    }

    private List<MatchResult> findImmutable(TemplateStructure template, FinderPage page) {
        final String text = page.getContent();

        if (ignoreCompletePage(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, 0, text.length()));
        }
        if (ignoreCompleteTemplate(template.normalizedName())) {
            return List.of(FinderMatchRange.of(text, template.start(), template.end()));
        }

        final List<MatchResult> immutables = new ArrayList<>();

        final int startName = template.start() + START_TEMPLATE.length();
        immutables.add(FinderMatchRange.of(text, startName, template.name()));

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

        int index = text.indexOf(PIPE, startTemplateContent, endTemplateContent);
        while (index >= 0 && index < endTemplateContent) {
            final int startParameterPipe = text.indexOf(PIPE, index, endTemplateContent);
            if (template.containsNested(startParameterPipe)) {
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
                startValue = startParameter;
                endValue = trimValue(text, startValue, endParameter);
            }

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
                    uppercaseFinder.findFirstExpressionUpperCase(page, startValue).ifPresent(immutables::add);
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
            endParameter = endTemplateContent;
        }
        return endParameter;
    }

    private int findParameterEquals(String text, int startParameter, int endParameter, TemplateStructure template) {
        int startValueEquals = text.indexOf(EQUALS, startParameter, endParameter);
        if (startValueEquals >= 0 && template.containsNested(startValueEquals)) {
            startValueEquals = -1;
        } else if (startValueEquals >= 0 && !isValidParameterEquals(text, startParameter, startValueEquals)) {
            startValueEquals = -1;
        }
        return startValueEquals;
    }

    private boolean isValidParameterEquals(String text, int startParameter, int startEquals) {
        for (int i = startParameter; i < startEquals; i++) {
            if (text.charAt(i) == '<') {
                return false;
            }
        }
        return true;
    }

    private int trimValue(String text, int start, int end) {
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
            final int extensionLength = trimEnd - (posDot + 1);
            return extensionLength >= 2 && extensionLength <= 4 && FinderUtils.isAscii(text, posDot + 1, trimEnd);
        } else {
            return false;
        }
    }
}
