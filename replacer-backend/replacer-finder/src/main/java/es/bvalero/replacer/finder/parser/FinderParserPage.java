package es.bvalero.replacer.finder.parser;

import es.bvalero.replacer.finder.FinderPage;
import es.bvalero.replacer.page.PageKey;
import lombok.Value;

@Value
public class FinderParserPage implements FinderPage {

    PageKey pageKey;
    String title;
    String content;

    Parser parser = new Parser();

    public static FinderParserPage of(FinderPage page) {
        return new FinderParserPage(page.getPageKey(), page.getTitle(), page.getContent());
    }
}
