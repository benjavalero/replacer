package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaPage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import lombok.experimental.UtilityClass;

@UtilityClass
class DumpPageMapper {

    WikipediaPage toDomain(DumpPage dumpPage) {
        return WikipediaPage
            .builder()
            .id(WikipediaPageId.of(dumpPage.getLang(), dumpPage.getId()))
            .namespace(dumpPage.getNamespace())
            .title(dumpPage.getTitle())
            .content(dumpPage.getContent())
            .lastUpdate(dumpPage.getLastUpdate())
            .build();
    }
}
