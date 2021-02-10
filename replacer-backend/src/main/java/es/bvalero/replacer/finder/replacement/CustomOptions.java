package es.bvalero.replacer.finder.replacement;

import lombok.Value;

@Value(staticConstructor = "of")
public class CustomOptions {

    String replacement;
    String suggestion;
}
