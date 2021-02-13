package es.bvalero.replacer.common;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WikipediaNamespace {
    // Odd numbers correspond to the Talk namespaces
    ARTICLE(0),
    USER(2),
    WIKIPEDIA(4),
    FILE(6),
    MEDIA_WIKI(8),
    TEMPLATE(10),
    HELP(12),
    CATEGORY(14),
    PORTAL(100),
    WIKI_PROJECT(102),
    ANNEX(104),
    MODULE(828);

    private static final Map<Integer, WikipediaNamespace> map = Arrays
        .stream(WikipediaNamespace.values())
        .collect(Collectors.toMap(WikipediaNamespace::getValue, Function.identity()));

    private final int value;

    public static Set<WikipediaNamespace> getProcessableNamespaces() {
        return EnumSet.of(ARTICLE, ANNEX);
    }

    public static WikipediaNamespace valueOf(int namespace) {
        return map.get(namespace);
    }
}
