package polyglot.config;

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
import com.google.common.collect.ImmutableList;

/** Provides easy access to the arguments passed on the command line. */
public class CommandLineArgs {
  @Option(name = "--full_method", metaVar = "<some.package.Service/doSomething>")
  private String fullMethodArg;

  @Option(name = "--endpoint", metaVar = "<host>:<port>")
  private String endpointArg;

  @Option(name = "--config_set_path", metaVar = "<path/to/config.pb.json>")
  private String configSetPathArg;

  @Option(name = "--config_name", metaVar = "<config-name>")
  private String configNameArg;

  // The flags below represent overrides for the configuration used at runtime.
  @Option(name = "--output_file_path", metaVar = "<path>")
  private String outputFilePathArg;

  @Option(name = "--use_tls", metaVar = "true|false")
  private String useTlsArg;

  @Option(name = "--add_protoc_includes", metaVar = "<path1>,<path2>")
  private String addProtocIncludesArg;

  @Option(name = "--proto_discovery_root", metaVar = "<path>")
  private String protoDiscoveryRootArg;

  @Option(name = "--deadline_ms", metaVar = "<number>")
  private Integer deadlineMs;

  @Option(name = "--tls_ca_cert_path", metaVar = "<path>")
  private String tlsCaCertPath;

  // *************************************************************************
  // * Initial step towards the migration to "polyglot <command> [flagz...]" *
  // *************************************************************************
  
  /** Command to make a GRPC call to an endpoint */
  public static final String CALL_COMMAND = "call";
  
  /** Command to list all known services defined in the proto files*/ 
  public static final String LIST_SERVICES_COMMAND = "list_services";
  
  @Option(name = "--command", metaVar = "<call|list_services>")
  private String commandArg;
  
  // TODO: Move to a "list_services"-specific flag container
  @Option(name = "--service_filter", metaVar = "service_name", 
          usage="Filters service names containing this string e.g. --service_filter TestService")
  private String serviceFilterArg;
  
  // TODO: Move to a "list_services"-specific flag container
  @Option(name = "--method_filter", metaVar = "method_name", 
          usage="Filters service methods to those containing this string e.g. --method_name List")
  private String methodFilterArg;
  
  //TODO: Move to a "list_services"-specific flag container
  @Option(name = "--with_message", metaVar = "true|false", 
          usage="If true, then the message specification for the method is rendered")
  private String withMessageArg;
  
  // *************************************************************************

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

  /** Returns the endpoint string */
  public Optional<String >endpoint() {
    return Optional.ofNullable(endpointArg);
  }
  
  /** Returns the endpoint method */
  public Optional<String >fullMethod() {
    return Optional.ofNullable(fullMethodArg);
  }

  /** Returns the root of the directory tree in which to discover proto files. */
  public Optional<Path> protoDiscoveryRoot() {
    return maybePath(protoDiscoveryRootArg);
  }

  /** Returns the location in which to store the response proto. */
  public Optional<Path> outputFilePath() {
    return maybePath(outputFilePathArg);
  }

  public Optional<Boolean> useTls() {
    if (useTlsArg == null) {
      return Optional.empty();
    }
    return Optional.of(Boolean.parseBoolean(useTlsArg));
  }

  public Optional<Path> configSetPath() {
    return maybePath(configSetPathArg);
  }

  public Optional<String> configName() {
    return Optional.ofNullable(configNameArg);
  }

  public Optional<Path> tlsCaCertPath() {
    return maybePath(tlsCaCertPath);
  }
  
  /**    
   * First stage of a migration towards a "command"-based instantiation of polyglot.
   * Supported commands:
   *    list_services [--service_filter XXX] [--method_filter YYY] 
   */
  public Optional<String> command() {
    return Optional.ofNullable(commandArg);
  }
  
  // **********************************************
  // * Flags supporting the list_services command *
  // **********************************************
  // TODO: Move to a "list_services"-specific flag container
  public Optional<String> serviceFilter() {
    return Optional.ofNullable(serviceFilterArg);
  }
  
  // TODO: Move to a "list_services"-specific flag container
  public Optional<String> methodFilter() {
    return Optional.ofNullable(methodFilterArg);
  }
  
  //TODO: Move to a "list_services"-specific flag container
  public Optional<Boolean> withMessage() {
    if (withMessageArg == null) {
      return Optional.empty();
    }
    return Optional.of(Boolean.parseBoolean(withMessageArg));
  }
  // *************************************************************************

  public ImmutableList<Path> additionalProtocIncludes() {
    if (addProtocIncludesArg == null) {
      return ImmutableList.of();
    }

    ImmutableList.Builder<Path> resultBuilder = ImmutableList.builder();
    for (String pathString : addProtocIncludesArg.split(",")) {
      Path includePath = Paths.get(pathString);
      Preconditions.checkArgument(Files.exists(includePath), "Invalid include: " + includePath);
      resultBuilder.add(includePath);
    }
    return resultBuilder.build();
  }

  public Optional<Integer> getRpcDeadlineMs() {
    return Optional.ofNullable(deadlineMs);
  }

  private static Optional<Path> maybePath(String rawPath) {
    if (rawPath == null) {
      return Optional.empty();
    }
    Path path = Paths.get(rawPath);
    Preconditions.checkArgument(Files.exists(path), "File " + rawPath + " does not exist");
    return Optional.of(Paths.get(rawPath));
  }
}
