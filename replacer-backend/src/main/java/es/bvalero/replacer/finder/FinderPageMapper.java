package es.bvalero.replacer.finder;

import es.bvalero.replacer.common.domain.WikipediaPage;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FinderPageMapper {

    public FinderPage fromDomain(WikipediaPage page) {
        return FinderPage.of(page.getId().getLang(), page.getContent(), page.getTitle());
    }
}
