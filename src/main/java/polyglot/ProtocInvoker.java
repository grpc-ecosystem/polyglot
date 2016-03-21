package polyglot;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.stream.Collectors;

import com.github.os72.protocjar.Protoc;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

public class ProtocInvoker {
  private static final PathMatcher PROTO_MATCHER =
      FileSystems.getDefault().getPathMatcher("glob:**/*.proto");

  public FileDescriptorSet invoke(Path protoRoot) throws Throwable {
    ImmutableSet<String> protoFilePaths = ImmutableSet.copyOf(Files.walk(protoRoot)
        .filter(path -> PROTO_MATCHER.matches(path))
        .map(path -> path.toAbsolutePath().toString())
        .collect(Collectors.toSet()));
    Path descriptorPath = Files.createTempFile("descriptor", ".pb.bin");

    ImmutableList<String> protocArgs = ImmutableList.<String>builder()
        .addAll(protoFilePaths)
        .add("--descriptor_set_out=" + descriptorPath.toAbsolutePath().toString())
        .add("--proto_path=" + protoRoot.toAbsolutePath().toString())
        .build();;

    int status = Protoc.runProtoc(protocArgs.toArray(new String[0]));
    if (status != 0) {
      throw new IllegalStateException("Got non-zero status code when executing protoc: " + status);
    }

    return FileDescriptorSet.parseFrom(Files.readAllBytes(descriptorPath));
  }
}
