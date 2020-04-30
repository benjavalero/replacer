package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFindService;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaLanguage;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
abstract class ArticleReviewService {

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ReplacementIndexService replacementIndexService;

    @Autowired
    private ReplacementFindService replacementFindService;

    @Autowired
    private SectionReviewService sectionReviewService;

    @Autowired
    private ModelMapper modelMapper;

    Optional<ArticleReview> findRandomArticleReview(ArticleReviewOptions options) {
        LOGGER.debug("START Find random article review");

        Optional<Integer> randomArticleId = findArticleIdToReview(options);
        while (randomArticleId.isPresent()) {
            // Try to obtain the review from the found article
            // If not, find a new random article ID
            Optional<ArticleReview> review = getArticleReview(randomArticleId.get(), options);
            if (review.isPresent()) {
                return review;
            }

            randomArticleId = findArticleIdToReview(options);
        }

        // If we get here, there are no more articles to review in the database
        LOGGER.info("END Find random article review. No article found.");
        return Optional.empty();
    }

    abstract Optional<Integer> findArticleIdToReview(ArticleReviewOptions options);

    Optional<ArticleReview> getArticleReview(int articleId, ArticleReviewOptions options) {
        LOGGER.info("START Build review for article: {}", articleId);
        Optional<ArticleReview> review = Optional.empty();

        // Load article from Wikipedia
        Optional<WikipediaPage> article = getArticleFromWikipedia(articleId, options);
        if (article.isPresent()) {
            review = buildArticleReview(article.get(), options);
        }

        LOGGER.info("END Build review for article: {}", articleId);
        return review;
    }

    Optional<WikipediaPage> getArticleFromWikipedia(int articleId, ArticleReviewOptions options) {
        // The "options" parameter is used in implementations
        LOGGER.info("START Find Wikipedia article: {}", articleId);
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(articleId);
            if (page.isPresent()) {
                // Check if the article is processable
                if (page.get().isProcessable()) {
                    LOGGER.info("END Found Wikipedia article: {} - {}", articleId, page.get().getTitle());
                    return page;
                } else {
                    LOGGER.warn(String.format("Found article is not processable by content: %s - %s",
                            articleId, page.get().getTitle()));
                }
            } else {
                LOGGER.warn(String.format("No article found. ID: %s", articleId));
            }

            // We get here if the article is not found or not processable
            replacementIndexService.reviewArticleReplacementsAsSystem(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page from Wikipedia", e);
        }

        LOGGER.info("Found no Wikipedia article: {}", articleId);
        return Optional.empty();
    }

    private Optional<ArticleReview> buildArticleReview(WikipediaPage article, ArticleReviewOptions options) {
        // Find the replacements in the article
        List<Replacement> replacements = findReplacements(article, options);

        if (replacements.isEmpty()) {
            return Optional.empty();
        } else {
            ArticleReview articleReview = buildArticleReview(article, replacements);

            // Try to reduce the review size by returning just a section of the page
            Optional<ArticleReview> sectionReview = sectionReviewService.findSectionReview(articleReview);
            if (sectionReview.isPresent()) {
                return sectionReview;
            } else {
                return Optional.of(articleReview);
            }
        }
    }

    private List<Replacement> findReplacements(WikipediaPage article, ArticleReviewOptions options) {
        LOGGER.info("START Find replacements for article: {}", article.getId());
        List<Replacement> replacements = findAllReplacements(article, options);

        // Return the replacements sorted as they appear in the text
        replacements.sort(Collections.reverseOrder());
        LOGGER.info("END Found {} replacements for article: {}", replacements.size(), article.getId());
        return replacements;
    }

    List<Replacement> findAllReplacements(WikipediaPage article, ArticleReviewOptions options) {
        // The "options" parameter is used in implementations
        return findAllReplacements(article);
    }

    List<Replacement> findAllReplacements(WikipediaPage article) {
        // TODO: Receive the language as a parameter
        List<Replacement> replacements = replacementFindService.findReplacements(article.getContent(), WikipediaLanguage.ALL);

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.debug("Update article replacements in database");
        replacementIndexService.indexArticleReplacements(article.getId(),
                replacements.stream().map(article::convertReplacementToIndexed)
                        .collect(Collectors.toList()));

        return replacements;
    }

    ArticleReview buildArticleReview(WikipediaPage article, List<Replacement> replacements) {
        ArticleReview review = modelMapper.map(article, ArticleReview.class);
        review.setReplacements(replacements.stream().map(this::convertToDto).collect(Collectors.toList()));
        return review;
    }

    private ArticleReplacement convertToDto(Replacement replacement) {
        return modelMapper.map(replacement, ArticleReplacement.class);
    }

}
