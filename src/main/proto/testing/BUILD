package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
    name = "test_service_proto",
    srcs = ["test_service.proto"],
    deps = [
        "//src/main/proto/testing/foo:foo_proto",
        "@com_google_protobuf//:any_proto",
        "@com_google_protobuf//:duration_proto",
    ],
)

java_proto_library(
    name = "test_service_java_proto",
    deps = [":test_service_proto"],
)

java_grpc_library(
    name = "test_service_proto_grpc",
    srcs = [":test_service_proto"],
    deps = [":test_service_java_proto"],
)

filegroup(
    name = "proto_files",
    srcs = glob(["*.proto"]),
    data = [
        "//src/main/proto/testing/foo:proto_files",
    ],
)
