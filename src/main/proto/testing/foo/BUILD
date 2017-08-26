load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

package(default_visibility = ["//visibility:public"])

java_proto_library(
    name = "foo_proto",
    protos = ["foo.proto"],
)

filegroup(
    name = "proto_files",
    srcs = glob(["*.proto"]),
)
