package es.bvalero.replacer.wikipedia;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class WikipediaUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WikipediaUtils.class);

    public static final TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");
    private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";

    private WikipediaUtils() {
    }

    @Nullable
    public static Date parseWikipediaDate(String dateStr) {
        Date wikiDate = null;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
            dateFormat.setTimeZone(TIME_ZONE);
            wikiDate = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            LOGGER.error("Error parsing Wikipedia date: {}", dateStr, e);
        }
        return wikiDate;
    }

    public static boolean isRedirectionArticle(@NotNull String articleContent) {
        return StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECTION)
                || StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECT);
    }

}
