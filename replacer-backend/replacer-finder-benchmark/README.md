To run a benchmark, we simply give the class name as an argument to the JAR:
```shell
java -jar target/replacer-finder-benchmark.jar CompleteTagFinderJmhBenchmarkTest
```

To run it in  Toolforge, first we upload the benchmark JAR:
```shell
./deploy-benchmark.sh
```

and then in the Production machine:
```shell
webservice jdk17 shell -- java -jar /data/project/replacer/replacer-finder-benchmark.jar SimpleFinderJmhBenchmarkTest
```
