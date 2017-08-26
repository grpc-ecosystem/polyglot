load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/config",
        "//src/main/proto:config_proto",
        "//third_party/guava",
        "//third_party/testing",
    ],
)
