package polyglot.testing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/** Utilities shared across tests. */
public class TestUtils {
  /** The root directory of the "testing" proto files. */
  public static final Path TESTING_PROTO_ROOT = Paths.get(getWorkspaceRoot().toString(),
      "src", "main", "proto", "testing");

  /** The root directory of the certificates we use for testing. */
  public static final Path TESTING_CERTS_DIR = Paths.get(getWorkspaceRoot().toString(),
      "src", "main", "java", "polyglot", "testing", "test-certificates");

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

  /** Loads a certificate with the given name from our test certificates directory. */
  public static File loadCertFile(String fileName) throws IOException {
    Path filePath = Paths.get(TESTING_CERTS_DIR.toString(), fileName);
    Preconditions.checkArgument(Files.exists(filePath), "File " + fileName + " was not found");
    return filePath.toFile();
  }
}
