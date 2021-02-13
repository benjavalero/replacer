package es.bvalero.replacer.finder.common;

import es.bvalero.replacer.common.WikipediaLanguage;
import es.bvalero.replacer.common.WikipediaNamespace;
import es.bvalero.replacer.page.IndexablePage;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;

/**
 * Fake page to simplify tests.
 */
@Value
@Builder
public class FakePage implements IndexablePage {

    int id;
    WikipediaLanguage lang;
    String title;
    WikipediaNamespace namespace;
    String content;
    LocalDate lastUpdate;

    public static FakePage of(String content) {
        return FakePage.builder().lang(WikipediaLanguage.getDefault()).content(content).build();
    }
}
