package es.bvalero.replacer.finder;

import es.bvalero.replacer.finder2.Immutable;
import es.bvalero.replacer.finder2.ImmutableFinder;
import es.bvalero.replacer.finder2.RegexIterable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Find some XML tags and all the content within, even other tags, e. g. `<code>An <span>example</span>.</code>`
 */
@Component
class CompleteTagFinder implements ImmutableFinder {
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

    // The benchmarks show a big difference (an order of magnitude) using a single regex with alternations
    // and back-references compared to run a simpler regex (even text-based) for each tag.
    // To capture the opening tag there are no big differences on using a dot or a negated class [^>]:
    // the negated class is better for some outsiders but the dot works better in the general case.
    private static final String REGEX_COMPLETE_TAGS = String.format(
        "<(%s).*?>.+?</\\1>",
        StringUtils.join(TAG_NAMES, "|")
    );
    private static final Pattern PATTERN_COMPLETE_TAGS = Pattern.compile(REGEX_COMPLETE_TAGS, Pattern.DOTALL);

    @Override
    public Iterable<Immutable> find(String text) {
        return new RegexIterable<Immutable>(text, PATTERN_COMPLETE_TAGS, this::convert, this::isValid);
    }
}
