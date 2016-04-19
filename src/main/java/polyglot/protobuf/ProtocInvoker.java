package polyglot.protobuf;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import polyglot.ConfigProto.ProtoConfiguration;

import com.github.os72.protocjar.Protoc;
import com.google.common.base.Preconditions;
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

  // Derived from the config.
  private final ImmutableList<Path> protocIncludePaths;

  /** Creates a new {@link ProtocInvoker} with the supplied configuration. */
  public static ProtocInvoker forConfig(ProtoConfiguration protoConfig) {
    Preconditions.checkArgument(!protoConfig.getProtoDiscoveryRoot().isEmpty(),
        "A proto discovery root is required for proto analysis");
    Path discoveryRootPath = Paths.get(protoConfig.getProtoDiscoveryRoot());
    Preconditions.checkArgument(Files.exists(discoveryRootPath),
        "Invalid proto discovery root path: " + discoveryRootPath);

    ImmutableList.Builder<Path> includePaths = ImmutableList.builder();
    for (String includePathString : protoConfig.getIncludePathsList()) {
      Path path = Paths.get(includePathString);
      Preconditions.checkArgument(Files.exists(path));
      includePaths.add(path.toAbsolutePath());
    }

    return new ProtocInvoker(includePaths.build());
  }

  /**
   * Takes an optional path to pass to protoc as --proto_path. Uses the invocation-time proto root
   * if none is passed.
   */
  private ProtocInvoker(ImmutableList<Path> protocIncludePaths) {
    this.protocIncludePaths = protocIncludePaths;
  }

  /**
   * Executes protoc on all .proto files in the subtree rooted at the supplied path and returns a
   * {@link FileDescriptorSet} which describes all the protos.
   */
  public FileDescriptorSet invoke(Path protoFiles) throws ProtocInvocationException {
    Path descriptorPath;
    try {
      descriptorPath = Files.createTempFile("descriptor", ".pb.bin");
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to create temporary file", e);
    }

    ImmutableList<String> protocArgs = ImmutableList.<String>builder()
        .addAll(scanProtoFiles(protoFiles))
        .addAll(includePathArgs())
        .add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())
        .add("--include_imports")
        .build();

    invokeBinary(protocArgs);

    try {
      return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
    } catch (IOException e) {
      throw new ProtocInvocationException("Unable to parse the generated descriptors", e);
    }
  }

  private ImmutableList<String> includePathArgs() {
    ImmutableList.Builder<String> resultBuilder = ImmutableList.builder();
    for (Path path : protocIncludePaths) {
      resultBuilder.add("-I" + path.toString());
    }
    return resultBuilder.build();
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
