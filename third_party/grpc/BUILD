package(default_visibility = ["//visibility:public"])

native.genrule(
    name = "tcnative_boringssl_static_uberjar",
    srcs = [
      "@tcnative_boringssl_static_uberjar//file",
    ],
    outs = ["tcnative_boringssl_static_uberjar.jar"],
    cmd = "cat $(locations @tcnative_boringssl_static_uberjar//file) > $@",
    licenses=["permissive"],
)

java_import(
  name = "tcnative-jar",
  jars = [
    ":tcnative_boringssl_static_uberjar"
  ],
  licenses = ["permissive"],
)

java_library(
  name = "grpc",
  exports = [
    "@grpc_auth_artifact//jar",
    "@grpc_benchmarks_artifact//jar",
    "@grpc_core_artifact//jar",
    "@grpc_netty_artifact//jar",
    "@grpc_protobuf_artifact//jar",
    "@grpc_protobuf_lite_artifact//jar",
    "@grpc_stub_artifact//jar",
    ":tcnative-jar",
  ],
  licenses = ["permissive"],
)

java_library(
  name = "testing",
  exports = [
    "@grpc_testing_artifact//jar",
  ],
  testonly = 1,
  licenses = ["permissive"],
)
