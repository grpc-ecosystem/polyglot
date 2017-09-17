#!/bin/bash

set -e

if [[ "$1" == "--fix" ]]; then
  MODE="-mode=fix"
else
  MODE="-mode=check"
fi


bazel build @com_github_bazelbuild_buildtools//buildifier:buildifier
RESULT=`find -name BUILD -or -name WORKSPACE | xargs bazel-bin/external/com_github_bazelbuild_buildtools/buildifier/buildifier $MODE -v`

# In theory, buildifier should return a non-zero exit code in check mode if there are errors. From
# staring at the buildifier code, it seems buildifier only returns the correct exit code for the
# *last* scanned file. So the only reliable way to determine if buildifier's checks passed is to test
# for empty output.
if [ -n "$RESULT" ]; then
  echo 'Buildifier formatting checks failed. Please run with "--fix"'
  exit 1
fi
