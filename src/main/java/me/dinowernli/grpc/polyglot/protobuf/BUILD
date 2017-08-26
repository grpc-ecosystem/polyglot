package(default_visibility = ["//visibility:public"])

java_library(
    name = "protobuf",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/proto:config_proto",
        "//third_party/google-oauth",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/logging:logging-api",
        "//third_party/protobuf",
        "//third_party/protoc-jar",
    ],
)
