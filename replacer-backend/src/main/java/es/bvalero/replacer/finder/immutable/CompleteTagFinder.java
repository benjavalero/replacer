package es.bvalero.replacer.finder.immutable;

import es.bvalero.replacer.finder.Immutable;
import es.bvalero.replacer.finder.ImmutableFinder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e. g. `<code>An <span>example</span>.</code>`
 */
@Component
public class CompleteTagFinder implements ImmutableFinder {
    private static final List<String> TAG_NAMES = Arrays.asList(
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
                int endOpenTag = text.indexOf('>', start);
                if (endOpenTag >= 0) {
                    String openTag = text.substring(start + 1, endOpenTag);
                    Optional<String> tag = TAG_NAMES.stream().filter(openTag::startsWith).findAny();
                    if (tag.isPresent()) {
                        String closeTag = String.format("</%s>", tag.get());
                        int startCloseTag = text.indexOf(closeTag, endOpenTag);
                        if (startCloseTag >= 0) {
                            int endCloseTag = startCloseTag + closeTag.length();
                            matches.add(Immutable.of(start, text.substring(start, endCloseTag)));
                            start = endCloseTag + 1;
                        } else {
                            start++;
                        }
                    } else {
                        start++;
                    }
                } else {
                    start++;
                }
            }
        }
        return matches;
    }
}
