package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.page.PageKey;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.TestOnly;

public interface FinderPage {
    int SHORT_CONTENT_LENGTH = 50;

    PageKey getPageKey();

    String getTitle();

    String getContent();

    @TestOnly
    static FinderPage of(String content) {
        return of(WikipediaLanguage.getDefault(), "", content);
    }

    @TestOnly
    static FinderPage of(WikipediaLanguage lang, String title, String content) {
        return new FinderPage() {
            @Override
            public PageKey getPageKey() {
                return PageKey.of(lang, 1);
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getContent() {
                return content;
            }
        };
    }

    default String getAbbreviatedContent() {
        return StringUtils.abbreviate(getContent(), SHORT_CONTENT_LENGTH);
    }
}
