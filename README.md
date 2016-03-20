# Polyglot - a universal grpc client

Polyglot is a grpc client which can talk to any grpc server. in order to make a call, the following are required:
* A compiled Polyglot binary, 
* a .proto file for the service *or* a compiled proto class on the classpath,
* and a request proto instance

In particular, it is not necessary to generate grpc classes or to compile the protos into the Polyglot binary.

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
