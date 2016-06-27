#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "Could not find WORKSPACE file - this must be run from the project root directory"
    exit 1
fi

bazel build src/main/java/me/dinowernli/grpc/polyglot && cat src/tools/example/request.pb.ascii | ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/polyglot \
  --command=call \
  --full_method=polyglot.HelloService/SayHello \
  --endpoint=localhost:12345 \
  --proto_discovery_root=./src/main/proto \
  --add_protoc_includes=. \
  --config_set_path=config.pb.json \
  --deadline_ms=3000
