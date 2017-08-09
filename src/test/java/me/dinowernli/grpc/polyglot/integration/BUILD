load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
  name = "plain",
  srcs = [
    "ClientServerIntegrationTest.java",
  ],
  size = "medium",
  data = [
    "//src/main/proto/testing:proto_files",
  ],
  deps = [
    "//src/main/proto/testing:test_service_proto_grpc",
    "//src/main/java/me/dinowernli/grpc/polyglot:polyglot-lib",
    "//src/main/java/me/dinowernli/grpc/polyglot/io",
    "//src/main/java/me/dinowernli/grpc/polyglot/testing",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)

auto_java_test(
  name = "tls",
  srcs = [
    "TlsIntegrationTest.java",
  ],
  size = "medium",
  data = [
    "//src/main/proto/testing:proto_files",
  ],
  deps = [
    "//src/main/proto/testing:test_service_proto_grpc",
    "//src/main/java/me/dinowernli/grpc/polyglot:polyglot-lib",
    "//src/main/java/me/dinowernli/grpc/polyglot/io",
    "//src/main/java/me/dinowernli/grpc/polyglot/testing",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
