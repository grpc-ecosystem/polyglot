load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/grpc",
        "//src/main/proto/testing:test_service_proto",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)
