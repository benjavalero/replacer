package es.bvalero.replacer.replacement;

import lombok.Value;

@Value
class TypeSubtypeCount {
    private String type;
    private String subtype;
    private long count;
}
