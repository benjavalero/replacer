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

    @Override
    public void write(List<? extends List<ReplacementEntity>> items) {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        List<ReplacementEntity> toInsert = flatList
            .stream()
            .filter(ReplacementEntity::isToCreate)
            .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            replacementDao.insert(toInsert);
        }

        List<ReplacementEntity> toUpdateContext = flatList
            .stream()
            .filter(ReplacementEntity::isToUpdateContext)
            .collect(Collectors.toList());
        if (!toUpdateContext.isEmpty()) {
            replacementDao.update(toUpdateContext);
        }

        List<ReplacementEntity> toUpdateDate = flatList
            .stream()
            .filter(ReplacementEntity::isToUpdateDate)
            .collect(Collectors.toList());
        if (!toUpdateDate.isEmpty()) {
            replacementDao.updateDate(toUpdateDate);
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
