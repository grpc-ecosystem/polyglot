package polyglot.protobuf;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

/**
 * A utility class which facilitates invoking the protoc compiler on all proto files in a
 * directory tree.
 */
public class ProtocInvoker {
  private static final PathMatcher PROTO_MATCHER =
      FileSystems.getDefault().getPathMatcher("glob:**/*.proto");

  private final Optional<Path> protocProtoPath;

  /**
   * Takes an optional path to pass to protoc as --proto_path. Uses the invocation-time proto root
   * if none is passed.
   */
  public ProtocInvoker(Optional<Path> protocProtoPath) {
    this.protocProtoPath = protocProtoPath;
  }

  /**
   * Exectutes the protoc binary on all proto files in the directory tree rooted at the supplied
   * path and returns a {@link FileDescriptorSet} which describes the proto files found this way.
   */
  public FileDescriptorSet invoke(Path protoRoot) throws ProtocInvocationException {
    Path descriptorPath;
    try {
      descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to create temporary file", e);
    }

    Path protoPath = protocProtoPath.orElse(protoRoot.toAbsolutePath());
    ImmutableList.Builder<String> protocArgs = ImmutableList.<String>builder()
        .addAll(scanProtoFiles(protoRoot))
        .add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())
        .add("--proto_path=" + protoPath.toAbsolutePath().toString());

    invokeBinary(protocArgs.build());

    try {
      return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to parse the generated descriptors", e);
    }
  }

  private void invokeBinary(ImmutableList<String> protocArgs) throws ProtocInvocationException {
    int status;
    try {
      status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
    } catch (IOException | InterruptedException e) {
      throw new ProtocInvocationException("Unable to execute protoc binary", e);
    }
    if (status != 0) {
      throw new ProtocInvocationException(
          String.format("Got exit code [%d] from protoc with args [%s]", status, protocArgs));
    }
  }

  private ImmutableSet<String> scanProtoFiles(Path protoRoot) throws ProtocInvocationException {
    try {
      return ImmutableSet.copyOf(Files.walk(protoRoot)
          .filter(path -> PROTO_MATCHER.matches(path))
          .map(path -> path.toAbsolutePath().toString())
          .collect(Collectors.toSet()));
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to scan proto tree for files", e);
    }
  }

  /** An error indicating that something went wrong while invoking protoc. */
  public class ProtocInvocationException extends Exception {
    private static final long serialVersionUID = 1L;

    private ProtocInvocationException(String message) {
      super(message);
    }

    private ProtocInvocationException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
