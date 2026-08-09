# Benchmark Module

This module uses [JMH](https://github.com/openjdk/jmh) to measure the performance of the finders.
All benchmarks extend `BaseFinderJmhBenchmark`, which handles JMH configuration, sample page loading, and chart generation.

## Benchmark Patterns

There are three types of benchmarks in this module, each serving a different purpose:

### 1. Algorithm comparison

Used to choose the best algorithm for a given finder problem (e.g. regex vs. linear vs. Aho-Corasick).
Examples: `CursiveFinderJmhBenchmark`, `CompleteTagFinderJmhBenchmark`, `UppercaseFinderJmhBenchmark`.

Once a winner is chosen, the discarded alternatives are kept commented out in the benchmark class as a decision log,
with inline notes like `// Very long` or `// Good` to explain why they were discarded or kept.
The active (uncommented) benchmarks represent the current candidates still under consideration.

### 2. Production finder comparison

Used to measure the relative cost of all production finders and detect global performance regressions.
Examples: `ImmutableFinderJmhBenchmark`, `ReplacementFinderJmhBenchmark`.

`BaselineFinder` is included as a lower-bound reference: it searches for a character (`~`) that is virtually
absent from Spanish Wikipedia articles, so its cost represents the minimum overhead of the finder infrastructure.

These benchmarks require Spring Boot to start during `@Setup`, which adds initialization overhead.
The warmup phase absorbs this, but keep in mind that results are not directly comparable with non-Spring benchmarks.

### 3. Old vs. New (iterative improvement)

Used when making incremental changes to an existing finder to validate that the new version does not regress.
Example: `CenturyFinderJmhBenchmark`, which compares `CenturyFinder` (production) with `CenturyNewFinder`.

`CenturyNewFinder` is kept in this module as a template for this pattern: copy the production finder here,
apply the change under study, and run the benchmark to compare. When not actively experimenting,
`CenturyNewFinder` mirrors the production finder exactly, so both benchmarks should yield the same results.

## What to optimize

The goal of the benchmarks is to minimize the time spent during dump indexation, which runs each finder ~2M times.
There are three phases in a finder, and not all of them are equally worth optimizing:

- **Search**: finding the candidate matches in the text. Always worth optimizing.
- **Validation**: checking if a candidate match is a real replacement. Also runs during indexation, so worth optimizing.
- **Conversion** (suggestions): computing the replacement suggestions to show to the user. This only runs when a user opens a page for review, not during indexation, so it is not a priority for performance.

In particular, avoid creating substrings or intermediate objects during search and validation if positions (start/end indices) are sufficient. Reserve heavier allocations for the conversion phase.

## Notes

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

## Run benchmarks

To run a benchmark, we simply give the class name as an argument to the JAR:
```shell
java -jar target/replacer-finder-benchmark.jar CompleteTagFinderJmhBenchmark
```

This runs the benchmark and writes the results as a JSON file into `src/main/resources/...`.
To regenerate the chart from an existing JSON (without re-running the benchmark),
comment out the `run(...)` call in the benchmark's `main`, recompile the JAR so the JSON
is included in the classpath, and run again:
```shell
# 1. Comment out run(...) in the benchmark's main, then:
mvn package -DskipTests
java -jar target/replacer-finder-benchmark.jar CompleteTagFinderJmhBenchmark
```

To run it in Toolforge, first we upload the benchmark JAR:
```shell
./deploy-benchmark.sh
```

and then in the Production machine:
```shell
webservice jdk21 shell -- java -jar /data/project/replacer/replacer-finder-benchmark.jar SimpleFinderJmhBenchmark
```
