package es.bvalero.replacer.finder.benchmark.uppercase;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.FinderMatchResult;
import es.bvalero.replacer.finder.util.FinderUtils;
import es.bvalero.replacer.finder.util.LinearMatchFinder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Single pass over the text looking for punctuation separator characters first.
 * On each separator hit, skip optional whitespace and check if the following word is in the uppercase set.
 * Pro: far fewer comparisons than iterating over all words or all text positions,
 * since separators are much less frequent than letters.
 */
class UppercasePunctuationFinder extends UppercaseBenchmarkFinder {

    // First characters of all valid separators — used as a fast pre-filter
    private static final boolean[] SEPARATOR_FIRST_CHARS = new boolean[128];

    static {
        SEPARATOR_FIRST_CHARS['='] = true;
        SEPARATOR_FIRST_CHARS['#'] = true;
        SEPARATOR_FIRST_CHARS['*'] = true;
        SEPARATOR_FIRST_CHARS['.'] = true;
        SEPARATOR_FIRST_CHARS['!'] = true;
        SEPARATOR_FIRST_CHARS['>'] = true;
        SEPARATOR_FIRST_CHARS['\n'] = true;
        SEPARATOR_FIRST_CHARS['|'] = true;
        SEPARATOR_FIRST_CHARS['<'] = true;
        SEPARATOR_FIRST_CHARS['t'] = true; // "text:"
    }

    private final Set<String> words;

    UppercasePunctuationFinder(Collection<String> words) {
        this.words = new HashSet<>(words);
    }

    @Override
    public Stream<MatchResult> findMatchResults(FinderPage page) {
        return LinearMatchFinder.find(page, this::findUppercase);
    }

    @Nullable
    private MatchResult findUppercase(FinderPage page, int start) {
        final String text = page.getContent();
        for (int i = start; i < text.length(); i++) {
            final char ch = text.charAt(i);
            if (ch >= 128 || !SEPARATOR_FIRST_CHARS[ch]) {
                continue;
            }
            final int separatorEnd = getSeparatorEnd(text, i);
            if (separatorEnd < 0) {
                continue;
            }
            // Skip optional whitespace after separator (but not newlines)
            int wordStart = separatorEnd;
            while (wordStart < text.length() && FinderUtils.isWhiteSpace(text.charAt(wordStart))) {
                wordStart++;
            }
            if (wordStart >= text.length() || !FinderUtils.startsWithUpperCase(text.charAt(wordStart) + "")) {
                continue;
            }
            // Find end of word
            int wordEnd = wordStart + 1;
            while (wordEnd < text.length() && FinderUtils.isLetter(text.charAt(wordEnd))) {
                wordEnd++;
            }
            final String word = text.substring(wordStart, wordEnd);
            if (words.contains(word) && FinderUtils.isWordCompleteInText(wordStart, wordEnd, text)) {
                return FinderMatchResult.of(wordStart, word);
            }
        }
        return null;
    }

    /**
     * Returns the position right after the separator starting at {@code pos},
     * or -1 if no valid separator starts there.
     */
    private static int getSeparatorEnd(String text, int pos) {
        final char ch = text.charAt(pos);
        return switch (ch) {
            case '=', '#', '*', '.', '!', '>' -> pos + 1;
            case '\n' -> {
                // \n| (table cell) or \n\n (paragraph start)
                if (pos + 1 < text.length()) {
                    final char next = text.charAt(pos + 1);
                    if (next == '|') yield pos + 2;
                    if (next == '\n') yield pos + 2;
                }
                yield -1;
            }
            case '|' -> {
                // || (cell separator) or |+ (caption)
                if (pos + 1 < text.length()) {
                    final char next = text.charAt(pos + 1);
                    if (next == '|' || next == '+') yield pos + 2;
                }
                yield -1;
            }
            case '<' -> {
                // <td>
                if (pos + 4 <= text.length() && text.startsWith("<td>", pos)) yield pos + 4;
                yield -1;
            }
            case 't' -> {
                // text:
                if (pos + 5 <= text.length() && text.startsWith("text:", pos)) yield pos + 5;
                yield -1;
            }
            default -> -1;
        };
    }
}
