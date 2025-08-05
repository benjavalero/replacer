package es.bvalero.replacer.finder.parser;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.finder.FinderPage;
import lombok.Getter;

@Getter
public class FinderParserPage extends FinderPage {

    private final Parser parser = new Parser();

    private FinderParserPage(PageKey pageKey, String title, String content) {
        super(pageKey, title, content);
    }

    public static FinderParserPage of(FinderPage page) {
        return new FinderParserPage(page.getPageKey(), page.getTitle(), page.getContent());
    }
}
