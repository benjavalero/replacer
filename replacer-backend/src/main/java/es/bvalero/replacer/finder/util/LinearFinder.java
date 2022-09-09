package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.common.domain.WikipediaPage;
import java.util.regex.MatchResult;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface LinearFinder {
    /**
     * @return the next result from the start position.
     */
    @Nullable
    MatchResult findResult(WikipediaPage page, int start);
    // For the moment we keep this interface as functional,
    // except if finally we see that most implementations are similar.
    // The general structure of this method will be:
    // final String text = page.getContent();
    // while (start < text.length()) {
    //   final int startMatch = findStartMatch(text, start);
    //   if (startMatch >= 0) {
    //     final int endMatch = findEndMatch(text, startMatch);
    //     if (endMatch >= 0) {
    //       final String match = text.substring(startMatch, endMatch);
    //       if (isValid(match)) {
    //         return LinearMatchResult.of(startMatch, match);
    //       } else {
    //         start = endMatch;
    //       }
    //     } else {
    //       start = startMatch + 1;
    //     }
    //   } else {
    //     return null;
    //   }
    // }
    // return null;
}
