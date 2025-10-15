package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.SequencedCollection;
import java.util.regex.MatchResult;

@FunctionalInterface
public interface LinearCollectionFinder {
    /** Return the next result(s) from the start position */
    SequencedCollection<MatchResult> findResults(FinderPage page, int start);
    // We keep this interface as functional.
    // Most implementations are similar, but just in the global "while" loop.
    // Besides, we should create fake matches to implement the "continue" feature
    //
    // The general structure of this method will be:
    // final String text = page.getContent();
    // while (start < text.length()) {
    //   final int startMatch = findStartMatch(text, start);
    //   if (startMatch >= 0) {
    //     final int endMatch = findEndMatch(text, startMatch);
    //     if (endMatch >= 0) {
    //       final String match = text.substring(startMatch, endMatch);
    //       if (isValid(match)) {
    //         return FinderMatchResult.of(startMatch, match);
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
