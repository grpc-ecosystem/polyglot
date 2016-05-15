package integration;

import static com.google.common.truth.Truth.assertThat;
import static polyglot.testing.TestUtils.makeArgument;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.protobuf.util.JsonFormat;

import polyglot.test.TestProto.TestRequest;
import polyglot.test.TestProto.TestResponse;
import polyglot.testing.TestServer;
import polyglot.testing.TestUtils;

/**
 * An integration test suite which tests Polyglot's ability to connect to servers using TLS.
 */
public class TlsIntegrationTest {
  private static final String TEST_UNARY_METHOD = "polyglot.test.TestService/TestMethod";

  private static final TestRequest REQUEST = TestRequest.newBuilder()
      .setMessage("i am totally a message")
      .build();

  private TestServer testServer;
  private InputStream storedStdin;
  private Path responseFilePath;

  @Before
  public void setUp() throws Throwable {
    responseFilePath = Files.createTempFile("response", "pb.ascii");
    storedStdin = System.in;
    testServer = TestServer.createAndStart(Optional.of(TestServer.serverSslContextForTesting()));
  }

  @After
  public void tearDown() throws Throwable {
    testServer.blockingShutdown();
    System.setIn(storedStdin);
    Files.delete(responseFilePath);
  }

  @Test
  public void makesRoundTripUnary() throws Throwable {
    int serverPort = testServer.getGrpcServerPort();
    ImmutableList<String> args = ImmutableList.<String>builder()
        .addAll(makeArgs(serverPort, TestUtils.TESTING_PROTO_ROOT.toString(), TEST_UNARY_METHOD))
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .add(makeArgument("tls_ca_cert_path", TestUtils.loadRootCaCert().getAbsolutePath()))
        .add(makeArgument("use_tls", "true"))
        .build();
    setStdinContents(JsonFormat.printer().print(REQUEST));

    // Run the full client.
    polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0)).isEqualTo(TestServer.UNARY_SERVER_RESPONSE);
  }

  private static ImmutableList<String> makeArgs(int port, String protoRoot, String method) {
    return TestUtils.makePolyglotArgs(Joiner.on(':').join("localhost", port), protoRoot, method);
  }

  private static void setStdinContents(String contents) {
    System.setIn(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)));
  }
}
