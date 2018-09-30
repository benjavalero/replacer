package es.bvalero.replacer.dump;

@SuppressWarnings("WeakerAccess")
public class DumpProcessStatus {

    private boolean running;
    private boolean forceProcess;
    private long numArticlesRead;
    private long numArticlesProcessed;
    private String dumpFileName;
    private long average;
    private String time;
    private String progress;

    public DumpProcessStatus(boolean running, boolean forceProcess, long numArticlesRead, long numArticlesProcessed,
                             String dumpFileName, long average, String time, String progress) {
        this.running = running;
        this.forceProcess = forceProcess;
        this.numArticlesRead = numArticlesRead;
        this.numArticlesProcessed = numArticlesProcessed;
        this.dumpFileName = dumpFileName;
        this.average = average;
        this.time = time;
        this.progress = progress;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isForceProcess() {
        return forceProcess;
    }

    public long getNumArticlesRead() {
        return numArticlesRead;
    }

    public long getNumArticlesProcessed() {
        return numArticlesProcessed;
    }

    public String getDumpFileName() {
        return dumpFileName;
    }

    public long getAverage() {
        return average;
    }

    public String getTime() {
        return time;
    }

    public String getProgress() {
        return progress;
    }

}
