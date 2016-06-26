#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
    exit 1
fi

bazel build src/main/java/me/dinowernli/grpc/polyglot && ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/polyglot \
  --command=list_services \
  --proto_discovery_root=./src/main/proto \
  --add_protoc_includes=. \
  --service_filter=HelloService \
  --with_message=true
