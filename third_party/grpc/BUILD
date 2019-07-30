package(default_visibility = ["//visibility:public"])

load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

# TODO(dino): Couldn't find a grpc java build target for the common grpc
# protos. This is copied from the grpc repo itself.
java_grpc_library(
    name = "_reflection_java_grpc",
    srcs = ["@io_grpc_grpc_proto//:reflection_proto_deprecated"],
    visibility = ["//visibility:private"],
    deps = ["@io_grpc_grpc_proto//:reflection_java_proto_deprecated"],
)

java_library(
    name = "grpc",
    licenses = ["permissive"],
    exports = [
        ":_reflection_java_grpc",
        "@io_grpc_grpc_java//auth",
        "@io_grpc_grpc_java//context",
        "@io_grpc_grpc_java//core",
        "@io_grpc_grpc_java//netty",
        "@io_grpc_grpc_java//protobuf",
        "@io_grpc_grpc_java//services:reflection",
        "@io_grpc_grpc_java//stub",
        "@io_grpc_grpc_proto//:reflection_java_proto",
        "@io_grpc_grpc_proto//:reflection_java_proto_deprecated",
    ],
)

java_library(
    name = "testing",
    testonly = 1,
    licenses = ["permissive"],
    exports = [
        "@grpc_testing_artifact//jar",
    ],
)
