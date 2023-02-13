Carleton University 
Department of Systems and Computer Engineering 
SYSC 3303A Real-Time Concurrent Systems Winter 2023 
Assignment 3 - Remote Procedure Calls

@author Trong Nguyen
@version 2.0
@date 04/03/2023
---------------------------------------------------------------------------------

# Client <-> Intermediate <-> Server Remote Procedure Calls

## Problem Description

The goal is to change the three part system consisting of a client, an 
intermediate host, and a server from asynchronous UDP and convert it to use 
synchronous communication to establish a two-way channel from the client to the 
server. In effect, you will now be using remote procedure calls to transfer data 
from the client to the server using the intermediate host. The following sequence 
diagram shows how a basic write request takes place.

[sequence-diagram](/src/resources/sequence-diagram.png)

## Specification

The algorithm is similar to asynchronous UDP. However, the Client will now wait 
for the Intermediate task to accept the data and then reply rather than continuing
on to wait for the acknowledgement packet directly. 

## Project Structure

lab3
|   .classpath
|   .gitignore
|   .project
|   A3-class-UML.pdf
|   A3-class-UML.png
|   A3.drawio
|   README.txt
|
+---.settings
|       org.eclipse.core.resources.prefs
|
+---bin
|   |   Client.class
|   |   Intermediate.class
|   |   Server.class
|   |
|   \---resources
|           sequence-diagram.png
|
\---src
    |   Client.java
    |   Intermediate.java
    |   Server.java
    |
    \---resources
            sequence-diagram.png
            
## Requirements and Dependencies

This application was created on Windows 10 OS using Eclipses IDE.
Run on the latest version of jdk-17 using Java 17.

No other external dependencies required.

## Compiling and Running the Application

Download and extract the .zip file. Then import the source code directly and 
run the program in local IDE, otherwise the program can be compiled and 
executed via Command Prompt. Note that each program requires its own 
terminal. In other words, it must be able to run multiple main programs 
(projects) concurrently. Order matters, to be able to run the program
successfully the sequence of execution is important for the DatagramSockets
to properly receive an inbound packet. Hence, the intermediate host must 
always be the first to be run.

```console
> cd C:\..\..\lab3\src\			// Navigate to the src directory	
> javac *.java					// Compile the source code
> java -cp . Intermediate		// Set classpath to run application
> java -cp . Client				// Set classpath to run application
> java -cp . Server				// Set classpath to run application
```

## Technical Specifications

### Client

The Client is designed to send DatagramPackets using DatagramSockets 
specified to a well-known port number: 23. The message in the packet
itself is encode as byte array. The Client creates a DatagramSocket to use
both send and receive repeat the following 11 times. In addition, prints out
receiving and sending packet details. The Client only directly interacts with
the Intermediate host where each `send()` is accompanied by a `reply()`. The 
Client sends messages to the Server via the Intermediate host. 

### Intermediate

The Intermediate host is a relay node within the network. The Intermediate 
receives packets from the Client on port 23. And creates another 
DatagramPacket with the same information to be sent to the Server on port 
69. It also prints out receiving and sending packet details. Hence, the 
Intermediate host corresponds messages DatagramPacket messages between the 
Client and the Server, where each request for data or acknowledge is 
accompanied with a reply.

### Server

The Server receives packets from the Intermediate on port 69, then validates 
the encoded message. Once validated, the Server creates another 
DatagramPacket with a different message to be sent back to the Intermediate
on the same port. It also prints out receiving and sending packet details.
Hence, the Server sends a message back to the Client via the Intermediate 
host.

## Questions

1. Why is it suggested to use more than one thread for the implementation 
of the Intermediate task?

Using more that one thread in this regards means using one thread for packets 
going from the Client to the Server and one for packets going from the Server
to the Client. There various reasons for doing so:

a) Favor independent computations
To perform concurrent computation the execution must be able to run 
independently of each other. By using one thread for packets going from 
Client to Server and another thread for packets going from Serve to Client, 
the separation of these events are no longer dependent on on another. In other
words, the thread managing the packets from Client to server does not influence
the thread packet being passed from Server to Client. 

b) Implement concurrency at hotspots/busy areas
The objective is to identify the highest level or busy areas where concurrency 
can be implemented so that hotspot of code can be executed concurrently. Placing 
concurrency at the highest possible level around hotspot is one of the best 
ways to achieve coarse-grained division of work to be assigned to threads.

c) Never assume a particular order of execution
Execution order of threads can be often nondeterministic and controller by OS
scheduling algorithms. Hence, there is no reliable way of predicting the order
of thread running from one execution to another or even which thread is 
scheduled to be run next. Code that relies on a particular order of execution
among threads that may run into deadlocks. Thus, using multiple threads 
minimizes the need for synchronization of the execution order of threads relative
to each other.

2. Is it necessary to use synchronized in the intermediate task? Explain.

Since we are using asynchronous UDP, there is no need to synchronize the 
intermediate task. However, we are also using remote procedure call (RPC) which 
uses the client-server model, which is a synchronous operation requiring the 
requesting program to be suspended or wait until the results of the remote 
procedure are returned. However, the use of threads that share the same address 
space enables multiple RPCs to be performed concurrently.

RPC is a request-response protocol, which is initiate by the Client sending a 
message to an known remote Server to execute a specific procedure. The remote 
Server sends a response to the Client and application continues its process. While
the Server is processing the call, the Client is blocked (it waits until the 
Server has finished processing before resuming execution), unless the Client sends
an asynchronous request to the server. In this case, it is not necessary to invoke
synchronized on the intermediate task. 

## Disclaimer

Copyright disclaimer under section 107 of the Copyright Act 1976, allowance is 
made for fair use for purposes such as criticism, comment, news reporting, 
teaching, scholarship, education and research.

Fair use is a use permitted by copyright statute that might otherwise be 
infringing.
