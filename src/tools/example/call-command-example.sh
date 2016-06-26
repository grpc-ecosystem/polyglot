#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
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
