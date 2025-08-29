#!/bin/sh

echo "1. Build JAR"
echo
mvn clean install -P benchmark
echo

echo "2. Copy JAR"
scp -i ~/.ssh/id_rsa replacer-backend/replacer-finder-benchmark/target/replacer-finder-benchmark.jar benjavalero@login.toolforge.org:/data/project/replacer
echo

echo "3. Done"
