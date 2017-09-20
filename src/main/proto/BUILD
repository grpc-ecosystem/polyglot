load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

package(default_visibility = ["//visibility:public"])

java_proto_library(
    name = "config_proto",
    protos = ["config.proto"],
)

java_proto_library(
    name = "output_proto",
    protos = ["output.proto"],
    imports = ["external/com_github_google_protobuf/src/"],
    inputs = ["@com_github_google_protobuf//:well_known_protos"],
)

java_proto_library(
    name = "hello_proto_grpc",
    protos = ["hello.proto"],
    with_grpc = True,
)

java_proto_library(
    name = "hello_proto",
    protos = ["hello.proto"],
    with_grpc = False,
)
