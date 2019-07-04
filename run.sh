#!/bin/sh
cd $HOME
exec java -XX:+HeapDumpOnOutOfMemoryError -jar $HOME/replacer/replacer-backend/target/replacer.jar