package(default_visibility = ["//visibility:public"])

config_setting(
  name = "target_windows",
  values = {"define": "target=windows"},
  licenses=["permissive"],
)

config_setting(
  name = "target_osx",
  values = {"define": "target=osx"},
  licenses=["permissive"],
)

config_setting(
  name = "target_linux",
  values = {"define": "target=linux"},
  licenses=["permissive"],
)

config_setting(
  name = "windows_64",
  values = {"cpu": "x64_windows"},
  licenses=["permissive"],
)

config_setting(
  name = "darwin_64",
  values = {"cpu": "darwin"},
  licenses=["permissive"],
)

config_setting(
  name = "linux_64",
  values = {"cpu": "k8"},
  licenses=["permissive"],
)

native.genrule(
    name = "tcnative_boringssl_static_jar",
    srcs = [
        "@tcnative_boringssl_static_osx_x86_64//file",
        "@tcnative_boringssl_static_windows_x86_64//file",
        "@tcnative_boringssl_static_linux_x86_64//file",
    ],
    executable = 1,
    outs = ["tcnative_boringssl_static.jar"],
    cmd = select({
      ":target_osx": "cat $(locations @tcnative_boringssl_static_osx_x86_64//file) > $@",
      ":target_linux": "cat $(locations @tcnative_boringssl_static_linux_x86_64//file) > $@",
      ":target_windows": "cat $(locations @tcnative_boringssl_static_windows_x86_64//file) > $@",
      "//conditions:default": "cat $(locations @tcnative_boringssl_static_linux_x86_64//file) > $@",
    }),
    licenses=["permissive"],
)

java_import(
  name = "tcnative-jar",
  jars = [
    ":tcnative_boringssl_static_jar"
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
  licenses = ["permissive"],
)
