package polyglot;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;

/** Provides easy access to the arguments passed on the commmand line. */
public class CommandLineArgs {
  @Option(name = "--full_method", required = true, metaVar = "<some.package.Service/doSomething>")
  private String fullMethodArg;

  @Option(name = "--endpoint", required = true, metaVar = "<host>:<port>")
  private String endpointArg;

  @Option(name = "--proto_root", required = true, metaVar = "<path>")
  private String protoRootArg;

  @Option(name = "--output", metaVar = "<path>")
  private String output;

  // Derived from the other fields.
  private HostAndPort hostAndPort;
  private ProtoMethodName grpcMethodName;

  /**
   * Parses the arguments from the supplied array. Throws {@link IllegalArgumentException} if the
   * supplied array is malformed.
   */
  public static CommandLineArgs parse(String[] args) {
    CommandLineArgs result = new CommandLineArgs();
    CmdLineParser parser = new CmdLineParser(result);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      throw new IllegalArgumentException("Unable to parse command line flags", e);
    }

    try {
      result.initialize();
    } catch (NullPointerException e) {
      throw new IllegalArgumentException("Unable to initialize command line arguments", e);
    }

    return result;
  }

  /** Returns a single-line usage string explaining how to pass the command line arguments. */
  public static String getUsage() {
    CommandLineArgs result = new CommandLineArgs();
    CmdLineParser parser = new CmdLineParser(result);
    OutputStream stream = new ByteArrayOutputStream();
    parser.printSingleLineUsage(stream);
    return stream.toString();
  }

  private CommandLineArgs() {
  }

  private void initialize() {
    Preconditions.checkNotNull(endpointArg, "The --endpoint argument is required");
    Preconditions.checkNotNull(fullMethodArg, "The --full_method argument is required");
    Preconditions.checkNotNull(protoRootArg, "The --proto_root argument is required");
    Preconditions.checkArgument(Files.exists(Paths.get(protoRootArg)));

    hostAndPort = HostAndPort.fromString(endpointArg);
    grpcMethodName = ProtoMethodName.parseFullGrpcMethodName(fullMethodArg);
  }

  public String host() {
    return hostAndPort.getHostText();
  }

  public int port() {
    return hostAndPort.getPort();
  }

  public Path protoRoot() {
    return Paths.get(protoRootArg).toAbsolutePath();
  }

  public ProtoMethodName grpcMethodName() {
    return grpcMethodName;
  }

  public Optional<Path> outputPath() {
    if (output == null) {
      return Optional.empty();
    }
    return Optional.of(Paths.get(output));
  }
}