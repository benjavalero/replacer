package es.bvalero.replacer.finder.util;

import es.bvalero.replacer.finder.FinderPage;
import java.util.regex.MatchResult;
import lombok.Getter;
import org.springframework.lang.Nullable;

/** Adapter to simplify the creation of specific result classes implementing MatchResult. */
public abstract class BaseMatchResult implements MatchResult {

    private final MatchResult matchResult;

    @Getter
    private final FinderPage finderPage;

    protected BaseMatchResult(MatchResult matchResult, FinderPage finderPage) {
        this.matchResult = matchResult;
        this.finderPage = finderPage;
    }

    @Override
    public int start() {
        return this.matchResult.start();
    }

    @Override
    public int start(int group) {
        return this.matchResult.start(group);
    }

    @Override
    public int end() {
        return this.matchResult.end();
    }

    @Override
    public int end(int group) {
        return this.matchResult.end(group);
    }

    @Override
    public String group() {
        return this.matchResult.group();
    }

    @Nullable
    @Override
    public String group(int group) {
        return this.matchResult.group(group);
    }

    @Override
    public int groupCount() {
        return this.matchResult.groupCount();
    }
}
