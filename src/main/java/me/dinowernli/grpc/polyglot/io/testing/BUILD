package(default_visibility = ["//visibility:public"])

java_library(
  name = "testing",
  srcs = glob(['*.java']),
  testonly = 1,
  deps = [
    "//src/main/proto/testing:test_service_proto",
    "//third_party/guava",
    "//third_party/protobuf",
  ],
)
