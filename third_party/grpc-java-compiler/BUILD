package(default_visibility = ["//visibility:public"])

native.genrule(
    name = "gen_java_grpc_rule",
    srcs = [
        "@gen_java_grpc_linux_x86_64//file",
        "@gen_java_grpc_osx_x86_64//file",
    ],
    executable = 1,
    outs = ["gen_java_grpc"],
    cmd = select({
        ":host_linux_64": "cat $(locations @gen_java_grpc_linux_x86_64//file) > $@",
        ":host_darwin_64": "cat $(locations @gen_java_grpc_osx_x86_64//file) > $@",
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
