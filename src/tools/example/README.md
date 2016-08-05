# Examples showing Polyglot usage

Before running any example, make sure you are in the project root directory.

### Starting test server

In order to run example Polyglot invocations we need to run example hello server. Use [`run-server.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/run-server.sh) to start server:

`$ bash src/tools/example/run-server.sh`

### Example RPC calls

In order to run a simple rpc call invoke [`call-command-example.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/call-command-example.sh):

`$ bash src/tools/example/call-command-example.sh`

It uses [simple JSON](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/request.pb.ascii) as a request.

In order to run streaming rpc call invoke [`stream-call-command-example.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/stream-call-command-example.sh):

`$ bash src/tools/example/stream-call-command-example.sh`

It uses [stream of jsons](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/requests_multi.pb.ascii) as requests.

### Example services listing

In order to simply list the services invoke [`list-command.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/list-command.sh):

`$ bash src/tools/example/list-command.sh`

In order to list and filter the services invoke [`list-command-with-filter.sh`](https://github.com/grpc-ecosystem/polyglot/tree/master/src/tools/example/list-command-with-filter.sh):

`$ bash src/tools/example/list-command-with-filter.sh`