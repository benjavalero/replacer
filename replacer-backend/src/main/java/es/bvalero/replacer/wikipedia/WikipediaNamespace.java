package es.bvalero.replacer.wikipedia;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

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

    private static final Map<Integer, WikipediaNamespace> map = new HashMap<>(12);

    static {
        Arrays.stream(WikipediaNamespace.values()).forEach(
                wikipediaNamespace -> map.put(wikipediaNamespace.value, wikipediaNamespace));
    }

    private final int value;

    WikipediaNamespace(int value) {
        this.value = value;
    }

    public static java.util.Collection<WikipediaNamespace> getProcessableNamespaces() {
        return EnumSet.of(ARTICLE, ANNEX);
    }

    public static WikipediaNamespace valueOf(int namespace) {
        return map.get(namespace);
    }

    public int getValue() {
        return value;
    }

}
