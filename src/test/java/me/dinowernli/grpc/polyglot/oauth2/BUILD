load("@autotest//bzl:autotest.bzl", "auto_java_test")

auto_java_test(
    name = "tests",
    size = "small",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/oauth2",
        "//src/main/proto:config_proto",
        "//third_party/google-oauth",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/protobuf",
        "//third_party/testing",
    ],
)
