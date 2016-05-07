#!/usr/bin/env bash

if [ ! -f WORKSPACE ]; then
    echo "This must be invoked from the WORKSPACE root"
    exit 1
fi

rm -rf output && mkdir output && chmod +x output

echo "Building Windows binary..."
mkdir output/windows && chmod +x output/windows
bazel build src/main/java/polyglot:polyglot_deploy.jar --define=target=windows
cp ./bazel-bin/src/main/java/polyglot/polyglot_deploy.jar ./output/windows/polyglot.jar
echo "Done"

echo "Building Linux binary..."
mkdir output/linux && chmod +x output/linux
bazel build src/main/java/polyglot:polyglot_deploy.jar --define=target=linux
cp ./bazel-bin/src/main/java/polyglot/polyglot_deploy.jar ./output/linux/polyglot.jar
echo "Done"

echo "Building Osx binary..."
mkdir output/osx && chmod +x output/osx
bazel build src/main/java/polyglot:polyglot_deploy.jar --define=target=osx
cp ./bazel-bin/src/main/java/polyglot/polyglot_deploy.jar ./output/osx/polyglot.jar
echo "Done"

