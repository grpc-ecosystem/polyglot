package polyglot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import polyglot.oauth2.OauthConfig;

import com.google.api.client.util.Charsets;
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

  @Option(name = "--protoc_proto_path", metaVar = "<path>")
  private String protocProtoPath;

  @Option(name = "--output", metaVar = "<path>")
  private String output;

  @Option(name = "--oauth2_client_id")
  private String oauth2ClientId;

  @Option(name = "--oauth2_client_secret")
  private String oauth2ClientSecret;

  @Option(name = "--oauth2_refresh_token_path", metaVar = "<path>")
  private String oauth2RefreshTokenPath;

  @Option(name = "--oauth2_token_endpoint")
  private String oauth2TokenEndpoint;

  @Option(name = "--oauth2_access_token_path", metaVar = "<path>")
  private String oauth2AccessTokenPath;

  @Option(name = "--use_tls", metaVar = "true|false")
  private String useTls;

  // Derived from the other fields.
  private HostAndPort hostAndPort;
  private ProtoMethodName grpcMethodName;

  /**
   * Parses the arguments from the supplied array. Throws {@link IllegalArgumentException} if the
   * supplied array is malformed.
   */
  public static CommandLineArgs parse(String[] args) {
    CommandLineArgs result = new CommandLineArgs();
    result.useTls = "true";

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
    Preconditions.checkState(oauth2AccessTokenPath == null || oauth2RefreshTokenPath == null, "--oauth2_access_token_path must not be used with --oauth2_refresh_token_path");

    hostAndPort = HostAndPort.fromString(endpointArg);
    grpcMethodName = ProtoMethodName.parseFullGrpcMethodName(fullMethodArg);
  }

  public HostAndPort endpoint() {
    return hostAndPort;
  }

  /** Returns the root of the directory tree in which to discover proto files. */
  public Path protoRoot() {
    return Paths.get(protoRootArg).toAbsolutePath();
  }

  /** Returns the fully qualified name of the supplied proto method. */
  public ProtoMethodName grpcMethodName() {
    return grpcMethodName;
  }

  /** Returns the location in which to store the response proto. */
  public Optional<Path> outputPath() {
    return maybePath(output);
  }

  /** Returns a directory to use as --proto_path for calls to protoc. */
  public Optional<Path> protocProtoPath() {
    return maybePath(protocProtoPath);
  }

  public boolean useTls() {
    return Boolean.parseBoolean(useTls);
  }

  public Optional<OauthConfig> oauthConfig() {
    if (oauth2ClientId == null || oauth2ClientSecret == null || oauth2TokenEndpoint == null) {
      return Optional.empty();
    }
    return Optional.of(new OauthConfig(oauth2ClientId, oauth2ClientSecret, oauth2TokenEndpoint));
  }

  public String oauth2RefreshToken() {
    try {
      return new String(Files.readAllBytes(Paths.get(oauth2RefreshTokenPath)), Charsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Unable ot get refresh token", e);
    }
  }

  public Optional<String> oauth2AccessToken() {
    if (oauth2AccessTokenPath == null) {
      return Optional.empty();
    } else {
      try {
        return Optional.of(new String(Files.readAllBytes(Paths.get(oauth2AccessTokenPath)), Charsets.UTF_8));
      } catch (IOException e) {
        throw new RuntimeException("Unable ot get access token", e);
      }
    }
  }

  private static Optional<Path> maybePath(String rawPath) {
    if (rawPath == null) {
      return Optional.empty();
    }
    return Optional.of(Paths.get(rawPath));
  }
}
