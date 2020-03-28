package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

@Slf4j
public class DumpWriter implements ItemWriter<List<ReplacementEntity>> {
    private ItemWriter<ReplacementEntity> insertWriter;
    private ItemWriter<ReplacementEntity> updateWriter;

    public DumpWriter(ItemWriter<ReplacementEntity> insertWriter, ItemWriter<ReplacementEntity> updateWriter) {
        this.insertWriter = insertWriter;
        this.updateWriter = updateWriter;
    }

    @Override
    public void write(List<? extends List<ReplacementEntity>> items) throws Exception {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        List<ReplacementEntity> toInsert = flatList
            .stream()
            .filter(r -> r.getId() == null)
            .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            insertWriter.write(toInsert);
        }

        List<ReplacementEntity> toUpdate = flatList
            .stream()
            .filter(r -> r.getId() != null)
            .collect(Collectors.toList());
        if (!toUpdate.isEmpty()) {
            updateWriter.write(toUpdate);
        }
    }
}
