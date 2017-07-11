load("@autotest//bzl:autotest.bzl", "auto_java_test")
load("//third_party/bazel:genproto.bzl", "proto_java_library")

auto_java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/proto:config_proto",
    "//src/main/java/me/dinowernli/grpc/polyglot/config",
    "//third_party/guava",
    "//third_party/testing",
  ],
)
