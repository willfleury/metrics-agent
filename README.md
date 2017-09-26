- [Overview](#overview)
  - [Motivation](#motivation)
    - [Code Bloat](#code-bloat)
  - [Instrumentation Metadata](#instrumentation-metadata)
    - [Annotations](#annotations)
    - [Configuration](#configuration)
    - [Metric Labels](#metric-labels)
      - [Dynamic Label Values](#dynamic-label-values)
    - [White & Black Lists](#white-black-lists)
    - [What we actually Transform](#what-we-actually-transform)
  - [Supported Metrics Systems](#supported-metrics-systems)
    - [Prometheus](#prometheus)
    - [Dropwizard](#dropwizard)
    - [Metric System Configuration](#metric-system-configuration)
      - [Adding JVM Level Metric Information](#adding-jvm-level-metric-information)
      - [Agent Reporting](#agent-reporting)
    - [Logger Configuration](#logger-configuration)
  - [Performance](#performance)
  - [Dependencies](#dependencies)
- [Binaries & Releases](#binaries-releases)
- [Building](#building)
- [Usage](#usage)
- [Debugging](#debugging)


# Overview

## Motivation
Agent based bytecode instrumentation is a far more elegant, faster and safer approach to instrumenting code on the JVM. Programatic addition of metrics into client code leads to severe code bloat and lack of clarity of the underlying business logic. 

The advantage of agent based bytecode instrumentation vs annotation driven using dependency injection (DI) frameworks like Spring and Guice is quite simple, you don't need to be using Spring or Guice to benefit from it. Another issue with such DI frameworks is that they can only code you they own the injection of which causes some headaches and you must still annotate or otherwise mark the locations to instrument in code. The agent doesn't care if the code you want to instrument is yours, a third party library or the JDK itself.

The ability to simply update a configuration file indicating the metric and code location we want to measure, and simply restart the application to begin gathering measurements in that new location is invaluable and saves a considerable amount of developer time and results in faster performance debugging sessions.


### Code Bloat
	
Say we want to instrument a method which calls some third party library or service and tracks the number of failures (as exceptions thrown). To do this we need to track both the total number of method invocations and the number of failed invocations. Most of the time in modern Java libraries, exceptions are unchecked which allows them to propagate up to an appropriate handler without polluting the code base. 

The following is an example of a basic block of code which performs a basic service call prior 
to instrumentation

```java
public Result performSomeTask() {
    return callSomeServiceMethodWhichCanThrowException(createArgs());
}
```

To instrument this programatically we perform the following

```java
// add class fields

final Counter total = Metrics.createCounter("requests_total");
final Counter failed = Metrics.createCounter("requests_failed");

public Result performSomeTask() {
    total.inc();

    Result result = null;
    try {
        //perform actual original call
        result = callSomeServiceMethodWhichCanThrowException(createArgs());
    } catch (Exception e) {
        failed.inc();
        throw e;
    }

    return result;
}
```

Now lets add a timer to this also so we can see how long the method call takes.

```java
// add class fields

final Counter total = Metrics.createCounter("requests_total");
final Counter failed = Metrics.createCounter("requests_failed");
final Timer timer = Metrics.createTimer("requests_timer");

public Result performSomeTask() {
    long startTime = System.nanoTime();
    total.inc();

    Result result = null;
    try {
        result = callSomeServiceMethodWhichCanThrowException(createArgs());
    } catch (Exception e) {
        failed.inc();
        throw e;
    } finally {
        timer.record(System.nanoTime() - startTime);
    }

    return result;
}
```
		
WOW! That turned ugly fast! We started with 3 LOC (lines of code) representing the business logic and ended up with 17 LOC, 14 of which were due to our metrics. This has the potential to destroy the clarity of a code base.

With agent based instrumentation we can inject bytecode which results in the exact same method bytecode as would be produced by writing and compiling the final metric example, but without touching the source.


## Instrumentation Metadata 

For those who like marking methods to measure programatically, we provide annotations to do just that. We also provide a configuration driven system where you define the methods you want to instrument in a yaml definition.

### Annotations

```java
@Counted (name = "", labels = { }, doc = "")
@Gauged (name = "", mode=inc | dec, labels = { }, doc = "")
@Timed (name = "", labels = { }, doc = "")
@ExceptionCounted (name = "", labels = { }, doc = "")
```

Annotations are provided for all metric types and can be added to methods including
constructors. 

```java
@Counted(name = "taskx_total", doc = "total invocations of task x")
@Timed (name = "taskx_time", doc = "duration of task x")
public Result performSomeTask() {
    //...
}
```

### Configuration

	metrics:
	  {class name}.{method name}{method signature}:
	    - type: Counted
		  name: {name}
		  doc: {metric documentation}
		  labels: ['{name:value}', '{name:value}']
	    - type: Gauged
		  name: {name}
		  mode: {mode}
		  doc: {metric documentation}
		  labels: ['{name:value}']
	    - type: ExceptionCounted
		  name: {name}
		  doc: {metric documentation}
		  labels: ['{name:value}']
	    - type: Timed
		  name: {name}
		  doc: {metric documentation}
		  labels: ['{name:value}']

Each metric is defined on a per method basis. A method is uniquely identified by the 
combination of `{class name}.{method name}{method signature}`. As an example, if we 
wanted to instrument the following method via configuration instead of annotations

```java
package com.fleury.test;
....

public class TestClass {
    ....
    
    @Counted(name = "taskx_total", doc = "total invocations of task x")
    public Result performSomeTask() {
        ...
    }
}
```

We write the configuration as follows

	metrics:
	  com/fleury/test/TestClass.performSomeTask()V:
	    - type: Counted
		  name: taskx_total
		  doc: total invocations of task x


Note the method signature is based on the method parameter types and return type. The parameter types are between the brackets `()` with the return type after. In this case we have no parameters and the return type is void which results in `()V`. [Here](http://journals.ecs.soton.ac.uk/java/tutorial/native1.1/implementing/method.html) is a good overview of Java method signature mappings.

In previous versions we allowed the package name to be specified using `.` instead of the internal `/` separator. While this is still supported for the metrics configuration section, it is not supported anywhere else and should be updated to only have the `/` package separator. 


### Metric Labels

Labels are a concept in some reporting systems that allow for multi-dimensional metric capture and analysis. Labels are composed of name value pairs `({name}:{value})`. You can have up to a maximum of five labels per metric. See the Prometheus metric library guidelines on metric and label naming [here](https://prometheus.io/docs/practices/naming/). Dropwizard metrics doesn't support the concept of labels and so we use the label values as part of the metric name. We apply these in order and so a metric definition of  

```java
@Counted(name = "taskx_total", labels = {"name1:value1", "name2:value2"})
```

would result in a metric name in the Dropwizard registry of `taskx_total.value1.value2`.


#### Dynamic Label Values

A powerful feature is the ability to set label values dynamic based on variables available on the method stack. Metric names cannot be dynamic. The way we specify dynamic label values is using the `${index}` syntax followed by the method argument index. The special value `$this` can be used for referencing `this.toString()` in non static methods (i.e. where `this.` is valid).
 
Note that we restrict the stack usage to the method arguments only. That is, we don't allow use of variables created within the method as that is a very fragile thing to do. The String representation as given by `String.valueOf()` of the parameter is used as the label value. That means for primitive types we perform boxing first and null objects will result in the String `"null"`. Argument indexes start at index `0`. 

```java
@Counted (name = "service_total", labels = { "client:$0" })
public void callService(String client) 
```

Each time this method is invoked it will use the value of the "client" parameter as the metric label value. We also support accessing nested property values. For example, `($1.httpMethod)` where `$1` is the first method parameter and is e.g. of type `HttpRequest`. This means you are essentially doing `httpRequest.getHttpMethod().toString();`. This nesting can be arbitrarily deep.

### White & Black Lists

Sometimes we only want to scan certain packages or classes which we wish to instrument. This could be to reduce the agent startup time or to work around problematic instrumentation situations. Note that the black and white lists do not take any annotations or metric configuration into account and essentially override them.

To white list a class or package include the fully qualified class or package name under the `whiteList` property. If no white list is specified, then all classes are scanned and eligible for transforming. 

    whiteList:
      - com/fleury/test/ClassName
      - com/fleury/package2
        
To black list a class or package add the fully qualified class or package name under the `blackList` property. If a class or package is in both white and black list, the black list wins and the class will not be touched.

    blackList:
       - com/


### What we actually Transform
As we allow the use of annotations to register metrics to track, if no black/white lists are defined we must scan all classes as they are loaded and check for the annotations. However, we do not want to have to rewrite all of these classes if we have not changed anything. There are many reasons you want to modify as little as possible with an agent but the general motto is, only touch what you have to. Hence, we only rewrite classes which have been changed due to the addition of metrics and all other classes, even though scanned, are returned untouched to the classloader.


## Supported Metrics Systems

### Prometheus
Prometheus is the primary supported metric system. The design of the metric meta data was driven by its design (name, labels, doc). It also has the most powerful reporting system and does not require the development / integration of a multitude of reporting sinks like Codeahale does (e.g. Graphite, Librato, etc). 

Also, given the fact that we use the multidimensional labels concept from Prometheus, if we use a system which doesn't support this notion then it is not nearly as powerful. 


### Dropwizard 

Sometimes however, we don't want to go to all the hassle of setting up and entire metrics monitoring system if we don't have one already in place and are only interested in the metrics on a single JVM. 
This is where we would recommend Dropwizard as it provides summary statistics (percentiles, rates etc) out of the box for certain metric types. These are automatically exposed via JVM and can be viewed and graphed with the likes of the VisualVM JMX plugin. 

Some differences in metric types exist between Prometheus and Dropwizard. In particular, with Prometheus you don't decrement counters, instead you use gauges for values that increase and decrease. Supporting this approach with Dropwizard gauges with an agent is tricky as we need to maintain a class variable. Hence, we simply use counters to back gauges with Dropwizard. 

Profiling agents can sometimes be far to heavy to attach to a JVM for a prolonged period of time and impact performance when we only want to monitor certain methods / hotspots. Also, most of the time these tools do not provide summary metrics exception counts which can be recorded with this library.

### Metric System Configuration

The metric systems configuration is passed as simple key-value pairs `(Map<String,String>)` to the constructor of each MetricSystem via their provider. These key-values are defined in the "system" section of the agent configuration.

    metrics:
        .....

    system:
        key1: value1
        
The dropwizard implementation supports the property `domain` which allows to change the exposed JMX domain name. It defaults to the dropwizard default of `metrics`. See the next section for some shared properties around JVM level metrics.
       
#### Adding JVM Level Metric Information

Both Dropwizard and Prometheus support adding JVM level metrics information obtained from the JVM via MBeans for 

- gc
- memory
- classloading
- threads

To enable each, simply add the metrics you want to a `jvm` property in the `system` section of the configuration yaml. For example, to add `gc` and `memory` information to the registry used:

    system:
        jvm:
           - gc
           - memory


#### Agent Reporting

We start the default reporting (endpoint) methods on both metrics systems. For Dropwizard that is JMX (which can be scraped via other services or agents), and for Prometheus that is the HttpServer. The default port for the Prometheus endpoint is `9899` and it can be changed by specifying the property `httpPort` in the system configuration section as follows

    system:
        httpPort: 9899

Additional reporting systems can be added for each agent programatically if required. Alternatively an additional Java agent could be attached to send the JMX metrics to e.g. Graphite for Dropwizard. The benefit of this approach is that it doesn't care how many metric registries are started within the application or by the agent(s). 
        
### Logger Configuration        

j.u.l is used for logging and can be configured by passing the agent argument `log-config:<properties path>` to the agent with the path to the logger properties file. 


## Performance
We use the Java ASM bytecode manipulation library. This is the lowest level and fastest of all the bytecode libraries which is used by the likes of cglib. It allows us to inject bytecode in a precise way which means we can craft the exact same bytecode as if it was hand written. To make the metric system plugable, we chose to abstract the metric work behind a generic SPI interface. The bytecode which we inject uses the SPI which can in turn be swapped out without any change to our bytecode. This makes it very flexible but it comes at the cost of not being able to keep field level static variable references for our metrics. Instead we perform a lookup from a ConcurrentHashMap to get the metric by name in the SPIs. Note that JIT takes care of the additional method dispatches up to the Map by performing inlining. If we were using a single implementation we would inject the metrics fields as static variables at the top of each class. If someone wishes to fork this for a single metric system that would be the best way to go.

It should be noted that as with hand crafted metrics, the additional bytecode and hence method size required to handle capturing all metrics could potentially lead to methods which might otherwise have been inlined or compiled by the JIT being skipped instead. This should be considered regardless off the instrumentation choice and if unsure, the appropriate JVM output should be checked (-XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining -XX:+PrintCompilation).
 

## Dependencies 
Very lightweight.
	
	asm
	jackson

The client libraries for whatever metric provider you choose are also included. Note that the final agent binaries are shaded and all dependencies relocated to prevent possible conflicts.


# Binaries & Releases

See the releases section of the github repository for releases along with the prebuilt agent binaries for dropwizard and prometheus.

# Building

The module metrics-agent-dist has build profiles for both Prometheus and Dropwizard. 

	mvn clean package -Pprometheus

	mvn clean package -Pdropwizard
	
The uber jar can be found under `/target/metrics-agent.jar`

# Usage

The agent must be attached to the JVM at startup. It cannot be attached to a running JVM.

	-javaagent:metrics-agent.jar

Example
	
	java -javaagent:metrics-agent.jar -jar myapp.jar 

Using the configuration file config.yaml is performed as follows

	java -javaagent:metrics-agent.jar=agent-config:agent.yaml -jar myapp.jar 


Using the configuration file config.yaml and logging configuration logger.properties is performed as follows

	java -javaagent:metrics-agent.jar=agent-config:agent.yaml,log-config:logger.properties -jar myapp.jar 


# Debugging

Note if you want to debug the metrics agent you should put the debugger agent first.

	-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=<port> -javaagent:metrics-agent.jar myapp.jar

