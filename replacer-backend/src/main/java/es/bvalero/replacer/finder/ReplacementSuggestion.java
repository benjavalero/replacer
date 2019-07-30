package es.bvalero.replacer.finder;

import lombok.Value;

@Value
public class ReplacementSuggestion {
    private String text;
    private String comment;
}
