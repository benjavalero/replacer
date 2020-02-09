package es.bvalero.replacer.finder.benchmark;

import lombok.Value;

@Value(staticConstructor = "of")
public class FinderResult {
    private int start;
    private String text;
}
