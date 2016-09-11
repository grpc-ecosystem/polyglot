#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
    exit 1
fi

rm -rf output && mkdir output && chmod +x output

echo "Building binary..."
bazel build src/main/java/me/dinowernli/grpc/polyglot:polyglot_deploy.jar
cp ./bazel-bin/src/main/java/me/dinowernli/grpc/polyglot/polyglot_deploy.jar ./output/polyglot.jar
echo "Done"

