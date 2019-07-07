#!/bin/sh
cd $HOME
exec java -Djdk.xml.totalEntitySizeLimit=0 -XX:+HeapDumpOnOutOfMemoryError -jar $HOME/replacer/replacer-backend/target/replacer.jar