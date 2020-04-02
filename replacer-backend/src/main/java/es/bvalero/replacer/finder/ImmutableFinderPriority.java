package es.bvalero.replacer.finder;

public enum ImmutableFinderPriority {
    NONE(0),
    LOW(1),
    MEDIUM(5),
    HIGH(10);

    private final int value;

    ImmutableFinderPriority(int value) {
        this.value = value;
    }

    int getValue() {
        return value;
    }
}
