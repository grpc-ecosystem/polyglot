workspace(name = "polyglot")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# JVM

RULES_JVM_EXTERNAL_TAG = "4.1"

RULES_JVM_EXTERNAL_SHA = "f36441aa876c4f6427bfb2d1f2d723b48e9d930b62662bf723ddfb8fc80f0140"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    urls = ["https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG],
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

# GRPC

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "a61a678f995f1d612bb23d5fb721d83b6960508cc1e0b0dc3c164d6d8d8d24e0",
    strip_prefix = "grpc-java-1.41.0",
    url = "https://github.com/grpc/grpc-java/archive/v1.41.0.zip",
)

load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_ARTIFACTS")
load("@io_grpc_grpc_java//:repositories.bzl", "IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS")

# Autotest

http_archive(
    name = "autotest",
    strip_prefix = "bazel-junit-autotest-0.0.2",
    urls = ["https://github.com/dinowernli/bazel-junit-autotest/archive/v0.0.2.zip"],
)

load("@autotest//bzl:autotest.bzl", "autotest_junit_repo")

autotest_junit_repo(
    autotest_workspace = "@autotest",
    junit_jar = "//third_party/testing",
)

# Direct java deps

MAVEN_ARTIFACTS = [
    "com.beust:jcommander:1.72",
    "com.fasterxml.jackson.core:jackson-core:2.6.3",
    "com.github.os72:protoc-jar:3.2.0",
    "com.google.truth:truth:0.28",
    "com.google.guava:guava:30.1-jre",
    "junit:junit:4.12",
    "org.mockito:mockito-all:1.10.19",
    "org.slf4j:jul-to-slf4j:1.7.13",
    "org.slf4j:slf4j-api:1.7.13",
    "org.slf4j:slf4j-simple:1.7.13",
    "com.google.oauth-client:google-oauth-client:1.30.1",
]

maven_install(
    artifacts = MAVEN_ARTIFACTS + IO_GRPC_GRPC_JAVA_ARTIFACTS,
    generate_compat_repositories = True,
    override_targets = IO_GRPC_GRPC_JAVA_OVERRIDE_TARGETS,
    repositories = [
        "https://jcenter.bintray.com/",
        "https://maven.google.com",
        "https://repo1.maven.org/maven2",
    ],
)

load("@maven//:compat.bzl", "compat_repositories")

compat_repositories()

load("@io_grpc_grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories()

# Protobuf

load("@com_google_protobuf//:protobuf_deps.bzl", "protobuf_deps")

protobuf_deps()

# Buildifier

http_archive(
    name = "io_bazel_rules_go",
    sha256 = "8e968b5fcea1d2d64071872b12737bbb5514524ee5f0a4f54f5920266c261acb",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/rules_go/releases/download/v0.28.0/rules_go-v0.28.0.zip",
        "https://github.com/bazelbuild/rules_go/releases/download/v0.28.0/rules_go-v0.28.0.zip",
    ],
)

http_archive(
    name = "bazel_gazelle",
    sha256 = "de69a09dc70417580aabf20a28619bb3ef60d038470c7cf8442fafcf627c21cb",
    urls = [
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-gazelle/releases/download/v0.24.0/bazel-gazelle-v0.24.0.tar.gz",
        "https://github.com/bazelbuild/bazel-gazelle/releases/download/v0.24.0/bazel-gazelle-v0.24.0.tar.gz",
    ],
)

load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")

go_rules_dependencies()

go_register_toolchains(version = "1.17.2")

gazelle_dependencies()

http_archive(
    name = "com_github_bazelbuild_buildtools",
    strip_prefix = "buildtools-master",
    url = "https://github.com/bazelbuild/buildtools/archive/master.zip",
)
