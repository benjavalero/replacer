# Notes

JMH doesn't allow a common state for all the benchmarks.
Therefore, on benchmarks where we need to load configuration, the Spring Boot instance will be loaded for every benchmark.

Times in Production are about three times worse compared to times obtained in a standard laptop.
Therefore, using the latter to compare algorithms is usually enough.

JMH benchmarks are configured to run 2 seconds per iteration.
However, there are some algorithms taking more than this time to run, and thus are discarded.

# Run benchmarks

To run a benchmark, we simply give the class name as an argument to the JAR:
```shell
java -jar target/replacer-finder-benchmark.jar CompleteTagFinderJmhBenchmarkTest
```

To run it in Toolforge, first we upload the benchmark JAR:
```shell
./deploy-benchmark.sh
```

and then in the Production machine:
```shell
webservice jdk17 shell -- java -jar /data/project/replacer/replacer-finder-benchmark.jar SimpleFinderJmhBenchmarkTest
```
