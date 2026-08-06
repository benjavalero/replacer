package es.bvalero.replacer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolConfiguration {

    // Thread pool used for @Async and @Scheduled tasks.
    // Tasks and their concurrency at worst case (e.g. during dump indexing):
    //   1. DumpScheduledTask    → @Scheduled triggers @Async indexLatestDumpFiles (long-running, hours)
    //   2. ListingScheduledLoader → @Scheduled, runs on startup and every hour (seconds)
    //   3. PageCountCacheRepository.init → @Scheduled, runs on startup (seconds)
    //   4. ReplacementCountCacheRepository.init + initCountGroupedByPage → @Scheduled, startup + 7 min delay
    // All 4 can run concurrently, so 4 threads is the right fit.
    //
    // Note: DumpParser additionally parallelizes dump files using the JVM common ForkJoinPool (not this pool).
    // No other task uses the ForkJoinPool, so there is no contention.
    private static final int THREADS_COUNT = 4;

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(THREADS_COUNT);
        return threadPoolTaskScheduler;
    }
}
