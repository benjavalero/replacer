package es.bvalero.replacer.wikipedia;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.TestOnly;

/**
 * A namespace used in Wikipedia pages.
 * Even when just a few namespaces are indexed, we need to enumerate all of them
 * as they can be found on pages retrieved from Wikipedia or dumps.
 */
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
    TIMED_TEXT(710),
    MODULE(828);

    private static final Map<Integer, WikipediaNamespace> map = Arrays
        .stream(WikipediaNamespace.values())
        .collect(Collectors.toUnmodifiableMap(WikipediaNamespace::getValue, Function.identity()));

    private final int value;

    // We choose a default namespace to be used on unit tests
    @TestOnly
    public static WikipediaNamespace getDefault() {
        return ARTICLE;
    }

    // We cannot override the static method "valueOf(String)"
    // but in this case as the value is an integer we can overload the method
    public static WikipediaNamespace valueOf(int namespace) {
        if (map.containsKey(namespace)) {
            return map.get(namespace);
        } else {
            throw new IllegalArgumentException("Wrong namespace value: " + namespace);
        }
    }
}
