package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class DumpWriter {

    @Autowired
    private ReplacementService replacementService;

    void write(List<? extends List<ReplacementEntity>> items) {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        List<ReplacementEntity> toInsert = flatList
            .stream()
            .filter(ReplacementEntity::isToCreate)
            .collect(Collectors.toList());
        if (!toInsert.isEmpty()) {
            replacementService.insert(toInsert);
        }

        List<ReplacementEntity> toUpdateContext = flatList
            .stream()
            .filter(ReplacementEntity::isToUpdateContext)
            .collect(Collectors.toList());
        if (!toUpdateContext.isEmpty()) {
            replacementService.update(toUpdateContext);
        }

        List<ReplacementEntity> toUpdateDate = flatList
            .stream()
            .filter(ReplacementEntity::isToUpdateDate)
            .collect(Collectors.toList());
        if (!toUpdateDate.isEmpty()) {
            replacementService.updateDate(toUpdateDate);
        }

        List<ReplacementEntity> toDelete = flatList
            .stream()
            .filter(ReplacementEntity::isToDelete)
            .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            replacementService.deleteAll(toDelete);
        }
    }
}
