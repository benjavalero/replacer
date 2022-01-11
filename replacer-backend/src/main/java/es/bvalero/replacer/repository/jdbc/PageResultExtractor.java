package es.bvalero.replacer.repository.jdbc;

import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

class PageResultExtractor implements ResultSetExtractor<Collection<PageModel>> {

    @Override
    public Collection<PageModel> extractData(ResultSet rs) throws SQLException, DataAccessException {
        // We can assume the lang is the same for all the results
        String lang = null;
        final Map<Integer, PageModel> pageMap = new HashMap<>();

        while (rs.next()) {
            if (lang == null) {
                lang = rs.getString("LANG");
            }
            final Integer pageId = rs.getInt("ARTICLE_ID");

            if (!pageMap.containsKey(pageId)) {
                pageMap.put(
                    pageId,
                    PageModel
                        .builder()
                        .lang(lang)
                        .pageId(pageId)
                        .title(rs.getString("TITLE"))
                        .lastUpdate(rs.getDate("LAST_UPDATE").toLocalDate())
                        .build()
                );
            }
            final PageModel page = pageMap.get(pageId);
            assert page != null;

            // The page might exist without replacements. We check it with the type, for instance.
            final String type = rs.getString("TYPE");
            if (type != null) {
                page.addReplacement(
                    ReplacementModel
                        .builder()
                        .id(rs.getLong("ID"))
                        .lang(lang)
                        .pageId(pageId)
                        .type(type)
                        .subtype(rs.getString("SUBTYPE"))
                        .position(rs.getInt("POSITION"))
                        .context(rs.getString("CONTEXT"))
                        .reviewer(rs.getString("REVIEWER"))
                        .build()
                );
            }
        }

        return pageMap.values();
    }
}
