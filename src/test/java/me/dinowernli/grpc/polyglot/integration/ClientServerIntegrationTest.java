package me.dinowernli.grpc.polyglot.integration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import me.dinowernli.grpc.polyglot.io.MessageWriter;
import me.dinowernli.grpc.polyglot.testing.RecordingTestService;
import me.dinowernli.grpc.polyglot.testing.TestServer;
import me.dinowernli.grpc.polyglot.testing.TestUtils;
import me.dinowernli.junit.TestClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import polyglot.test.TestProto.TestRequest;
import polyglot.test.TestProto.TestResponse;

import static com.google.common.truth.Truth.assertThat;
import static me.dinowernli.grpc.polyglot.testing.TestUtils.makeArgument;

/**
 * An integration test suite which has the Polyglot client talk to a server which records requests.
 */
@TestClass
public class ClientServerIntegrationTest {
  private static final String TEST_UNARY_METHOD = "polyglot.test.TestService/TestMethod";
  private static final String TEST_STREAM_METHOD = "polyglot.test.TestService/TestMethodStream";
  private static final String TEST_CLIENT_STREAM_METHOD =
      "polyglot.test.TestService/TestMethodClientStream";
  private static final String TEST_BIDI_METHOD = "polyglot.test.TestService/TestMethodBidi";

  private static final TestRequest REQUEST = TestRequest.newBuilder()
      .setMessage("i am totally a message")
      .build();

  @Rule public TemporaryFolder tempDirectory = new TemporaryFolder();

  private TestServer testServer;
  private InputStream storedStdin;
  private Path responseFilePath;
  private String storedHomeProperty;

  @Before
  public void setUp() throws Throwable {
    storedHomeProperty = System.getProperty("user.home");
    System.setProperty("user.home", tempDirectory.getRoot().getAbsolutePath());

    responseFilePath = Files.createTempFile("response", "pb.ascii");
    storedStdin = System.in;
    testServer = TestServer.createAndStart(Optional.empty() /* sslContext */);
  }

  @After
  public void tearDown() throws Throwable {
    System.setProperty("user.home", storedHomeProperty);

    testServer.blockingShutdown();
    System.setIn(storedStdin);
    Files.delete(responseFilePath);
  }

  @Test
  public void makesRoundTripUnary() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add(makeArgument("use_reflection", "false"))
        .addAll(makeArgs(serverPort, TEST_UNARY_METHOD))
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0)).isEqualTo(TestServer.UNARY_SERVER_RESPONSE);
  }

  @Test
  public void makesRoundTripUnary_WithReflection() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("use_reflection", "true"))
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add("call")
        .add(makeArgument("endpoint", Joiner.on(':').join("localhost", serverPort)))
        .add(makeArgument("full_method", TEST_UNARY_METHOD))
        .build();

    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0)).isEqualTo(TestServer.UNARY_SERVER_RESPONSE);
  }

  @Test
  public void makesRoundTripServerStream() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add(makeArgument("use_reflection", "false"))
        .addAll(makeArgs(serverPort, TEST_STREAM_METHOD))
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).containsExactly(TestServer.STREAMING_SERVER_RESPONSE);
  }

  @Test
  public void makesRoundTripClientStream() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add(makeArgument("use_reflection", "false"))
        .addAll(makeArgs(serverPort, TEST_CLIENT_STREAM_METHOD))
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST, REQUEST, REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).containsExactly(TestServer.CLIENT_STREAMING_SERVER_RESPONSE);
  }

  @Test
  public void makesRoundTripBidiStream() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add(makeArgument("use_reflection", "false"))
        .addAll(makeArgs(serverPort, TEST_BIDI_METHOD))
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST, REQUEST, REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).containsExactly(
        TestServer.BIDI_SERVER_RESPONSE,
        TestServer.BIDI_SERVER_RESPONSE,
        TestServer.BIDI_SERVER_RESPONSE);
  }

  @Test(expected = RuntimeException.class)
  public void rejectsBadInput() {
    ImmutableList<String> args = makeArgs(testServer.getGrpcServerPort(), TEST_UNARY_METHOD);
    setStdinContents("this is not a valid text proto!");

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    RecordingTestService recordingTestService = testServer.getServiceImpl();
    assertThat(recordingTestService.numRequests()).isEqualTo(1);
    assertThat(recordingTestService.getRequest(0)).isEqualTo(REQUEST);
  }

  @Test
  public void throwsErrorOnRpcTimeout() {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .addAll(makeArgs(serverPort, TEST_UNARY_METHOD))
        .add(makeArgument("deadline_ms", "1"))  // Small enough to guarantee a timeout.
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST)));

    // Run the full client.
    try {
      me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));
      throw new RuntimeException("The rpc should have timed out and thrown");
    } catch (Throwable t) {
      Throwable rootCause = Throwables.getRootCause(t);
      assertThat(rootCause).isInstanceOf(StatusRuntimeException.class);
      assertThat(((StatusRuntimeException) rootCause).getStatus().getCode())
          .isEqualTo(Status.DEADLINE_EXCEEDED.getCode());
    }
  }

  private static ImmutableList<String> makeArgs(int port, String method) {
    return TestUtils.makePolyglotCallArgs(Joiner.on(':').join("localhost", port), method);
  }

  private static void setStdinContents(String contents) {
    System.setIn(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)));
  }
}
