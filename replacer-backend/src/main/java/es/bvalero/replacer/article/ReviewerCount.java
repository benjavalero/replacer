package es.bvalero.replacer.article;

import lombok.Value;

@Value
class ReviewerCount {
    private String reviewer;
    private long count;
}
