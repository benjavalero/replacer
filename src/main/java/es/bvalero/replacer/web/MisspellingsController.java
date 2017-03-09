package es.bvalero.replacer.web;

import es.bvalero.replacer.domain.RandomArticle;
import es.bvalero.replacer.domain.Replacement;
import es.bvalero.replacer.domain.ReplacementBD;
import es.bvalero.replacer.service.MisspellingService;
import es.bvalero.replacer.service.ReplacementService;
import es.bvalero.replacer.service.WikipediaService;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
class MisspellingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MisspellingsController.class);

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private MisspellingService misspellingService;

    @Autowired
    private WikipediaService wikipediaService;

    @RequestMapping(value = "/find/random")
    RandomArticle findRandom() {
        ReplacementBD randomReplacement = replacementService.findRandomReplacementToFix();
        if (randomReplacement == null) {
            return findRandom();
        }

        String title = randomReplacement.getTitle();
        String content = wikipediaService.getArticleContent(title);
        if (content == null || RegExUtils.isRedirectionArticle(content)) {
            replacementService.deleteReplacementsByTitle(title);
            return findRandom();
        }

        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(content);

        // We will work since now with the escaped content
        List<Replacement> replacements = replacementService.findReplacements(escapedContent);
        if (replacements.isEmpty()) {
            replacementService.deleteReplacementsByTitle(title);
            return findRandom();
        }

        String replacedContent = escapedContent;
        Collections.sort(replacements);

        // Replace the possible replacements with buttons to interactuate with them
        Map<Integer, Replacement> fixes = new TreeMap<>();
        for (Replacement replacement : replacements) {
            String buttonText = "<button id=\"miss-" + replacement.getPosition() + "\" " +
                    "title=\"" + replacement.getExplain() + "\" type=\"button\" class=\"miss btn btn-danger\" " +
                    "data-toggle=\"tooltip\" data-placement=\"top\">" + replacement.getWord() + "</button>";
            try {
                replacedContent = StringUtils.replaceAt(replacedContent, replacement.getPosition(),
                        replacement.getWord(), buttonText);
            } catch (Exception e) {
                LOGGER.error("Error replacing the fixes with buttons", e);
                return findRandom();
            }

            fixes.put(replacement.getPosition(), replacement);
        }

        // Return only the text blocks with replacements
        replacedContent = StringUtils.removeParagraphsWithoutMisspellings(replacedContent);

        // Highlight syntax
        replacedContent = StringUtils.highlightSyntax(replacedContent);

        return new RandomArticle(title, replacedContent, fixes);
    }

    @RequestMapping(value = "/save/random")
    public RandomArticle saveRandom(@RequestBody RandomArticle article) {
        String title = article.getTitle();

        // Find the fixes verified by the user
        List<Replacement> fixes = new ArrayList<>();
        for (Replacement replacement : article.getFixes().values()) {
            if (replacement.isFixed()) {
                fixes.add(replacement);
            }
        }

        if (fixes.isEmpty()) {
            LOGGER.info("Nothing to fix in article {}", title);
            return findRandom();
        }

        // Apply the fixes
        String content = wikipediaService.getArticleContent(title);
        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(content);

        Collections.sort(fixes);
        String replacedContent = escapedContent;

        try {
            for (Replacement fix : fixes) {
                LOGGER.debug("Fixing article {}: {} -> {}", title, fix.getWord(), fix.getFix());
                replacedContent = StringUtils.replaceAt(replacedContent, fix.getPosition(),
                        fix.getWord(), fix.getFix());
            }
            String contentToUpload = StringUtils.unescapeText(replacedContent);

            // Upload the new content to Wikipedia
            // TODO Check it has not been modified meanwhile. Has the API any check? Yes, but it seems the library doesn't support it.
            if (!contentToUpload.equals(content)) {
                // TODO Try to add a reference in the summary to the tool
                wikipediaService.editArticleContent(title, contentToUpload, "Correcciones ortográficas");
            }

            // Mark the article has reviewed in the database
            replacementService.setArticleAsReviewed(title);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("The article content could not be replaced with the fixes", e);
        } catch (Exception e) {
            LOGGER.error("The article fixed content could not be uploaded for unknown reasons", e);
        }

        // Return a new article to check
        return findRandom();
    }

}
