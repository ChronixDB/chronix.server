#!/usr/bin/env bash

rm *.log

java -jar antlr-4.7.1-complete.jar ../src/main/antlr/CQL.g4 -package de.qaware.chronix.cql.antlr -Xlog -Xexact-output-dir -o ../src/main/java/de/qaware/chronix/cql/antlr
 #-long-messages -visitor -package de.qaware.chronix.cql.antlr -Xlog
