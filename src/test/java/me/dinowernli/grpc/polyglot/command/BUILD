load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/java/me/dinowernli/grpc/polyglot/command",
    "//src/main/java/me/dinowernli/grpc/polyglot/testing",
    "//src/main/proto/testing/foo:foo_proto",
    "//src/main/proto/testing:test_service_proto",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
