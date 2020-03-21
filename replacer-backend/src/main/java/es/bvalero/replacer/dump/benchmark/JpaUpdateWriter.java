package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import es.bvalero.replacer.replacement.ReplacementRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class JpaUpdateWriter implements ItemWriter<ReplacementEntity> {

    @Autowired
    private ReplacementRepository replacementRepository;

    @Override
    @Transactional
    public void write(@NotNull List<? extends ReplacementEntity> replacements) {


        replacementRepository.saveAll(replacements);
    }

}
