package es.bvalero.replacer.article;

import es.bvalero.replacer.finder.ArticleReplacement;
import es.bvalero.replacer.wikipedia.WikipediaPage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ArticleIndexService {

    private static final int BATCH_SIZE = 1000;

    @Autowired
    private ReplacementRepository replacementRepository;

    // List of replacements to save and delete in batch
    // We use sets to compare easily in the unit tests
    private Collection<Replacement> toSaveInBatch = new HashSet<>();
    private Collection<Replacement> toDeleteInBatch = new HashSet<>();

    /* INDEX ARTICLES */

    void indexArticleReplacements(WikipediaPage article, Collection<ArticleReplacement> articleReplacements) {
        LOGGER.debug("START Index replacements for article: {} - {}", article.getId(), article.getTitle());
        indexReplacements(article,
                convertArticleReplacements(article, articleReplacements),
                replacementRepository.findByArticleId(article.getId()),
                false);
        LOGGER.debug("END Index replacements for article: {} - {}", article.getId(), article.getTitle());
    }

    public void indexReplacements(WikipediaPage article, Collection<Replacement> replacements,
                                  Collection<Replacement> dbReplacements, boolean indexInBatch) {
        LOGGER.debug("START Index list of replacements\n" +
                        "New: {} - {}\n" +
                        "Old: {} - {}",
                replacements.size(), replacements,
                dbReplacements.size(), dbReplacements);

        // Trick: In case of no replacements found we insert a fake reviewed replacement
        // in order to be able to skip the article when reindexing
        if (replacements.isEmpty() && dbReplacements.isEmpty()) {
            Replacement newReplacement = new Replacement(article.getId(), "", "", 0)
                    .withLastUpdate(article.getLastUpdate().toLocalDate())
                    .withReviewer(ArticleService.SYSTEM_REVIEWER);
            saveReplacement(newReplacement, indexInBatch);
        }

        replacements.forEach(replacement -> {
            Optional<Replacement> existing = findSameReplacementInCollection(replacement, dbReplacements);
            if (existing.isPresent()) {
                handleExistingReplacement(replacement, existing.get(), indexInBatch);
                dbReplacements.remove(existing.get());
            } else {
                // New replacement
                saveReplacement(replacement, indexInBatch);
                LOGGER.debug("Replacement inserted in DB: {}", replacement);
            }
        });

        // Remove the remaining replacements
        dbReplacements.stream().filter(Replacement::isToBeReviewed).forEach(rep -> {
            deleteReplacement(rep, indexInBatch);
            LOGGER.debug("Replacement deleted in DB: {}", rep);
        });
        LOGGER.debug("END Index list of replacements");
    }

    private Optional<Replacement> findSameReplacementInCollection(Replacement replacement,
                                                                  Collection<Replacement> replacements) {
        return replacements.stream().filter(rep -> rep.isSame(replacement)).findAny();
    }

    public Collection<Replacement> convertArticleReplacements(WikipediaPage article,
                                                              Collection<ArticleReplacement> articleReplacements) {
        return articleReplacements.stream().map(
                articleReplacement -> new Replacement(
                        article.getId(), articleReplacement.getType(), articleReplacement.getSubtype(),
                        articleReplacement.getStart())
                        .withLastUpdate(article.getLastUpdate().toLocalDate()))
                .collect(Collectors.toList());
    }

    private void handleExistingReplacement(Replacement newReplacement, Replacement dbReplacement, boolean indexInBatch) {
        if (dbReplacement.getLastUpdate().isBefore(newReplacement.getLastUpdate())) { // DB older than Dump
            Replacement updated = dbReplacement.withLastUpdate(newReplacement.getLastUpdate());
            saveReplacement(updated, indexInBatch);
            LOGGER.debug("Replacement updated in DB: {}", updated);
        } else {
            LOGGER.debug("Replacement existing in DB: {}", dbReplacement);
        }
    }

    private void saveReplacement(Replacement replacement, boolean saveInBatch) {
        if (saveInBatch) {
            toSaveInBatch.add(replacement);
            if (toSaveInBatch.size() >= BATCH_SIZE) {
                flushReplacementsInBatch();
            }
        } else {
            replacementRepository.save(replacement);
        }
    }

    private void deleteReplacement(Replacement replacement, boolean deleteInBatch) {
        if (deleteInBatch) {
            toDeleteInBatch.add(replacement);
            if (toDeleteInBatch.size() >= BATCH_SIZE) {
                flushReplacementsInBatch();
            }
        } else {
            replacementRepository.delete(replacement);
        }
    }

    public void flushReplacementsInBatch() {
        LOGGER.debug("START Save and delete replacements in database. To save: {}. To delete: {}",
                toSaveInBatch.size(), toDeleteInBatch.size());
        replacementRepository.deleteInBatch(toDeleteInBatch);
        replacementRepository.saveAll(toSaveInBatch);

        // Using .clear() the unit tests don't pass don't know why
        toDeleteInBatch = new HashSet<>();
        toSaveInBatch = new HashSet<>();

        // Flush and clear to avoid memory leaks (we are performing millions of updates when indexing the dump)
        replacementRepository.flush();
        replacementRepository.clear(); // This clears all the EntityManager
        LOGGER.debug("END Save and delete replacements in database");
    }

    void reviewReplacement(Replacement replacement, String reviewer) {
        replacementRepository.save(replacement
                .withReviewer(reviewer)
                .withLastUpdate(LocalDate.now()));
    }

}
