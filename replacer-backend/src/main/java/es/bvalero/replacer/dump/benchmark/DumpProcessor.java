package es.bvalero.replacer.dump.benchmark;

import es.bvalero.replacer.replacement.ReplacementEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DumpProcessor implements ItemProcessor<DumpPage, DumpPage> {

    @Override
    public DumpPage process(DumpPage dumpPage) {
        return dumpPage;
    }

}
