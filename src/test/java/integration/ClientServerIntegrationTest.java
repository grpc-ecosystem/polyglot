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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * An integration test suite which has the Polyglot client talk to a server which records requests.
 */
public class ClientServerIntegrationTest {
  private static final long NUM_SERVER_START_TRIES = 3;
  private static final int MIN_SERVER_PORT = 50_000;
  private static final int MAX_SERVER_PORT = 60_000;
  private static final String TEST_PROTO_ROOT = Paths.get("src/main/proto/testing").toString();
  private static final String TEST_UNARY_METHOD = "polyglot.test.TestService/TestMethod";
  private static final String TEST_STREAM_METHOD = "polyglot.test.TestService/TestMethodStream";

  private static final TestResponse UNARY_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some fancy message")
      .build();
  private static final TestResponse STREAMING_SERVER_RESPONSE = TestResponse.newBuilder()
      .setMessage("some other message")
      .build();
  private static final TestRequest REQUEST = TestRequest.newBuilder()
      .setMessage("i am totally a message")
      .build();

  private Server server;
  private int serverPort;
  private RecordingTestService recordingTestService;
  private InputStream storedStdin;
  private Path responseFilePath;

  @Before
  public void setUp() throws Throwable {
    responseFilePath = Files.createTempFile("response", "pb.ascii");
    storedStdin = System.in;
    recordingTestService = new RecordingTestService(
        UNARY_SERVER_RESPONSE, STREAMING_SERVER_RESPONSE);
    startServer();
  }

  @After
  public void tearDown() throws Throwable {
    server.shutdown().awaitTermination();
    System.setIn(storedStdin);
    Files.delete(responseFilePath);
  }

  @Test
  public void makesRoundTripUnary() throws Throwable {
    ImmutableList<String> args = ImmutableList.<String>builder()
        .addAll(makeArgs(serverPort, TEST_PROTO_ROOT, TEST_UNARY_METHOD))
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .build();
    setStdinContents(REQUEST.toString());

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    TestResponse response = readResponseFile();
    assertThat(response).isEqualTo(UNARY_SERVER_RESPONSE);
  }

  @Test
  public void makesRoundTripStream() throws Throwable {
    ImmutableList<String> args = ImmutableList.<String>builder()
        .addAll(makeArgs(serverPort, TEST_PROTO_ROOT, TEST_STREAM_METHOD))
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .build();
    setStdinContents(REQUEST.toString());

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    TestResponse response = readResponseFile();
    assertThat(response).isEqualTo(STREAMING_SERVER_RESPONSE);
  }

  @Test(expected = RuntimeException.class)
  public void rejectsBadInput() throws Throwable {
    ImmutableList<String> args = makeArgs(serverPort, TEST_PROTO_ROOT, TEST_UNARY_METHOD);
    setStdinContents("this is not a valid text proto!");

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    assertThat(recordingTestService.numRequests()).isEqualTo(1);
    assertThat(recordingTestService.getRequest(0)).isEqualTo(REQUEST);
  }

  /** Attempts to read a response proto from the created temp file. */
  private TestResponse readResponseFile() throws IOException {
    String fileContent = Joiner.on("\n").join(Files.readAllLines(responseFilePath));
    TestResponse.Builder responseBuilder = TestResponse.newBuilder();
    TextFormat.getParser().merge(fileContent, responseBuilder);
    return responseBuilder.build();
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
        .add(makeArgument("proto_root", TEST_PROTO_ROOT))
        .add(makeArgument("full_method", method))
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
    private final TestResponse unaryResponse;
    private final TestResponse streamResponse;
    private final List<TestRequest> recordedRequests;

    public RecordingTestService(TestResponse unaryResponse, TestResponse streamResponse) {
      this.unaryResponse = unaryResponse;
      this.streamResponse = streamResponse;
      this.recordedRequests = new ArrayList<>();
    }

    @Override
    public void testMethod(TestRequest request, StreamObserver<TestResponse> responseStream) {
      recordedRequests.add(request);
      responseStream.onNext(unaryResponse);
      responseStream.onCompleted();
    }

    public int numRequests() {
      return recordedRequests.size();
    }

    public TestRequest getRequest(int index) {
      return recordedRequests.get(index);
    }

    @Override
    public StreamObserver<TestRequest> testMethodBidi(StreamObserver<TestResponse> responseStream) {
      throw new NotImplementedException();
    }

    @Override
    public void testMethodStream(TestRequest request, StreamObserver<TestResponse> responseStream) {
      recordedRequests.add(request);
      responseStream.onNext(streamResponse);
      responseStream.onCompleted();
    }
  }
}
