workspace(name = "polyglot")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

# GRPC

http_archive(
    name = "io_grpc_grpc_java",
    sha256 = "9d23d9fec84e24bd3962f5ef9d1fd61ce939d3f649a22bcab0f19e8167fae8ef",
    strip_prefix = "grpc-java-1.20.0",
    urls = ["https://github.com/grpc/grpc-java/archive/v1.20.0.zip"],
)

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

maven_jar(
    name = "junit_artifact",
    artifact = "junit:junit:4.12",
)

maven_jar(
    name = "protobuf_java_artifact",
    artifact = "com.google.protobuf:protobuf-java:3.2.0",
)

maven_jar(
    name = "protobuf_java_util_artifact",
    artifact = "com.google.protobuf:protobuf-java-util:3.2.0",
)

maven_jar(
    name = "slf4j_api_artifact",
    artifact = "org.slf4j:slf4j-api:1.7.13",
)

maven_jar(
    name = "slf4j_simple_artifact",
    artifact = "org.slf4j:slf4j-simple:1.7.13",
)

maven_jar(
    name = "jul_to_slf4j_artifact",
    artifact = "org.slf4j:jul-to-slf4j:1.7.13",
)

maven_jar(
    name = "mockito_artifact",
    artifact = "org.mockito:mockito-all:1.10.19",
)

maven_jar(
    name = "truth_artifact",
    artifact = "com.google.truth:truth:0.28",
)

maven_jar(
    name = "protoc_jar_artifact",
    artifact = "com.github.os72:protoc-jar:3.2.0",
)

maven_jar(
    name = "jcommander_artifact",
    artifact = "com.beust:jcommander:1.72",
)

maven_jar(
    name = "google_oauth_client_artifact",
    artifact = "com.google.oauth-client:google-oauth-client:1.20.0",
)

maven_jar(
    name = "google_oauth2_http_artifact",
    artifact = "com.google.auth:google-auth-library-oauth2-http:0.3.1",
)

maven_jar(
    name = "google_auth_credentials_artifact",
    artifact = "com.google.auth:google-auth-library-credentials:0.3.1",
)

maven_jar(
    name = "google_http_client_artifact",
    artifact = "com.google.http-client:google-http-client:1.20.0",
)

maven_jar(
    name = "google_http_jackson2_artifact",
    artifact = "com.google.http-client:google-http-client-jackson2:1.20.0",
)

maven_jar(
    name = "jackson_core_artifact",
    artifact = "com.fasterxml.jackson.core:jackson-core:2.6.3",
)
