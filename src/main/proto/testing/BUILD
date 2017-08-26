load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

package(default_visibility = ["//visibility:public"])

java_proto_library(
    name = "test_service_proto",
    proto_deps = [
        "//src/main/proto/testing/foo:foo_proto",
    ],
    protos = ["test_service.proto"],
)

java_proto_library(
    name = "test_service_proto_grpc",
    proto_deps = [
        "//src/main/proto/testing/foo:foo_proto",
    ],
    protos = ["test_service.proto"],
    with_grpc = True,
)

filegroup(
    name = "proto_files",
    srcs = glob(["*.proto"]),
    data = [
        "//src/main/proto/testing/foo:proto_files",
    ],
)
