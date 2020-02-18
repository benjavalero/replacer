package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e. g. `<code>An <span>example</span>.</code>`
 */
@Component
public class CompleteTagFinder implements ImmutableFinder {
    private static final Set<String> TAG_NAMES = new HashSet<>(
        Arrays.asList(
            "blockquote",
            "cite",
            "code",
            "math",
            "nowiki",
            "poem",
            "pre",
            "ref",
            "score",
            "source",
            "syntaxhighlight"
        )
    );

    @Override
    public Iterable<Immutable> find(String text) {
        // We cannot use an automaton because we need a lazy operator to capture inner tags
        // We simulate it manually with a great performance
        List<Immutable> matches = new ArrayList<>(100);
        int start = 0;
        while (start >= 0) {
            start = text.indexOf('<', start);
            if (start >= 0) {
                int startOpenTag = start++;
                // Find the tag
                StringBuilder tagBuilder = new StringBuilder();
                char ch = text.charAt(start);
                while (Character.isLetter(ch)) {
                    tagBuilder.append(ch);
                    ch = text.charAt(++start);
                }
                String tag = tagBuilder.toString();
                if (TAG_NAMES.contains(tag)) {
                    String closeTag = new StringBuilder("</").append(tag).append('>').toString();
                    int startCloseTag = text.indexOf(closeTag, start);
                    if (startCloseTag >= 0) {
                        int endCloseTag = startCloseTag + closeTag.length();
                        String completeTag = text.substring(startOpenTag, endCloseTag);
                        matches.add(Immutable.of(startOpenTag, completeTag));
                        start = endCloseTag + 1;
                    }
                }
            }
        }
        return matches;
    }
}
