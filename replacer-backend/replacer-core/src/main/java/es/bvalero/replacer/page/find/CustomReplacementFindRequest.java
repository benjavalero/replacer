package es.bvalero.replacer.page.find;

import lombok.Value;

@Value(staticConstructor = "of")
public class CustomReplacementFindRequest {

    String word;
    boolean caseSensitive;
    String comment;
}
