package es.bvalero.replacer.common.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;

/** Enumerates the kinds (or base types) of replacements supported in the application */
@AllArgsConstructor
public enum ReplacementKind {
    SIMPLE((byte) 2),
    COMPOSED((byte) 3),
    CUSTOM((byte) 1),
    STYLE((byte) 5),
    EMPTY((byte) 99); // To be used but not meant to be serialized

    private static final Map<Byte, ReplacementKind> map = Arrays.stream(ReplacementKind.values())
        .filter(k -> k != EMPTY)
        .collect(Collectors.toUnmodifiableMap(ReplacementKind::getCode, Function.identity()));

    private final byte code;

    public byte getCode() {
        if (this == EMPTY) {
            throw new IllegalStateException("Empty kind cannot be serialized");
        }
        return this.code;
    }

    // We cannot override the static method "valueOf(String)"
    // but in this case as the value is an integer we can overload the method
    public static ReplacementKind valueOf(byte code) {
        if (map.containsKey(code)) {
            return map.get(code);
        } else {
            throw new IllegalArgumentException("Wrong replacement kind code: " + code);
        }
    }
}
