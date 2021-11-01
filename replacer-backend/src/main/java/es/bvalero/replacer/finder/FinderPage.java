package es.bvalero.replacer.finder;

import es.bvalero.replacer.domain.WikipediaLanguage;
import lombok.Value;
import org.jetbrains.annotations.TestOnly;

@Value(staticConstructor = "of")
public class FinderPage {

    WikipediaLanguage lang;
    String content;
    String title; // For tracing purposes

    @TestOnly
    public static FinderPage of(String content) {
        return FinderPage.of(WikipediaLanguage.getDefault(), content, "");
    }
}
