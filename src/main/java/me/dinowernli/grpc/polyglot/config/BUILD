package(default_visibility = ["//visibility:public"])

java_library(
  name = "config",
  srcs = glob(['*.java']),
  deps = [
    "//src/main/proto:config_proto",
    "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
    "//third_party/args4j",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/netty",
    "//third_party/protobuf",
    "@flagz_core_artifact//jar",
  ],
)
