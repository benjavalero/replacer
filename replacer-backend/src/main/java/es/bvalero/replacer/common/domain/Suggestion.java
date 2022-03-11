package es.bvalero.replacer.common.domain;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Suggestion for a replacement found in the content of a page */
@Value(staticConstructor = "of")
public class Suggestion {

    @NonNull
    String text;

    @Nullable
    String comment;

    public Suggestion(String text, @Nullable String comment) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalArgumentException("Blank suggestion text");
        }

        this.text = text;
        this.comment = comment;
    }

    @TestOnly
    public static Suggestion ofNoComment(String text) {
        return of(text, null);
    }

    static Collection<Suggestion> mergeSuggestions(Collection<Suggestion> suggestions) {
        // Use a LinkedHashMap to keep the order
        return suggestions
            .stream()
            .collect(Collectors.toMap(Suggestion::getText, Function.identity(), Suggestion::merge, LinkedHashMap::new))
            .values();
    }

    private Suggestion merge(Suggestion suggestion) {
        if (!this.getText().equals(suggestion.getText())) {
            throw new IllegalArgumentException();
        }

        String mergedComment = String.format("%s; %s", this.comment, suggestion.getComment());
        return Suggestion.of(this.text, mergedComment);
    }

    static List<Suggestion> sortSuggestions(Collection<Suggestion> suggestions, String originalText) {
        // Use a linked list to remove and rearrange easily
        List<Suggestion> sorted = new LinkedList<>();

        // If any of the suggestions matches the original then move it as the first suggestion
        // If not we add it
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

        return sorted;
    }
}
