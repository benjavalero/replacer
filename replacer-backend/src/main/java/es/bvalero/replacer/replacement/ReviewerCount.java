package es.bvalero.replacer.replacement;

import lombok.Value;

@Value(staticConstructor = "of")
class ReviewerCount {

    String reviewer;
    long count;
}
