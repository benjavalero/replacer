package es.bvalero.replacer.finder.benchmark.iterable;

import lombok.Data;

@Data
final class MutableMatch {

    private int start;
    private String word;

    public int end() {
        return start + word.length();
    }
}
