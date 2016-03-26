package(default_visibility = ["//visibility:public"])

java_binary(
  name = "polyglot",
  main_class = "polyglot.Main",
  runtime_deps = [
    ":polyglot-lib",
    "//third_party/logging:logging-impl-stdout",
  ]
)

java_library(
  name = "polyglot-lib",
  srcs = glob(['*.java']),
  deps = [
    "//third_party/args4j",
    "//third_party/grpc",
    "//third_party/guava",
    "//third_party/logging:logging-api",
    "//third_party/netty",
    "//third_party/protobuf",
    "//third_party/protoc-jar",
  ],
)
