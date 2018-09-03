# GRPC
http_archive(
    name = "grpc_java",
    strip_prefix = "grpc-java-1.5.0",
    # We are using 1.4.0 below, but v1.5.0 is the first release of grpc that has the convenient import scripts.
    urls = ["https://github.com/grpc/grpc-java/archive/v1.5.0.zip"],
)

load("@grpc_java//:repositories.bzl", "grpc_java_repositories")

grpc_java_repositories(
    omit_com_google_code_findbugs_jsr305 = True,
    omit_com_google_errorprone_error_prone_annotations = True,
    omit_com_google_protobuf = True,
)

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
    strip_prefix = "rules_protobuf-0.8.1",
    urls = ["https://github.com/pubref/rules_protobuf/archive/v0.8.1.zip"],
)

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")

java_proto_repositories()

# Buildifier
http_archive(
    name = "io_bazel_rules_go",
    sha256 = "d322432e804dfa7936c1f38c24b00f1fb71a0be090a1273d7100a1b4ce281ee7",
    strip_prefix = "rules_go-a390e7f7eac912f6e67dc54acf67aa974d05f9c3",
    urls = [
        "http://bazel-mirror.storage.googleapis.com/github.com/bazelbuild/rules_go/archive/a390e7f7eac912f6e67dc54acf67aa974d05f9c3.tar.gz",
        "https://github.com/bazelbuild/rules_go/archive/a390e7f7eac912f6e67dc54acf67aa974d05f9c3.tar.gz",
    ],
)

load("@io_bazel_rules_go//go:def.bzl", "go_repositories", "new_go_repository")

go_repositories()

new_go_repository(
    name = "org_golang_x_tools",
    commit = "3d92dd60033c312e3ae7cac319c792271cf67e37",
    importpath = "golang.org/x/tools",
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
