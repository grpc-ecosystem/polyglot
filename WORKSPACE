http_file(
  name = "gen_java_grpc_linux_x86_64",
  url = "https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.0.0/protoc-gen-grpc-java-1.0.0-linux-x86_64.exe",
  sha256 = "4c16cd65e63a92f11f8a24c26a8b418ea6312484d341f66e94d4b5aa27e6e57b",
)

http_file(
  name = "gen_java_grpc_osx_x86_64",
  url = "https://repo1.maven.org/maven2/io/grpc/protoc-gen-grpc-java/1.0.0/protoc-gen-grpc-java-1.0.0-osx-x86_64.exe",
  sha256 = "306ea54a8c84874c880edeb454d92b4c8eed6f60affb1dfb0047532c62754253",
)

http_file(
  name = "protoc_osx_x86_64",
  url = "https://repo1.maven.org/maven2/com/google/protobuf/protoc/3.0.0-beta-3/protoc-3.0.0-beta-3-osx-x86_64.exe",
  sha256 = "3e197d491ba3e798bbe93b1b41f451ba1a15a731eb4da347e29b132bfbd814bc",
)

http_file(
  name = "protoc_linux_x86_64",
  url = "http://search.maven.org/remotecontent?filepath=com/google/protobuf/protoc/3.0.0-beta-3/protoc-3.0.0-beta-3-linux-x86_64.exe",
  sha256 = "3d93855585bf8e8b152f6cec494f2d62932d4afa34c646bf1f73f7a09425e04c",
)

http_file(
  name = "tcnative_boringssl_static_uberjar",
  url = "http://search.maven.org/remotecontent?filepath=io/netty/netty-tcnative-boringssl-static/1.1.33.Fork19/netty-tcnative-boringssl-static-1.1.33.Fork19.jar",
  sha256 = "929a451198f342ef0f6c4600aa7d3448670348e294ef09b022f2bf63510fe3a5",
)

maven_jar(
  name = "grpc_auth_artifact",
  artifact = "io.grpc:grpc-auth:1.0.0",
)

maven_jar(
  name = "grpc_benchmarks_artifact",
  artifact = "io.grpc:grpc-benchmarks:1.0.0",
)

maven_jar(
  name = "grpc_core_artifact",
  artifact = "io.grpc:grpc-core:1.0.0",
)

maven_jar(
  name = "grpc_netty_artifact",
  artifact = "io.grpc:grpc-netty:1.0.0",
)

maven_jar(
  name = "grpc_protobuf_artifact",
  artifact = "io.grpc:grpc-protobuf:1.0.0",
)

maven_jar(
  name = "grpc_protobuf_lite_artifact",
  artifact = "io.grpc:grpc-protobuf-lite:1.0.0",
)

maven_jar(
  name = "grpc_stub_artifact",
  artifact = "io.grpc:grpc-stub:1.0.0",
)

maven_jar(
  name = "grpc_testing_artifact",
  artifact = "io.grpc:grpc-testing:1.0.0",
)

maven_jar(
  name = "guava_artifact",
  artifact = "com.google.guava:guava:19.0",
)

maven_jar(
  name = "junit_artifact",
  artifact = "junit:junit:4.10",
)

maven_jar(
  name = "netty_artifact",
  artifact = "io.netty:netty-all:4.1.3.Final",
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
  name = "google_gson_artifact",
  artifact = "com.google.code.gson:gson:2.6.2",
)

git_repository(
  name = "autotest",
  remote = "https://github.com/dinowernli/bazel-junit-autotest.git",
  commit = "da9ffdb73841fe14459a7eec36bcd84c12e1ac53",
)

load("@autotest//bzl:autotest.bzl", "autotest_junit_repo")
autotest_junit_repo(junit_jar = "//third_party/testing", autotest_workspace="@autotest")
