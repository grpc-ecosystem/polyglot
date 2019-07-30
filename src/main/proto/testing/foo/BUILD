package(default_visibility = ["//visibility:public"])

proto_library(
    name = "foo_proto",
    srcs = ["foo.proto"],
)

java_proto_library(
    name = "foo_java_proto",
    deps = [":foo_proto"],
)

filegroup(
    name = "proto_files",
    srcs = glob(["*.proto"]),
)
