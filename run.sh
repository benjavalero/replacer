#!/bin/sh
java -Xmx1024m -Xss2m -XX:+HeapDumpOnOutOfMemoryError -jar target/replacer.jar
