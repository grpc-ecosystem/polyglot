load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/proto/testing:test_service_proto",
    "//src/main/java/me/dinowernli/grpc/polyglot/grpc",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
