package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.springframework.lang.NonNull;

/**
 * An <strong>immutable</strong> is a section in the page contents to be left untouched,
 * for instance a literal quote, so any replacement found within it must be ignored
 * and not offered to the user for revision.
 */
@Value(staticConstructor = "of")
public class Immutable implements FinderResult {

    int start;

    @NonNull
    String text;
}
