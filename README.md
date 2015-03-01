# Overview

## Motivation
Agent based instrumentation vs client code based.

### Code Bloat
	
Say we want to instrument a method which does some which calls some third party library or service and track the number of failures (as exceptions thrown). To do this we need to track both the total number 
of method invocations and the number of failed invocations. 

Most of the time in modern Java libraries, exceptions are unchecked which allows them to 
propagate up to an appropriate handler without polluting the code base. 

The following is an example of a basic block of code which performs a basic service call prior 
to instrumentation

	public Result performSomeTask() {
		return callSomeServiceMethodWhichCanThrowException(createArgs());
	}
	

To instrument this programatically we perform the following

	// add class fields

	final Counter total = Metrics.createCounter("requests_total");
	final Counter failed = Metrics.createCounter("requests_failed");

	public Result performSomeTask() {
		total.inc();

		Result result = null;
		try {
			//do some preparation
			result = callSomeServiceMethodWhichCanThrowException(createArgs());
		} catch (Exception e) {
			failed.inc();
			throw e;
		}

		return result;
	}

Now lets add a timer to this also so we can see how long the method call takes.

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
		
WOW! That turned ugly fast! We started with 3 LOC (lines of code) representing the business logic
and ended up with 17 LOC, 14 of which were due to our metrics. This has the potential to destroy
the clarity of a code base.

With agent based instrumentation we can inject bytecode which results in the 
exact same method bytecode as would be produced by writing and compiling the final metric example,
but without touching the source.


### Third Party Code
Sometimes we don't have access to the source code for a piece of code we would like to 
instrument. With agent based instrumentation this isn't an issue and we can instrument 
it as if it were ours.

Other times, even if the code is available we may still want to instrument certain parts of 
it without forking it, adding metric code and maintaining it. Agent based metrics gets us out
of this bind also.

Don't forget, the JDK is just another library which can be instrumented!

### Human Error
Monotonous code is more susceptible to bugs. Instrumentation of the above metrics can 
be seen as exceptionally tedious. Therefore, as the number of instrumented methods increase,
the likelyhood of a bug increases rapidly. Using agent based instrumentation removes this 
possibility. 

Incorrectly naming a metric can also be considered a bug. Fixing this with manual instrumentation 
requires a code change and redeploy. This is also the case if using annotation based agent
instrumentation. However, if using the configuration driven agent instrumentation then this 
is a simple matter of updating the configuration and restarting the application.


## Features

## Labels

Labels are composed of name value pairs ({name}:{value}). You can have up to a maximum 
of five labels per metric. 

TODO - link to Prometheus docs on good usage and definitions etc.

### Dynamic Label Values

Label values can be dynamic and set based on variables available on the method stack.
Metric names cannot be dynamic .. yet (not 100% sure they should be in addition to labels). 
The way we specify this is using the ${index} syntax followed by the variable index on the stack. Note 
that we will restrict the stack usage from zero up to the number of method arguments. That is,
we don't allow use of variables created within the method as that is a very fragile thing to do. 

The String representation as given by String.valueOf() of the parameter is used as the label value. 
That means for primitive types we perform boxing first and null objects will result in the 
String "null". Note the first variable on the stack is always the reference to "this" which means
method parameters start at index 1. 

	@Counted (name = "service_total" labels = { "client:$1" }
	public void callService(String client) 

Each time this method is invoked it will use the value of the "client" parameter as the
metric label value. 

Note that we plan on supporting the ability to navigate object types to get child values
like follows ($1.httpMethod) where $1 is the first method parameter and is e.g. of type HttpRequest. This
means you are essentially doing HttpRequest.getHttpMethod().toString();

We will also allow access to class field values as given using $fieldname syntax.


## Instrumentation Metadata 

You can use either annotations or configuration to instrument your code. Obviously the later 
doesn't require you to actually change your code whereas the former does. 

### Annotations

	@Counted (name = "", labels = { }, doc = "")
	@Gauged (name = "", mode=inc | dec, labels = { }, doc = "")
	@Timed (name = "", labels = { }, doc = "")
	@ExceptionCounted (name = "", labels = { } doc = "")


Annotations are provided for all metric types and can be added to methods including
constructors. 

	@Counted(name = "taskx_total" doc = "total invocations of task x")
	@Timed (name = "taskx_time" doc = "duration of task x")
	public Result performSomeTask() {
		...
	}


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
combination of {class name}.{method name}{method signature}. As an example, if we 
wanted to instrument the following method via configuration instead of annotations

	package com.fleury.test;
	....

	public class TestClass {
		....
		
		@Counted(name = "taskx_total" doc = "total invocations of task x")
		public Result performSomeTask() {
			...
		}
	}


We write the configuration as follows

	metrics:
	  com.fleury.test.TestClass.performSomeTask()V:
	    - type: Counted
		  name: taskx_total
		  doc: total invocations of task x


Note the method signature is based on the method parameter types and return type. The
parameter types are between the brackets () with the return type after. In this case
we have no parameters and the return type is void (V). Here is a good overview of Java
method signature mappings (TODO: link to correct JVM spec section)

http://journals.ecs.soton.ac.uk/java/tutorial/native1.1/implementing/method.html



## Supported Metrics Systems

### Prometheus
Prometheus is the primary supported metric system. The design of the metric meta data was 
driven by its design (name, labels, doc). It also has the most powerful reporting system and 
does not require the development / integration of a multitude of reporting sinks like Codeahale 
does (e.g. Graphite, Librato, etc). 

Also, given the fact that we use the multidimensional labels concept from Prometheus, if we 
use a system which doesn't support this notion then it is not nearly as powerful. 


### Codahale 

Sometimes however, we don't want to go to all the hassle of setting up and entire metrics
monitoring system if we don't have one already in place and are only interested in the metrics 
on a single JVM. 
This is where we would recommend Codahale as it provides summary statistics (percentiles, rates etc) out
of the box for certain metric types. These are automatically exposed via JVM and can be viewed
and graphed with the likes of the VisualVM JMX plugin. 

Profiling agents can sometimes be far to heavy to attach to a JVM for a prolonged period of 
time and impact performance when we only want to monitor certain methods / hotspots. 
Also, most of the time these tools do not provide summary metrics exception counts which 
can be recorded with this library.


## Performance
We use the Java ASM bytecode manipulation library. This is the lowest level and fastest of all the bytecode 
libraries and is used by the likes of cglib. 

Application startup time is affected slightly (approx 2 sec to instrument every method in core Java library - RT.jar)

As the bytecode is the exact same as if you were to manually instrument the code by hand,
the Application runtime has no additional performance penalty for using this library than
if you manually instrumented.
 

## Dependencies 
Very lightweight.
	
	asm
	jackson
	slf4j
	log4j

The client libraries for whatever metric provider you choose are also included. 
Note that the final agent binaries are shaded and all dependencies relocated to prevent
possible conflicts.


# Building

The module metrics-agent-dist has build profiles for both Prometheus and Codahale. 

	mvn clean package -Pprometheus

	mvn clean package -Pcodahale

# Usage

The agent must be attached to the JVM at startup. It cannot be attached to a running JVM.

	-javaagent:metric-agent-dist.jar

Example
	
	java -javaagent:metric-agent-dist.jar -jar myapp.jar 

Using the configuration file config.yaml is performed as follows

	java -javaagent:metric-agent-dist.jar=agent-config:config.yaml -jar myapp.jar 


# Debugging

Note if you want to debug the metrics agent you should put the debugger agent first.

	-agentlib:jdwp=transport=dt_socket,server=n,address=localhost -javaagent:metric-agent-dist.jar myapp.jar


# TODO

Produce configuration for common use cases like Jetty server metrics. Each configuration
should be held in its own module and a build profile added to metrics-agent-dist to 
include that dependency.

Allow specifying counted or gauged to be invoked at either method start or method end (currently
only method start).

Make reporting capabilities pluggable for codahale (shading issue?).

Ability to capture the local variables of methods frame stack when an exception occurs and
record / report to some external sink for debugging (e.g. encrypted file or server). However,
this may branch into another project as it isn't monitoring, rather debugging!
