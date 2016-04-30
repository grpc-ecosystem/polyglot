load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "medium",
  data = [
    "//src/main/proto/testing:proto_files",
  ],
  deps = [
    "//src/main/proto/testing:test_service_proto_grpc",
    "//src/main/java/polyglot:polyglot-lib",
    "//src/main/java/polyglot/io",
    "//src/main/java/polyglot/testing",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
