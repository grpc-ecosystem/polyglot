package me.dinowernli.grpc.polyglot.config;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

/** Provides easy access to the arguments passed on the command line. */
@Parameters(separators = "= ")
public class CommandLineArgs {

  // Options

  @Parameter(names = "--help", help = true, order = 99)
  private boolean help;

  @Parameter(names = "--config_set_path",
    description ="Overrides the default location for the config file 'config.pb.json'",
    order = 0)
  private String configSetPathArg;

  @Parameter(names = "--config_name",
    description ="Overrides the name of the configuration to use from the set (default: use the first one)",
    order = 1)
  private String configNameArg;

  // The flags below represent overrides for the configuration used at runtime.

  @Parameter(names = "--proto_discovery_root",
    description ="Root directory to scan for proto files",
    order = 2)
  private String protoDiscoveryRootArg;

  @Parameter(names = "--add_protoc_includes",
    description ="Adds to the protoc include paths",
    order = 3)
  private String addProtocIncludesArg;

  @Parameter(names = "--output_file_path",
    description ="File to use for output when destination is set to write to file",
    order = 4)
  private String outputFilePathArg;

  @Parameter(names = "--use_reflection",
    description ="Try to use reflection first to resolve protos (default: true)",
    order = 5)
  private String useReflection;

  // Commands

  /** Command to make a GRPC call to an endpoint */
  public static final String CALL_COMMAND = "call";
  /** Command to list all known services defined in the proto files*/
  public static final String LIST_SERVICES_COMMAND = "list_services";
  /** Captures the command called */
  private String commandArg;

  private final CallCommand callCommand = new CallCommand();
  private final ListServicesCommand listServicesCommand = new ListServicesCommand();

  @Parameters(separators = "= ", commandDescription = "Make a GRPC call to an endpoint")
  private class CallCommand {
    @Parameter(names = "--full_method", required = true,
      description ="Full name of the method to call: <some.package.Service/doSomething>",
      order = 0)
    private String fullMethodArg;

    @Parameter(names = "--endpoint", required = true,
      description ="Service endpoint to call: <host>:<port>",
      order = 1)
    private String endpointArg;

    // The flags below represent overrides for the configuration used at runtime.

    @Parameter(names = "--deadline_ms",
      description ="How long to wait for a call to complete (see gRPC doc)",
      order = 2)
    private Integer deadlineMs;

    @Parameter(names = "--metadata",
      description = "Metadata for this call in the form of key-value pairs: k1:v1,k2:v2,...",
      order = 3)
    private String metadataArg;

    @Parameter(names = "--use_tls",
      description ="Whether to use a secure TLS connection (see gRPC doc)",
      order = 4)
    private String useTlsArg;

    @Parameter(names = "--tls_ca_cert_path",
      description ="File to use as a root certificate for calls using TLS",
      order = 5)
    private String tlsCaCertPath;

    @Parameter(names = "--tls_client_cert_path",
      description ="If set, will use client certs for calls using TLS")
    private String tlsClientCertPath;

    @Parameter(names = "--tls_client_key_path",
      description ="<path>")
    private String tlsClientKeyPath;

    @Parameter(names = "--tls_client_override_authority",
      description ="<host>")
    private String tlsClientOverrideAuthority;

  }

  @Parameters(separators = "= ", commandDescription = "List all known services defined in the proto files")
  private class ListServicesCommand {
    @Parameter(names = "--service_filter",
      description = "Filters service names containing this string")
    private String serviceFilterArg;

    @Parameter(names = "--method_filter",
      description = "Filters service methods to those containing this string")
    private String methodFilterArg;

    @Parameter(names = "--with_message",
      description = "If true, then the message specification for the method is rendered")
    private String withMessageArg;
  }

  /**
   * Parses the arguments from the supplied array. Throws {@link IllegalArgumentException} if the
   * supplied array is malformed.
   */
  public static CommandLineArgs parse(String[] args) {
    CommandLineArgs result = new CommandLineArgs();
    try {
      JCommander jc = result.asCommander();
      jc.parse(args);
      result.commandArg = jc.getParsedCommand();
    } catch (ParameterException e) {
      throw new IllegalArgumentException("Unable to parse command line flags", e);
    }

    return result;
  }

  /** Returns a single-line usage string explaining how to pass the command line arguments. */
  public static String getUsage() {
    JCommander jc = new CommandLineArgs().asCommander();
    StringBuilder out = new StringBuilder();
    jc.usage(out);

    return out.toString();
  }

  private CommandLineArgs() {
  }

  private JCommander asCommander() {
    return JCommander.newBuilder()
      .programName("java -jar polyglot.jar")
      .addObject(this)
      .addCommand(CALL_COMMAND, callCommand)
      .addCommand(LIST_SERVICES_COMMAND, listServicesCommand)
      .build();
  }

  public boolean isHelp() {
    return help;
  }

  public Optional<String> command() {
    return Optional.ofNullable(commandArg);
  }

  /** Returns the root of the directory tree in which to discover proto files. */
  public Optional<Path> protoDiscoveryRoot() {
    return maybeInputPath(protoDiscoveryRootArg);
  }

  /** Returns the location in which to store the response proto. */
  public Optional<Path> outputFilePath() {
    return maybeOutputPath(outputFilePathArg);
  }

  public Optional<Path> configSetPath() {
    return maybeInputPath(configSetPathArg);
  }

  public Optional<String> configName() {
    return Optional.ofNullable(configNameArg);
  }

  /** Defaults to true. */
  public boolean useReflection() {
    return useReflection == null || useReflection.equals("true");
  }

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

  // *************************************
  // * Flags supporting the call command *
  // *************************************

  /** Returns the endpoint string */
  public Optional<String> endpoint() {
    return Optional.ofNullable(callCommand.endpointArg);
  }

  /** Returns the endpoint method */
  public Optional<String> fullMethod() {
    return Optional.ofNullable(callCommand.fullMethodArg);
  }

  public Optional<Boolean> useTls() {
    if (callCommand.useTlsArg == null) {
      return Optional.empty();
    }
    return Optional.of(Boolean.parseBoolean(callCommand.useTlsArg));
  }

  public Optional<Path> tlsCaCertPath() {
    return maybeInputPath(callCommand.tlsCaCertPath);
  }

  public Optional<Path> tlsClientCertPath() {
    return maybeInputPath(callCommand.tlsClientCertPath);
  }

  public Optional<Path> tlsClientKeyPath() {
    return maybeInputPath(callCommand.tlsClientKeyPath);
  }

  public Optional<String> tlsClientOverrideAuthority() {
    return Optional.ofNullable(callCommand.tlsClientOverrideAuthority);
  }

  public Optional<ImmutableMultimap<String, String>> metadata() {
    if (callCommand.metadataArg == null) {
      return Optional.empty();
    }

    List<Map.Entry<String, String>> parts = Splitter.on(",")
      .omitEmptyStrings()
      .splitToList(callCommand.metadataArg)
      .stream()
      .map(s -> {
        String[] keyValue = s.split(":");

        Preconditions.checkArgument(keyValue.length == 2,
            "Metadata entry must be defined in key:value format: " + callCommand.metadataArg);

        return Maps.immutableEntry(keyValue[0], keyValue[1]);
      })
    .collect(Collectors.toList());

    ImmutableMultimap.Builder<String, String> builder = new ImmutableMultimap.Builder<>();
    for (Map.Entry<String, String> keyValue : parts) {
      builder.put(keyValue.getKey(), keyValue.getValue());
    }

    return Optional.of(builder.build());
  }

  public Optional<Integer> getRpcDeadlineMs() {
    return Optional.ofNullable(callCommand.deadlineMs);
  }

  // **********************************************
  // * Flags supporting the list_services command *
  // **********************************************
  public Optional<String> serviceFilter() {
    return Optional.ofNullable(listServicesCommand.serviceFilterArg);
  }

  public Optional<String> methodFilter() {
    return Optional.ofNullable(listServicesCommand.methodFilterArg);
  }

  public Optional<Boolean> withMessage() {
    if (listServicesCommand.withMessageArg == null) {
      return Optional.empty();
    }
    return Optional.of(Boolean.parseBoolean(listServicesCommand.withMessageArg));
  }

  // ******************
  // * Helper methods *
  // ******************
  private static Optional<Path> maybeOutputPath(String rawPath) {
    if (rawPath == null) {
      return Optional.empty();
    }
    Path path = Paths.get(rawPath);
    return Optional.of(Paths.get(rawPath));
  }

  private static Optional<Path> maybeInputPath(String rawPath) {
    return maybeOutputPath(rawPath).map(path -> {
      Preconditions.checkArgument(Files.exists(path), "File " + rawPath + " does not exist");
      return path;
    });
  }
}
