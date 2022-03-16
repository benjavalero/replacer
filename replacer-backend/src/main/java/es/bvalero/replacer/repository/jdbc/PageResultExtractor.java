package es.bvalero.replacer.repository.jdbc;

import es.bvalero.replacer.repository.PageModel;
import es.bvalero.replacer.repository.ReplacementModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
            final int pageId = rs.getInt("ARTICLE_ID");
            final String pageLang = lang;
            final String title = rs.getString("TITLE");
            final LocalDate lastUpdate = rs.getDate("LAST_UPDATE").toLocalDate();
            final PageModel page = pageMap.computeIfAbsent(
                pageId,
                id -> PageModel.builder().lang(pageLang).pageId(pageId).title(title).lastUpdate(lastUpdate).build()
            );

            // The page might exist without replacements. We check it with the type, for instance.
            final byte kind = rs.getByte("KIND");
            if (kind > 0) {
                page.addReplacement(
                    ReplacementModel
                        .builder()
                        .id(rs.getInt("ID"))
                        .lang(lang)
                        .pageId(pageId)
                        .kind(kind)
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
