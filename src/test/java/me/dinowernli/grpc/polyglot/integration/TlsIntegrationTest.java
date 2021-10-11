package me.dinowernli.grpc.polyglot.integration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import me.dinowernli.grpc.polyglot.io.MessageWriter;
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
 * An integration test suite which tests Polyglot's ability to connect to servers using TLS.
 */
@TestClass
public class TlsIntegrationTest {
  private static final String TEST_UNARY_METHOD = "polyglot.test.TestService/TestMethod";

  private static final TestRequest REQUEST = TestRequest.newBuilder()
      .setMessage("i am totally a message")
      .build();

  @Rule public TemporaryFolder tempDirectory = new TemporaryFolder();

  private TestServer testServer;
  private InputStream storedStdin;
  private Path responseFilePath;

  private String storedHomeProperty;
  private String storedSecurityProperty;

  private static final String PROPERTY_HOME = "user.home";
  private static final String PROPERTY_SECURITY_CA = "jdk.security.allowNonCaAnchor";

  @Before
  public void setUp() throws Throwable {
    storedHomeProperty = System.getProperty(PROPERTY_HOME);
    System.setProperty(PROPERTY_HOME, tempDirectory.getRoot().getAbsolutePath());

    // A check for specific certificate extensions was added to the JDK as part of "Oracle
    // Security-in-Depth Fix 8230318: Better trust store usage". The certificates we use for
    // testing are valid enough to test the end-to-end flow, but happen to not have this particular
    // extension. Until we generate new certs with the right extension, we need to disable this one
    // check.
    // TODO(dino): Remove this once we have updated the certificates used for TLS testing.
    storedSecurityProperty = System.getProperty(PROPERTY_SECURITY_CA);
    System.setProperty(PROPERTY_SECURITY_CA, "true");

    responseFilePath = Files.createTempFile("response", "pb.ascii");
    storedStdin = System.in;
  }

  @After
  public void tearDown() throws Throwable {
    if (storedSecurityProperty == null) {
      System.clearProperty(PROPERTY_SECURITY_CA);
    } else {
      System.setProperty(PROPERTY_SECURITY_CA, storedSecurityProperty);
    }

    if (storedHomeProperty == null) {
      System.clearProperty(PROPERTY_HOME);
    } else {
      System.setProperty(PROPERTY_HOME, storedHomeProperty);
    }

    testServer.blockingShutdown();
    System.setIn(storedStdin);
    Files.delete(responseFilePath);
  }

  @Test
  public void makesRoundTripUnary() throws Throwable {
    testServer = TestServer.createAndStart(Optional.of(TestServer.serverSslContextForTesting()));
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .addAll(makeArgs(testServer.getGrpcServerPort(), TEST_UNARY_METHOD))
        .add(makeArgument("tls_ca_cert_path", TestUtils.loadRootCaCert().getAbsolutePath()))
        .add(makeArgument("use_tls", "true"))
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
  public void makesRoundTripWithClientCerts() throws Throwable {
    testServer = TestServer.createAndStart(
        Optional.of(TestServer.serverSslContextWithClientCertsForTesting()));
    ImmutableList<String> args = ImmutableList.<String>builder()
        .add(makeArgument("output_file_path", responseFilePath.toString()))
        .addAll(makeArgs(testServer.getGrpcServerPort(), TEST_UNARY_METHOD))
        .add(makeArgument("tls_ca_cert_path", TestUtils.loadRootCaCert().getAbsolutePath()))
        .add(makeArgument("tls_client_cert_path", TestUtils.loadClientCert().getAbsolutePath()))
        .add(makeArgument("tls_client_key_path", TestUtils.loadClientKey().getAbsolutePath()))
        .add(makeArgument("use_tls", "true"))
        .build();
    setStdinContents(MessageWriter.writeJsonStream(ImmutableList.of(REQUEST)));

    // Run the full client.
    me.dinowernli.grpc.polyglot.Main.main(args.toArray(new String[0]));

    // Make sure we can parse the response from the file.
    ImmutableList<TestResponse> responses = TestUtils.readResponseFile(responseFilePath);
    assertThat(responses).hasSize(1);
    assertThat(responses.get(0)).isEqualTo(TestServer.UNARY_SERVER_RESPONSE);
  }

  private static ImmutableList<String> makeArgs(int port, String method) {
    return TestUtils.makePolyglotCallArgs(Joiner.on(':').join("localhost", port), method);
  }

  private static void setStdinContents(String contents) {
    System.setIn(new ByteArrayInputStream(contents.getBytes(Charsets.UTF_8)));
  }
}
