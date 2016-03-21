package polyglot;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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

  /**
   * Exectutes the protoc binary on all proto files in the directory tree rooted at the supplied
   * path and returns a {@link FileDescriptorSet} which describes the proto files found this way.
   */
  public FileDescriptorSet invoke(Path protoRoot) throws ProtocInvocationException {
    ImmutableSet<String> protoFilePaths = scanProtoFiles(protoRoot);

    Path descriptorPath;
    try {
      descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to create temporary file", e);
    }

    ImmutableList<String> protocArgs = ImmutableList.<String>builder()
        .addAll(protoFilePaths)
        .add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())
        .add("--proto_path=" + protoRoot.toAbsolutePath().toString())
        .build();;
        invokeBinary(protocArgs);

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
      throw new ProtocInvocationException("Got non-zero status from protoc binary: " + status);
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
