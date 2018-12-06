package es.bvalero.replacer.article;

import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Provides methods to find articles with potential replacements.
 */
@Service
public class ArticleService {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private IWikipediaFacade wikipediaFacade;

    @Autowired
    private List<IgnoredReplacementFinder> ignoredReplacementFinders;

    @Autowired
    private List<ArticleReplacementFinder> articleReplacementFinders;

    @Value("${replacer.hide.empty.paragraphs}")
    private boolean trimText;

    /**
     * @deprecated Used before to remove nested ignore replacements. Kept in case it is needed in the future.
     */
    @Deprecated
    static List<ArticleReplacement> removeNestedReplacements(List<ArticleReplacement> replacements) {
        // The list of replacements must be of type LinkedList
        if (replacements.isEmpty()) {
            return replacements;
        }

        replacements.sort(Collections.reverseOrder());
        ListIterator<ArticleReplacement> it = replacements.listIterator();
        ArticleReplacement previous = it.next();
        while (it.hasNext()) {
            ArticleReplacement current = it.next();
            if (current.isContainedIn(previous)) {
                it.remove();
            } else if (current.intersects(previous)) {
                // Merge previous and current
                ArticleReplacement merged = previous.withText(previous.getText().substring(0, current.getStart() - previous.getStart()) + current.getText());
                it.remove(); // Remove the current match
                it.previous();
                it.remove(); // Remove the previous match
                it.add(merged); // Add the merged match
                previous = merged;
            } else {
                previous = current;
            }
        }
        return replacements;
    }

    ArticleReview findRandomArticleWithReplacements() throws UnfoundArticleException, InvalidArticleException {
        // Find random article in Replacer database (the result can be empty)
        List<Article> randomArticles = articleRepository.findRandomArticleNotReviewed(PageRequest.of(0, 1));
        if (randomArticles.isEmpty()) {
            LOGGER.warn("No random article found to review");
            throw new UnfoundArticleException("No se ha encontrado ningún artículo para revisar");
        }

        Article randomArticle = randomArticles.get(0);

        try {
            // Get the content of the article from Wikipedia
            String articleContent = wikipediaFacade.getArticleContent(randomArticle.getTitle());

            // Check if the article is processable
            if (WikipediaUtils.isRedirectionArticle(articleContent)) {
                LOGGER.warn("Found article is a redirection page: {}", randomArticle.getTitle());
                throw new InvalidArticleException("Found article is a redirection page");
            }

            // Find the replacements sorted (the first ones in the list are the last in the text)
            List<ArticleReplacement> replacements = findReplacements(articleContent);
            if (replacements.isEmpty()) {
                throw new InvalidArticleException("No replacements found in article: " + randomArticle.getTitle());
            }

            Collections.sort(replacements);

            return ArticleReview.builder()
                    .setTitle(randomArticle.getTitle())
                    .setContent(articleContent)
                    .setReplacements(replacements)
                    .setTrimText(trimText)
                    .build();
        } catch (InvalidArticleException e) {
            replacementRepository.deleteByArticle(randomArticle);
            articleRepository.delete(randomArticle);
            throw e;
        } catch (WikipediaException e) {
            LOGGER.warn("Content could not be retrieved for title: {}", randomArticle.getTitle());
            replacementRepository.deleteByArticle(randomArticle);
            articleRepository.delete(randomArticle);
            throw new InvalidArticleException(e);
        }
    }

    ArticleReview findRandomArticleWithReplacements(String word) throws UnfoundArticleException, InvalidArticleException {
        // Find random article in Replacer database (the result can be empty)
        List<Article> randomArticles = replacementRepository.findRandomByWord(word, PageRequest.of(0, 1));
        if (randomArticles.isEmpty()) {
            LOGGER.warn("No random article found to review");
            throw new UnfoundArticleException("No se ha encontrado ningún artículo para revisar");
        }

        Article randomArticle = randomArticles.get(0);

        ArticleReview articleReview;
        try {
            // Get the content of the article from Wikipedia
            String articleContent = wikipediaFacade.getArticleContent(randomArticle.getTitle());

            // Check if the article is processable
            if (WikipediaUtils.isRedirectionArticle(articleContent)) {
                LOGGER.warn("Found article is a redirection page: {}", randomArticle.getTitle());
                throw new InvalidArticleException("Found article is a redirection page");
            }

            // Find the replacements sorted (the first ones in the list are the last in the text)
            List<ArticleReplacement> replacements = findReplacements(articleContent);
            if (replacements.isEmpty()) {
                throw new InvalidArticleException("No replacements found in article: " + randomArticle.getTitle());
            }

            Collections.sort(replacements);

            articleReview = ArticleReview.builder()
                    .setTitle(randomArticle.getTitle())
                    .setContent(articleContent)
                    .setReplacements(replacements)
                    .setTrimText(trimText)
                    .build();
        } catch (InvalidArticleException e) {
            replacementRepository.deleteByArticle(randomArticle);
            articleRepository.delete(randomArticle);
            throw e;
        } catch (WikipediaException e) {
            LOGGER.warn("Content could not be retrieved for title: {}", randomArticle.getTitle());
            replacementRepository.deleteByArticle(randomArticle);
            articleRepository.delete(randomArticle);
            throw new InvalidArticleException(e);
        }

        // Check if the requested word is found in the replacements
        // If not we delete the replacement from the article but let the article
        boolean wordFound = false;
        for (ArticleReplacement replacement : articleReview.getReplacements()) {
            if (word.equals(replacement.getText())) {
                wordFound = true;
            }
        }
        if (!wordFound) {
            LOGGER.warn("Word {} not found as a replacement for article: {}", word, randomArticle.getTitle());
            replacementRepository.deleteByArticleAndText(randomArticle, word);
            throw new InvalidArticleException("Word not found as a replacement");
        }

        return articleReview;
    }

    /**
     * @return A list with all the occurrences of replacements.
     * Replacements contained in exceptions are ignored.
     * If there are no replacements, the list will be empty.
     */
    public List<ArticleReplacement> findReplacements(String text) {
        // Find the replacements in the article content
        // LinkedList is better to run iterators and remove items from it
        List<ArticleReplacement> articleReplacements = new LinkedList<>();
        for (ArticleReplacementFinder finder : articleReplacementFinders) {
            articleReplacements.addAll(finder.findReplacements(text));
        }

        // No need to find the exceptions if there are no replacements found
        if (articleReplacements.isEmpty()) {
            return articleReplacements;
        }

        // Ignore the replacements which must be ignored
        for (IgnoredReplacementFinder ignoredFinder : ignoredReplacementFinders) {
            List<ArticleReplacement> ignoredReplacements = ignoredFinder.findIgnoredReplacements(text);
            ignoredReplacements.forEach(articleReplacement -> LOGGER.debug("IGNORED: {}", articleReplacement));
            articleReplacements.removeIf(replacement -> replacement.isContainedIn(ignoredReplacements));

            if (articleReplacements.isEmpty()) {
                break;
            }
        }

        return articleReplacements;
    }

    /**
     * Saves in Wikipedia the changes on an article validated in the front-end.
     */
    boolean saveArticleChanges(String title, String text) {
        // Upload the new content to Wikipedia
        try {
            wikipediaFacade.editArticleContent(title, text);

            // Mark the article as reviewed in the database
            markArticleAsReviewed(title);

            return true;
        } catch (WikipediaException e) {
            LOGGER.error("Error saving or retrieving the content of the article: {}", title);
            Article dbArticle = articleRepository.findByTitle(title);
            replacementRepository.deleteByArticle(dbArticle);
            articleRepository.delete(dbArticle);
            return false;
        }
    }

    boolean markArticleAsReviewed(String articleTitle) {
        Article dbArticle = articleRepository.findByTitle(articleTitle);
        if (dbArticle == null) {
            LOGGER.error("Article not found with title: {}", articleTitle);
            return false;
        } else {
            Article articleToSave = dbArticle.withReviewDate(LocalDateTime.now());
            replacementRepository.deleteInBatch(replacementRepository.findByArticle(articleToSave));
            articleRepository.save(articleToSave);
            return true;
        }
    }

}
