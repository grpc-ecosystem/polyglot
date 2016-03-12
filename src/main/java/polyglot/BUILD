package(default_visibility = ["//visibility:public"])

java_binary(
  name = "main",
  srcs = glob(['*.java']),
  main_class = "polyglot.Main",
  deps = [
    "//src/main/proto:hello_proto",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/logging:logging-api",
    "//third_party/logging:logging-impl-stdout",
    "//third_party/netty",
  ]
)
