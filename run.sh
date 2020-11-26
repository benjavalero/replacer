#!/bin/sh
# cp replacer-backend/target/replacer.jar $HOME/
cd $HOME
exec java -Djdk.xml.totalEntitySizeLimit=0 -XX:+HeapDumpOnOutOfMemoryError -DLOGZ_TOKEN=xxxxxx -jar $HOME/replacer.jar
