package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.data.repository.CrudRepository;

public class RepositoryInsertWriter extends RepositoryItemWriter<ReplacementEntity> {

    public RepositoryInsertWriter(CrudRepository<ReplacementEntity, Long> repository) {
        super();
        setRepository(repository);
        setMethodName("save");
    }
}
