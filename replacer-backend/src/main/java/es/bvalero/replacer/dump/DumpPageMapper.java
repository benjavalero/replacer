package es.bvalero.replacer.dump;

import es.bvalero.replacer.common.domain.WikipediaPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class DumpPageMapper {

    static WikipediaPage toDomain(DumpPage dumpPage) {
        return WikipediaPage
            .builder()
            .lang(dumpPage.getLang())
            .id(dumpPage.getId())
            .namespace(dumpPage.getNamespace())
            .title(dumpPage.getTitle())
            .content(dumpPage.getContent())
            .lastUpdate(dumpPage.getLastUpdate())
            .build();
    }
}
