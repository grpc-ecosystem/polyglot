#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
    exit 1
fi

bazel build src/main/java/me/dinowernli/grpc/polyglot/server:main && ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/server/main
