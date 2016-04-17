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
    name = "protoc_exe",
    # TODO(dino): Support other platforms.
    srcs = [
        "@protoc_linux_x86_64//file",
    ],
    executable = 1,
    outs = ["protoc"],
    cmd = "cat $(locations @protoc_linux_x86_64//file) > $@",
    licenses=["permissive"],
)
