package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.find.WikipediaTimestamp;
import es.bvalero.replacer.user.AccessToken;
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
