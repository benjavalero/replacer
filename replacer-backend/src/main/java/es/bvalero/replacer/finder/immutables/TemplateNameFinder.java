package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import es.bvalero.replacer.page.IndexablePage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find template names, e.g. `Bandera` in `{{Bandera|España}}`
 */
@Component
public class TemplateNameFinder implements ImmutableFinder {

    private static final String START_TEMPLATE = "{{";
    private static final Set<Character> CHARS_TEMPLATE_NAME = Set.of('-', '_', '/');
    private static final Set<Character> END_TEMPLATE_NAME = Set.of('|', '}', ':');

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 90;
    }

    @Override
    public Iterable<Immutable> find(IndexablePage page) {
        return new LinearIterable<>(page, this::findResult, this::convert);
    }

    @Nullable
    public MatchResult findResult(IndexablePage page, int start) {
        List<MatchResult> matches = new ArrayList<>(100);
        while (start >= 0 && start < page.getContent().length() && matches.isEmpty()) {
            start = findTemplateName(page.getContent(), start, matches);
        }
        return matches.isEmpty() ? null : matches.get(0);
    }

    private int findTemplateName(String text, int start, List<MatchResult> matches) {
        int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            int startTemplateName = startTemplate + START_TEMPLATE.length();
            int endTemplateName = findEndTemplateName(text, startTemplateName);
            if (endTemplateName >= 0) {
                String templateNameComplete = text.substring(startTemplateName, endTemplateName);
                String templateName = templateNameComplete.trim();
                if (StringUtils.isNotEmpty(templateName) && validateTemplateNameChars(templateName)) {
                    int nameStart = templateNameComplete.indexOf(templateName);
                    matches.add(LinearMatcher.of(startTemplateName + nameStart, templateName));
                }
                return endTemplateName + 1;
            } else {
                // Template name ending not found
                return startTemplateName;
            }
        } else {
            // No more templates
            return -1;
        }
    }

    private int findStartTemplate(String text, int start) {
        return text.indexOf(START_TEMPLATE, start);
    }

    private int findEndTemplateName(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (END_TEMPLATE_NAME.contains(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean validateTemplateNameChars(String templateName) {
        for (int i = 0; i < templateName.length(); i++) {
            if (!isTemplateNameChar(templateName.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isTemplateNameChar(char ch) {
        return Character.isLetterOrDigit(ch) || Character.isWhitespace(ch) || CHARS_TEMPLATE_NAME.contains(ch);
    }
}
