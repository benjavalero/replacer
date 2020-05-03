package es.bvalero.replacer.dump;

import lombok.Data;

@Data
public class DumpIndexation {
    private boolean running;
    private long numArticlesRead;
    private long numArticlesProcessed;
    private long numArticlesEstimated;
    private String dumpFileName;
    private long start;
    private Long end;
}
