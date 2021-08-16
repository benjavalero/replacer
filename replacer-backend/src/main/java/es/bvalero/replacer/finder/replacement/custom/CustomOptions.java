package es.bvalero.replacer.finder.replacement.custom;

import lombok.Value;

@Value(staticConstructor = "of")
public class CustomOptions {

    String replacement;
    boolean caseSensitive;
    String suggestion;
}
