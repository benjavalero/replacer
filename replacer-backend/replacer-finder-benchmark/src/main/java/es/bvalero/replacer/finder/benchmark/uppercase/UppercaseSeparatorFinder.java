package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Find the next separator string in the text using {@code indexOfAny}, then check if the following word
 * is in the uppercase set.
 * Con: searches for complete separator strings rather than single characters, so no lookup-table pre-filter.
 */
class UppercaseSeparatorFinder extends UppercaseBenchmarkFinder {

    private static final String[] SEPARATORS = {
        "=",
        "#",
        "*",
        ".",
        "!",
        ">",
        "\n|",
        "||",
        "|+",
        "<td>",
        "text:",
        "\n\n",
    };

    private final Set<String> words;

    UppercaseSeparatorFinder(Collection<String> words) {
        this.words = new HashSet<>(words);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findUppercase);
    }

    @Nullable
    private MatchResult findUppercase(FinderPage page, int start) {
        final String text = page.getContent();
        while (start < text.length()) {
            final MatchResult separator = FinderUtils.indexOfAny(text, start, SEPARATORS);
            if (separator == null) {
                return null;
            }
            int startWord = separator.end();
            while (startWord < text.length() && FinderUtils.isWhiteSpace(text.charAt(startWord))) {
                startWord++;
            }
            if (startWord < text.length() && FinderUtils.startsWithUpperCase(String.valueOf(text.charAt(startWord)))) {
                int endWord = startWord + 1;
                while (endWord < text.length() && FinderUtils.isLetter(text.charAt(endWord))) {
                    endWord++;
                }
                final String word = text.substring(startWord, endWord);
                if (words.contains(word) && FinderUtils.isWordCompleteInText(startWord, endWord, text)) {
                    return FinderMatchRange.of(text, startWord, word);
                }
            }
            start = separator.end();
        }
        return null;
    }
}
