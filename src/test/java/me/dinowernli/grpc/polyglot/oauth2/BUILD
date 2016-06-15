load("//third_party/bazel:genproto.bzl", "proto_java_library")

java_test(
  name = "tests",
  srcs = glob(['*.java']),
  size = "small",
  deps = [
    "//src/main/proto:config_proto",
    "//src/main/java/me/dinowernli/grpc/polyglot/oauth2",
    "//third_party/google-oauth",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/protobuf",
    "//third_party/testing",
  ],
)
