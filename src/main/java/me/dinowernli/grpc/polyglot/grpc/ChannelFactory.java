package me.dinowernli.grpc.polyglot.grpc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.SSLException;

import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import io.grpc.Channel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import polyglot.ConfigProto;

/** Knows how to construct grpc channels. */
public class ChannelFactory {
  private ConfigProto.CallConfiguration callConfiguration;

  public ChannelFactory(ConfigProto.CallConfiguration callConfiguration) {
    this.callConfiguration = callConfiguration;
  }

  public Channel createChannel(HostAndPort endpoint) {
    if (!callConfiguration.getUseTls()) {
      return createPlaintextChannel(endpoint);
    }
    NettyChannelBuilder nettyChannelBuilder =
        NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
            .sslContext(createSslContext())
            .negotiationType(NegotiationType.TLS);

    if (!callConfiguration.getTlsClientOverrideAuthority().isEmpty()) {
      nettyChannelBuilder.overrideAuthority(callConfiguration.getTlsClientOverrideAuthority());
    }

    return nettyChannelBuilder.build();
  }

  private static Channel createPlaintextChannel(HostAndPort endpoint) {
    return NettyChannelBuilder.forAddress(endpoint.getHostText(), endpoint.getPort())
        .negotiationType(NegotiationType.PLAINTEXT)
        .build();
  }

  private SslContext createSslContext() {
    SslContextBuilder resultBuilder = GrpcSslContexts.forClient();
    if (!callConfiguration.getTlsCaCertPath().isEmpty()) {
      resultBuilder.trustManager(loadFile(callConfiguration.getTlsCaCertPath()));
    }
    if (!callConfiguration.getTlsClientCertPath().isEmpty()) {
      resultBuilder.keyManager(
          loadFile(callConfiguration.getTlsClientCertPath()),
          loadFile(callConfiguration.getTlsClientKeyPath()));
    }
    try {
      return resultBuilder.build();
    } catch (SSLException e) {
      throw new RuntimeException("Unable to build sslcontext for client call", e);
    }
  }

  private static File loadFile(String fileName) {
    Path filePath = Paths.get(fileName);
    Preconditions.checkArgument(Files.exists(filePath), "File " + fileName + " was not found");
    return filePath.toFile();
  }
}
