#!/bin/sh
cp replacer-backend/target/replacer.jar $HOME/
cd $HOME
exec java -Djdk.xml.totalEntitySizeLimit=0 -XX:+HeapDumpOnOutOfMemoryError -jar $HOME/replacer.jar
