package es.bvalero.replacer.finder;

public enum ImmutableFinderPriority {
    NONE(0),
    LOW(2),
    MEDIUM(5),
    HIGH(12),
    VERY_HIGH(25);

    private final int value;

    ImmutableFinderPriority(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
