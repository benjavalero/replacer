package es.bvalero.replacer.web;

import es.bvalero.replacer.domain.RandomArticle;
import es.bvalero.replacer.domain.Replacement;
import es.bvalero.replacer.persistence.ReplacementDao;
import es.bvalero.replacer.persistence.pojo.ReplacementDb;
import es.bvalero.replacer.service.IWikipediaService;
import es.bvalero.replacer.service.ReplacementService;
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
    private ReplacementDao replacementDao;

    @Autowired
    private ReplacementService replacementService;

    @Autowired
    private IWikipediaService wikipediaService;

    @RequestMapping(value = "/find/random")
    RandomArticle findRandom() {
        LOGGER.info("Finding random article with misspellings...");

        ReplacementDb randomReplacement = replacementDao.findRandomReplacementToFix();
        if (randomReplacement == null) {
            LOGGER.info("No random replacement could be found. Try again...");
            return findRandom();
        }

        String title = randomReplacement.getTitle();
        String content = wikipediaService.getArticleContent(title);
        if (content == null || RegExUtils.isRedirectionArticle(content)) {
            replacementDao.deleteReplacementsByTitle(title);
            return findRandom();
        }

        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(content);

        // We will work since now with the escaped content
        List<Replacement> replacements = replacementService.findReplacements(escapedContent);
        if (replacements.isEmpty()) {
            replacementDao.deleteReplacementsByTitle(title);
            return findRandom();
        }

        String replacedContent = escapedContent;
        Collections.sort(replacements);

        // Replace the possible replacements with buttons to interact with them
        Map<Integer, Replacement> fixes = new TreeMap<>();
        for (Replacement replacement : replacements) {
            String buttonText = "<button id=\"miss-" + replacement.getPosition() + "\" " +
                    "title=\"" + replacement.getExplain() + "\" type=\"button\" class=\"miss btn btn-danger\" " +
                    "data-toggle=\"tooltip\" data-placement=\"top\">" + replacement.getWord() + "</button>";
            replacedContent = StringUtils.replaceAt(replacedContent, replacement.getPosition(),
                    replacement.getWord(), buttonText);

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
        LOGGER.info("Saving changes in: {}", title);

        // Find the fixes verified by the user
        List<Replacement> fixes = new ArrayList<>();
        for (Replacement replacement : article.getFixes().values()) {
            if (replacement.isFixed()) {
                fixes.add(replacement);
            }
        }

        if (fixes.isEmpty()) {
            LOGGER.info("Nothing to fix in article: {}", title);

            // Mark the article has reviewed in the database
            replacementDao.setArticleAsReviewed(title);

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
            replacementDao.setArticleAsReviewed(title);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("The article content could not be replaced with the fixes", e);
        } catch (Exception e) {
            LOGGER.error("The article fixed content could not be uploaded for unknown reasons", e);
        }

        // Return a new article to check
        return findRandom();
    }

}
