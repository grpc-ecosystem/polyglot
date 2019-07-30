load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "plain",
    size = "medium",
    srcs = [
        "ClientServerIntegrationTest.java",
    ],
    data = [
        "//src/main/proto/testing:proto_files",
    ],
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot:polyglot-lib",
        "//src/main/java/me/dinowernli/grpc/polyglot/io",
        "//src/main/java/me/dinowernli/grpc/polyglot/testing",
        "//src/main/proto/testing:test_service_java_proto",
        "//src/main/proto/testing:test_service_proto_grpc",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)

auto_java_test(
    name = "tls",
    size = "medium",
    srcs = [
        "TlsIntegrationTest.java",
    ],
    data = [
        "//src/main/proto/testing:proto_files",
    ],
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot:polyglot-lib",
        "//src/main/java/me/dinowernli/grpc/polyglot/io",
        "//src/main/java/me/dinowernli/grpc/polyglot/testing",
        "//src/main/proto/testing:test_service_java_proto",
        "//src/main/proto/testing:test_service_proto_grpc",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)
