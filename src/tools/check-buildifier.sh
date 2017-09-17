#!/bin/bash

set -e

if [[ "$1" == "--fix" ]]; then
  MODE="-mode=fix"
else
  MODE="-mode=check"
fi


bazel build @com_github_bazelbuild_buildtools//buildifier:buildifier
find -name BUILD -or -name WORKSPACE | xargs bazel-bin/external/com_github_bazelbuild_buildtools/buildifier/buildifier $MODE -v

