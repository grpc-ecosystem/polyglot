package me.dinowernli.grpc.polyglot.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.net.HostAndPort;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.DynamicMessage;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import me.dinowernli.grpc.polyglot.grpc.ChannelFactory;
import me.dinowernli.grpc.polyglot.grpc.CompositeStreamObserver;
import me.dinowernli.grpc.polyglot.grpc.DynamicGrpcClient;
import me.dinowernli.grpc.polyglot.grpc.ServerReflectionClient;
import me.dinowernli.grpc.polyglot.io.LoggingStatsWriter;
import me.dinowernli.grpc.polyglot.io.MessageReader;
import me.dinowernli.grpc.polyglot.io.MessageWriter;
import me.dinowernli.grpc.polyglot.io.Output;
import me.dinowernli.grpc.polyglot.oauth2.OauthCredentialsFactory;
import me.dinowernli.grpc.polyglot.protobuf.ProtoMethodName;
import me.dinowernli.grpc.polyglot.protobuf.ProtocInvoker;
import me.dinowernli.grpc.polyglot.protobuf.ServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polyglot.ConfigProto.CallConfiguration;
import polyglot.ConfigProto.ProtoConfiguration;

/** Makes a call to an endpoint, rendering the result */
public class ServiceCall {
  private static final Logger logger = LoggerFactory.getLogger(ServiceCall.class);

  /** Calls the endpoint specified in the arguments */
  public static void callEndpoint(
      Output output,
      ProtoConfiguration protoConfig,
      Optional<String> endpoint,
      Optional<String> fullMethod,
      Optional<Path> protoDiscoveryRoot,
      Optional<Path> configSetPath,
      ImmutableList<Path> additionalProtocIncludes,
      CallConfiguration callConfig) {
    Preconditions.checkState(endpoint.isPresent(), "--endpoint argument required");
    Preconditions.checkState(fullMethod.isPresent(), "--full_method argument required");
    validatePath(protoDiscoveryRoot);
    validatePath(configSetPath);
    validatePaths(additionalProtocIncludes);

    HostAndPort hostAndPort = HostAndPort.fromString(endpoint.get());
    ProtoMethodName grpcMethodName =
        ProtoMethodName.parseFullGrpcMethodName(fullMethod.get());
    ChannelFactory channelFactory = ChannelFactory.create(callConfig);

    logger.info("Creating channel to: " + hostAndPort.toString());
    Channel channel;
    if (callConfig.hasOauthConfig()) {
      channel = channelFactory.createChannelWithCredentials(
          hostAndPort, new OauthCredentialsFactory(callConfig.getOauthConfig()).getCredentials());
    } else {
      channel = channelFactory.createChannel(hostAndPort);
    }

    // Fetch the appropriate file descriptors for the service.
    final FileDescriptorSet fileDescriptorSet;
    Optional<FileDescriptorSet> reflectionDescriptors = Optional.empty();
    if (protoConfig.getUseReflection()) {
      reflectionDescriptors =
          resolveServiceByReflection(channel, grpcMethodName.getFullServiceName());
    }

    if (reflectionDescriptors.isPresent()) {
      logger.info("Using proto descriptors fetched by reflection");
      fileDescriptorSet = reflectionDescriptors.get();
    } else {
      try {
        fileDescriptorSet = ProtocInvoker.forConfig(protoConfig).invoke();
        logger.info("Using proto descriptors obtained from protoc");
      } catch (Throwable t) {
        throw new RuntimeException("Unable to resolve service by invoking protoc", t);
      }
    }

    // Set up the dynamic client and make the call.
    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor = serviceResolver.resolveServiceMethod(grpcMethodName);

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient = DynamicGrpcClient.create(methodDescriptor, channel);

    ImmutableList<DynamicMessage> requestMessages =
        MessageReader.forStdin(methodDescriptor.getInputType()).read();
    StreamObserver<DynamicMessage> streamObserver =
        CompositeStreamObserver.of(new LoggingStatsWriter(), MessageWriter.create(output));
    logger.info(String.format(
        "Making rpc with %d request(s) to endpoint [%s]", requestMessages.size(), hostAndPort));
    try {
      dynamicClient.call(requestMessages, streamObserver, callOptions(callConfig)).get();
    } catch (Throwable t) {
      throw new RuntimeException("Caught exception while waiting for rpc", t);
    }
  }

  /**
   * Returns a {@link FileDescriptorSet} describing the supplied service if the remote server
   * advertizes it by reflection. Returns an empty optional if the remote server doesn't support
   * reflection. Throws a NOT_FOUND exception if we determine that the remote server does not
   * support the service.
   */
  private static Optional<FileDescriptorSet> resolveServiceByReflection(
      Channel channel, String serviceName) {
    ServerReflectionClient serverReflectionClient = ServerReflectionClient.create(channel);
    ImmutableList<String> services;
    try {
      services = serverReflectionClient.listServices().get();
    } catch (Throwable t) {
      // If this fails, assume the remote server just doesn't to reflection.
      return Optional.empty();
    }

    if (!services.contains(serviceName)) {
      throw Status.NOT_FOUND
          .withDescription(String.format(
              "Remote server does not have service %s. Services: %s", serviceName, services))
          .asRuntimeException();
    }

    try {
      return Optional.of(serverReflectionClient.lookupService(serviceName).get());
    } catch (Throwable t) {
      logger.warn("Unable to lookup service by reflection: " + serviceName, t);
      return Optional.empty();
    }
  }

  private static CallOptions callOptions(CallConfiguration callConfig) {
    CallOptions result = CallOptions.DEFAULT;
    if (callConfig.getDeadlineMs() > 0) {
      result = result.withDeadlineAfter(callConfig.getDeadlineMs(), TimeUnit.MILLISECONDS);
    }
    return result;
  }

  private static void validatePath(Optional<Path> maybePath) {
    if (maybePath.isPresent()) {
      Preconditions.checkArgument(Files.exists(maybePath.get()));
    }
  }

  private static void validatePaths(Iterable<Path> paths) {
    for (Path path : paths) {
      Preconditions.checkArgument(Files.exists(path));
    }
  }
}
