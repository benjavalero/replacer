package es.bvalero.replacer.wikipedia;

import org.apache.commons.lang3.StringUtils;

public final class WikipediaUtils {

    private static final String TAG_REDIRECTION = "#REDIRECCIÓN";
    private static final String TAG_REDIRECT = "#REDIRECT";

    private WikipediaUtils() {
    }

    public static boolean isRedirectionArticle(CharSequence articleContent) {
        return StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECTION)
                || StringUtils.containsIgnoreCase(articleContent, TAG_REDIRECT);
    }

}
