#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "Could not find WORKSPACE file - this must be run from the project root directory"
    exit 1
fi

bazel build src/main/java/me/dinowernli/grpc/polyglot && ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/polyglot \
  --command=list_services \
  --proto_discovery_root=./src/main/proto \
  --add_protoc_includes=. \
  --service_filter=HelloService \
  --with_message=true
