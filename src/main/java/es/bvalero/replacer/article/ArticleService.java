package es.bvalero.replacer.article;

import es.bvalero.replacer.article.exception.ErrorExceptionFinder;
import es.bvalero.replacer.utils.RegExUtils;
import es.bvalero.replacer.utils.RegexMatch;
import es.bvalero.replacer.utils.RegexMatchType;
import es.bvalero.replacer.utils.StringUtils;
import es.bvalero.replacer.wikipedia.IWikipediaFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ArticleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleService.class);
    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";
    private static final String REGEX_PARAGRAPH = "(^|\\n{2,})(.+?)(?=\\n{2,}|$)";
    private static final String REGEX_BUTTON_TAG = "<button.+?</button>";
    private static final int THRESHOLD = 200;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private IWikipediaFacade wikipediaService;

    @Autowired
    private List<ErrorExceptionFinder> errorExceptionFinders;

    @Autowired
    private List<PotentialErrorFinder> potentialErrorFinders;

    @Value("${replacer.highlight.exceptions}")
    private boolean highlightExceptions;

    @Value("${replacer.hide.empty.paragraphs}")
    private boolean hideEmptyParagraphs;

    @Value("${replacer.trim.paragraphs}")
    private boolean trimParagraphs;

    ArticleData findRandomArticleWithPotentialErrors() {

        // Find random article in Replacer database
        Random randomGenerator = new Random();
        Integer startRow = randomGenerator.nextInt(articleRepository.findMaxId());
        Article randomArticle = articleRepository.findFirstByIdGreaterThanAndReviewDateNull(startRow);
        if (randomArticle == null) {
            LOGGER.warn("No random replacement could be found. Try again...");
            return findRandomArticleWithPotentialErrors();
        }

        // Get the content of the article from Wikipedia
        String articleContent;
        try {
            articleContent = wikipediaService.getArticleContent(randomArticle.getTitle());
        } catch (Exception e) {
            LOGGER.warn("Content could not be retrieved for title: " + randomArticle.getTitle() + ". Try again...", e);
            return findRandomArticleWithPotentialErrors();
        }

        if (isRedirectionArticle(articleContent)) {
            LOGGER.warn("Article found is a redirection page: " + randomArticle.getTitle() + ". Try again...");
            articleRepository.delete(randomArticle.getId());
            return findRandomArticleWithPotentialErrors();
        }

        // Escape the content just in case it contains XML tags
        String escapedContent = StringUtils.escapeText(articleContent);

        // Find the possible errors in the article content
        List<RegexMatch> regexMatches = findPotentialErrorsAndExceptions(escapedContent);
        if (regexMatches.isEmpty()) {
            LOGGER.info("Article found has no potential errors to review: " + randomArticle.getTitle() + ". Try again...");
            articleRepository.delete(randomArticle.getId());
            return findRandomArticleWithPotentialErrors();
        }

        // Replace the proposed replacements with buttons to interact with them
        // Replace the error exceptions with a span to highlight them
        String replacedContent = escapedContent;
        Map<Integer, ArticleReplacement> proposedFixes = new TreeMap<>();
        for (RegexMatch regexMatch : regexMatches) {
            try {
                if (RegexMatchType.MISSPELLING.equals(regexMatch.getType())) {
                    ArticleReplacement replacement = (ArticleReplacement) regexMatch;
                    String buttonText = getReplacementButtonText(replacement);
                    replacedContent = StringUtils.replaceAt(replacedContent, replacement.getPosition(),
                            replacement.getOriginalText(), buttonText);
                    proposedFixes.put(replacement.getPosition(), replacement);
                } else if (highlightExceptions) {
                    String spanText = getErrorExceptionSpanText(regexMatch);
                    replacedContent = StringUtils.replaceAt(replacedContent, regexMatch.getPosition(),
                            regexMatch.getOriginalText(), spanText);
                }
            } catch (IllegalArgumentException iae) {
                LOGGER.error("Error replacing text", iae);
            }
        }

        // Return only the text blocks with replacements
        if (hideEmptyParagraphs) {
            replacedContent = removeParagraphsWithoutReplacements(replacedContent);
        }

        ArticleData articleData = new ArticleData();
        articleData.setId(randomArticle.getId());
        articleData.setTitle(randomArticle.getTitle());
        articleData.setContent(replacedContent);
        articleData.setFixes(proposedFixes);
        return articleData;
    }

    /**
     * @return A list with all the occurrences of potential errors and exceptions.
     * Potential errors contained in exceptions are ignored.
     * If there are no potential errors, the list will be empty.
     */
    public List<RegexMatch> findPotentialErrorsAndExceptions(String text) {
        // Find the error exceptions in the text
        List<RegexMatch> errorExceptions = findErrorExceptions(text);

        // Find the possible errors in the article content ignoring the ones contained in the exceptions
        List<RegexMatch> articleReplacements = new ArrayList<>();
        for (PotentialErrorFinder potentialErrorFinder : potentialErrorFinders) {
            for (ArticleReplacement articleReplacement : potentialErrorFinder.findPotentialErrors(text)) {
                if (!articleReplacement.isContainedIn(errorExceptions)) {
                    articleReplacements.add(articleReplacement);
                }
            }
        }

        // Return all the possible errors along with the exceptions
        List<RegexMatch> regexMatches = new ArrayList<>(articleReplacements);
        if (!regexMatches.isEmpty()) {
            regexMatches.addAll(errorExceptions);
            Collections.sort(regexMatches);
        }
        return regexMatches;
    }

    public boolean isRedirectionArticle(String articleContent) {
        return org.apache.commons.lang3.StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECTION)
                || org.apache.commons.lang3.StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECT);
    }

    private List<RegexMatch> findErrorExceptions(String text) {
        List<RegexMatch> allErrorExceptions = new ArrayList<>();
        for (ErrorExceptionFinder errorExceptionFinder : errorExceptionFinders) {
            allErrorExceptions.addAll(errorExceptionFinder.findErrorExceptions(text));
        }

        // Remove nested exceptions
        boolean[] toDelete = new boolean[allErrorExceptions.size()];
        for (int i = 0; i < allErrorExceptions.size(); i++) {
            RegexMatch regexMatch = allErrorExceptions.get(i);
            for (int j = 0; j < allErrorExceptions.size(); j++) {
                if (i != j && regexMatch.isContainedIn(allErrorExceptions.get(j))) {
                    toDelete[i] = true;
                }
            }
        }
        List<RegexMatch> resultErrorExceptions = new ArrayList<>();
        for (int i = 0; i < allErrorExceptions.size(); i++) {
            if (!toDelete[i]) {
                RegexMatch errorException = allErrorExceptions.get(i);
                errorException.setType(RegexMatchType.EXCEPTION);
                resultErrorExceptions.add(errorException);
            }
        }

        return resultErrorExceptions;
    }

    String getReplacementButtonText(ArticleReplacement replacement) {
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

    private String getErrorExceptionSpanText(RegexMatch regexMatch) {
        return "<span class=\"syntax exception\">" + regexMatch.getOriginalText() + "</span>";
    }

    /* Removes from the text the paragraphs without potential errors */
    String removeParagraphsWithoutReplacements(String text) {
        Pattern patternParagraph = Pattern.compile(REGEX_PARAGRAPH, Pattern.DOTALL);
        StringBuilder reducedContent = new StringBuilder();

        Matcher matcher = patternParagraph.matcher(text);
        while (matcher.find()) {
            String paragraph = matcher.group(2);
            if (paragraph.contains("id=\"miss-")) {
                if (reducedContent.length() != 0) {
                    reducedContent.append("\n<hr>\n");
                }
                reducedContent.append(trimParagraphs ? trimText(paragraph, THRESHOLD) : paragraph);
            }
        }

        return reducedContent.toString();
    }

    String trimText(String text, int threshold) {
        List<RegexMatch> intervals = RegExUtils.findMatches(text, REGEX_BUTTON_TAG, Pattern.DOTALL);

        StringBuilder reducedContent = new StringBuilder();
        int lastFin = 0;
        for (int idx = 0; idx < intervals.size(); idx++) {
            int ini = intervals.get(idx).getPosition();
            int fin = intervals.get(idx).getEnd();
            String buttonText = text.substring(ini, fin);
            String textBefore = text.substring(lastFin, ini);
            lastFin = fin;

            if (idx == 0) {
                reducedContent.append(StringUtils.trimRight(textBefore, threshold)).append(buttonText);
            } else {
                reducedContent.append(StringUtils.trimLeftRight(textBefore, threshold)).append(buttonText);
            }
        }

        reducedContent.append(StringUtils.trimLeft(text.substring(lastFin), threshold));

        return reducedContent.toString();
    }

}
