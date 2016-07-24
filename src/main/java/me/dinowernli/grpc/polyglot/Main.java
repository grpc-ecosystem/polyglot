package me.dinowernli.grpc.polyglot;

import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import me.dinowernli.grpc.polyglot.command.ServiceCall;
import me.dinowernli.grpc.polyglot.command.ServiceList;
import me.dinowernli.grpc.polyglot.config.CommandLineArgs;
import me.dinowernli.grpc.polyglot.config.ConfigurationLoader;
import me.dinowernli.grpc.polyglot.io.Output;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker.ProtocInvocationException;
import polyglot.ConfigProto.Configuration;
import polyglot.ConfigProto.ProtoConfiguration;

public class Main {
  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static void main(String[] args) {
    // Fix the logging setup.
    setupJavaUtilLogging();

    final CommandLineArgs arguments;
    try {
      arguments = CommandLineArgs.parse(args);
    } catch (RuntimeException e) {
      logger.info("Unable to parse flags", e);
      return;
    }

    // Catch the help case.
    if (arguments.isHelp()) {
      logger.info("Usage: " + CommandLineArgs.getUsage());
      return;
    }

    ConfigurationLoader configLoader = arguments.configSetPath().isPresent()
        ? ConfigurationLoader.forFile(arguments.configSetPath().get())
        : ConfigurationLoader.forDefaultConfigSet();
    configLoader = configLoader.withOverrides(arguments);
    Configuration config = arguments.configName().isPresent()
        ? configLoader.getNamedConfiguration(arguments.configName().get())
        : configLoader.getDefaultConfiguration();
    logger.info("Loaded configuration: " + config.getName());

    FileDescriptorSet fileDescriptorSet = getFileDescriptorSet(config.getProtoConfig());

    String command;
    if (arguments.command().isPresent()) {
      command = arguments.command().get();
    } else {
      logger.warn("Missing --command flag - defaulting to 'call' (but please update your args)");
      command = CommandLineArgs.CALL_COMMAND;
    }

    try(Output commandLineOutput = Output.forConfiguration(config.getOutputConfig())) {
      switch (command) {
        case CommandLineArgs.LIST_SERVICES_COMMAND:
          ServiceList.listServices(
              commandLineOutput,
              fileDescriptorSet, config.getProtoConfig().getProtoDiscoveryRoot(),
              arguments.serviceFilter(), arguments.methodFilter(), arguments.withMessage());
          break;

        case CommandLineArgs.CALL_COMMAND:
          ServiceCall.callEndpoint(
              commandLineOutput,
              fileDescriptorSet,
              arguments.endpoint(),
              arguments.fullMethod(),
              arguments.protoDiscoveryRoot(),
              arguments.configSetPath(),
              arguments.additionalProtocIncludes(),
              config.getCallConfig());
          break;

        default:
          logger.warn("Unknown command: " + arguments.command().get());
      }
    } catch (Exception e) {
      throw new RuntimeException("Caught exception during command execution", e);
    }
  }

  /** Invokes protoc and returns a {@link FileDescriptorSet} used for discovery. */
  private static FileDescriptorSet getFileDescriptorSet(ProtoConfiguration protoConfig) {
    try {
      return ProtocInvoker.forConfig(protoConfig).invoke();
    } catch (ProtocInvocationException e) {
      throw new RuntimeException("Failed to invoke the protoc binary", e);
    }
  }

  /** Redirects the output of standard java loggers to our slf4j handler. */
  private static void setupJavaUtilLogging() {
    LogManager.getLogManager().reset();
    SLF4JBridgeHandler.removeHandlersForRootLogger();
    SLF4JBridgeHandler.install();
  }
}
