package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class MatchResult {

    private int start;
    private String text;

    int getEnd() {
        return this.start + this.text.length();
    }

}
