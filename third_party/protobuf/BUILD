package(default_visibility = ["//visibility:public"])

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
    name = "protoc_rule",
    srcs = [
        "@protoc_linux_x86_64//file",
        "@protoc_osx_x86_64//file",
    ],
    executable = 1,
    outs = ["protoc_exe"],
    cmd = select({
        ":host_darwin_64": "cat $(locations @protoc_osx_x86_64//file) > $@",
        ":host_linux_64": "cat $(locations @protoc_linux_x86_64//file) > $@",
    }),
    licenses=["permissive"],
)

config_setting(
  name = "host_darwin_64",
  values = {"host_cpu": "darwin"},
  licenses=["permissive"],
)

config_setting(
  name = "host_linux_64",
  values = {"host_cpu": "k8"},
  licenses=["permissive"],
)
