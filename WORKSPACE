# GRPC
http_archive(
    name = "grpc_java",
    strip_prefix = "grpc-java-1.5.0",
    # We are using 1.4.0 below, but v1.5.0 is the first release of grpc that has the convenient import scripts.
    urls = ["https://github.com/grpc/grpc-java/archive/v1.5.0.zip"],
)

load("@grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(omit_com_google_code_findbugs_jsr305 = True)

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

# Proto rules
http_archive(
    name = "org_pubref_rules_protobuf",
    strip_prefix = "rules_protobuf-0.7.2",
    urls = ["https://github.com/pubref/rules_protobuf/archive/v0.7.2.zip"],
)

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")

java_proto_repositories()

# Buildifier
http_archive(
    name = "io_bazel_rules_go",
    sha256 = "f7e42a4c1f9f31abff9b2bdee6fe4db18bc373287b7e07a5b844446e561e67e2",
    strip_prefix = "rules_go-4c9a52aba0b59511c5646af88d2f93a9c0193647",
    urls = [
        "http://bazel-mirror.storage.googleapis.com/github.com/bazelbuild/rules_go/archive/4c9a52aba0b59511c5646af88d2f93a9c0193647.tar.gz",
        "https://github.com/bazelbuild/rules_go/archive/4c9a52aba0b59511c5646af88d2f93a9c0193647.tar.gz",
    ],
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories", "new_go_repository")

go_repositories()

new_go_repository(
    name = "org_golang_x_tools",
    commit = "3d92dd60033c312e3ae7cac319c792271cf67e37",
    importpath = "golang.org/x/tools",
)

git_repository(
    name = "com_github_bazelbuild_buildtools",
    remote = "https://github.com/bazelbuild/buildtools",
    tag = "0.4.5",
)

# Direct java deps

maven_jar(
    name = "grpc_auth_artifact",
    artifact = "io.grpc:grpc-auth:1.4.0",
)

maven_jar(
    name = "grpc_benchmarks_artifact",
    artifact = "io.grpc:grpc-benchmarks:1.4.0",
)

maven_jar(
    name = "grpc_context_artifact",
    artifact = "io.grpc:grpc-context:1.4.0",
)

maven_jar(
    name = "grpc_core_artifact",
    artifact = "io.grpc:grpc-core:1.4.0",
)

maven_jar(
    name = "grpc_netty_artifact",
    artifact = "io.grpc:grpc-netty:1.4.0",
)

maven_jar(
    name = "grpc_protobuf_artifact",
    artifact = "io.grpc:grpc-protobuf:1.4.0",
)

maven_jar(
    name = "grpc_protobuf_lite_artifact",
    artifact = "io.grpc:grpc-protobuf-lite:1.4.0",
)

maven_jar(
    name = "grpc_services_artifact",
    artifact = "io.grpc:grpc-services:1.4.0",
)

maven_jar(
    name = "grpc_stub_artifact",
    artifact = "io.grpc:grpc-stub:1.4.0",
)

maven_jar(
    name = "grpc_testing_artifact",
    artifact = "io.grpc:grpc-testing:1.4.0",
)

maven_jar(
    name = "junit_artifact",
    artifact = "junit:junit:4.10",
)

maven_jar(
    name = "netty_artifact",
    artifact = "io.netty:netty-all:4.1.13.Final",
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
    name = "args4j_artifact",
    artifact = "args4j:args4j:2.32",
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

maven_jar(
    name = "com_google_instrumentation_instrumentation_api",
    artifact = "com.google.instrumentation:instrumentation-api:0.4.3",
    sha1 = "41614af3429573dc02645d541638929d877945a2",
)
