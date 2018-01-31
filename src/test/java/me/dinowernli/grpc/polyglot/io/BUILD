load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    data = [
        ":testdata",
    ],
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/io",
        "//src/main/java/me/dinowernli/grpc/polyglot/io/testing",
        "//src/main/java/me/dinowernli/grpc/polyglot/testing",
        "//src/main/proto:config_proto",
        "//src/main/proto/testing:test_service_proto",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)

filegroup(
    name = "testdata",
    srcs = glob(["testdata/*"]),
)
