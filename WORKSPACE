# GRPC
new_git_repository(
    name = "grpc_java",
    remote = "https://github.com/grpc/grpc-java.git",
    # We are using 1.4.0 below, but v1.5.0 is the first release of grpc that has the convenient import scripts.
    tag = "v1.5.0",
    build_file_content = "",
)
load("@grpc_java//:repositories.bzl", "grpc_java_repositories")
grpc_java_repositories(omit_com_google_code_findbugs_jsr305=True)

# Autotest
git_repository(
  name = "autotest",
  remote = "https://github.com/dinowernli/bazel-junit-autotest.git",
  tag = "v0.0.1",
)

load("@autotest//bzl:autotest.bzl", "autotest_junit_repo")
autotest_junit_repo(junit_jar = "//third_party/testing", autotest_workspace="@autotest")

# Proto rules
git_repository(
  name = "org_pubref_rules_protobuf",
  remote = "https://github.com/pubref/rules_protobuf",
  tag = "v0.7.2",
)

load("@org_pubref_rules_protobuf//java:rules.bzl", "java_proto_repositories")
java_proto_repositories()


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
  artifact = "com.google.protobuf:protobuf-java:3.0.0-beta-3"
)

maven_jar(
  name = "protobuf_java_util_artifact",
  artifact = "com.google.protobuf:protobuf-java-util:3.0.0-beta-3",
)

maven_jar(
  name = "slf4j_api_artifact",
  artifact= "org.slf4j:slf4j-api:1.7.13",
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
  artifact = "com.github.os72:protoc-jar:3.0.0-b2.1",
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

