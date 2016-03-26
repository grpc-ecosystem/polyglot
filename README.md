# Polyglot - a universal grpc client

Polyglot is a grpc client which can talk to any grpc server. In order to make a call, the following are required:
* A compiled Polyglot binary, 
* the .proto files for the service,
* and a request proto instance in text format.

In particular, it is not necessary to generate grpc classes for the service or to compile the protos into the Polyglot binary.

## Status

Polyglot is currently very much *work in progress*. At the time of writing, the project is still very rough around the edges, but improvements to usability are on their way.

## Usage

For now, it is required to build polyglot before usage (currently only works on Linux):

`$ bazel build src/main/java/polyglot`

After calling this, you should have a fresh binary at:

`./bazel-bin/src/main/java/polyglot/polyglot`

Then, the binary itself can be invoked as follows:

```
$ echo <text-format-request> | polyglot \
    --endpoint=<host>:<port> \
    --full_method=<some.package.Service/doSomething> \
    --proto_root=<path> \
    [--output=<path>]
```

For an example, see `run-client-example.sh`.

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

`$ bazel test ...`

## Upcoming features

* TLS support
* Streamed responses
* OAuth integration for authenticated requests
* Binary distribution
* Building on Mac and Windows
