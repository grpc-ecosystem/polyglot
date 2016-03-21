# Polyglot - a universal grpc client

Polyglot is a grpc client which can talk to any grpc server. In order to make a call, the following are required:
* A compiled Polyglot binary, 
* a .proto file for the service *or* a compiled proto class on the classpath,
* and a request proto instance

In particular, it is not necessary to generate grpc classes or to compile the protos into the Polyglot binary.

## Build requirements

We do not yet have a binary distribution, so right now it is required to build the binary from source in order to use it. The build requirements are:

* Java 8
* Bazel

## Run the example

First, start the server: 

`$ ./run-server.sh`

Then, in a different terminal, run the client example:

`$ ./run-client-example.sh`

## Build and run tests

`$ bazel test src/...`

## Upcoming features

* TLS support
* Streamed responses
* OAuth integration for authenticated requests
* Integration for running the protoc compiler
* Binary distribution
