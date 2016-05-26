package polyglot.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auth.Credentials;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.net.HostAndPort;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.util.JsonFormat;

import io.grpc.CallOptions;
import io.grpc.stub.StreamObserver;
import polyglot.ConfigProto.CallConfiguration;
import polyglot.ConfigProto.OutputConfiguration;
import polyglot.grpc.CompositeStreamObserver;
import polyglot.grpc.DynamicGrpcClient;
import polyglot.io.FileMessageWriter;
import polyglot.io.LoggingMessageWriter;
import polyglot.io.LoggingStatsWriter;
import polyglot.oauth2.OauthCredentialsFactory;
import polyglot.protobuf.ProtoMethodName;
import polyglot.protobuf.ServiceResolver;

/** Makes a call to an endpoint, rendering the result */
public class ServiceCall {
  
  private static final Logger logger = LoggerFactory.getLogger(ServiceCall.class);
  
  /** Calls the endpoint specified in the arguments */
  public static void callEndpoint(
      FileDescriptorSet fileDescriptorSet,
      Optional<String> endpoint,
      Optional<String> fullMethod,
      Optional<Path> protoDiscoveryRoot,
      Optional<Path> configSetPath,
      ImmutableList<Path> additionalProtocIncludes,
      CallConfiguration callConfig,
      OutputConfiguration outputConfig) {

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

    logger.info("Creating dynamic grpc client");
    DynamicGrpcClient dynamicClient;
    if (callConfig.hasOauthConfig()) {
      Credentials credentials = 
          new OauthCredentialsFactory(callConfig.getOauthConfig()).getCredentials();
      
      dynamicClient = DynamicGrpcClient.createWithCredentials(
          methodDescriptor, hostAndPort, callConfig, credentials);
    
    } else {
      dynamicClient = DynamicGrpcClient.create(methodDescriptor, hostAndPort, callConfig);
    }

    logger.info("Making rpc call to endpoint: " + endpoint);
    DynamicMessage requestMessage = getProtoFromStdin(methodDescriptor.getInputType());
    StreamObserver<DynamicMessage> streamObserver = 
        CompositeStreamObserver.of(new LoggingStatsWriter(), messageOutputObserver(outputConfig));
    try {
      dynamicClient.call(requestMessage, streamObserver, callOptions(callConfig)).get();
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

  /**
   * Returns an observer which writes the obtained message to the specified
   * output.
   */
  private static StreamObserver<DynamicMessage> messageOutputObserver(OutputConfiguration config) {
    switch (config.getDestination()) {
    case FILE:
      return FileMessageWriter.forFile(Paths.get(config.getFilePath()));
    case LOG:
      return LoggingMessageWriter.create();
    default:
      throw new IllegalArgumentException("Illegal output destination: " + config.getDestination());
    }
  }

  private static DynamicMessage getProtoFromStdin(Descriptor protoDescriptor) {
    final String protoText;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, Charsets.UTF_8));
      protoText = Joiner.on("\n").join(CharStreams.readLines(reader));
    } catch (IOException e) {
      throw new RuntimeException("Unable to read text proto input stream", e);
    }

    DynamicMessage.Builder resultBuilder = DynamicMessage.newBuilder(protoDescriptor);
    try {
      JsonFormat.parser().merge(protoText, resultBuilder);
    } catch (InvalidProtocolBufferException e) {
      throw new RuntimeException("Unable to parse text proto", e);
    }
    return resultBuilder.build();
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
