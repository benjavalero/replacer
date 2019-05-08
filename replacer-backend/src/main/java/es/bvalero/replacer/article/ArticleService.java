package es.bvalero.replacer.article;

import com.github.scribejava.core.model.OAuth1AccessToken;
import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.persistence.Article;
import es.bvalero.replacer.persistence.ArticleRepository;
import es.bvalero.replacer.persistence.ReplacementRepository;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Provides methods to find articles with potential replacements.
 */
@Service
public class ArticleService {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Value("${replacer.hide.empty.paragraphs}")
    private boolean trimText;

    /**
     * @deprecated Used before to remove nested ignore replacements. Kept as it will be probably needed in the future.
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
        return findRandomArticleWithReplacements(null);
    }

    ArticleReview findRandomArticleWithReplacements(@Nullable String word) throws UnfoundArticleException, InvalidArticleException {
        Article randomArticle = findRandomArticleNotReviewedInDb(word);
        try {
            String articleContent = findArticleContent(randomArticle.getTitle());
            List<ArticleReplacement> replacements = findArticleReplacements(articleContent);
            checkWordExistsInReplacements(word, replacements);
            return ArticleReview.builder()
                    .setTitle(randomArticle.getTitle())
                    .setContent(articleContent)
                    .setReplacements(replacements)
                    .setTrimText(trimText)
                    .build();
        } catch (InvalidArticleException e) {
            LOGGER.info("Deleting invalid article: " + e.getMessage());
            deleteArticle(randomArticle);
            throw e;
        }
    }

    private Article findRandomArticleNotReviewedInDb(@Nullable String word) throws UnfoundArticleException {
        List<Article> randomArticles = (word == null)
                ? replacementRepository.findRandom(PageRequest.of(0, 1))
                : replacementRepository.findRandomByWord(word, PageRequest.of(0, 1));

        if (randomArticles.isEmpty()) {
            LOGGER.warn("No random article found to review");
            throw new UnfoundArticleException("No se ha encontrado ningún artículo para revisar");
        }
        return randomArticles.get(0);
    }

    private String findArticleContent(String title) throws InvalidArticleException {
        try {
            String articleContent = wikipediaService.getPageContent(title);

            // Check if the article is processable
            WikipediaPage page = WikipediaPage.builder().setTitle(title).setContent(articleContent).build();
            if (page.isRedirectionPage()) {
                throw new InvalidArticleException("Found article is a redirection page");
            }

            return articleContent;
        } catch (WikipediaException e) {
            throw new InvalidArticleException("Content could not be retrieved");
        }
    }

    private List<ArticleReplacement> findArticleReplacements(String articleContent) throws InvalidArticleException {
        // Find the replacements sorted (the first ones in the list are the last in the text)

        // LinkedList is better to run iterators and remove items from it
        List<ArticleReplacement> articleReplacements = replacementFinderService.findReplacements(articleContent);
        if (articleReplacements.isEmpty()) {
            throw new InvalidArticleException("No replacements found in article");
        }

        Collections.sort(articleReplacements);
        return articleReplacements;
    }

    public void deleteArticle(Article article) {
        replacementRepository.deleteByArticle(article);
        articleRepository.delete(article);
    }

    private void checkWordExistsInReplacements(@Nullable String word, List<ArticleReplacement> replacements) throws InvalidArticleException {
        // If the requested word is not found in the replacements,
        // we delete the replacement from database let the article
        if (word == null) {
            return;
        }

        Optional<ArticleReplacement> wordReplacement = replacements.stream().filter(replacement -> word.equals(replacement.getText())).findAny();
        if (!wordReplacement.isPresent()) {
            throw new InvalidArticleException("Word not found as a replacement");
        }
    }

    /**
     * Saves in Wikipedia the changes on an article validated in the front-end.
     */
    boolean saveArticleChanges(String title, String text, OAuth1AccessToken accessToken) {
        try {
            // Upload new content to Wikipedia
            wikipediaService.savePageContent(title, text, LocalDateTime.now(), accessToken);

            // Mark article as reviewed in the database
            markArticleAsReviewed(title);

            return true;
        } catch (WikipediaException e) {
            LOGGER.error("Error saving or retrieving the content of the article: {}", title);
            deleteArticle(articleRepository.findByTitle(title));
            return false;
        }
    }

    void markArticleAsReviewed(String articleTitle) {
        // Remove the replacements in DB and update the article
        Article articleToSave = articleRepository.findByTitle(articleTitle)
                .withLastUpdate(LocalDate.now());
        replacementRepository.deleteByArticle(articleToSave);
        articleRepository.save(articleToSave);
    }

}
