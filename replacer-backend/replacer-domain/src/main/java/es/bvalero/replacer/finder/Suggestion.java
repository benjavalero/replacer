package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.util.ReplacerUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/** Suggestion for a replacement found in the content of a page */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Value
public class Suggestion {

    @NonNull
    String text;

    @Nullable
    String comment;

    public static Suggestion of(String text, @Nullable String comment) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank suggestion text");
        }
        return new Suggestion(text, StringUtils.isBlank(comment) ? null : comment);
    }

    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

    public Suggestion toUpperCase() {
        return Suggestion.of(ReplacerUtils.setFirstUpperCaseIgnoringNonLetters(this.text), this.comment);
    }

    /** Merge the given suggestions in case some of them have the same text */
    static List<Suggestion> mergeSuggestions(List<Suggestion> suggestions) {
        // Use a LinkedHashMap to keep the order
        return suggestions
            .stream()
            .collect(Collectors.toMap(Suggestion::getText, Function.identity(), Suggestion::merge, LinkedHashMap::new))
            .values()
            .stream()
            .toList();
    }

    /* Merge two suggestions with the same text into one suggestion with the non-null comments concatenated */
    private Suggestion merge(Suggestion suggestion) {
        if (!getText().equals(suggestion.getText())) {
            throw new IllegalArgumentException();
        }

        // Only merge the non-null comments
        List<String> comments = Stream.of(this.comment, suggestion.getComment()).filter(Objects::nonNull).toList();

        String mergedComment = String.join("; ", comments);
        return Suggestion.of(this.text, mergedComment);
    }

    /**
     * Return an immutable list of suggestions based on the given one
     * but moving to the head the suggestion matching the original text if it exists
     */
    static List<Suggestion> sortSuggestions(List<Suggestion> suggestions, String originalText) {
        // According to Error-Prone a linked list is rarely better than an array list
        List<Suggestion> sorted = new ArrayList<>();

        // If any of the suggestions matches the original text, then move it as the first suggestion.
        // If not, we add it.
        boolean originalFound = false;
        for (Suggestion suggestion : suggestions) {
            if (suggestion.getText().equals(originalText)) {
                sorted.add(0, suggestion);
                originalFound = true;
            } else {
                sorted.add(suggestion);
            }
        }
        if (!originalFound) {
            sorted.add(0, Suggestion.ofNoComment(originalText));
        }

        return List.copyOf(sorted);
    }
}
