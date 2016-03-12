package(default_visibility = ["//visibility:public"])

java_binary(
  name = "main",
  srcs = glob(['*.java']),
  main_class = "polyglot.Main",
  deps = [
    "//third_party/grpc",
    "//third_party/logging:logging-api",
    "//third_party/logging:logging-impl-stdout",
  ]
)
