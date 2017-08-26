package(default_visibility = ["//visibility:public"])

java_library(
    name = "logging-api",
    licenses = ["permissive"],
    exports = [
        "@jul_to_slf4j_artifact//jar",
        "@slf4j_api_artifact//jar",
    ],
)

java_library(
    name = "logging-impl-stdout",
    licenses = ["permissive"],
    exports = [
        "@slf4j_simple_artifact//jar",
    ],
)
