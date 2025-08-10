package es.bvalero.replacer.wikipedia.page;

import es.bvalero.replacer.auth.AccessToken;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveCommand;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveRepository;
import es.bvalero.replacer.wikipedia.WikipediaPageSaveResult;
import es.bvalero.replacer.wikipedia.WikipediaTimestamp;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("offline")
class WikipediaPageSaveOfflineRepository implements WikipediaPageSaveRepository {

    @Override
    public WikipediaPageSaveResult save(WikipediaPageSaveCommand pageSave, AccessToken accessToken) {
        return buildOfflineSaveResult();
    }

    private WikipediaPageSaveResult buildOfflineSaveResult() {
        return WikipediaPageSaveResult.builder()
            .oldRevisionId(1)
            .oldRevisionId(2)
            .newTimestamp(WikipediaTimestamp.now())
            .build();
    }
}
