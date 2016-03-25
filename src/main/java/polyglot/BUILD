package(default_visibility = ["//visibility:public"])

java_binary(
  name = "main",
  srcs = ["Main.java"],
  main_class = "polyglot.Main",
  deps = [
    ":polyglot",
    "//third_party/guava",
    "//third_party/logging:logging-api",
    "//third_party/logging:logging-impl-stdout",
    "//third_party/protobuf",
  ]
)

java_library(
  name = "polyglot",
  srcs = glob(['*.java'], exclude = ["Main.java"]),
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
