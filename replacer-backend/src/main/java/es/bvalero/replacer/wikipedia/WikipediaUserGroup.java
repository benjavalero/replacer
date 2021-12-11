package es.bvalero.replacer.wikipedia;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumurate the Wikipedia user groups supported by the application */
@Getter
@AllArgsConstructor
public enum WikipediaUserGroup {
    AUTOCONFIRMED("autoconfirmed"),
    BOT("bot");

    private static final Map<String, WikipediaUserGroup> map = Arrays
        .stream(WikipediaUserGroup.values())
        .collect(Collectors.toMap(WikipediaUserGroup::getGroup, Function.identity()));

    private final String group;

    // We cannot override the static method "valueOf(String)"
    public static WikipediaUserGroup valueOfLabel(String group) {
        return map.get(group);
    }

    @Override
    public String toString() {
        return this.group;
    }
}
