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

    @SuppressWarnings("unused")
    private DumpProcessStatus() {
    }

    private DumpProcessStatus(boolean running, boolean forceProcess, long numArticlesRead, long numArticlesProcessed,
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

    static class DumpProcessStatusBuilder {
        private boolean running;
        private boolean forceProcess;
        private long numArticlesRead;
        private long numArticlesProcessed;
        private String dumpFileName;
        private long average;
        private String time;
        private String progress;

        DumpProcessStatusBuilder setRunning(boolean running) {
            this.running = running;
            return this;
        }

        DumpProcessStatusBuilder setForceProcess(boolean forceProcess) {
            this.forceProcess = forceProcess;
            return this;
        }

        DumpProcessStatusBuilder setNumArticlesRead(long numArticlesRead) {
            this.numArticlesRead = numArticlesRead;
            return this;
        }

        DumpProcessStatusBuilder setNumArticlesProcessed(long numArticlesProcessed) {
            this.numArticlesProcessed = numArticlesProcessed;
            return this;
        }

        DumpProcessStatusBuilder setDumpFileName(String dumpFileName) {
            this.dumpFileName = dumpFileName;
            return this;
        }

        DumpProcessStatusBuilder setAverage(long average) {
            this.average = average;
            return this;
        }

        DumpProcessStatusBuilder setTime(String time) {
            this.time = time;
            return this;
        }

        DumpProcessStatusBuilder setProgress(String progress) {
            this.progress = progress;
            return this;
        }

        DumpProcessStatus createDumpProcessStatus() {
            return new DumpProcessStatus(running, forceProcess, numArticlesRead, numArticlesProcessed, dumpFileName,
                    average, time, progress);
        }

    }

}
