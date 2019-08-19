# JMH Benchmark
The JMH benchmark evaluates the differences between reusing a CQL instance and creating a new instance.
At the time of this writing, the CQL class is not thread-safe.
The benchmark is used to test whether, from the point of view of runtime, we can instead use a newly created instance of the CQL instead of the shared CQL-instance.
From the below results (note the microseconds) i would say **yes**.
```text
Benchmark                            Mode  Cnt  Score   Error   Units
CQLJMHBenchmarkTest.cqlNewInstance  thrpt   20  0.068 ± 0.001  ops/us
CQLJMHBenchmarkTest.cqlReuse        thrpt   20  0.079 ± 0.002  ops/us
```