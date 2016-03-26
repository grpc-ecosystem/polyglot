load("//third_party/bazel:genproto.bzl", "proto_java_library")

package(default_visibility = ["//visibility:public"])

proto_java_library(
  name = "foo_proto",
  src = "foo.proto",
)

filegroup(
  name = "proto_files",
  srcs = glob(["*.proto"]),
)