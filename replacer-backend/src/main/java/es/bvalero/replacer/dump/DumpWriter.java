package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementIndexService;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class DumpWriter {

    @Autowired
    private ReplacementIndexService replacementIndexService;

    void write(List<? extends List<ReplacementEntity>> items) {
        List<ReplacementEntity> flatList = items.stream().flatMap(Collection::stream).collect(Collectors.toList());

        replacementIndexService.saveIndexedReplacements(flatList);
    }
}
