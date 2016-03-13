package(default_visibility = ["//visibility:public"])

native.genrule(
    name = "gen_java_grpc_rule",
    srcs = [
        "@gen_java_grpc//file",
    ],
    executable = 1,
    outs = ["gen_java_grpc"],
    cmd = "cat $(locations @gen_java_grpc//file) > $@",
    licenses=["permissive"],
)

