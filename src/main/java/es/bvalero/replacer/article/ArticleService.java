package es.bvalero.replacer.article;

import es.bvalero.replacer.article.exception.ExceptionMatchFinder;
import es.bvalero.replacer.article.finder.PotentialErrorFinder;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.StringUtils;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import es.bvalero.replacer.wikipedia.WikipediaException;
import es.bvalero.replacer.wikipedia.WikipediaUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides methods to find articles with potential errors.
 */
@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);

    private static final Pattern REGEX_BUTTON_TAG =
            Pattern.compile("<button.+?</button>", Pattern.DOTALL);
    private static final int TRIM_THRESHOLD = 200;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private PotentialErrorRepository potentialErrorRepository;

    @Autowired
    private IWikipediaFacade wikipediaFacade;

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

    ArticleData findRandomArticleWithPotentialErrors() throws UnfoundArticleException, InvalidArticleException {
        // Find random article in Replacer database (the result can be empty)
        List<Article> randomArticles = articleRepository.findRandomArticleNotReviewed(new PageRequest(0, 1));
        if (randomArticles.isEmpty()) {
            LOGGER.warn("No random article found to review");
            throw new UnfoundArticleException();
        }

        Article randomArticle = randomArticles.get(0);

        // Get the content of the article from Wikipedia
        try {
            String articleContent = wikipediaFacade.getArticleContent(randomArticle.getTitle());

            // Check if the article is processable
            if (WikipediaUtils.isRedirectionArticle(articleContent)) {
                LOGGER.warn("Found article is a redirection page: {}", randomArticle.getTitle());
                articleRepository.delete(randomArticle.getId());
                throw new InvalidArticleException();
            }

            return getArticleDataWithReplacements(randomArticle, articleContent);
        } catch (WikipediaException e) {
            LOGGER.warn("Content could not be retrieved for title: {}", randomArticle.getTitle(), e);
            articleRepository.delete(randomArticle.getId());
            throw new InvalidArticleException();
        } catch (InvalidArticleException e) {
            articleRepository.delete(randomArticle.getId());
            throw e;
        }
    }

    @NotNull
    ArticleData findRandomArticleWithPotentialErrors(@NotNull String word)
            throws UnfoundArticleException, InvalidArticleException {
        // Find random article in Replacer database (the result can be empty)
        List<Article> randomArticles = potentialErrorRepository
                .findRandomByWord(word, new PageRequest(0, 1));
        if (randomArticles.isEmpty()) {
            LOGGER.warn("No random article found to review");
            throw new UnfoundArticleException();
        }

        Article randomArticle = randomArticles.get(0);

        // Get the content of the article from Wikipedia
        try {
            String articleContent = wikipediaFacade.getArticleContent(randomArticle.getTitle());

            // Check if the article is processable
            if (WikipediaUtils.isRedirectionArticle(articleContent)) {
                LOGGER.warn("Found article is a redirection page: {}", randomArticle.getTitle());
                articleRepository.delete(randomArticle.getId());
                throw new InvalidArticleException();
            }

            ArticleData articleData = getArticleDataWithReplacements(randomArticle, articleContent);

            // Check if the requested word is found in the potential fixes
            boolean wordFound = false;
            for (ArticleReplacement replacement : articleData.getFixes().values()) {
                if (word.equals(replacement.getOriginalText())) {
                    wordFound = true;
                }
            }
            // If not we delete the potential error from the article but let the article
            if (!wordFound) {
                LOGGER.warn("Word {} not found as a potential error for article: {}", word, randomArticle.getTitle());
                Iterator<PotentialError> it = randomArticle.getPotentialErrors().iterator();
                while (it.hasNext()) {
                    PotentialError potentialError = it.next();
                    if (potentialError.getText().equals(word)) {
                        it.remove();
                    }
                }
                articleRepository.save(randomArticle);

                throw new InvalidArticleException();
            }

            return articleData;
        } catch (WikipediaException e) {
            LOGGER.warn("Content could not be retrieved for title: {}", randomArticle.getTitle(), e);
            articleRepository.delete(randomArticle.getId());
            throw new InvalidArticleException();
        } catch (InvalidArticleException e) {
            articleRepository.delete(randomArticle.getId());
            throw e;
        }
    }

    @NotNull
    private ArticleData getArticleDataWithReplacements(@NotNull Article article, @NotNull String articleContent)
            throws InvalidArticleException {
        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(articleContent);

        // This method is called when processing only one article from the web interface
        // so the performance is not so important. We retrieve all exception matches and remove the nested ones.
        List<RegexMatch> exceptionMatches = new LinkedList<>();
        for (ExceptionMatchFinder exceptionMatchFinder : exceptionMatchFinders) {
            exceptionMatches.addAll(exceptionMatchFinder.findExceptionMatches(escapedContent, true));
        }
        // LinkedList is better to run iterators and remove items from it
        RegexMatch.removedNestedMatches(exceptionMatches);

        // Find the potential errors
        List<ArticleReplacement> articleReplacements =
                findPotentialErrorsIgnoringExceptions(escapedContent, exceptionMatches);
        LOGGER.info("Article has {} potential errors to review: {}", articleReplacements.size(), article.getTitle());
        if (articleReplacements.isEmpty()) {
            throw new InvalidArticleException();
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
                throw new InvalidArticleException();
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
        // Find the potential errors in the article content
        List<ArticleReplacement> articleReplacements = findPotentialErrors(text);

        // No need to find the exceptions if there are no replacements found
        if (articleReplacements.isEmpty()) {
            return articleReplacements;
        }

        // Ignore the potential errors included in exceptions
        for (ExceptionMatchFinder exceptionMatchFinder : exceptionMatchFinders) {
            List<RegexMatch> exceptionMatches = exceptionMatchFinder.findExceptionMatches(text, false);

            Iterator<ArticleReplacement> it = articleReplacements.iterator();
            while (it.hasNext()) {
                ArticleReplacement articleReplacement = it.next();
                if (articleReplacement.isContainedIn(exceptionMatches)) {
                    it.remove();
                }
            }

            if (articleReplacements.isEmpty()) {
                return articleReplacements;
            }
        }

        return articleReplacements;
    }

    @NotNull
    private List<ArticleReplacement> findPotentialErrorsIgnoringExceptions(
            @NotNull String text, @NotNull List<RegexMatch> exceptionMatches) {
        // Find the potential errors in the article content
        List<ArticleReplacement> articleReplacements = findPotentialErrors(text);

        // Ignore the potential errors included in exceptions
        Iterator<ArticleReplacement> it = articleReplacements.iterator();
        while (it.hasNext()) {
            ArticleReplacement articleReplacement = it.next();
            if (articleReplacement.isContainedIn(exceptionMatches)) {
                it.remove();
            }
        }

        return articleReplacements;
    }

    /**
     * @return A list with all the occurrences of potential errors.
     * If there are no potential errors, the list will be empty.
     */
    @NotNull
    private List<ArticleReplacement> findPotentialErrors(@NotNull String text) {
        // LinkedList is better to run iterators and remove items from it
        List<ArticleReplacement> articleReplacements = new LinkedList<>();
        for (PotentialErrorFinder potentialErrorFinder : potentialErrorFinders) {
            articleReplacements.addAll(potentialErrorFinder.findPotentialErrors(text));
        }
        return articleReplacements;
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

    /**
     * Saves in Wikipedia the changes on an article validated in the front-end.
     */
    boolean saveArticleChanges(@NotNull ArticleData article) {
        // Find the fixes verified by the user
        List<ArticleReplacement> fixedReplacements = new ArrayList<>();
        for (ArticleReplacement replacement : article.getFixes().values()) {
            if (replacement.isFixed()) {
                fixedReplacements.add(replacement);
            }
        }

        if (fixedReplacements.isEmpty()) {
            LOGGER.info("Nothing to fix in article: {}", article.getTitle());
            markArticleAsReviewed(article);
            return true;
        }

        // Apply the fixes

        String currentContent;
        try {
            currentContent = wikipediaFacade.getArticleContent(article.getTitle());
        } catch (WikipediaException e) {
            LOGGER.error("Error getting the current content of the article: {}", article.getTitle(), e);
            return false;
        }

        // Escape the content just in case it contains XML tags, as done when finding the replacements.
        String replacedContent = StringUtils.escapeText(currentContent);

        Collections.sort(fixedReplacements);
        for (ArticleReplacement fix : fixedReplacements) {
            LOGGER.debug("Fixing article {}: {} -> {}", article.getTitle(), fix.getOriginalText(), fix.getFixedText());
            replacedContent = StringUtils.replaceAt(replacedContent, fix.getPosition(), fix.getOriginalText(), fix.getFixedText());
            if (replacedContent == null) {
                LOGGER.error("Error replacing the validated fixes");
                return false;
            }
        }
        String contentToUpload = StringUtils.unEscapeText(replacedContent);

        // Upload the new content to Wikipedia
        // It may happen there has been changes during the edition, but in this point the fixes can be applied anyway.
        // Check just before uploading there are no changes during the edition
        if (contentToUpload.equals(currentContent)) {
            LOGGER.warn("The content to upload matches with the current content");
            markArticleAsReviewed(article);
            return true;
        }

        try {
            wikipediaFacade.editArticleContent(article.getTitle(), contentToUpload, "Correcciones ortográficas");
        } catch (WikipediaException e) {
            LOGGER.error("Error committing fixes to Wikipedia", e);
            return false;
        }

        // Mark the article as reviewed in the database
        markArticleAsReviewed(article);

        return true;
    }

    private void markArticleAsReviewed(@NotNull ArticleData article) {
        Article articleDb = articleRepository.findOne(article.getId());
        articleDb.setReviewDate(new Timestamp(System.currentTimeMillis()));
        articleDb.getPotentialErrors().clear();
        articleRepository.save(articleDb);
    }

}
