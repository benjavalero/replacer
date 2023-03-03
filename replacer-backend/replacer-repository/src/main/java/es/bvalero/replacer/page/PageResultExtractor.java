package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.StandardType;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.replacement.IndexedReplacement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class PageResultExtractor implements ResultSetExtractor<Collection<IndexedPage>> {

    @Override
    public Collection<IndexedPage> extractData(ResultSet rs) throws SQLException, DataAccessException {
        final Map<PageKey, IndexedPage> pageMap = new HashMap<>();

        while (rs.next()) {
            final int pageId = rs.getInt("PAGE_ID");
            final WikipediaLanguage pageLang = WikipediaLanguage.valueOfCode(rs.getString("LANG"));
            final PageKey pageKey = PageKey.of(pageLang, pageId);

            final String title = rs.getString("TITLE");
            final LocalDate lastUpdate = rs.getDate("LAST_UPDATE").toLocalDate();
            final IndexedPage page = pageMap.computeIfAbsent(
                pageKey,
                key -> IndexedPage.builder().pageKey(key).title(title).lastUpdate(lastUpdate).build()
            );

            // The page might exist without replacements. We check it with the kind, for instance.
            final byte kind = rs.getByte("KIND");
            if (kind > 0) {
                page.addReplacement(
                    IndexedReplacement
                        .builder()
                        .id(rs.getInt("ID"))
                        .pageKey(pageKey)
                        .type(StandardType.of(kind, rs.getString("SUBTYPE")))
                        .start(rs.getInt("START"))
                        .context(rs.getString("CONTEXT"))
                        .reviewer(rs.getString("REVIEWER"))
                        .build()
                );
            }
        }

        return pageMap.values();
    }
}
