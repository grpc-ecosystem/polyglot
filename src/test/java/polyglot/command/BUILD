load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/java/polyglot/command",
    "//src/main/proto/testing/foo:foo_proto",
    "//src/main/proto/testing:test_service_proto",
    "//src/main/java/polyglot/testing",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
