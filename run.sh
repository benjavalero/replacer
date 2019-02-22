#!/bin/sh
cd $HOME
exec java -Xmx1024m -Xss2m -XX:+HeapDumpOnOutOfMemoryError -jar $HOME/replacer/target/replacer.jar