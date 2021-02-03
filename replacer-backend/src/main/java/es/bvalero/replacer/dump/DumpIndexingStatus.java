package es.bvalero.replacer.dump;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
@Builder
class DumpIndexingStatus {

    // All fields are nullable but "running"
    boolean running;
    Long numPagesRead;
    Long numPagesProcessed;
    Long numPagesEstimated;
    String dumpFileName;
    Long start;
    Long end;

    static DumpIndexingStatus ofEmpty() {
        return DumpIndexingStatus.builder().build();
    }
}
