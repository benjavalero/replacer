package es.bvalero.replacer.article;

import lombok.Value;

@Value
class TypeSubtypeCount {
    private String type;
    private String subtype;
    private long count;
}
