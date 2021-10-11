package me.dinowernli.grpc.polyglot.testing;

import com.google.common.base.Throwables;
import com.google.protobuf.Any;
import com.google.protobuf.Duration;
import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestProto.TunnelMessage;
import polyglot.test.TestServiceGrpc.TestServiceImplBase;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Random;

/**
 * Holds a real grpc server for use in tests. The server returns canned responses for a fixed set of
 * methods and is optimized for ease of setup in tests (rather than configurability).
 */
public class TestServer {
  /** A response sent whenever the test server sees a request to its unary method. */
  public static final TestResponse UNARY_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some fancy message")
      .setAny(Any.pack(TunnelMessage.newBuilder()
          .setNumber(12345)
          .build()))
      .build();

  /** A response sent whenever the test server sees a request to its streaming method. */
  public static final TestResponse STREAMING_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some other message")
      .setDuration(Duration.newBuilder()
          .setSeconds(12)
          .setNanos(45))
      .build();

  /** A response sent whenever the test server sees a request to its client streaming method. */
  public static final TestResponse CLIENT_STREAMING_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("woohoo client stream")
      .build();

  /** A response sent whenever the test server sees a request to its client streaming method. */
  public static final TestResponse BIDI_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("woohoo bidi stream")
      .build();

  private static final long NUM_SERVER_START_TRIES = 3;
  private static final int MIN_SERVER_PORT = 50_000;
  private static final int MAX_SERVER_PORT = 60_000;

  private final Server grpcServer;
  private final int grpcServerPort;
  private final RecordingTestService recordingService;

  /**
   * Tries a few times to start a server. If this returns, the server has been started. Throws
   * {@link RuntimeException} on failure.
   */
  public static TestServer createAndStart(Optional<SslContext> sslContext) {
    RecordingTestService recordingTestService = new RecordingTestService(
        UNARY_SERVER_RESPONSE,
        STREAMING_SERVER_RESPONSE,
        CLIENT_STREAMING_SERVER_RESPONSE,
        BIDI_SERVER_RESPONSE);

    Random random = new Random();
    for (int i = 0; i < NUM_SERVER_START_TRIES; ++i) {
      int port = random.nextInt(MAX_SERVER_PORT - MIN_SERVER_PORT + 1) + MIN_SERVER_PORT;
      try {
        Server server = tryStartServer(port, recordingTestService, sslContext);

        // If we got this far, we have successfully started the server.
        return new TestServer(server, port, recordingTestService);
      } catch (IOException e) {
        // The random port might have been in use, try again...
        continue;
      }
    }

    // If we got to here, we didn't manage to start a server.
    throw new RuntimeException("Unable to start server after " + NUM_SERVER_START_TRIES + " tries");
  }

  public TestServer(
      Server grpcServer, int grpcServerPort, RecordingTestService recordingTestService) {
    this.grpcServer = grpcServer;
    this.grpcServerPort = grpcServerPort;
    this.recordingService = recordingTestService;
  }

  public int getGrpcServerPort() {
    return grpcServerPort;
  }

  public RecordingTestService getServiceImpl() {
    return recordingService;
  }

  public void blockingShutdown() {
    try {
      grpcServer.shutdown().awaitTermination();
    } catch (InterruptedException e) {
      // Propagate, ensuring the test fails.
      Throwables.propagate(e);
    }
  }

  /** An {@link SslContext} for use in unit test servers. Loads our testing certificates. */
  public static SslContext serverSslContextForTesting() throws IOException {
    return getSslContextBuilder().build();
  }

  /** An {@link SslContext} for use in unit test servers with client certs. Loads our testing certificates. */
  public static SslContext serverSslContextWithClientCertsForTesting() throws IOException {
    return getSslContextBuilder()
        .clientAuth(ClientAuth.REQUIRE)
        .build();
  }

  private static SslContextBuilder getSslContextBuilder() {
    return GrpcSslContexts.forServer(TestUtils.loadServerChainCert(), TestUtils.loadServerKey())
        .trustManager(TestUtils.loadRootCaCert())
        .sslProvider(SslProvider.OPENSSL);
  }

  /** Starts a grpc server on the given port, throws {@link IOException} on failure. */
  private static Server tryStartServer(
      int port,
      TestServiceImplBase testService,
      Optional<SslContext> sslContext) throws IOException {
    NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port)
        .addService(testService)
        .addService(ProtoReflectionService.newInstance());
    if (sslContext.isPresent()) {
      serverBuilder.sslContext(sslContext.get());
    }
    return serverBuilder.build().start();
  }
}
