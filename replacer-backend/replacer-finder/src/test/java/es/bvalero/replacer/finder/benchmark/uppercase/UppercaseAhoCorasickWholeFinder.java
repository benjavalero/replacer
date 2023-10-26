package es.bvalero.replacer.finder.benchmark.uppercase;

import com.roklenarcic.util.strings.StringMap;
import com.roklenarcic.util.strings.WholeWordMatchMap;
import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.finder.util.ResultMatchListener;
import java.util.Collection;
import java.util.regex.MatchResult;

class UppercaseAhoCorasickWholeFinder extends UppercaseBenchmarkFinder {

    private final StringMap<String> stringMap;

    UppercaseAhoCorasickWholeFinder(Collection<String> words) {
        char[] falseWordChars = { '-' };
        boolean[] wordCharFlags = { false };
        this.stringMap = new WholeWordMatchMap<>(words, words, true, falseWordChars, wordCharFlags);
    }

    @Override
    public Iterable<MatchResult> findMatchResults(FinderPage page) {
        final ResultMatchListener listener = new ResultMatchListener();
        this.stringMap.match(page.getContent(), listener);
        return listener.getMatches();
    }

    @Override
    public boolean validate(MatchResult matchResult, FinderPage page) {
        return isWordPrecededByPunctuation(matchResult.start(), page.getContent());
    }
}
