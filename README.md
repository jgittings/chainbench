Chainbench attempts to provide meaningful comparisons of the *relative* performance
of different Java distributed locking frameworks, such as Hazelcast, Terracotta, etc.

For each run it randomly selects a framework from a specified list, as well as randomly selecting other
parameters such as the number of servers, threads per server, and contended resources.
The results of each run are logged to a results file.

By averaging over many such iterations (i.e. using multiple runs per data point), as well as calculating
the standard error for each data point, it is our contention that temporary variations in system
resources are averaged out, and meaningful conclusions
can be drawn about the relative performance of these frameworks.

Each thread in each server competes to randomly update one of the contended resources (a database record).

As well as studying performance, Chainbench tests whether the locking framework does actually protect
the contended resources from concurrent (dirty) updates. Collisions are detected via a Hibernate-style
version field, and if one occurs it is immediately reported.

A UI front end is used to select appropriate parameters.

### Currently supported frameworks:
* Hazelcast 2.0
* Ehcache/Terracotta 2.0
* Gemfire
* Apache Zookeeper via recipes (pending licensing review)
* Infinispan
Adding a new framework only requires implementing a simple adapter class.

### Supported parameters:
* Results output file
* Locking framework or list of frameworks
* DB connection parameters
* Number of servers to launch (or range for generating a graph)
* Number of threads per server (or range for generating a graph)
* External boxes to push some servers to via SSH
* Number of contended resources being competed for (i.e. what we’re protecting with the locks)
* Heap size of servers
* Heap exhaustion % of each server (to simulate low heap conditions)
* Locking enabled?  (to check the locking framework is actually doing something). If locking is switched off, collisions occur which are detected by the utility.
* Nodes to stall and when to stall them
* Nodes to run slow and when and how to slow them down

### Metrics gathered per contended resource:
* Last/Mean/Worst time to acquire lock  (both in real clock time and in CPU time)

### Metrics gathered per server/thread:
* Locks granted, locks refused,   granted/total lock requests


