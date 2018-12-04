#!/usr/bin/env bash

JAR="`pwd`/antlr-4.7.1-complete.jar"

#Jump to grammar

cd ../src/main/java/de/qaware/chronix/cql/antlr

export CLASSPATH=".:$JAR:$CLASSPATH"

java org.antlr.v4.gui.TestRig CQL -gui