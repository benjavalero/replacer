package es.bvalero.replacer.article;

import es.bvalero.replacer.article.exception.ExceptionMatchFinder;
import es.bvalero.replacer.article.finder.PotentialErrorFinder;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Provides methods to find articles with potential errors.
 */
@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);

    private static final String REGEX_BUTTON_TAG = "<button.+?</button>";
    private static final int TRIM_THRESHOLD = 200;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Autowired
    private IWikipediaFacade wikipediaService;

    @Autowired
    private List<ExceptionMatchFinder> exceptionMatchFinders;

    @Autowired
    private List<PotentialErrorFinder> potentialErrorFinders;

    @Value("${replacer.highlight.exceptions}")
    private boolean highlightExceptions;

    @Value("${replacer.hide.empty.paragraphs}")
    private boolean hideEmptyParagraphs;

    private boolean isHighlightExceptions() {
        return highlightExceptions;
    }

    @SuppressWarnings("SameParameterValue")
    void setHighlightExceptions(boolean highlightExceptions) {
        this.highlightExceptions = highlightExceptions;
    }

    private boolean isHideEmptyParagraphs() {
        return hideEmptyParagraphs;
    }

    @SuppressWarnings("SameParameterValue")
    void setHideEmptyParagraphs(boolean hideEmptyParagraphs) {
        this.hideEmptyParagraphs = hideEmptyParagraphs;
    }

    ArticleData findRandomArticleWithPotentialErrors() {
        return findRandomArticleWithPotentialErrors(null);
    }

    @NotNull
    ArticleData findRandomArticleWithPotentialErrors(@Nullable String word) {
        // Find random article in Replacer database. It should never be null.
        Integer maxRowIdNotReviewed = (word == null
                ? articleRepository.findMaxIdNotReviewed()
                : potentialErrorRepository.findMaxArticleIdByWordAndNotReviewed(word));
        if (maxRowIdNotReviewed == null) {
            LOGGER.info("No unreviewed article found for word: {}", word);
            ArticleData articleData = new ArticleData();
            articleData.setTitle("No hay artículos por revisar");
            return articleData;
        }

        Random randomGenerator = new Random();
        Integer startRow = randomGenerator.nextInt(maxRowIdNotReviewed);
        Article randomArticle = (word == null
                ? articleRepository.findFirstByIdGreaterThanAndReviewDateNull(startRow)
                : potentialErrorRepository.findByWordAndIdGreaterThanAndReviewDateNull(startRow, word, new PageRequest(0, 1)).get(0));

        // Get the content of the article from Wikipedia
        String articleContent;
        try {
            articleContent = wikipediaService.getArticleContent(randomArticle.getTitle());
        } catch (WikipediaException e) {
            LOGGER.warn("Content could not be retrieved for title: {}", randomArticle.getTitle(), e);
            articleRepository.delete(randomArticle.getId());
            return findRandomArticleWithPotentialErrors(word);
        }

        if (WikipediaUtils.isRedirectionArticle(articleContent)) {
            LOGGER.warn("Found article is a redirection page: {}", randomArticle.getTitle());
            articleRepository.delete(randomArticle.getId());
            return findRandomArticleWithPotentialErrors(word);
        }

        ArticleData articleData = getArticleDataWithReplacements(randomArticle, articleContent);
        if (articleData == null) {
            LOGGER.warn("Issues when finding replacements for article: {}", randomArticle.getTitle());
            articleRepository.delete(randomArticle.getId());
            return findRandomArticleWithPotentialErrors(word);
        } else {
            return articleData;
        }
    }

    @Nullable
    private ArticleData getArticleDataWithReplacements(@NotNull Article article, @NotNull String articleContent) {
        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(articleContent);

        // Find the possible exceptions and errors in the article content
        List<RegexMatch> exceptionMatches = findExceptionMatches(escapedContent);
        List<ArticleReplacement> articleReplacements = findPotentialErrorsIgnoringExceptions(escapedContent, exceptionMatches);
        if (articleReplacements.isEmpty()) {
            LOGGER.info("Article found has no potential errors to review: {}", article.getTitle());
            articleRepository.delete(article.getId());
            return null;
        }

        // Replace the proposed replacements with buttons to interact with them
        // Replace the error exceptions with a span to highlight them
        String replacedContent = escapedContent;

        if (isHighlightExceptions()) {
            // Include the exception matches as replacements to be highlighted
            for (RegexMatch exceptionMatch : exceptionMatches) {
                articleReplacements.add(new ArticleReplacement(exceptionMatch));
            }
        }

        Collections.sort(articleReplacements);
        Map<Integer, ArticleReplacement> proposedFixes = new TreeMap<>();

        for (ArticleReplacement replacement : articleReplacements) {
            // Check if the replacement is actually a replacement or an exception match
            String newText;
            if (replacement.getType() == null && isHighlightExceptions()) {
                newText = getErrorExceptionSpanText(replacement);
            } else {
                proposedFixes.put(replacement.getPosition(), replacement);
                newText = getReplacementButtonText(replacement);
            }

            replacedContent = StringUtils.replaceAt(replacedContent, replacement.getPosition(),
                    replacement.getOriginalText(), newText);
            if (replacedContent == null) {
                return null;
            }
        }

        // Return only the text blocks with replacements
        if (isHideEmptyParagraphs()) {
            replacedContent = hideNotMatchingParagraphs(replacedContent);
        }

        return new ArticleData(article.getId(), article.getTitle(), replacedContent, proposedFixes);
    }

    /**
     * @return A list with all the occurrences of potential errors.
     * Potential errors contained in exceptions are ignored.
     * If there are no potential errors, the list will be empty.
     */
    @NotNull
    public List<ArticleReplacement> findPotentialErrorsIgnoringExceptions(@NotNull String text) {
        return findPotentialErrorsIgnoringExceptions(text, findExceptionMatches(text));
    }

    @NotNull
    private List<ArticleReplacement> findPotentialErrorsIgnoringExceptions(
            @NotNull String text, @NotNull List<RegexMatch> exceptionMatches) {
        // Find the potential errors in the article content
        List<ArticleReplacement> articleReplacements = findPotentialErrors(text);

        // Ignore the potential errors included in exceptions
        List<ArticleReplacement> filteredArticleReplacements = new ArrayList<>();

        for (ArticleReplacement articleReplacement : articleReplacements) {
            if (!articleReplacement.isContainedIn(exceptionMatches)) {
                filteredArticleReplacements.add(articleReplacement);
            }
        }

        return filteredArticleReplacements;
    }

    /**
     * @return A list with all the occurrences of potential errors.
     * If there are no potential errors, the list will be empty.
     */
    @NotNull
    private List<ArticleReplacement> findPotentialErrors(@NotNull String text) {
        List<ArticleReplacement> articleReplacements = new ArrayList<>();
        for (PotentialErrorFinder potentialErrorFinder : potentialErrorFinders) {
            articleReplacements.addAll(potentialErrorFinder.findPotentialErrors(text));
        }
        return articleReplacements;
    }

    /**
     * @return A list with all the occurrences of text exceptions.
     * If there are no exception matches, the list will be empty.
     */
    @NotNull
    private List<RegexMatch> findExceptionMatches(@NotNull String text) {
        List<RegexMatch> allErrorExceptions = new ArrayList<>();
        // TODO Reduce the amount of errorExceptionFinders by merging
        for (ExceptionMatchFinder exceptionMatchFinder : exceptionMatchFinders) {
            allErrorExceptions.addAll(exceptionMatchFinder.findExceptionMatches(text));
        }

        return RegexMatch.removedNestedMatches(allErrorExceptions);
    }

    @NotNull
    private String getReplacementButtonText(@NotNull ArticleReplacement replacement) {
        return "<button " +
                "id=\"miss-" + replacement.getPosition() + "\" " +
                "title=\"" + replacement.getComment() + "\" " +
                "type=\"button\" " +
                "class=\"miss btn btn-danger\" " +
                "data-toggle=\"tooltip\" " +
                "data-placement=\"top\">" +
                replacement.getOriginalText() +
                "</button>";
    }

    @NotNull
    private String getErrorExceptionSpanText(@NotNull RegexMatch regexMatch) {
        return "<span class=\"syntax exception\">" + regexMatch.getOriginalText() + "</span>";
    }

    @NotNull
    private String hideNotMatchingParagraphs(@NotNull String text) {
        List<String> matchingParagraphs = StringUtils.removeParagraphsNotMatching(text, REGEX_BUTTON_TAG);

        // Join the paragraphs, trimming them, and adding a ruler between.
        StringBuilder reducedContent = new StringBuilder();
        for (String paragraph : matchingParagraphs) {
            if (reducedContent.length() != 0) {
                reducedContent.append("\n<hr>\n");
            }
            reducedContent.append(StringUtils.trimText(paragraph, TRIM_THRESHOLD, REGEX_BUTTON_TAG));
        }
        return reducedContent.toString();
    }

}
