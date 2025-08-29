# Notes

JMH doesn't allow a common state for all the benchmarks.
Therefore, on benchmarks where we need to load configuration, the Spring Boot instance will be loaded for every benchmark.

Times in Production are about three times worse compared to times obtained in a standard laptop.
Therefore, using the latter to compare algorithms is usually enough.

JMH benchmarks are configured to run 2 seconds per iteration, instead of the 5 seconds by default.
This is usually enough to run the benchmark enough times.
However, there are some algorithms taking more than this time to run, and thus are discarded.

We are interested in the average time it takes a specific algorithm to find the immutables or replacements in a text.
This time usually depends on the size of the text.
Therefore, we have prepared a random sample of Wikipedia pages which represents the whole dump of pages.
In fact, we have checked that the distribution of text lengths in the sample matches the one in the dump.
Thus, the times obtained per algorithm represent the ones of running it on the whole sample.
In case we want to infer the time per page, we could just divide the time by the sample size (50).

JMH returns the time results as the mean and the standard error, which gives us the confidence interval,
and it is quite convenient to generate bar charts to compare the algorithms visually.

# Run benchmarks

To run a benchmark, we simply give the class name as an argument to the JAR:
```shell
java -jar target/replacer-finder-benchmark.jar CompleteTagFinderJmhBenchmark
```

To run it in Toolforge, first we upload the benchmark JAR:
```shell
./deploy-benchmark.sh
```

and then in the Production machine:
```shell
webservice jdk17 shell -- java -jar /data/project/replacer/replacer-finder-benchmark.jar SimpleFinderJmhBenchmark
```
