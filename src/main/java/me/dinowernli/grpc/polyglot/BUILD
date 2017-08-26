package(default_visibility = ["//visibility:public"])

java_binary(
    name = "polyglot",
    main_class = "me.dinowernli.grpc.polyglot.Main",
    runtime_deps = [
        ":polyglot-lib",
    ],
)

java_library(
    name = "polyglot-lib",
    srcs = glob(["*.java"]),
    deps = [
        "//src/main/java/me/dinowernli/grpc/polyglot/command",
        "//src/main/java/me/dinowernli/grpc/polyglot/config",
        "//src/main/java/me/dinowernli/grpc/polyglot/io",
        "//src/main/java/me/dinowernli/grpc/polyglot/protobuf",
        "//src/main/proto:config_proto",
        "//third_party/logging:logging-api",
        "//third_party/logging:logging-impl-stdout",
        "//third_party/protobuf",
    ],
)
