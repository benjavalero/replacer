package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.Replacement;
import es.bvalero.replacer.finder.ReplacementFinderService;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import es.bvalero.replacer.wikipedia.WikipediaService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
abstract class ArticleReviewService {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Autowired
    private WikipediaService wikipediaService;

    @Autowired
    private ArticleIndexService articleIndexService;

    @Autowired
    private ReplacementFinderService replacementFinderService;

    @Autowired
    private SectionReviewService sectionReviewService;

    @Autowired
    private ModelMapper modelMapper;

    Optional<ArticleReview> findRandomArticleReview() {
        LOGGER.info("START Find random article review");

        Optional<Integer> randomArticleId = findArticleIdToReview();
        while (randomArticleId.isPresent()) {
            // Try to obtain the review from the found article
            // If not, find a new random article ID
            Optional<ArticleReview> review = getArticleReview(randomArticleId.get());
            if (review.isPresent()) {
                LOGGER.info("END Find random article to review. Found article ID: {}", randomArticleId.get());
                return review;
            }

            randomArticleId = findArticleIdToReview();
        }

        // If we get here, there are no more articles to review in the database
        LOGGER.info("END Find random article review. No article found.");
        return Optional.empty();
    }

    Optional<Integer> findArticleIdToReview() {
        // The default method is finding articles to review from database
        PageRequest pagination = PageRequest.of(0, 1);
        return replacementRepository.findRandomArticleIdsToReview(pagination).stream().findAny();
    }

    Optional<ArticleReview> getArticleReview(int articleId) {
        LOGGER.info("START Build review for article: {}", articleId);
        Optional<ArticleReview> review = Optional.empty();

        // Load article from Wikipedia
        Optional<WikipediaPage> article = getArticleFromWikipedia(articleId);
        if (article.isPresent()) {
            review = buildArticleReview(article.get());
        }

        LOGGER.info("END Build review for article");
        return review;
    }

    Optional<WikipediaPage> getArticleFromWikipedia(int articleId) {
        try {
            Optional<WikipediaPage> page = wikipediaService.getPageById(articleId);
            if (page.isPresent()) {
                // Check if the article is processable
                if (page.get().isProcessable()) {
                    return page;
                } else {
                    LOGGER.warn(String.format("Found article is not processable by content: %s - %s",
                            articleId, page.get().getTitle()));
                }
            } else {
                LOGGER.warn(String.format("No article found. ID: %s", articleId));
            }

            // We get here if the article is not found or not processable
            articleIndexService.reviewArticleAsSystem(articleId);
        } catch (WikipediaException e) {
            LOGGER.error("Error finding page from Wikipedia", e);
        }

        return Optional.empty();
    }

    private Optional<ArticleReview> buildArticleReview(WikipediaPage article) {
        // Find the replacements in the article
        List<Replacement> replacements = findReplacements(article);
        LOGGER.info("Potential replacements found in text: {}", replacements.size());

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

    private List<Replacement> findReplacements(WikipediaPage article) {
        List<Replacement> replacements = findAllReplacements(article);

        // Return the replacements sorted as they appear in the text
        replacements.sort(Collections.reverseOrder());
        return replacements;
    }

    List<Replacement> findAllReplacements(WikipediaPage article) {
        List<Replacement> replacements = replacementFinderService.findReplacements(article.getContent());

        // We take profit and we update the database with the just calculated replacements (also when empty)
        LOGGER.info("Update article replacements in database");
        articleIndexService.indexArticleReplacements(article, replacements);

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
