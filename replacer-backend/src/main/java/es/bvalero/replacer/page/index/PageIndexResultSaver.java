package es.bvalero.replacer.page.index;

import es.bvalero.replacer.page.repository.IndexablePageDB;
import es.bvalero.replacer.page.repository.IndexablePageRepository;
import es.bvalero.replacer.page.repository.IndexableReplacementDB;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PageIndexResultSaver {

    @Autowired
    IndexablePageRepository indexablePageRepository;

    @Value("${replacer.dump.batch.chunk.size}")
    private int chunkSize;

    // Singleton field to add the items until the batch limit is reached
    private PageIndexResult batchResult;

    /* Save in DB the result of a page indexing no matter the size of the batch */
    public void save(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        this.saveBatchResult();
    }

    /* Save in DB the results of page indexing when the batch is large enough */
    public void saveBatch(PageIndexResult pageIndexResult) {
        this.addResultToBatch(pageIndexResult);
        if (this.batchResult.size() >= chunkSize) {
            this.saveBatchResult();
        }
    }

    private void addResultToBatch(PageIndexResult pageIndexResult) {
        // Add all the items to the batch result
        this.batchResult =
            this.batchResult.toBuilder()
                .createPages(pageIndexResult.getCreatePages())
                .updatePages(pageIndexResult.getUpdatePages())
                .createReplacements(pageIndexResult.getCreateReplacements())
                .updateReplacements(pageIndexResult.getUpdateReplacements())
                .deleteReplacements(pageIndexResult.getDeleteReplacements())
                .build();
    }

    private void saveBatchResult() {
        // For the new pages, first we create the pages and then the related replacements.
        Collection<IndexableReplacementDB> newReplacements =
            this.batchResult.getCreatePages()
                .stream()
                .map(IndexablePageDB::getReplacements)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        this.batchResult = this.batchResult.toBuilder().createReplacements(newReplacements).build();

        indexablePageRepository.insertPages(batchResult.getCreatePages());
        indexablePageRepository.updatePageTitles(batchResult.getUpdatePages());
        indexablePageRepository.insertReplacements(batchResult.getCreateReplacements());
        indexablePageRepository.updateReplacements(batchResult.getUpdateReplacements());
        indexablePageRepository.deleteReplacements(batchResult.getDeleteReplacements());
    }
}
