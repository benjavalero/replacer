package es.bvalero.replacer.wikipedia;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

/** Enumerate the most common Wikipedia user groups supported by the application */
@Slf4j
@Getter
@AllArgsConstructor
public enum WikipediaUserGroup {
    GENERIC("*"), // All users even the unregistered ones
    USER("user"), // Registered users
    AUTO_CONFIRMED("autoconfirmed"), // Low seniority or number of editions
    ROLL_BACKER("rollbacker"),
    AUTO_VERIFIED("autopatrol"),
    VERIFIER("patroller"),
    BUREAUCRAT("bureaucrat"),
    SYSOP("sysop"), // Administrator
    BOT("bot");

    private static final Map<String, WikipediaUserGroup> map = Arrays
        .stream(WikipediaUserGroup.values())
        .collect(Collectors.toMap(WikipediaUserGroup::getGroup, Function.identity()));

    private final String group;

    // We cannot override the static method "valueOf(String)"
    // It is nullable because there are lots of groups and some of them even depend on the Wikipedia language
    @Nullable
    public static WikipediaUserGroup valueOfLabel(String group) {
        if (!map.containsKey(group)) {
            LOGGER.warn("Unknown group label: " + group);
        }
        return map.get(group);
    }
}
