load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
  name = "tests",
  srcs = glob(["*.java"]),
  size = "small",
  deps = [
    "//src/main/proto/testing:test_service_proto",
    "//src/main/java/me/dinowernli/grpc/polyglot/io",
    "//src/main/java/me/dinowernli/grpc/polyglot/io/testing",
    "//src/main/java/me/dinowernli/grpc/polyglot/testing",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
  data = [
    ":testdata",
  ],
)

filegroup(
  name = "testdata",
  srcs = glob(["testdata/*"]),
)
