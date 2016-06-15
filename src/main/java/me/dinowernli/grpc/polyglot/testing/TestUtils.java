package me.dinowernli.grpc.polyglot.testing;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;

import me.dinowernli.grpc.polyglot.io.FileMessageReader;
import polyglot.test.TestProto.TestResponse;

/** Utilities shared across tests. */
public class TestUtils {
  /** The root directory of the "testing" proto files. */
  public static final Path TESTING_PROTO_ROOT = Paths.get(getWorkspaceRoot().toString(),
      "src", "main", "proto", "testing");

  /** The root directory of the certificates we use for testing. */
  public static final Path TESTING_CERTS_DIR = Paths.get(getWorkspaceRoot().toString(),
      "src", "main", "java", "me", "dinowernli", "grpc","polyglot", "testing", "test-certificates");

  /** Returns the root directory of the runtime workspace. */
  public static Path getWorkspaceRoot() {
    // Bazel runs binaries with the workspace root as working directory.
    return Paths.get(".").toAbsolutePath();
  }

  public static ImmutableList<String> makePolyglotArgs(
      String endpoint, String protoRoot, String method) {
    return ImmutableList.<String>builder()
        .add(makeArgument("endpoint", endpoint))
        .add(makeArgument("proto_discovery_root", TestUtils.TESTING_PROTO_ROOT.toString()))
        .add(makeArgument("full_method", method))
        .add(makeArgument("add_protoc_includes", TestUtils.getWorkspaceRoot().toString()))
        .build();
  }

  public static String makeArgument(String key, String value) {
    return String.format("--%s=%s", key, value);
  }

  /** Attempts to read a response proto from the supplied file. */
  public static ImmutableList<TestResponse> readResponseFile(Path file)
      throws InvalidProtocolBufferException {
    FileMessageReader reader = FileMessageReader.create(file, TestResponse.getDescriptor());
    ImmutableList<DynamicMessage> responses = reader.read();

    ImmutableList.Builder<TestResponse> resultBuilder = ImmutableList.builder();
    for (DynamicMessage response : responses) {
      resultBuilder.add(TestResponse.parseFrom(response.toByteString()));
    }
    return resultBuilder.build();
  }

  /** Returns a file containing a root CA certificate for use in tests. */
  public static File loadRootCaCert() {
    return Paths.get(TESTING_CERTS_DIR.toString(), "ca.pem").toFile();
  }

  /** Returns a file containing a certificate chain from our testing root CA to our server. */
  public static File loadServerChainCert() {
    return Paths.get(TESTING_CERTS_DIR.toString(), "server.pem").toFile();
  }

  /** Returns a file containing the key pair of our server. */
  public static File loadServerKey() {
    return Paths.get(TESTING_CERTS_DIR.toString(), "server.key").toFile();
  }
}
