load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

licenses(["permissive"])
package(default_visibility = ["//visibility:public"])

java_library(
    name = "protobuf",
    exports = [
        "@com_google_code_gson//jar",
        "@protobuf_java_artifact//jar",
        "@protobuf_java_util_artifact//jar",
    ],
)

java_proto_library(
    name = "descriptor_java_proto",
    protos = ["google/protobuf/descriptor.proto"],
)
