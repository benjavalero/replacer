package es.bvalero.replacer.page.repository;

import es.bvalero.replacer.common.domain.WikipediaLanguage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class PageResultExtractor implements ResultSetExtractor<List<PageModel>> {

    @Override
    public List<PageModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
        // We can assume the lang is the same for all the results
        WikipediaLanguage lang = null;

        Map<Integer, String> titleMap = new HashMap<>();
        ListValuedMap<Integer, ReplacementModel> replacementMap = new ArrayListValuedHashMap<>();

        while (rs.next()) {
            lang = WikipediaLanguage.valueOf(rs.getString("LANG"));
            Integer pageId = rs.getInt("ARTICLE_ID");

            titleMap.put(pageId, rs.getString("TITLE"));
            replacementMap.put(
                pageId,
                ReplacementModel
                    .builder()
                    .id(rs.getLong("ID"))
                    .lang(lang)
                    .pageId(pageId)
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
            Objects.requireNonNull(lang);
            List<PageModel> pageList = new ArrayList<>(titleMap.size());
            for (Map.Entry<Integer, String> entry : titleMap.entrySet()) {
                Integer pageId = entry.getKey();
                String title = entry.getValue();
                pageList.add(
                    PageModel
                        .builder()
                        .lang(lang)
                        .pageId(pageId)
                        .title(title)
                        .replacements(replacementMap.get(pageId))
                        .build()
                );
            }
            return pageList;
        }
    }
}
