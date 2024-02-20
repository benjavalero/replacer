package es.bvalero.replacer.replacement;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enumerates the types of review */
@Getter
@AllArgsConstructor
public enum ReviewType {
    UNKNOWN((byte) 0),
    MODIFIED((byte) 1),
    NOT_MODIFIED((byte) 2),
    IGNORED((byte) 3);

    private static final Map<Byte, ReviewType> map = Arrays
        .stream(ReviewType.values())
        .collect(Collectors.toUnmodifiableMap(ReviewType::getCode, Function.identity()));

    private final byte code;

    // We cannot override the static method "valueOf(String)"
    // but in this case as the value is an integer we can overload the method
    public static ReviewType valueOf(byte code) {
        if (map.containsKey(code)) {
            return map.get(code);
        } else {
            throw new IllegalArgumentException("Wrong review type code: " + code);
        }
    }
}
