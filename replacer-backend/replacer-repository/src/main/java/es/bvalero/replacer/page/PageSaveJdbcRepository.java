package es.bvalero.replacer.page;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.common.domain.WikipediaLanguage;
import es.bvalero.replacer.finder.StandardType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.namedparam.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Qualifier("pageSaveJdbcRepository")
@Transactional
@Repository
class PageSaveJdbcRepository implements PageSaveRepository {

    // Dependency injection
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final PageRepository pageRepository;

    PageSaveJdbcRepository(
        NamedParameterJdbcTemplate jdbcTemplate,
        ApplicationEventPublisher applicationEventPublisher,
        PageRepository pageRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.pageRepository = pageRepository;
    }

    @Override
    public void save(Collection<IndexedPage> pages) {
        assert pages
            .stream()
            .map(IndexedPage::getStatus)
            .noneMatch(s -> s == IndexedPageStatus.UNDEFINED || s == IndexedPageStatus.MODIFIED);
        assert pages
            .stream()
            .flatMap(p -> p.getReplacements().stream())
            .map(IndexedReplacement::getStatus)
            .noneMatch(s -> s == IndexedReplacementStatus.UNDEFINED || s == IndexedReplacementStatus.REVIEWED);

        // Pages must be added before adding the related replacements
        // We assume the replacements removed correspond to not removed pages

        // Add pages
        // Without the replacements as they will be added later
        add(
            pages
                .stream()
                .filter(p -> p.getStatus() == IndexedPageStatus.ADD)
                .toList()
        );

        // Update pages
        update(
            pages
                .stream()
                .filter(p -> p.getStatus() == IndexedPageStatus.UPDATE)
                .toList()
        );

        // Add the replacements (pending)
        addReplacements(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.ADD)
                .toList()
        );

        // Update the replacements (pending)
        updateReplacements(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.UPDATE)
                .toList()
        );

        // Remove the obsolete replacements (from both tables)
        removeReplacements(
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
    public void review(IndexedPage page) {
        updateLastUpdate(page);
        moveToReviewed(page.getReplacements());
        addCustomReplacements(page.getCustomReplacements());
    }

    private void updateLastUpdate(IndexedPage page) {
        assert page.getStatus() == IndexedPageStatus.MODIFIED;
        String sql = "UPDATE page SET last_update = :lastUpdate WHERE lang = :lang AND page_id = :pageId";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lastUpdate", page.getLastUpdate())
            .addValue("lang", page.getPageKey().getLang().getCode())
            .addValue("pageId", page.getPageKey().getPageId());
        jdbcTemplate.update(sql, namedParameters);
    }

    /** Add a collection of custom replacements */
    private void addCustomReplacements(Collection<IndexedCustomReplacement> customReplacements) {
        // Add the page to the database in case it doesn't exist yet
        customReplacements
            .stream()
            .map(IndexedCustomReplacement::getPageKey)
            .distinct()
            .forEach(pageKey -> {
                if (pageRepository.findByKey(pageKey).isEmpty()) {
                    IndexedPage indexedPage = IndexedPage.builder()
                        .pageKey(pageKey)
                        .title("") // It will be set in a next indexation
                        .lastUpdate(LocalDate.now(ZoneId.systemDefault()))
                        .status(IndexedPageStatus.ADD)
                        .build();
                    save(List.of(indexedPage));
                }
            });

        final String sql =
            "INSERT INTO custom (lang, page_id, replacement, cs, start, reviewer, review_type, review_timestamp, old_rev_id, new_rev_id) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :type.subtype, :cs, :start, :reviewer, :reviewType.code, :reviewTimestamp, :oldRevId, :newRevId)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(customReplacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    @Override
    public void removeByKey(Collection<PageKey> pageKeys) {
        // No need to delete first the replacements as they are deleted on cascade by the database
        // (both replacement and reviewed tables have ON DELETE CASCADE)
        // Note the count caches will not be updated with this action
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(pageKeys.toArray());
        String sqlPages = "DELETE FROM page WHERE lang = :lang.code AND page_id = :pageId";
        jdbcTemplate.batchUpdate(sqlPages, namedParameters);
    }

    //region Indexed Replacements

    /** Add a collection of page replacements (pending) assuming the related pages already exist */
    private void addReplacements(Collection<IndexedReplacement> replacements) {
        assert replacements.stream().allMatch(r -> r.getStatus() == IndexedReplacementStatus.ADD);
        String sql =
            "INSERT INTO replacement (page_id, lang, kind, subtype, start, context) " +
            "VALUES (:pageKey.pageId, :pageKey.lang.code, :type.kind.code, :type.subtype, :start, :context)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    /** Update a collection of page replacements (pending) */
    private void updateReplacements(Collection<IndexedReplacement> replacements) {
        assert replacements.stream().allMatch(r -> r.getStatus() == IndexedReplacementStatus.UPDATE);
        String sql = "UPDATE replacement SET start = :start, context = :context WHERE id = :id";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(sql, namedParameters);
    }

    /** Delete a collection of page replacements from both tables */
    private void removeReplacements(Collection<IndexedReplacement> replacements) {
        assert replacements.stream().allMatch(r -> r.getStatus() == IndexedReplacementStatus.REMOVE);
        if (replacements.isEmpty()) {
            // We need to check this so the "IN" clause in the query doesn't fail
            return;
        }

        // Remove from pending
        Set<Integer> pendingIds = replacements
            .stream()
            .filter(r -> r.getReviewer() == null)
            .map(r -> Objects.requireNonNull(r.getId()))
            .collect(Collectors.toUnmodifiableSet());
        if (!pendingIds.isEmpty()) {
            String sql = "DELETE FROM replacement WHERE id IN (:ids)";
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", pendingIds);
            jdbcTemplate.update(sql, namedParameters);
        }

        // Remove from reviewed
        Set<Integer> reviewedIds = replacements
            .stream()
            .filter(r -> r.getReviewer() != null)
            .map(r -> Objects.requireNonNull(r.getId()))
            .collect(Collectors.toUnmodifiableSet());
        if (!reviewedIds.isEmpty()) {
            String sql = "DELETE FROM reviewed WHERE id IN (:ids)";
            SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("ids", reviewedIds);
            jdbcTemplate.update(sql, namedParameters);
        }
    }

    /**
     * Move replacements from the pending table to the reviewed table.
     * This is the "review" operation: DELETE from replacement + INSERT into reviewed.
     * Only the replacements to review (pending) are moved.
     */
    private void moveToReviewed(Collection<IndexedReplacement> replacements) {
        if (replacements.isEmpty()) {
            return; // Do nothing
        }

        // We can assume all replacements belong to the same page
        final Collection<PageKey> pageKeys = replacements
            .stream()
            .map(IndexedReplacement::getPageKey)
            .collect(Collectors.toUnmodifiableSet());
        assert pageKeys.size() == 1;

        final WikipediaLanguage lang = pageKeys.stream().findAny().orElseThrow(IllegalArgumentException::new).getLang();
        replacements
            .stream()
            .map(IndexedReplacement::getType)
            .distinct()
            .forEach(type -> applicationEventPublisher.publishEvent(PageCountDecrementEvent.of(lang, type)));

        // Insert into reviewed
        String insertSql =
            "INSERT INTO reviewed (lang, page_id, kind, subtype, start, context, reviewer, review_type, review_timestamp, old_rev_id, new_rev_id) " +
            "VALUES (:pageKey.lang.code, :pageKey.pageId, :type.kind.code, :type.subtype, :start, :context, :reviewer, :reviewType.code, :reviewTimestamp, :oldRevId, :newRevId)";
        SqlParameterSource[] namedParameters = SqlParameterSourceUtils.createBatch(replacements.toArray());
        jdbcTemplate.batchUpdate(insertSql, namedParameters);

        // Delete from pending (match by page-key, type and start)
        String deleteSql =
            "DELETE FROM replacement " +
            "WHERE lang = :pageKey.lang.code AND page_id = :pageKey.pageId " +
            "AND kind = :type.kind.code AND subtype = :type.subtype AND start = :start";
        jdbcTemplate.batchUpdate(deleteSql, namedParameters);
    }

    @Override
    public void updateReviewerByType(WikipediaLanguage lang, StandardType type, String reviewer) {
        // Move all pending replacements of this type to reviewed (as reviewed by system)
        String insertSql =
            "INSERT INTO reviewed (lang, page_id, kind, subtype, start, context, reviewer, review_type, review_timestamp) " +
            "SELECT lang, page_id, kind, subtype, start, context, :reviewer, 0, NOW() " +
            "FROM replacement WHERE lang = :lang AND kind = :kind AND subtype = :subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("reviewer", reviewer)
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(insertSql, namedParameters);

        String deleteSql = "DELETE FROM replacement WHERE lang = :lang AND kind = :kind AND subtype = :subtype";
        jdbcTemplate.update(deleteSql, namedParameters);
    }

    @Override
    public void removeByType(WikipediaLanguage lang, StandardType type) {
        String sql = "DELETE FROM replacement WHERE lang = :lang AND kind = :kind AND subtype = :subtype";
        SqlParameterSource namedParameters = new MapSqlParameterSource()
            .addValue("lang", lang.getCode())
            .addValue("kind", type.getKind().getCode())
            .addValue("subtype", type.getSubtype());
        jdbcTemplate.update(sql, namedParameters);
    }
    // endregion
}
