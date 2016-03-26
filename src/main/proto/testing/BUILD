load("//third_party/bazel:genproto.bzl", "proto_java_library")

package(default_visibility = ["//visibility:public"])

proto_java_library(
  name = "test_service_proto",
  src = "test_service.proto",
)

proto_java_library(
  name = "test_service_proto_grpc",
  src = "test_service.proto",
  gen_grpc = True,
)

filegroup(
  name = "test_service_files",
  srcs = glob(["*.proto"]),
)