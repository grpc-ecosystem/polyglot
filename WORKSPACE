workspace(name = "polyglot")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# JVM

RULES_JVM_EXTERNAL_TAG = "3.0"
RULES_JVM_EXTERNAL_SHA = "62133c125bf4109dfd9d2af64830208356ce4ef8b165a6ef15bbff7460b35c3a"
http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)
load("@rules_jvm_external//:defs.bzl", "maven_install")

# GRPC

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "446ad7a2e85bbd05406dbf95232c7c49ed90de83b3b60cb2048b0c4c9f254d29",
    strip_prefix = "grpc-java-1.29.0",
    url = "https://github.com/grpc/grpc-java/archive/v1.29.0.zip",
)
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_ARTIFACTS")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS")
load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

# Autotest

http_archive(
    name = "autotest",
    strip_prefix = "bazel-junit-autotest-0.0.1",
    urls = ["https://github.com/dinowernli/bazel-junit-autotest/archive/v0.0.1.zip"],
)

load("@autotest//bzl:autotest.bzl", "autotest_junit_repo")

autotest_junit_repo(
    autotest_workspace = "@autotest",
    junit_jar = "//third_party/testing",
)

# Buildifier

http_archive(
    name = "io_bazel_rules_go",
    sha256 = "9fb16af4d4836c8222142e54c9efa0bb5fc562ffc893ce2abeac3e25daead144",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/rules_go/releases/download/0.19.0/rules_go-0.19.0.tar.gz",
        "https://github.com/bazelbuild/rules_go/releases/download/0.19.0/rules_go-0.19.0.tar.gz",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")

go_rules_dependencies()

go_register_toolchains()

http_archive(
    name = "bazel_gazelle",
    sha256 = "be9296bfd64882e3c08e3283c58fcb461fa6dd3c171764fcc4cf322f60615a9b",
    urls = [
        "https://storage.googleapis.com/bazel-mirror/github.com/bazelbuild/bazel-gazelle/releases/download/0.18.1/bazel-gazelle-0.18.1.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/0.18.1/bazel-gazelle-0.18.1.tar.gz",
    ],
)

load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

gazelle_dependencies()

http_archive(
    name = "com_github_bazelbuild_buildtools",
    strip_prefix = "buildtools-master",
    url = "https://github.com/bazelbuild/buildtools/archive/master.zip",
)

# Direct java deps

MAVEN_ARTIFACTS = [
    "com.beust:jcommander:1.72",
    "com.fasterxml.jackson.core:jackson-core:2.6.3",
    "com.github.os72:protoc-jar:3.2.0",
    # "com.google.auth:google-auth-library-credentials:0.3.1",
    # "com.google.auth:google-auth-library-oauth2-http:0.3.1",
    # "com.google.http-client:google-http-client-jackson2:1.20.0",
    # "com.google.http-client:google-http-client:1.20.0",
    # "com.google.oauth-client:google-oauth-client:1.20.0",
    # "com.google.protobuf:protobuf-java-util:3.2.0",
    # "com.google.protobuf:protobuf-java:3.2.0",
    "com.google.truth:truth:0.28",
    "junit:junit:4.12",
    "org.mockito:mockito-all:1.10.19",
    "org.slf4j:jul-to-slf4j:1.7.13",
    "org.slf4j:slf4j-api:1.7.13",
    "org.slf4j:slf4j-simple:1.7.13",
]

maven_install(
    artifacts = MAVEN_ARTIFACTS + IO_GRPC_GRPC_JAVA_ARTIFACTS,
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    generate_compat_repositories = True,
    #maven_install_json = "//:maven_install.json",
    repositories = [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

