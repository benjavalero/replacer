package es.bvalero.replacer.cosmetic;

import es.bvalero.replacer.finder.Replacement;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CosmeticChangesService {

    private SameLinkFinder sameLinkFinder = new SameLinkFinder();

    public String applyCosmeticChanges(String text) {
        List<Replacement> replacements = sameLinkFinder.findReplacements(text);
        // By default the article replacements are sorted in descending order by the start
        Collections.sort(replacements);
        for (Replacement replacement : replacements) {
            text = replaceInText(replacement, text);
        }
        return text;
    }

    private String replaceInText(Replacement replacement, String text) {
        int start = replacement.getStart();
        String oldText = replacement.getText();

        // Check just in case that the replacement is correct
        String current = text.substring(start, start + oldText.length());
        if (!current.equals(oldText)) {
            throw new IllegalArgumentException("Wrong replacement: " + current + " - " + oldText);
        }

        String newText = replacement.getSuggestions().get(0).getText();
        return text.substring(0, start) +
                newText +
                text.substring(start + oldText.length());
    }

}
