package es.bvalero.replacer.finder.replacement.custom;

import lombok.Value;
import org.springframework.lang.NonNull;

@Value(staticConstructor = "of")
public class CustomOptions {

    @NonNull
    String replacement;

    boolean caseSensitive;

    @NonNull
    String suggestion;
}
