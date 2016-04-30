package(default_visibility = ["//visibility:public"])

config_setting(
  name = "darwin_64",
  values = {"host_cpu": "darwin"},
  licenses=["permissive"],
)

config_setting(
  name = "linux_64",
  values = {"host_cpu": "k8"},
  licenses=["permissive"],
)

java_library(
  name = "protobuf",
  exports = [
    "@protobuf_java_artifact//jar",
    "@protobuf_java_util_artifact//jar",
    "@google_gson_artifact//jar",
  ],
  licenses = ["permissive"],
)

native.genrule(
    name = "protoc_osx_exe",
    # TODO(dino): Support other platforms.
    srcs = [
        "@protoc_linux_osx_64//file",
    ],
    executable = 1,
    outs = ["protoc"],
    cmd = "cat $(locations @protoc_osx_x86_64//file) > $@",
    licenses=["permissive"],
)

native.genrule(
    name = "protoc_linux_exe",
    # TODO(dino): Support other platforms.
    srcs = [
        "@protoc_linux_x86_64//file",
    ],
    executable = 1,
    outs = ["protoc"],
    cmd = "cat $(locations @protoc_linux_x86_64//file) > $@",
    licenses=["permissive"],
)
