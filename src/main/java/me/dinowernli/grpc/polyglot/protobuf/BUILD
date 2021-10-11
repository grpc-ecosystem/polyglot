package(default_visibility = ["//visibility:public"])

java_library(
    name = "protobuf",
    srcs = glob(["*.java"]),
    resources = [
        "@com_google_protobuf//:well_known_protos",
    ],
    deps = [
        "//src/main/proto:config_java_proto",
        "//third_party/google-oauth",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/logging:logging-api",
        "//third_party/protobuf",
        "//third_party/protoc-jar",
    ],
)
