package(default_visibility = ["//visibility:public"])

java_binary(
  name = "polyglot",
  main_class = "polyglot.Main",
  runtime_deps = [
    ":polyglot-lib",
  ]
)

java_library(
  name = "polyglot-lib",
  srcs = glob(['*.java']),
  deps = [
    "//src/main/java/polyglot/command",
    "//src/main/java/polyglot/config",
    "//src/main/java/polyglot/protobuf",
    "//src/main/proto:config_proto",
    "//third_party/logging:logging-api",
    "//third_party/logging:logging-impl-stdout",
    "//third_party/protobuf",
  ],
)
