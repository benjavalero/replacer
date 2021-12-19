package es.bvalero.replacer.wikipedia;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

/** Enumerate the Wikipedia user groups supported by the application */
@Slf4j
@Getter
@AllArgsConstructor
public enum WikipediaUserGroup {
    GENERIC("*"),
    USER("user"),
    AUTOCONFIRMED("autoconfirmed"),
    ROLLBACKER("rollbacker"),
    PATROLLER("patroller"),
    AUTOPATROLLED("autopatrolled"),
    BOT("bot");

    private static final Map<String, WikipediaUserGroup> map = Arrays
        .stream(WikipediaUserGroup.values())
        .collect(Collectors.toMap(WikipediaUserGroup::getGroup, Function.identity()));

    private final String group;

    // We cannot override the static method "valueOf(String)"
    // TODO: Temporarily return an optional to detect unknown user groups
    @Nullable
    public static WikipediaUserGroup valueOfLabel(String group) {
        if (!map.containsKey(group)) {
            LOGGER.error("Wrong group label: " + group);
        }
        return map.get(group);
    }
}
