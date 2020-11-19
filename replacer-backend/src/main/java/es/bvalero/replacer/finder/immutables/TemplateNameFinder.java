package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Find template names, e.g. `Bandera` in `{{Bandera|España}}`
 */
@Component
public class TemplateNameFinder implements ImmutableFinder {
    static final Set<Character> END_TEMPLATE_NAME = new HashSet<>(Arrays.asList('|', '}', ':'));

    @Override
    public ImmutableFinderPriority getPriority() {
        return ImmutableFinderPriority.LOW;
    }

    @Override
    public int getMaxLength() {
        return 250;
    }

    @Override
    public Iterable<Immutable> find(String text, WikipediaLanguage lang) {
        return new LinearIterable<>(text, this::findTemplateName, this::convert);
    }

    @Nullable
    private MatchResult findTemplateName(String text, int start) {
        int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            int startTemplateName = startTemplate + 2;
            int endTemplateName = findEndTemplateName(text, startTemplateName);
            if (endTemplateName >= 0) {
                // Don't make the trim to improve slightly the performance
                return LinearMatcher.of(startTemplateName, text.substring(startTemplateName, endTemplateName));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private int findStartTemplate(String text, int start) {
        return text.indexOf("{{", start);
    }

    private int findEndTemplateName(String text, int start) {
        for (int i = start; i < text.length(); i++) {
            if (END_TEMPLATE_NAME.contains(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }
}
