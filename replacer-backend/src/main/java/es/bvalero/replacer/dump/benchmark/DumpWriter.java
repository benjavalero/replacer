package es.bvalero.replacer.dump.benchmark;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@Slf4j
public class DumpWriter implements ItemWriter<DumpPage> {

    @Override
    public void write(List<? extends DumpPage> items) {
        LOGGER.info("ID: {}", items.get(0).id);
    }
}
