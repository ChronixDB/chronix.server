#!/usr/bin/env bash

rm *.log

# java -jar antlr-4.5.1-complete.jar ../src/main/antlr/CQLCF.g4 -package de.qaware.chronix.cql.antlr -Xlog -Xexact-output-dir -o ../src/main/java/de/qaware/chronix/cql/antlr
java -jar antlr-4.5.1-complete.jar ../src/main/antlr/CQLCF.g4 -package de.qaware.chronix.cql.antlr -Xlog -o ../src/main/java/de/qaware/chronix/cql/antlr