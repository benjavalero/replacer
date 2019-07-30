package es.bvalero.replacer.dump;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
class DumpProcessStatus {

    private boolean running;
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessed;
    private String dumpFileName;
    private Long start;
    private Long end;

}
