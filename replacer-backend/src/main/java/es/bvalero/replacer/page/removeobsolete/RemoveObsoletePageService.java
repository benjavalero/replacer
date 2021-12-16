package es.bvalero.replacer.page.removeobsolete;

import es.bvalero.replacer.common.domain.WikipediaPageId;
import es.bvalero.replacer.repository.PageRepository;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RemoveObsoletePageService {

    @Autowired
    private PageRepository pageRepository;

    public void removeObsoletePages(Collection<WikipediaPageId> pageIds) {
        pageRepository.removePagesById(pageIds);
    }
}
