package integration;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.TextFormat;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import polyglot.test.TestProto.TestRequest;
import polyglot.test.TestProto.TestResponse;
import polyglot.test.TestServiceGrpc;
import polyglot.test.TestServiceGrpc.TestService;

/**
 * An integration test suite which has the Polyglot client talk to a server which records requests.
 */
public class ClientServerIntegrationTest {
  private static final long NUM_SERVER_START_TRIES = 3;
  private static final int MIN_SERVER_PORT = 50_000;
  private static final int MAX_SERVER_PORT = 60_000;
  private static final Path TEST_PROTO_ROOT = Paths.get("src/main/proto/testing");
  private static final String TEST_METHOD = "polyglot.test.TestService/TestMethod";

  private static final TestResponse SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some fancy message")
      .build();
  private static final TestRequest REQUEST = TestRequest.newBuilder()
      .setMessage("i am totally a message")
      .build();

  private Server server;
  private int serverPort;
  private RecordingTestService recordingTestService;
  private InputStream storedStdin;

  @Before
  public void setUp() {
    storedStdin = System.in;
    recordingTestService = new RecordingTestService(SERVER_RESPONSE);
    startServer();
  }

  @After
  public void tearDown() throws InterruptedException {
    server.shutdown().awaitTermination();
    System.setIn(storedStdin);
  }

  @Test
  public void makesRoundTrip() throws Throwable {
    ImmutableList<String> args = makeArgs(serverPort, TEST_PROTO_ROOT.toString(), TEST_METHOD);
    setStdinContents(REQUEST.toString());

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    // Make sure the server saw the requests.
    assertThat(recordingTestService.numRequests()).isEqualTo(1);
    assertThat(recordingTestService.getRequest(0)).isEqualTo(REQUEST);
  }

  @Test
  public void writesResponseToFile() throws Throwable {
    Path responseFilePath = Files.createTempFile("response", "pb.ascii");
    ImmutableList<String> args = ImmutableList.<String>builder()
        .addAll(makeArgs(serverPort, TEST_PROTO_ROOT.toString(), TEST_METHOD))
        .add(makeArgument("output", responseFilePath.toString()))
        .build();
    setStdinContents(REQUEST.toString());

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    String fileContent = Joiner.on("\n").join(Files.readAllLines(responseFilePath));
    TestResponse.Builder responseBuilder = TestResponse.newBuilder();
    TextFormat.getParser().merge(fileContent, responseBuilder);
    assertThat(responseBuilder.build()).isEqualTo(SERVER_RESPONSE);
  }

  @Test(expected = RuntimeException.class)
  public void rejectsBadInput() throws Throwable {
    ImmutableList<String> args = makeArgs(serverPort, TEST_PROTO_ROOT.toString(), TEST_METHOD);
    setStdinContents("this is not a valid text proto!");

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    assertThat(recordingTestService.numRequests()).isEqualTo(1);
    assertThat(recordingTestService.getRequest(0)).isEqualTo(REQUEST);
  }

  /**
   * Tries a few times to start a server. If this returns, the server has been started. Throws
   * {@link RuntimeException} on failure.
   */
  private void startServer() {
    Random random = new Random();
    for (int i = 0; i < NUM_SERVER_START_TRIES; ++i) {
      int port = random.nextInt(MAX_SERVER_PORT - MIN_SERVER_PORT + 1) + MIN_SERVER_PORT;
      try {
        Server server = tryStartServer(port, recordingTestService);

        // If we got this far, we have successfully started the server.
        this.server = server;
        this.serverPort = port;
        return;
      } catch (IOException e) {
        // The random port might have been in use, try again...
        continue;
      }
    }

    // If we got to here, we didn't manage to start a server.
    throw new RuntimeException("Unable to start server after " + NUM_SERVER_START_TRIES + " tries");
  }

  /** Starts a grpc server on the given port, throws {@link IOException} on failure. */
  private static Server tryStartServer(int port, TestService testService) throws IOException {
    return ServerBuilder
        .forPort(port)
        .addService(TestServiceGrpc.bindService(testService))
        .build()
        .start();
  }

  private static ImmutableList<String> makeArgs(int port, String protoRoot, String method) {
    return ImmutableList.<String>builder()
        .add(makeArgument("endpoint", Joiner.on(':').join("localhost", port)))
        .add(makeArgument("proto_root", TEST_PROTO_ROOT.toString()))
        .add(makeArgument("full_method", "polyglot.test.TestService/TestMethod"))
        .add(makeArgument("protoc_proto_path", getWorkspaceRoot().toString()))
        .add(makeArgument("use_tls", "false"))
        .build();
  }

  private static Path getWorkspaceRoot() {
    // Bazel runs binaries with the workspace root as working directory.
    return Paths.get(".").toAbsolutePath();
  }

  private static void setStdinContents(String contents) {
    System.setIn(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)));
  }

  private static String makeArgument(String key, String value) {
    return String.format("--%s=%s", key, value);
  }

  /**
   * An implementation of {@link TestService} which records the calls and produces a constant
   * response.
   */
  private static class RecordingTestService implements TestService {
    private final TestResponse response;
    private final List<TestRequest> recordedRequests;

    public RecordingTestService(TestResponse response) {
      this.response = response;
      this.recordedRequests = new ArrayList<>();
    }

    @Override
    public void testMethod(TestRequest request, StreamObserver<TestResponse> responseStream) {
      recordedRequests.add(request);
      responseStream.onNext(response);
      responseStream.onCompleted();
    }

    public int numRequests() {
      return recordedRequests.size();
    }

    public TestRequest getRequest(int index) {
      return recordedRequests.get(index);
    }
  }
}
