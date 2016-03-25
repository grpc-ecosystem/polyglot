package polyglot;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;

/** Provides easy access to the arguments passed on the commmand line. */
public class CommandLineArgs {
  @Option(name = "--full_method")
  private String fullMethodArg;

  @Option(name = "--endpoint")
  private String endpointArg;

  @Option(name = "--proto_root")
  private String protoRootArg;

  private HostAndPort hostAndPort;
  private ProtoMethodName grpcMethodName;

  public static CommandLineArgs parse(String[] args) {
    CommandLineArgs result = new CommandLineArgs();
    CmdLineParser parser = new CmdLineParser(result);
    try {
      parser.parseArgument(args);
    } catch (CmdLineException e) {
      throw new IllegalArgumentException("Unable to parse command line flags", e);
    }
    result.initialize();
    return result;
  }

  private CommandLineArgs() {
  }

  private void initialize() {
    Preconditions.checkNotNull(endpointArg, "The --endpoint argument is required");
    Preconditions.checkNotNull(fullMethodArg, "The --full_method argument is required");
    Preconditions.checkNotNull(protoRootArg, "The --proto_root argument is required");

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
}