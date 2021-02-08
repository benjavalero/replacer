package es.bvalero.replacer.replacement;

import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
class LanguageCount {

    // TODO: Include TypeCount and SubtypeCount as private nested classes
    // and manage here the complete building of the language count

    WikipediaLanguage lang;

    @Getter(AccessLevel.NONE)
    Map<String, TypeCount> typeCounts = new TreeMap<>();

    List<TypeCount> getTypeCounts() {
        return new ArrayList<>(this.typeCounts.values());
    }

    static LanguageCount ofEmpty() {
        return new LanguageCount(WikipediaLanguage.getDefault());
    }

    boolean contains(String type) {
        return typeCounts.containsKey(type);
    }

    void add(TypeCount typeCount) {
        this.typeCounts.put(typeCount.getType(), typeCount);
    }

    void remove(String type) {
        this.typeCounts.remove(type);
    }

    TypeCount get(String type) {
        return typeCounts.get(type);
    }
}
