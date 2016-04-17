load("//third_party/bazel:genproto.bzl", "proto_java_library")

package(default_visibility = ["//visibility:public"])

proto_java_library(
  name = "config_proto",
  src = "config.proto",
)

proto_java_library(
  name = "hello_proto_grpc",
  src = "hello.proto",
  gen_grpc = True,
)

proto_java_library(
  name = "hello_proto",
  src = "hello.proto",
  gen_grpc = False,
)