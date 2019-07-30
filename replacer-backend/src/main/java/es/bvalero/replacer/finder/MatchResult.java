package es.bvalero.replacer.finder;

import lombok.Value;
import lombok.experimental.NonFinal;

import java.util.List;

@NonFinal
@Value
public class MatchResult implements Comparable<MatchResult> {

    private int start;
    private String text;

    int getEnd() {
        return this.start + this.text.length();
    }

    @Override
    public int compareTo(MatchResult o) {
        return o.start == start ? getEnd() - o.getEnd() : o.start - start;
    }

    boolean isContainedIn(List<MatchResult> matchResults) {
        boolean isContained = false;
        for (MatchResult matchResult : matchResults) {
            if (isContainedIn(matchResult)) {
                isContained = true;
                break;
            }
        }
        return isContained;
    }

    boolean isContainedIn(MatchResult matchResult) {
        return start >= matchResult.start && getEnd() <= matchResult.getEnd();
    }

}
