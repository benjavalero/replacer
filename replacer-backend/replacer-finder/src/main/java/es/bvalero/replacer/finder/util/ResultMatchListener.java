package es.bvalero.replacer.finder.util;

import com.roklenarcic.util.strings.MapMatchListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import lombok.Getter;

/** Listener to capture the results for the Aho-Corasick algorithm */
@Getter
public class ResultMatchListener implements MapMatchListener<String> {

    private final List<MatchResult> matches = new ArrayList<>(100);

    @Override
    public boolean match(String text, int start, int end, String word) {
        matches.add(FinderMatchResult.of(start, word));
        return true;
    }
}
