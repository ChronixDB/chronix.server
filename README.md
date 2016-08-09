[![Build Status](https://travis-ci.org/ChronixDB/chronix.server.svg)](https://travis-ci.org/ChronixDB/chronix.server)
[![Coverage Status](https://coveralls.io/repos/ChronixDB/chronix.server/badge.svg?branch=master&service=github)](https://coveralls.io/github/ChronixDB/chronix.server?branch=master)
[![Sputnik](https://sputnik.ci/conf/badge)](https://sputnik.ci/app#/builds/ChronixDB/chronix.server)
[![Stories in Ready](https://badge.waffle.io/ChronixDB/chronix.server.png?label=ready&title=Ready)](https://waffle.io/ChronixDB/chronix.server)
[![Apache License 2](http://img.shields.io/badge/license-ASF2-blue.svg)](https://github.com/ChronixDB/chronix.server/blob/master/LICENSE)
[![Join the chat at https://gitter.im/ChronixDB/chronix.server](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ChronixDB/chronix.server?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[ ![Download](https://api.bintray.com/packages/chronix/maven/chronix-server-client/images/download.svg) ](https://bintray.com/chronix/maven/chronix-server-client/_latestVersion)
[![Dependency Status](https://dependencyci.com/github/ChronixDB/chronix.server/badge)](https://dependencyci.com/github/ChronixDB/chronix.server)

# Chronix Server
The Chronix Server is an implementation of the Chronix API that stores time series in [Apache Solr](http://lucene.apache.org/solr/).
Chronix uses several techniques to optimize query times and storage demand.
Thus Chronix can access a single data point out of 68.000.000.000 stored pairs of time stamp and numeric value in about 30 ms.
The storage demand is about 30 GB.
Everything runs on a standard laptop computer.
No need of clustering, parallel processing or another complex stuff.
Check it out and give it a try.

The repository [chronix.examples](https://github.com/ChronixDB/chronix.examples) contains some examples.

## How Chronix Server stores time series
![Chronix Architecture](https://bintray.com/artifact/download/chronix/Images/chronix-architecture.jpg)

The key data type of Chronix is called a *record*.
It stores a chunk of time series data in a compressed binary large object.
The record also stores technical fields, time stamps for start and end, that describe the time range of the chunk of data, and a set of arbitrary user-defined attributes.
Storing records instead of individual pairs of time stamp and value has two major advantages:
1. A reduced storage demand due to compression 
2. Almost constant query times for accessing a chunk due to indexable attributes and a constant overhead for decompression.

The architecture of Chronix has the four building blocks shown in Figure.
It is well-suited to the parallelism of multi-core systems.
All blocks can work in parallel to each other to increase the throughput.
###Semantic Compression
Semantic Compression is **optional** and reduces the amount of time series with the goal of storing fewer records.
It uses techniques that exploit knowledge on the shape and the significance of a time series to remove irrelevant details even if some accuracy is lost, e.g. dimensionality reduction through aggregation.

###Attributes and Chunks
Attributes and Chunks breaks down time series into chunks of *n* data points that are serialized into *c* Bytes.
It also calculates the attributes and the pre-calculated values of the records.
Part of this serialization is a *Date-Delta Compaction* that compares the deltas between time stamps.
It serializes only the value if the aberration of two deltas is within a defined range, otherwise it writes both the time stamp and the value to the record's data field.

###Basic Compression
Then Basic Compression uses gzip, a lossless compression technique that operates on *c* consecutive bytes.
Only the record's data field is compressed to reduce the storage demand while the attributes remain uncompressed for access.
Compression of operational time series data yields a high compression rate due its value characteristics.
In spite of the decompression costs when accessing data, compression actually improves query times as data is processed faster.

###Multi-Dimensional Storage
The Multi-Dimensional Storage holds the records in a compressed binary format.
Only the fields that are necessary to locate the records are visible as so-called dimensions to the data storage system.
Queries can then use any combination of those dimensions to locate records.
Chronix uses Apache Solr as it ideally matches the requirements.
Furthermore Chronix has built-in analysis functions, e.g, a trend and outlier detector, to optimize operational time series analyses. 

## Data model
Chronix allows one to store any kind of time series and hence the data model is open to your needs.
Chronix Server per default uses the [Kassiopeia Simple](https://github.com/ChronixDB/chronix.kassiopeia) time series package.
The data model for the Kassiopeia Simple package.

A time series has at least the following required fields:

| Field Name  | Value Type |
| ------------- | ------------- |
| start      | Long    |
| end        | Long    |
| metric     | String  |
| data       | Byte[]  |

The data field contains json serialized and gzip compressed points of time stamp (long) and numeric value (double).
Furthermore a time series can have arbitrary user-defined attributes. 
The type of an attribute is restricted by the available [fields](https://cwiki.apache.org/confluence/display/solr/Solr+Field+Types) of Apache Solr.

## Chronix Server Client ([Source](https://github.com/ChronixDB/chronix.server/tree/master/chronix-server-client))

A Java client that is used to store and stream time series from Chronix.
The following code snippet shows how to setup an connection to Chronix and stream time series.
The examples uses the [Chronix API](https://github.com/ChronixDB/chronix.api), Chronix Server Client, 
[Chronix Kassiopeia](https://github.com/ChronixDB/chronix.kassiopeia) and [SolrJ](http://mvnrepository.com/artifact/org.apache.solr/solr-solrj/5.5.0)
```Java
//An connection to Solr
SolrClient solr = new HttpSolrClient("http://localhost:8983/solr/chronix/");

//Define a group by function for the time series records
Function<MetricTimeSeries, String> groupBy = ts -> ts.getMetric() + "-" + ts.attribute("host");

//Define a reduce function for the grouped time series records
BinaryOperator<MetricTimeSeries> reduce = (ts1, ts2) -> {
      MetricTimeSeries.Builder reduced = new MetricTimeSeries.Builder(ts1.getMetric())
            .points(concat(ts1.getTimestamps(), ts2.getTimestamps()),
                  concat(ts1.getValues(), ts2.getValues()))
            .attributes(ts1.attributes());
            return reduced.build();
        };

//Create a Chronix Client with Kassiopeia Simple and the Chronix Solr Storage
ChronixClient<MetricTimeSeries,SolrClient,SolrQuery> chronix = 
                                          new ChronixClient<>(new KassiopeiaSimpleConverter(),
                                          new ChronixSolrStorage<>(nrOfDocsPerBatch,groupBy,reduce));

//Lets stream time series from Chronix. We want the maximum of all time series that metric matches *load*.
SolrQuery query = new SolrQuery("metric:*load*");
query.addFilterQuery("function=max");

//The result is a Java Stream. We simply collect the result into a list.
List<MetricTimeSeries> maxTS = chronix.stream(solr, query).collect(Collectors.toList());
```

## Chronix Server Parts
The Chronix server parts are Solr extensions (e.g. a custom query handler).
Hence there is no need to build a custom modified Solr.
We just plug the Chronix server parts into a standard Solr.

The following sub projects are Solr extensions and ship with the binary release of Chronix.
The latest release of Chronix server is based on Apache Solr version 6.0.1

## Chronix Server Query Handler ([Source](https://github.com/ChronixDB/chronix.server/tree/master/chronix-server-query-handler))
The Chronix Server Query Handler is the entry point for requests asking for time series.
It splits a request based on the filter queries up in range or function queries:

- fq=function=* (for aggregations, analyses, or transformations)
- fq='' (empty, for range queries)

But before the Chronix Query Handler delegates a request, it modifies the user query string.
This is necessary as Chronix stores records and hence a query asking for a specific time range has to be modified.
As a result it converts a query:
```
host:prodI4 AND metric:\\HeapMemory\\Usage\\Used AND start:NOW-1MONTH AND end:NOW-10DAYS
```
in the following query:
```
host:prodI4 AND metric:\\HeapMemory\\Usage\\Used AND -start:[NOW-10DAYS-1ms TO *] AND -end:[* TO NOW-1MONTH-1ms]
```

### Range Query
A range query is answered using the default Solr query handler which supports all the great features (fields, facets, ...) of Apache Solr.

Example Result:
```
{
  "responseHeader":{
    "query_start_long":0,
    "query_end_long":9223372036854775807,
    "status":0,
    "QTime":3},
  "response":{"numFound":21,"start":0,"docs":[
      {
        "start":1377468017361,
        "metric":"\\Load\\max",
        "end":1377554376850,
        "data":"byte[]" // serialized and compressed points
       },...
   ]
}
```

### Function Query
A custom query handler answers function queries.
Chronix determines if a query is a function query by using the filter query mechanism of Apache Solr.
There are three types of functions: Aggregations, Transformations, and High-level Analyses.

Currently the following functions are available:

- Maximum (function=max)
- Minimum (function=min)
- Average (function=avg)
- Standard Deviation (function=dev)
- Percentiles (function=p:[0.1,...,1.0])
- Count (function=count) (*Release 0.2*)
- Sum (function=sum) (*Release 0.2*)
- Range (function=range) (*Release 0.2*)
- First/Last (function=first/last) (*Release 0.2*)
- Bottom/Top (function=bottom/top:10) (*Release 0.2*)
- Derivative (function=derivative) (*Release 0.2*)
- Non Negative Derivative (function=nnderivative) (*Release 0.2*)
- Difference (function=diff) (*Release 0.2*)
- Signed Difference (function=sdiff) (*Release 0.2*)
- Scale (function=scale:0.5) (*Release 0.2*)
- Divide (function=divide:4) (*Release 0.2*)
- Moving Average (function=movavg:10,MINUTES) (*Release 0.2*)
- Add (function=add:4) (*Release 0.2*)
- Subtract (function=sub:4) (*Release 0.2*)
- A linear trend detection (function=trend)
- Outlier detection (function=outlier)
- Frequency detection (function=frequency:10,6)
- Time series similarity search (function=fastdtw:(metric:\*Load\*),1,0.8)
- Timeshift (function=timeshift:[+/-]10,DAYS) (*Release 0.3*)

Multiple analyses, aggregations, and transformations are allowed per query.
If so, Chronix will first execute the transformations in the order they occur.
Then it executes the analyses and aggregations on the result of the chained transformations.
For example the query:

```
fq=function=max;min;trend;movavg:10,minutes;scale:4
```

is executed as follows:

1. Calculate the moving average
2. Scale the result of the moving average by 4
3. Calculate the max, min, and the trend based on the prior result.
 
A function query does not return the raw time series data by default.
It returns all requested time series attributes, the analysis and its result.
With the enabled option ```fl=+data``` Chronix will return the data for the analyses.
The attributes are merged using a set to avoid duplicates.
For example a query for a metric that is collected on several hosts might return the following result:
```
{
  "responseHeader":{
    "query_start_long":0,
    "query_end_long":9223372036854775807,
    "status":0,
    "QTime":3},
  "response":{"numFound":21,"start":0,"docs":[
      {
        "start":1377468017361,
        "metric":"\\Load\\max",
        "end":1377554376850,
        "host:"["host-1","host-2", ...]
       }...
   ]
}
```

A few example analyses:
```
q=metric:*load* // Get all time series that metric name matches *load*

+ fq=function=max //Get the maximum of 
+ fq=function=p:0.25 //To get the 25% percentile of the time series data
+ fq=function=trend //Returns all time series that have a positive trend
+ fq=function=frequency=10,6 //Checks time frames of 10 minutes if there are more than 6 points. If true it returns the time series.
+ fq=function=fastdtw(metric:*load*),1,0.8 //Uses fast dynamic time warping to search for similar time series
```

### Join Time Series Records
An query can include multiple records of time series and therefore Chronix has to know how to group records that belong together.
Chronix uses a so called *join function* that can use any arbitrary set of time series attributes to group records.
For example we want to join all records that have the same attribute values for host, process, and metric:
```
fq=join=host,process,metric
```
If no join function is defined Chronix applies a default join function that uses the metric name.

### Modify Chronix' response
Per default Chronix returns (as Solr does) all defined fields in the *schema.xml*.
One has three ways to modify the response using the *fl* parameter:

#### One specific user defined field
If only a specific user defined field is needed, e.g. the host field, one can set:
```
fl=host
```
Then Chronix will return the *host* field and the required fields (start,end,data,id).

#### Exclude a specific field
If one do not need a specific field, such as the data field, one can pass *-data* in the *fl* parameter.
```
fl=-data
``` 
In that case all fields, expect the data field, are returned.
Even when the excluded field is a required field.

#### Explicit return of a field
This is useful in combination with an analysis. 
Analyses per default do not return the raw data for performance reasons.
But if the raw data is needed, one can pass
```
fl=+data
```

### Chronix Response Writer
This allows one to query raw (uncompressed) data from Chronix in JSON format.
To execute the transformer you have to add it to the *fl* parameter:
```
q=metric:*load*&fl=+dataAsJson //to get all fields and the dataAsJson field
q=metric:*load*&fl=dataAsJson //to get only the required fields (except the data field) and dataAsJson
```
The records in the result contains a field called *dataAsJson* that holds the raw time series data as json.
Note: The data field that normally ship the compressed data is not included in the result.

Example Result:
```
{
  "responseHeader":{
    "query_start_long":0,
    "query_end_long":9223372036854775807,
    "status":0,
    "QTime":3},
  "response":{"numFound":21,"start":0,"docs":[
      {
        "start":1377468017361,
        "metric":"\\Load\\max",
        "end":1377554376850,
        "dataAsJson":"[[timestamps],[values]]" //as json string
       }...
   ]
}
```

### Chronix Server Retention ([Source](https://github.com/ChronixDB/chronix.server/tree/master/chronix-server-retention))
The Chronix Server Retention plugin deletes time series data that is older than a given threshold.
The configuration of the plugin is within the *config.xml* of the Solr Core.
The following snippet of Solr config.xml shows the configuration:
```XML
<requestHandler name="/retention" class="de.qaware.chronix.solr.retention.ChronixRetentionHandler">
  <lst name="invariants">
   <!-- Use the end field of a record to determine its age. -->
   <str name="queryField">end</str>
   <!-- Delete time series that are older than 40DAYS -->
   <str name="timeSeriesAge">40DAYS</str> 
    <!-- Do it daily at 12 o'clock -->
   <str name="removeDailyAt">12</str>
   <!-- Define the source  -->
   <str name="retentionUrl">http://localhost:8983/solr/chronix/retention</str>
   <!-- Define how the index is updated after deletion -->
   <str name="optimizeAfterDeletion">false</str>
   <str name="softCommit">false</str>
  </lst>
</requestHandler>
```

## Usage
All libraries are available in the [Chronix Bintary Maven](https://bintray.com/chronix/maven) repository.
A build script snippet for use in all Gradle versions, using the Chronix Bintray Maven repository:
```groovy
repositories {
    mavenCentral()
    maven {
        url "http://dl.bintray.com/chronix/maven"
    }
}
dependencies {
   compile 'de.qaware.chronix:chronix-server-client:0.3'
   compile 'de.qaware.chronix:chronix-server-query-handler:0.3'
   compile 'de.qaware.chronix:chronix-server-retention:0.3'
}
```

## Contributing
Is there anything missing? Do you have ideas for new features or improvements? You are highly welcome to contribute
your improvements, to the Chronix projects. All you have to do is to fork this repository,
improve the code and issue a pull request.

## Building Chronix from Scratch
Everything should run out of the box. The only two things that must be available:
- Git
- JDK 1.8

Just do the following steps:

```bash
cd <checkout-dir>
git clone https://github.com/ChronixDB/chronix.server.git
cd chronix.server
./gradlew clean build
```

## Maintainer

Florian Lautenschlager @flolaut

## License

This software is provided under the Apache License, Version 2.0 license.

See the `LICENSE` file for details.
