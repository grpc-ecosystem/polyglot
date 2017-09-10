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
import me.dinowernli.grpc.polyglot.protobuf.ServiceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import polyglot.ConfigProto.CallConfiguration;

/** Makes a call to an endpoint, rendering the result */
public class ServiceCall {
  private static final Logger logger = LoggerFactory.getLogger(ServiceCall.class);

  /** Calls the endpoint specified in the arguments */
  public static void callEndpoint(
      Output output,
      FileDescriptorSet fileDescriptorSet,
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

    ServiceResolver serviceResolver = ServiceResolver.fromFileDescriptorSet(fileDescriptorSet);
    MethodDescriptor methodDescriptor = serviceResolver.resolveServiceMethod(grpcMethodName);
    ChannelFactory channelFactory = ChannelFactory.create(callConfig);

    logger.info("Creating channel to: " + hostAndPort.toString());
    Channel channel;
    if (callConfig.hasOauthConfig()) {
      channel = channelFactory.createChannelWithCredentials(
          hostAndPort, new OauthCredentialsFactory(callConfig.getOauthConfig()).getCredentials());
    } else {
      channel = channelFactory.createChannel(hostAndPort);
    }

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient = DynamicGrpcClient.create(methodDescriptor, channel);

    ServerReflectionClient serverReflectionClient = ServerReflectionClient.create(channel);
    logger.error(">>>>> listing services using reflection");
    try {
      ImmutableList<String> services = serverReflectionClient.listServices().get();
      logger.error(">>>>> services: " + services);

      logger.error(">>>>>>>>>>> RESOLVING");
      FileDescriptorSet descriptors = serverReflectionClient.lookupService(services.get(0)).get();
    } catch (Throwable t) {
      logger.error(">>>>> error listing services", t);
    }








    ImmutableList<DynamicMessage> requestMessages =
        MessageReader.forStdin(methodDescriptor.getInputType()).read();
    StreamObserver<DynamicMessage> streamObserver =
        CompositeStreamObserver.of(new LoggingStatsWriter(), MessageWriter.create(output));
    logger.info(String.format(
        "Making rpc with %d request(s) to endpoint [%s]", requestMessages.size(), hostAndPort));
    try {
      dynamicClient.call(requestMessages, streamObserver, callOptions(callConfig)).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Caught exeception while waiting for rpc", e);
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
