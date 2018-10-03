package es.bvalero.replacer.wikipedia;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WikipediaUtils {

    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";

    private WikipediaUtils() {
    }

    public static LocalDateTime parseWikipediaDate(String dateStr) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern(DATE_PATTERN);
        return LocalDateTime.from(dateFormat.parse(dateStr));
    }

    public static boolean isRedirectionArticle(@NotNull String articleContent) {
        return StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECTION)
                || StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECT);
    }

}
