load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_library")

package(default_visibility = ["//visibility:public"])

java_proto_library(
  name = "test_service_proto",
  protos = ["test_service.proto"],
  proto_deps = [
    "//src/main/proto/testing/foo:foo_proto",
  ],
)

java_proto_library(
  name = "test_service_proto_grpc",
  protos = ["test_service.proto"],
  with_grpc = True,
  proto_deps = [
    "//src/main/proto/testing/foo:foo_proto",
  ],
)

filegroup(
  name = "proto_files",
  srcs = glob(["*.proto"]),
  data = [
    "//src/main/proto/testing/foo:proto_files",
  ],
)
