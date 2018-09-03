#!/usr/bin/env bash

POLYGLOT_PATH="src/main/java/me/dinowernli/grpc/polyglot"
POLYGLOT_BIN="./bazel-bin/${POLYGLOT_PATH}/polyglot"

if [ ! -f WORKSPACE ]; then
    echo "Could not find WORKSPACE file - this must be run from the project root directory"
    exit 1
fi

bazel build ${POLYGLOT_PATH} && ${POLYGLOT_BIN}  \
  --proto_discovery_root=./src/main/proto \
  --add_protoc_includes=. \
  list_services
