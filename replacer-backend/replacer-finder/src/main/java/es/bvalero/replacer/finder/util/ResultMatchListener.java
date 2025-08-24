package es.bvalero.replacer.finder.util;

import com.roklenarcic.util.strings.MapMatchListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.stream.Stream;

/** Listener to capture the results for the Aho-Corasick algorithm */
public class ResultMatchListener implements MapMatchListener<String> {

    private final List<MatchResult> matches = new ArrayList<>(100);

    public Stream<MatchResult> getMatches() {
        return matches.stream();
    }

    @Override
    public boolean match(String text, int start, int end, String word) {
        matches.add(FinderMatchResult.of(start, word));
        return true;
    }
}
