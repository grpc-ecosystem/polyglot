package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "hello_proto",
    srcs = ["hello.proto"],
    deps = [
        ":greeting_proto",
        "@com_google_protobuf//:any_proto",
        "@com_google_protobuf//:duration_proto",
        "@com_google_protobuf//:timestamp_proto",
    ],
)

java_proto_library(
    name = "hello_java_proto",
    deps = [":hello_proto"],
)

proto_library(
    name = "greeting_proto",
    srcs = ["greeting.proto"],
)

java_proto_library(
    name = "greeting_java_proto",
    deps = [":greeting_proto"],
)

proto_library(
    name = "config_proto",
    srcs = ["config.proto"],
)

java_proto_library(
    name = "config_java_proto",
    deps = [":config_proto"],
)

java_grpc_library(
    name = "hello_proto_grpc",
    srcs = [":hello_proto"],
    deps = [":hello_java_proto"],
)
