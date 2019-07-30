load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    data = [
        "//src/main/proto/testing/protobuf:proto_files",
    ],
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
        "//src/main/java/me/dinowernli/grpc/polyglot/testing",
        "//src/main/proto:config_java_proto",
        "//src/main/proto/testing:test_service_java_proto",
        "//src/main/proto/testing/foo:foo_java_proto",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)
