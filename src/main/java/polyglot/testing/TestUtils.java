package polyglot.testing;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Utilities shared across tests. */
public class TestUtils {
  /** The root directory of the "testing" proto files. */
  public static final Path TESTING_PROTO_ROOT =
      Paths.get(TestUtils.getWorkspaceRoot().toString(), "src", "main", "proto", "testing");

  public static Path getWorkspaceRoot() {
    // Bazel runs binaries with the workspace root as working directory.
    return Paths.get(".").toAbsolutePath();
  }
}
