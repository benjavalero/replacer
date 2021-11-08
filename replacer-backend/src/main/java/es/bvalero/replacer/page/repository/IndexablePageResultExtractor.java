package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.domain.WikipediaLanguage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class IndexablePageResultExtractor implements ResultSetExtractor<List<IndexablePageDB>> {

    @Override
    public List<IndexablePageDB> extractData(ResultSet rs) throws SQLException, DataAccessException {
        // We can assume the lang is the same for all the results
        String lang = null;
        Map<Integer, String> titleMap = new HashMap<>();
        ListValuedMap<Integer, IndexableReplacementDB> replacementMap = new ArrayListValuedHashMap<>();

        while (rs.next()) {
            lang = rs.getString("LANG");
            Integer pageId = rs.getInt("ARTICLE_ID");

            titleMap.put(pageId, rs.getString("TITLE"));
            replacementMap.put(
                pageId,
                IndexableReplacementDB
                    .builder()
                    .id(rs.getLong("ID"))
                    .type(rs.getString("TYPE"))
                    .subtype(rs.getString("SUBTYPE"))
                    .position(rs.getInt("POSITION"))
                    .context(rs.getString("CONTEXT"))
                    .lastUpdate(rs.getDate("LAST_UPDATE").toLocalDate())
                    .reviewer(rs.getString("REVIEWER"))
                    .build()
            );
        }

        if (lang == null) {
            return Collections.emptyList();
        } else {
            List<IndexablePageDB> pageList = new ArrayList<>(titleMap.size());
            for (Map.Entry<Integer, String> entry : titleMap.entrySet()) {
                Integer pageId = entry.getKey();
                String title = entry.getValue();
                pageList.add(
                    IndexablePageDB
                        .builder()
                        .lang(WikipediaLanguage.valueOf(lang))
                        .id(pageId)
                        .title(title)
                        .replacements(replacementMap.get(pageId))
                        .build()
                );
            }
            return pageList;
        }
    }
}
