package es.bvalero.replacer.page.save;

import es.bvalero.replacer.common.domain.PageKey;
import es.bvalero.replacer.page.IndexedPage;
import es.bvalero.replacer.page.IndexedPageStatus;
import es.bvalero.replacer.page.find.PageRepository;
import es.bvalero.replacer.replacement.CustomRepository;
import es.bvalero.replacer.replacement.IndexedCustomReplacement;
import es.bvalero.replacer.replacement.IndexedReplacementStatus;
import es.bvalero.replacer.replacement.save.ReplacementSaveRepository;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final PageRepository pageRepository;
    private final CustomRepository customRepository;

    PageSaveJdbcRepository(
        NamedParameterJdbcTemplate jdbcTemplate,
        ReplacementSaveRepository replacementSaveRepository,
        PageRepository pageRepository,
        CustomRepository customRepository
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.replacementSaveRepository = replacementSaveRepository;
        this.pageRepository = pageRepository;
        this.customRepository = customRepository;
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

        // Update the reviewed replacements
        replacementSaveRepository.updateReviewer(
            pages
                .stream()
                .flatMap(p -> p.getReplacements().stream())
                .filter(p -> p.getStatus() == IndexedReplacementStatus.REVIEWED)
                .toList()
        );

        // Update the custom replacements
        addCustomReplacements(pages.stream().flatMap(p -> p.getCustomReplacements().stream()).toList());

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
                        .lastUpdate(LocalDate.now())
                        .status(IndexedPageStatus.ADD)
                        .build();
                    save(List.of(indexedPage));
                }
            });

        customRepository.add(customReplacements);
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
