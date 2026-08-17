package es.bvalero.replacer.finder.benchmark.uppercase;

import com.roklenarcic.util.strings.LongestMatchMap;
import com.roklenarcic.util.strings.MapMatchListener;
import com.roklenarcic.util.strings.StringMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchRange;
import es.bvalero.replacer.finder.util.FinderUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/**
 * Use Aho-Corasick (leftmost longest) to find separator strings in a single pass, then check if the following word
 * is in the uppercase set.
 * Pro: single pass over the text to find all separators, no per-separator indexOf calls.
 * Note: LongestMatchMap avoids overlaps between separators sharing a prefix (e.g. "\n|" and "\n\n").
 */
class UppercaseAhoCorasickSeparatorFinder extends UppercaseBenchmarkFinder {

    private static final List<String> SEPARATORS = List.of(
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
        "\n\n"
    );

    private final StringMap<String> separatorMap;
    private final Set<String> words;

    UppercaseAhoCorasickSeparatorFinder(Collection<String> words) {
        this.separatorMap = new LongestMatchMap<>(SEPARATORS, SEPARATORS, true);
        this.words = new HashSet<>(words);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        final String text = page.getContent();
        final List<MatchResult> matches = new ArrayList<>();
        separatorMap.match(
            text,
            new MapMatchListener<>() {
                @Override
                public boolean match(String t, int start, int end, String value) {
                    int startWord = end;
                    while (startWord < text.length() && FinderUtils.isWhiteSpace(text.charAt(startWord))) {
                        startWord++;
                    }
                    if (
                        startWord < text.length() &&
                        FinderUtils.startsWithUpperCase(String.valueOf(text.charAt(startWord)))
                    ) {
                        int endWord = startWord + 1;
                        while (endWord < text.length() && FinderUtils.isLetter(text.charAt(endWord))) {
                            endWord++;
                        }
                        final String word = text.substring(startWord, endWord);
                        if (words.contains(word) && FinderUtils.isWordCompleteInText(startWord, endWord, text)) {
                            matches.add(FinderMatchRange.of(text, startWord, word));
                        }
                    }
                    return true;
                }
            }
        );
        return matches.stream();
    }
}
