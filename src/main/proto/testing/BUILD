load("//third_party/bazel:genproto.bzl", "proto_java_library")

package(default_visibility = ["//visibility:public"])

proto_java_library(
  name = "test_service_proto",
  src = "test_service.proto",
  deps = [
    "//src/main/proto/testing/foo:foo_proto",
  ],
  gensrcjar_deps = [
    "//src/main/proto/testing/foo:foo.proto",
  ],
)

proto_java_library(
  name = "test_service_proto_grpc",
  src = "test_service.proto",
  gen_grpc = True,
  deps = [
    "//src/main/proto/testing/foo:foo_proto",
  ],
  gensrcjar_deps = [
    "//src/main/proto/testing/foo:foo.proto",
  ],
)

filegroup(
  name = "proto_files",
  srcs = glob(["*.proto"]),
  data = [
    "//src/main/proto/testing/foo:proto_files",
  ],
)