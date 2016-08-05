#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "Could not find WORKSPACE file - this must be run from the project root directory"
    exit 1
fi

bazel build src/main/java/me/dinowernli/grpc/polyglot/server:main && \
./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/server/main
