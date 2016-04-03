package(default_visibility = ["//visibility:public"])

java_library(
  name = "guava",
  exports = [
    "@guava_artifact//jar",
  ],
  licenses = ["permissive"],
)

native.genrule(
    name = "guava_source_jar",
    srcs = [
        "@guava_sources//file",
    ],
    outs = ["guava_source.jar"],
    cmd = "cat $(locations @guava_sources//file) > $@",
    licenses=["permissive"],
)