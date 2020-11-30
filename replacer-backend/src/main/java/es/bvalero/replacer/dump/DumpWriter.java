package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementDao;
import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@StepScope
@Component
public class DumpWriter implements ItemWriter<List<ReplacementEntity>> {
    @Autowired
    private ReplacementDao replacementDao;

    private final ItemWriter<ReplacementEntity> insertWriter;
    private final ItemWriter<ReplacementEntity> updateWriter;

    public DumpWriter(ItemWriter<ReplacementEntity> insertWriter, ItemWriter<ReplacementEntity> updateWriter) {
        this.insertWriter = insertWriter;
        this.updateWriter = updateWriter;
    }

    @Override
    public void write(List<? extends List<ReplacementEntity>> items) throws Exception {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        List<ReplacementEntity> toInsert = flatList
            .stream()
            .filter(ReplacementEntity::isToCreate)
            .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            insertWriter.write(toInsert);
        }

        List<ReplacementEntity> toUpdate = flatList
            .stream()
            .filter(ReplacementEntity::isToUpdate)
            .collect(Collectors.toList());
        if (!toUpdate.isEmpty()) {
            updateWriter.write(toUpdate);
        }

        List<ReplacementEntity> toDelete = flatList
            .stream()
            .filter(ReplacementEntity::isToDelete)
            .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            replacementDao.deleteAll(toDelete);
        }
    }
}
