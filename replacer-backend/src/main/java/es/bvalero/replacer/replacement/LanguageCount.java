package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

@Getter
class LanguageCount {

    private final WikipediaLanguage lang;
    private final List<TypeCount> typeCounts = new ArrayList<>();

    LanguageCount(WikipediaLanguage lang) {
        this.lang = lang;
    }

    static LanguageCount ofEmpty() {
        return new LanguageCount(WikipediaLanguage.getDefault());
    }

    boolean contains(String type) {
        return typeCounts.stream().anyMatch(t -> t.getType().equals(type));
    }

    void add(TypeCount typeCount) {
        this.typeCounts.add(typeCount);
    }

    void remove(String type) {
        this.typeCounts.removeIf(t -> t.getType().equals(type));
    }

    TypeCount get(String type) {
        return typeCounts
            .stream()
            .filter(t -> t.getType().equals(type))
            .findAny()
            .orElseThrow(IllegalArgumentException::new);
    }
}
