load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
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
    "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
    "//src/main/java/me/dinowernli/grpc/polyglot/testing",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
