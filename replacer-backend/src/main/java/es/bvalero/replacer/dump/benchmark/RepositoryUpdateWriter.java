package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.transaction.annotation.Transactional;

public class RepositoryUpdateWriter extends RepositoryItemWriter<ReplacementEntity> {
    private ReplacementRepository replacementRepository;

    public RepositoryUpdateWriter(ReplacementRepository repository) {
        super();
        setRepository(repository);
        setMethodName("save");
        this.replacementRepository = repository;
    }

    @Override
    @Transactional
    public void write(@NotNull List<? extends ReplacementEntity> replacements) throws Exception {
        int minId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).min().orElse(0);
        int maxId = replacements.stream().mapToInt(ReplacementEntity::getArticleId).max().orElse(Integer.MAX_VALUE);
        List<ReplacementEntity> dbReps = replacementRepository.findByArticles(minId, maxId);
        List<ReplacementEntity> toUpdate = new ArrayList<>();
        for (int i = 0; i < dbReps.size(); i++) {
            if (i % 5 == 0) {
                dbReps.get(i).setLastUpdate(LocalDate.now().plusDays(1));
                toUpdate.add(dbReps.get(i));
            }
        }
        super.write(toUpdate);
    }
}
