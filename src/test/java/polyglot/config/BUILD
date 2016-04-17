load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/proto:config_proto",
    "//src/main/java/polyglot/config",
    "//third_party/testing",
  ],
)
