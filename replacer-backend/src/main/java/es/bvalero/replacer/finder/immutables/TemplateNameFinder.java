package es.bvalero.replacer.finder.immutables;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Find template names, e. g. `Bandera` in `{{Bandera|España}}`
 */
@Component
public class TemplateNameFinder implements ImmutableFinder {
    static final Set<Character> END_TEMPLATE_NAME = new HashSet<>(Arrays.asList('|', '}', ':'));

    @Override
    public Iterable<Immutable> find(String text) {
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            start = findTemplateName(text, start, matches);
        }
        return matches;
    }

    private int findTemplateName(String text, int start, List<Immutable> matches) {
        int startTemplate = findStartTemplate(text, start);
        if (startTemplate >= 0) {
            int startTemplateName = startTemplate + 2;
            int endTemplateName = findEndTemplateName(text, startTemplateName);
            if (endTemplateName >= 0) {
                // Don't make the trim to improve slightly the performance
                matches.add(Immutable.of(startTemplateName, text.substring(startTemplateName, endTemplateName)));
                return endTemplateName;
            } else {
                return startTemplateName;
            }
        } else {
            return -1;
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
