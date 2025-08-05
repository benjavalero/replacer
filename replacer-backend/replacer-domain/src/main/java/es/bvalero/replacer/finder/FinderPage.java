package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import lombok.Value;
import lombok.With;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.TestOnly;
import org.springframework.lang.NonNull;

/*
 * This class contains the minimum data to retrieve the replacements from a text.
 * The page key contains the language of the text.
 * The page title is also needed for tracing purposes.
 */
@NonFinal
@Value
public class FinderPage {

    @NonNull
    PageKey pageKey;

    @NonNull
    String title;

    @NonNull
    @With
    String content;

    @TestOnly
    public static FinderPage of(String content) {
        return of(WikipediaLanguage.getDefault(), content);
    }

    @TestOnly
    public static FinderPage of(WikipediaLanguage lang, String content) {
        return of(lang, "", content);
    }

    @TestOnly
    public static FinderPage of(String title, String content) {
        return of(WikipediaLanguage.getDefault(), title, content);
    }

    @TestOnly
    private static FinderPage of(WikipediaLanguage lang, String title, String content) {
        return of(PageKey.of(lang, 1), title, content);
    }

    public static FinderPage of(PageKey pageKey, String title, String content) {
        return new FinderPage(pageKey, title, content);
    }
}
