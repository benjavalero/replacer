package es.bvalero.replacer.dump;

import es.bvalero.replacer.replacement.ReplacementEntity;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ReplacementProcessor implements ItemProcessor<ReplacementEntity, ReplacementEntity> {

    @Override
    public ReplacementEntity process(ReplacementEntity replacement) {
        replacement.setLastUpdate(LocalDate.now());
        return replacement;
    }

}
