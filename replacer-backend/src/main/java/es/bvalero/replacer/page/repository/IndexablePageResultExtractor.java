package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.common.domain.WikipediaPageId;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class IndexablePageResultExtractor implements ResultSetExtractor<List<IndexablePage>> {

    @Override
    public List<IndexablePage> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<WikipediaPageId, String> titleMap = new HashMap<>();
        ListValuedMap<WikipediaPageId, IndexableReplacement> replacementMap = new ArrayListValuedHashMap<>();

        while (rs.next()) {
            WikipediaPageId pageId = WikipediaPageId.of(
                WikipediaLanguage.valueOf(rs.getString("LANG")),
                rs.getInt("ARTICLE_ID")
            );

            titleMap.put(pageId, rs.getString("TITLE"));
            replacementMap.put(
                pageId,
                IndexableReplacement
                    .builder()
                    .id(rs.getLong("ID"))
                    .indexablePageId(pageId)
                    .type(rs.getString("TYPE"))
                    .subtype(rs.getString("SUBTYPE"))
                    .position(rs.getInt("POSITION"))
                    .context(rs.getString("CONTEXT"))
                    .lastUpdate(rs.getDate("LAST_UPDATE").toLocalDate())
                    .reviewer(rs.getString("REVIEWER"))
                    .build()
            );
        }

        if (titleMap.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<IndexablePage> pageList = new ArrayList<>(titleMap.size());
            for (Map.Entry<WikipediaPageId, String> entry : titleMap.entrySet()) {
                WikipediaPageId pageId = entry.getKey();
                String title = entry.getValue();
                pageList.add(
                    IndexablePage.builder().id(pageId).title(title).replacements(replacementMap.get(pageId)).build()
                );
            }
            return pageList;
        }
    }
}
