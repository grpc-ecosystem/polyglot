#!/usr/bin/env bash

POLYGLOT_PATH="src/main/java/me/dinowernli/grpc/polyglot"
POLYGLOT_BIN="./bazel-bin/${POLYGLOT_PATH}/polyglot"

if [ ! -f WORKSPACE ]; then
    echo "Could not find WORKSPACE file - this must be run from the project root directory"
    exit 1
fi

bazel build ${POLYGLOT_PATH} && \
cat src/tools/example/request.pb.ascii | ${POLYGLOT_BIN}  \
  --command=call \
  --full_method=polyglot.HelloService/SayHello \
  --endpoint=localhost:12345 \
  --config_set_path=config.pb.json \
  --deadline_ms=3000 \
  $@
