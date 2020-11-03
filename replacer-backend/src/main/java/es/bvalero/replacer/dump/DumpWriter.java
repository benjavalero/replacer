package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class DumpWriter implements ItemWriter<List<ReplacementEntity>> {
    private final ItemWriter<ReplacementEntity> insertWriter;
    private final ItemWriter<ReplacementEntity> updateWriter;
    private final ItemWriter<ReplacementEntity> deleteWriter;

    public DumpWriter(
        ItemWriter<ReplacementEntity> insertWriter,
        ItemWriter<ReplacementEntity> updateWriter,
        ItemWriter<ReplacementEntity> deleteWriter
    ) {
        this.insertWriter = insertWriter;
        this.updateWriter = updateWriter;
        this.deleteWriter = deleteWriter;
    }

    @Override
    public void write(List<? extends List<ReplacementEntity>> items) throws Exception {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        List<ReplacementEntity> toInsert = flatList
            .stream()
            .filter(ReplacementEntity::isToInsert)
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
            deleteWriter.write(toDelete);
        }
    }
}
