load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  data = [
    "//src/main/proto/testing/protobuf:proto_files",
  ],
  deps = [
    "//src/main/proto:config_proto",
    "//src/main/proto/testing/foo:foo_proto",
    "//src/main/proto/testing:test_service_proto",
    "//src/main/java/polyglot/protobuf",
    "//src/main/java/polyglot/testing",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
