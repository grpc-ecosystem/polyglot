load("@autotest//bzl:autotest.bzl", "auto_java_test")

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
