package(default_visibility = ["//visibility:public"])

java_binary(
    name = "main",
    srcs = glob(["*.java"]),
    main_class = "me.dinowernli.grpc.polyglot.server.Main",
    deps = [
        "//src/main/proto:hello_proto_grpc",
        "//third_party/grpc",
        "//third_party/guava",
        "//third_party/logging:logging-api",
        "//third_party/logging:logging-impl-stdout",
        "//third_party/netty",
    ],
)
