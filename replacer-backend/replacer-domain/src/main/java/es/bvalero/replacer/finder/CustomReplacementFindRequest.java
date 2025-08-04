package es.bvalero.replacer.finder;

import lombok.Value;

@Value(staticConstructor = "of")
public class CustomReplacementFindRequest {

    String word;
    boolean caseSensitive;
    String comment;
}
