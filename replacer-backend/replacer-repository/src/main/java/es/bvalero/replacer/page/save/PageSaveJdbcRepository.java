package es.bvalero.replacer.page.save;

import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.PageKey;
import es.bvalero.replacer.replacement.save.IndexedReplacementStatus;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import java.time.LocalDate;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Qualifier("pageSaveJdbcRepository")
@Transactional
@Repository
class PageSaveJdbcRepository implements PageSaveRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ReplacementSaveRepository replacementSaveRepository;

    PageSaveJdbcRepository(
        NamedParameterJdbcTemplate jdbcTemplate,
        ReplacementSaveRepository replacementSaveRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.replacementSaveRepository = replacementSaveRepository;
    }

    @Override
    public void save(Collection<IndexedPage> pages) {
        assert pages.stream().noneMatch(p -> p.getStatus() == IndexedPageStatus.UNDEFINED);
        assert pages
            .stream()
            .flatMap(p -> p.getReplacements().stream())
            .noneMatch(p -> p.getStatus() == IndexedReplacementStatus.UNDEFINED);

        // Pages must be added before adding the related replacements
        // We assume the replacements removed correspond to not removed pages

        // Add pages
        // Without the replacements as they will be added later
        add(pages.stream().filter(p -> p.getStatus() == IndexedPageStatus.ADD).toList());

        // Update pages
        update(pages.stream().filter(p -> p.getStatus() == IndexedPageStatus.UPDATE).toList());

        // Add the replacements
        replacementSaveRepository.add(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.ADD)
                .toList()
        );

        // Update the replacements
        replacementSaveRepository.update(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.UPDATE)
                .toList()
        );

        // Remove the replacements
        replacementSaveRepository.remove(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.REMOVE)
                .toList()
        );
    }

    /** Add a collection of pages without adding the replacements */
    private void add(Collection<IndexedPage> pages) {
        assert pages.stream().allMatch(p -> p.getStatus() == IndexedPageStatus.ADD);
        String sql =
            "INSERT INTO page (lang, page_id, title, last_update) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :title, :lastUpdate)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    /** Update a collection of pages */
    private void update(Collection<IndexedPage> pages) {
        assert pages.stream().allMatch(p -> p.getStatus() == IndexedPageStatus.UPDATE);
        String sql =
            "UPDATE page SET title = :title, last_update = :lastUpdate " +
            "WHERE lang = :pageKey.lang.code AND page_id = :pageKey.pageId";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pages.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void updateLastUpdate(PageKey pageKey, LocalDate lastUpdate) {
        String sql = "UPDATE page SET last_update = :lastUpdate WHERE lang = :lang AND page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lastUpdate", lastUpdate)
            .addValue("lang", pageKey.getLang().getCode())
            .addValue("pageId", pageKey.getPageId());
        jdbcTemplate.update(sql, namedParameters);
    }

    @Override
    public void removeByKey(Collection<PageKey> pageKeys) {
        // No need to delete first the replacements as they are deleted on cascade by the database
        // Note the count caches will not be updated with this action
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pageKeys.toArray());
        String sqlPages = "DELETE FROM page WHERE lang = :lang.code AND page_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }
}
