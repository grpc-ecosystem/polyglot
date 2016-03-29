package(default_visibility = ["//visibility:public"])

native.genrule(
    name = "tcnative_boringssl_static_linux_x86_64_jar",
    srcs = [
        "@tcnative_boringssl_static_linux_x86_64//file",
    ],
    executable = 1,
    outs = ["tcnative_boringssl_static_linux_x86_64.jar"],
    cmd = "cat $(locations @tcnative_boringssl_static_linux_x86_64//file) > $@",
    licenses=["permissive"],
)

java_import(
  name = "tcnative-jar",
  jars = [
    ":tcnative_boringssl_static_linux_x86_64_jar",
  ],
  licenses = ["permissive"],
)

java_library(
  name = "grpc",
  exports = [
    "@grpc_artifact//jar",
    ":tcnative-jar",
  ],
  licenses = ["permissive"],
)
