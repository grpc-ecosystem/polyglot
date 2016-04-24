package(default_visibility = ["//visibility:public"])

config_setting(
  name = "windows_64",
  values = {"cpu": "x64_windows"},
  licenses=["permissive"],
)

config_setting(
  name = "darwin_64",
  values = {"cpu": "darwin_x86_64"},
  licenses=["permissive"],
)

config_setting(
  name = "linux_64",
  values = {"cpu": "k8"},
  licenses=["permissive"],
)

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

native.genrule(
    name = "tcnative_boringssl_static_windows_x86_64_jar",
    srcs = [
        "@tcnative_boringssl_static_windows_x86_64//file",
    ],
    executable = 1,
    outs = ["tcnative_boringssl_static_windows_x86_64.jar"],
    cmd = "cat $(locations @tcnative_boringssl_static_windows_x86_64//file) > $@",
    licenses=["permissive"],
)

native.genrule(
    name = "tcnative_boringssl_static_osx_x86_64_jar",
    srcs = [
        "@tcnative_boringssl_static_osx_x86_64//file",
    ],
    executable = 1,
    outs = ["tcnative_boringssl_static_osx_x86_64.jar"],
    cmd = "cat $(locations @tcnative_boringssl_static_osx_x86_64//file) > $@",
    licenses=["permissive"],
)

java_import(
  name = "tcnative-jar",
  jars = select({
    ":linux_64": [":tcnative_boringssl_static_linux_x86_64_jar"],
    ":windows_64": [":tcnative_boringssl_static_windows_x86_64_jar"],
    ":darwin_64": [":tcnative_boringssl_static_osx_x86_64_jar"],
  }),
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
