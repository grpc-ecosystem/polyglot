package(default_visibility = ["//visibility:public"])

java_library(
  name = "grpc",
  srcs = glob(['*.java']),
  deps = [
    "//src/main/java/me/dinowernli/grpc/polyglot/oauth2",
    "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
    "//src/main/proto:config_proto",
    "//third_party/google-oauth",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/netty",
    "//third_party/logging:logging-api",
    "//third_party/protobuf",
  ],
)
