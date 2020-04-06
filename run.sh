#!/bin/sh
cp replacer-backend/target/replacer.jar $HOME/
cd $HOME
exec java -XX:+HeapDumpOnOutOfMemoryError -jar $HOME/replacer.jar
