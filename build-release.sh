#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
    exit 1
fi

bazel build src/main/java/polyglot:polyglot_deploy.jar && mkdir -p output && chmod +x output && rm -f ./output/polyglot && cp ./bazel-bin/src/main/java/polyglot/polyglot_deploy.jar ./output/polyglot

echo "Created new binary output/polyglot"
