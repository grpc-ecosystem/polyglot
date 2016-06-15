package me.dinowernli.grpc.polyglot.testing;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import com.google.common.base.Throwables;

import io.grpc.Server;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslProvider;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestServiceGrpc;
import polyglot.test.TestServiceGrpc.TestService;

/**
 * Holds a real grpc server for use in tests. The server returns canned responses for a fixed set of
 * methods and is optimized for ease of setup in tests (rather than configurability).
 */
public class TestServer {
  /** A response sent whenever the test server sees a request to its unary method. */
  public static final TestResponse UNARY_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some fancy message")
      .build();

  /** A response sent whenever the test server sees a request to its streaming method. */
  public static final TestResponse STREAMING_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some other message")
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
        UNARY_SERVER_RESPONSE, STREAMING_SERVER_RESPONSE);

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

  public Server getGrpcServer() {
    return grpcServer;
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
    return GrpcSslContexts
        .forServer(TestUtils.loadServerChainCert(), TestUtils.loadServerKey())
        .trustManager(TestUtils.loadRootCaCert())
        .sslProvider(SslProvider.OPENSSL)
        .build();
  }

  /** Starts a grpc server on the given port, throws {@link IOException} on failure. */
  private static Server tryStartServer(
      int port, TestService testService, Optional<SslContext> sslContext) throws IOException {
    NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(port)
        .addService(TestServiceGrpc.bindService(testService));
    if (sslContext.isPresent()) {
      serverBuilder.sslContext(sslContext.get());
    }
    return serverBuilder.build().start();
  }
}
