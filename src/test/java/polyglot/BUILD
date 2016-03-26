load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/proto/testing:test_service_proto",
    "//src/main/java/polyglot:polyglot-lib",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
