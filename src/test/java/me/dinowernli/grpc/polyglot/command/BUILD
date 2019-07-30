load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/command",
        "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
        "//src/main/java/me/dinowernli/grpc/polyglot/testing",
        "//src/main/proto/testing:test_service_java_proto",
        "//src/main/proto/testing/foo:foo_java_proto",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)
