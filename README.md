# polyglot
A universal grpc client.

## Build and run tests

`$ bazel test src/...`

## Run the example

First, start the server: 

`$ bazel build src/... && bazel-bin/src/main/java/polyglot/server/main`

Then, run the client:

`$ ./run.sh`
