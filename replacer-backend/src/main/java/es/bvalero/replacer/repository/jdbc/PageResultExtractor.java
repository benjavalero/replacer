package es.bvalero.replacer.repository.jdbc;

import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementModel;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class PageResultExtractor implements ResultSetExtractor<Collection<PageModel>> {

    @Override
    public Collection<PageModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
        // We can assume the lang is the same for all the results
        String lang = null;

        Map<Integer, PageModel> pageMap = new HashMap<>();
        ListValuedMap<Integer, ReplacementModel> replacementMap = new ArrayListValuedHashMap<>();

        while (rs.next()) {
            lang = rs.getString("LANG");
            Integer pageId = rs.getInt("ARTICLE_ID");

            pageMap.put(
                pageId,
                PageModel
                    .builder()
                    .lang(lang)
                    .pageId(pageId)
                    .title(rs.getString("TITLE"))
                    .lastUpdate(Optional.ofNullable(rs.getDate("PAGE_UPDATE")).map(Date::toLocalDate).orElse(null))
                    .build()
            );
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
                    .lastUpdate(rs.getDate("REP_UPDATE").toLocalDate())
                    .reviewer(rs.getString("REVIEWER"))
                    .build()
            );
        }

        if (pageMap.isEmpty()) {
            return Collections.emptyList();
        } else {
            Objects.requireNonNull(lang);
            List<PageModel> pageList = new ArrayList<>(pageMap.size());
            for (Map.Entry<Integer, PageModel> entry : pageMap.entrySet()) {
                Integer pageId = entry.getKey();
                PageModel page = entry.getValue();
                pageList.add(
                    PageModel
                        .builder()
                        .lang(lang)
                        .pageId(pageId)
                        .title(page.getTitle())
                        .lastUpdate(page.getLastUpdate())
                        .replacements(replacementMap.get(pageId))
                        .build()
                );
            }
            return pageList;
        }
    }
}
