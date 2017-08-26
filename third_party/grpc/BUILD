package(default_visibility = ["//visibility:public"])

java_library(
    name = "grpc",
    licenses = ["permissive"],
    exports = [
        "@com_google_instrumentation_instrumentation_api//jar",
        "@grpc_auth_artifact//jar",
        "@grpc_benchmarks_artifact//jar",
        "@grpc_context_artifact//jar",
        "@grpc_core_artifact//jar",
        "@grpc_netty_artifact//jar",
        "@grpc_protobuf_artifact//jar",
        "@grpc_protobuf_lite_artifact//jar",
        "@grpc_stub_artifact//jar",
        "@io_netty_tcnative_boringssl_static//jar",
    ],
)

java_library(
    name = "testing",
    testonly = 1,
    licenses = ["permissive"],
    exports = [
        "@grpc_testing_artifact//jar",
    ],
)
