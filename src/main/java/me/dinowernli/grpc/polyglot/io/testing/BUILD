package(default_visibility = ["//visibility:public"])

java_library(
    name = "testing",
    testonly = 1,
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/proto/testing:test_service_java_proto",
        "//third_party/guava",
        "//third_party/protobuf",
    ],
)
