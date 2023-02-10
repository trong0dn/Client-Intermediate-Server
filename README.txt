Carleton University 
Department of Systems and Computer Engineering 
SYSC 3303A Real-Time Concurrent Systems Winter 2023 
Assignment 3 - Remote Procedure Calls

@author Trong Nguyen
@version 2.0
@date 04-03-2023
---------------------------------------------------------------------------------

# Client <-> Intermediate <-> Server DatagramPacket transfers

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

            
## Requirements and Dependencies

This application was created on Windows 10 OS using Eclipses IDE.
Run on the latest version of jdk-17 using Java 17.

No other external dependencies required.

## Compiling and Running the Application

Download and extract the .zip file. Then import the source code directly and 
run the program in local IDE, otherwise the program can be compiled and 
executed via Command Prompt. Note that each program requires its own 
terminal. In other words, it must be able to run multiple main programs 
(projects) concurrently.

```console
> cd C:\..\..\\lab2\src\			// Navigate to the src directory	
> javac *.java					// Compile the source code
> java -cp . Server				// Set classpath to run application
> java -cp . Intermediate		// Set classpath to run application
> java -cp . Client				// Set classpath to run application
```

## Technical Specifications

### Client

The Client is designed to send DatagramPackets using DatagramSockets 
specified to a well-known port number: 23. The message in the packet
itself is encode as byte array. The client creates a DatagramSocket to use
both send and receive repeat the following 11 times. In addition, prints out
receiving and sending packet details. The Client also receives packets from
the Server as well.

### Intermediate

The Intermediate Host is a relay node within the network. The Intermediate 
receives packets from the Client on port 23. And creates another 
DatagramPacket with the same information to be sent to the Server on port 
69. It also prints out receiving and sending packet details. Moreover, 
the Intermediate also receive packets from the Server which it also creates 
another DatagramPacket to send back to the Client.

### Server

The Server receives packets from the Intermediate on port 69, then validates 
the encoded message. Once validated, the Intermediate creates another 
DatagramPacket with a different message to be sent back to the Intermediate
on the same port. It also prints out receiving and sending packet details.

## Disclaimer

Copyright disclaimer under section 107 of the Copyright Act 1976, allowance is 
made for fair use for purposes such as criticism, comment, news reporting, 
teaching, scholarship, education and research.

Fair use is a use permitted by copyright statute that might otherwise be 
infringing.
