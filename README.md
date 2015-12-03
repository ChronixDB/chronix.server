[![Build Status](https://travis-ci.org/ChronixDB/chronix.server.svg)](https://travis-ci.org/ChronixDB/chronix.server)
[![Coverage Status](https://coveralls.io/repos/ChronixDB/chronix.server/badge.svg?branch=master&service=github)](https://coveralls.io/github/ChronixDB/chronix.server?branch=master)
[![Stories in Ready](https://badge.waffle.io/ChronixDB/chronix.server.png?label=ready&title=Ready)](https://waffle.io/ChronixDB/chronix.server)
[![Apache License 2](http://img.shields.io/badge/license-ASF2-blue.svg)](https://github.com/ChronixDB/chronix.server/blob/master/LICENSE)


# Chronix Server

A time series storage that is based on Apache Solr. It consits of multiple sub projects.

## Chronix Server Client
The Java client that is used with the Chronix time series storage. Chronix can be used with every type of time series.
With the client one can add and stream time series from Chronix.

### Usage
Build script snippet for use in all Gradle versions, using the Bintray Maven repository:

```groovyv
repositories {
    mavenCentral()
    maven { 
        url "http://dl.bintray.com/chronix/maven" 
    }
}
dependencies {
   compile 'de.qaware.chronix:chronix-server-client:0.0.1'
}
```
## Chronix Server Parts (Solr Extensions)
The following subprojects are Solr extensions and ships with the binary release of Chronix based on Apache Solr 3.5.1

### Chronix Server Query Handler
The Chronix Server Query Handler implements some useful functions when using Solr as a time series storage.
It splits a request based on the filter queries up in range or aggregation queries.
### Range Query
A Range query is answered using the default Solr query handler. 
But it modifies the user query especially the user defined start and end.
As a result it converts a query: 
```
host:prodI4 AND metric:\\HeapMemory\Usage\Used AND start:NOW-1MONTH AND end:NOW-10DAYS
```
in the following query:
```
host:prodI4 AND metric:\\HeapMemory\Usage\Used AND -start:[NOW-10DAYS-1ms TO *] AND -end:[* TO NOW-1MONTH-1ms]
```
This is needed as Chronix stores binary chunks of time series data.
More details about the storage will follow in the documentation pages.

#### Aggregation Query
An aggregation query (adding a filter query like ag=max) is answered using a specialized aggregation query handler.
The following aggregations are currently available:
```
- Maximum (max)
- Minimum (min)
- Average (avg)
- Standard Deviation (dev)
- Percentiles (p=0.1 ... 1.0)
 ```
Only one aggregation is allowed in a query. 
An aggregation query does not return the raw time series data.
It only converts the time series attributes, the aggregation and its result.
An example query:
```
fq=ag=p=0.25 //To get the 25% percentile of the time series data
```
##### Aggregation Join
An aggregation join is a further filter query that defines which stored documents belong to each other.
As already mentioned Chronix stores chunks of time series data with arbitrary used-defined attributes.
With a join one can define which documents based on the attributes belong together.
For example we want to join all document that have the same attribute values for host, process, and metric:
```
fq=join=host,process,metric
```

### Chronix Server Retention
This module manages the data household. 
The retention plugin is configured in the Solr config.xml file.
It automatically deletes old data by a given date threshold.
The threshold is defined with a solr date term:
```
30DAYS // to delete all time series data that is older than 30 days
```

## Test modules
The Chronix Test Extensions contains classes and functions for testing.
The Chronix Test Integration runs several integrations tests against a fresh downloaded Solr 3.5.1.

## Contributing

Is there anything missing? Do you have ideas for new features or improvements? You are highly welcome to contribute
your improvements, to the Chronix projects. All you have to do is to fork this repository,
improve the code and issue a pull request.

### Build Setup
Everything should run out of the box. The only things that have to be available: Git, JDK 1.8, and Gradle. 
Just clone the repository and run
```
gradle clean build
```
in the chronix.server directory.
## Maintainer

Florian Lautenschlager @flolaut

## License

This software is provided under the Apache License, Version 2.0 license.

See the `LICENSE` file for details.
