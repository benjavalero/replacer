package es.bvalero.replacer.finder.util;

import java.util.regex.MatchResult;

public interface SimpleMatchResult extends MatchResult {
    @Override
    default int start(int group) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int end() {
        return start() + group().length();
    }

    @Override
    default int end(int group) {
        throw new UnsupportedOperationException();
    }

    @Override
    default String group(int group) {
        throw new UnsupportedOperationException();
    }

    @Override
    default int groupCount() {
        return 0;
    }
}
