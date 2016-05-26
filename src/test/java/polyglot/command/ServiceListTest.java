package polyglot.command;

import static com.google.common.truth.Truth.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.DescriptorProtos.FileDescriptorSet;

import polyglot.test.TestProto;
import polyglot.test.foo.FooProto;

/** Unit tests for {@link ServiceList}. */
public class ServiceListTest {
  private static FileDescriptorSet PROTO_FILE_DESCRIPTORS = FileDescriptorSet.newBuilder()
      .addFile(TestProto.getDescriptor().toProto())
      .addFile(FooProto.getDescriptor().toProto())
      .build();

  private final String EXPECTED_SERVICE = "polyglot.test.TestService";

  private PrintStream currentPrintStream;
  private ByteArrayOutputStream baos;

  @Before
  public void setUp() throws Throwable {
    currentPrintStream = System.out;
    baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
  }

  @After
  public void tearDown() throws Throwable {
    System.setOut(currentPrintStream);
  }

  @Test
  public void testServiceListOutput() {
    ServiceList.listServices(
        PROTO_FILE_DESCRIPTORS, "", Optional.empty(), Optional.empty(), Optional.empty());

    validateOutput(baos.toString(), EXPECTED_SERVICE,
        new String[] { "TestMethod", "TestMethodStream", "TestMethodBidi" });
  }

  @Test
  public void testServiceListOutputWithServiceFilter() {
    ServiceList.listServices(
        PROTO_FILE_DESCRIPTORS, "", Optional.of("TestService"), Optional.empty(), Optional.empty());

    validateOutput(baos.toString(), EXPECTED_SERVICE,
        new String[] { "TestMethod", "TestMethodStream", "TestMethodBidi" });
  }

  @Test
  public void testServiceListOutputWithMethodFilter() {
    ServiceList.listServices(
        PROTO_FILE_DESCRIPTORS, "", Optional.of("TestService"), 
        Optional.of("TestMethodStream"), Optional.empty());

    validateOutput(baos.toString(), EXPECTED_SERVICE, new String[] { "TestMethodStream" });
  }

  @Test
  public void testServiceListOutputWithMessageDetail() {
    ServiceList.listServices(
        PROTO_FILE_DESCRIPTORS, "", Optional.of("TestService"), 
        Optional.of("TestMethodStream"), Optional.of(true));
    
    validateMessageOutput(baos.toString());
  }

  /** Compares the actual output with the expected output format */
  private void validateOutput(String output, String serviceName, String[] methodNames) {
    // Assuming no filters, we expect output of the form (note that [tmp_path]
    // is a placeholder):
    //
    // polyglot.test.TestService ->
    // [tmp_path]/src/main/proto/testing/test_service.proto
    // polyglot.test.TestService/TestMethod
    // polyglot.test.TestService/TestMethodStream
    // polyglot.test.TestService/TestMethodBidi

    String[] lines = output.trim().split("\n");
    assertThat(lines.length).isEqualTo(methodNames.length + 1);

    // Parse the first line (always [ServiceName] -> [FileName]
    assertThat(lines[0]).startsWith(serviceName + " -> ");

    // Parse the subsequent lines (always [ServiceName]/[MethodName])
    for (int i = 0; i < methodNames.length; i++) {
      assertThat(lines[i + 1].trim()).isEqualTo(serviceName + "/" + methodNames[i]);
    }
  }

  /** Ensures that the message-rendering logic is correct */
  private void validateMessageOutput(String output) {
    // Assuming the filter is for TestService/TestMethodStream, then the message
    // should render as:
    //
    // polyglot.test.TestService ->
    // [tmp_path]/src/main/proto/testing/test_service.proto
    // polyglot.test.TestService/TestMethodStream
    // message[<optional> <single>]: STRING
    // foo[<optional> <single>] {
    // message[<optional> <single>]: STRING
    // }

    String[] lines = output.trim().split("\n");

    // Parse the first line (always [ServiceName] -> [FileName]
    assertThat(lines[0]).startsWith("polyglot.test.TestService -> ");

    String[] expectedLines = { 
        "polyglot.test.TestService/TestMethodStream", 
        "message[<optional> <single>]: STRING",
        "foo[<optional> <single>] {", 
        "message[<optional> <single>]: STRING", 
        "}" };

    for (int i = 0; i < expectedLines.length; i++) {
      assertThat(lines[i + 1].trim()).isEqualTo(expectedLines[i]);
    }
  }
}
