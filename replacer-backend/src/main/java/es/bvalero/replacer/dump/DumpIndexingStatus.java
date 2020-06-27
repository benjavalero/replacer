package es.bvalero.replacer.dump;

import lombok.Data;

@Data
public class DumpIndexingStatus {
    private boolean running;
    private long numPagesRead;
    private long numPagesProcessed;
    private long numPagesEstimated;
    private String dumpFileName;
    private long start;
    private Long end;
}
