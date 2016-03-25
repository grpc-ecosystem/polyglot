package polyglot;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides easy access to the arguments passed on the commmand line. */
public class CommandLineArgs {
  private static final Logger logger = LoggerFactory.getLogger(CommandLineArgs.class);
  private static final String USAGE = "polyglot call <host> <port> <protoclass> <service> <method>";

  @Option(name = "--host")
  private String host;

  @Option(name = "--port")
  private int port;

  @Option(name = "--proto_class")
  private String protoClass;

  @Option(name = "--service")
  private String service;

  @Option(name = "--method")
  private String method;

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

  private CommandLineArgs() {
  }

  public String host() {
    return host;
  }

  public int port() {
    return port;
  }

  public String service() {
    return service;
  }

  public String method() {
    return method;
  }

  public String protoClass() {
    return protoClass;
  }
}